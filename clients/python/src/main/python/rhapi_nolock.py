from __future__ import print_function

import requests
import re
import os
import json
import sys
import logging
import tempfile
import subprocess
import json
import warnings
from base64 import b64encode, b64decode
import xml.etree.ElementTree as ET

import urllib3
from urllib.parse import urlparse
from requests.utils import requote_uri

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
requests.packages.urllib3.disable_warnings()
# warnings.filterwarnings("error")

if sys.version_info < (3,):
    from cookielib import Cookie, MozillaCookieJar
else:
    from http.cookiejar import Cookie, MozillaCookieJar


class CernSSO:
    DEFAULT_TIMEOUT_SECONDS = 10

    def load_cookies_from_mozilla(self, filename):
        ns_cookiejar = MozillaCookieJar()
        ns_cookiejar.load(filename, ignore_discard=True, ignore_expires=True)
        return ns_cookiejar

    def krb_sign_on(self, url, cookiejar={}, force_level=0):
        tfile = tempfile.mktemp()
        cmd = 'auth-get-sso-cookie -u "%s" -o "%s" -v --nocertverify' % (url, tfile)
        with warnings.catch_warnings():
            p = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
        logging.debug("%s returned %s" % (cmd, p.returncode))
        logging.debug(p.stdout.read())
        err = p.stderr.read()
        logging.debug(err)
        cookies = self.load_cookies_from_mozilla(tfile)
        os.remove(tfile)
        return cookies

    def file_mtime(self, file_path):
        try:
            return os.path.getmtime(file_path)
        except OSError:
            return -1

    def html_root(self, r):
        c = r.content.decode('utf-8')
        c = re.sub('<meta [^>]*>', '', c, flags=re.IGNORECASE)
        c = re.sub('<hr>', '', c, flags=re.IGNORECASE)
        c = re.sub("=\\'([^']*)\\'", '="\g<1>"', c, flags=re.IGNORECASE)
        c = re.sub(" autofocus ", " ", c, flags=re.IGNORECASE)
        c = re.sub('<img ([^>]*)>', '<img \g<1>/>', c, flags=re.IGNORECASE)
        c = re.sub('<script>[^<]*</script>', '', c, flags=re.IGNORECASE)
        return ET.fromstring(c)

    def read_form(self, r):
        root = self.html_root(r)
        form = root.find(".//{http://www.w3.org/1999/xhtml}form")
        action = form.get('action')
        form_data = dict(
            ((e.get('name'), e.get('value')) for e in form.findall(".//{http://www.w3.org/1999/xhtml}input")))
        return action, form_data

    def is_email(self, s):
        regex = '^[a-z0-9]+[\._]?[a-z0-9]+[@]\w+[.]\w{2,3}$'
        return re.search(regex, s)

    def split_url(self, url):
        a = len(urlparse(url).query) + len(urlparse(url).path)
        return url[:-(a + 1)], url[len(url) - a - 1:]

    def login_sign_on(self, url, cache_file=".session.cache", force_level=0):

        from getpass import getpass
        from os import remove, path

        cache = None
        cache_file = path.abspath(cache_file)
        cache_lock_id = b64encode(cache_file.encode('utf-8')).decode()
        cache_time = self.file_mtime(cache_file)

        if force_level == 1 and cache_time != self.file_mtime(cache_file):
            force_level = 0

        if force_level == 2:
            remove(cache_file)

        if path.isfile(cache_file):

            logging.debug('%s found', cache_file)
            with open(cache_file, 'r') as f:
                cache = json.loads(f.read())

        else:

            logging.debug('%s not found', cache_file)

        if force_level > 0 or cache is None or 'cookies' not in cache:

            if cache is not None and 'secret' in cache:
                secret = b64decode(cache['secret'].encode()).decode()
                username, password = secret.split('/')
                password = b64decode(password.encode()).decode()
            else:
                username = input("Username: ")
                password = getpass("Password: ")
                logging.warning('Credentials will be stored in a NOT secure way!')

            with requests.Session() as s:

                r1 = s.get(url, timeout=10, verify=False, allow_redirects=True)
                r1.raise_for_status()

                if self.is_email(username):
                    logging.debug("%s is guest account." % username)

                    root = self.html_root(r1)
                    link = root.find(".//{http://www.w3.org/1999/xhtml}a[@id='zocial-guest']")
                    guest_url = self.split_url(r1.url)[0] + self.split_url(link.get('href'))[1]
                    logging.debug(guest_url)
                    r1 = s.get(guest_url, timeout=10, verify=False, allow_redirects=True)
                    r1.raise_for_status()

                else:
                    logging.debug("%s is a regular account." % username)

                action, form_data = self.read_form(r1)

                form_data['username'] = username
                form_data['password'] = password

                r2 = s.post(url=action, data=form_data, timeout=self.DEFAULT_TIMEOUT_SECONDS, allow_redirects=True)
                r2.raise_for_status()
                action, form_data = self.read_form(r2)

                r3 = s.post(url=action, data=form_data, timeout=self.DEFAULT_TIMEOUT_SECONDS, allow_redirects=True)

                cache = {
                    'secret': b64encode((username + '/' + b64encode(password.encode()).decode()).encode()).decode(),
                    'location': url,
                    'cookies': {c.name: c.value for c in s.cookies}
                }

                with open(cache_file, 'w') as f:
                    f.write(json.dumps(cache))

        return cache['cookies']


class RhApiRowCountError(Exception):

    def __init__(self, totalRows, fetchedRows):
        self.totalRows = totalRows
        self.fetchedRows = fetchedRows

    def __str__(self):
        return 'Total rows count (%d) mismatch with fetched rows count (%d)' % (self.totalRows, self.fetchedRows)


class RhApiRowLimitError(Exception):

    def __init__(self, count, rowsLimit):
        self.count = count
        self.rowsLimit = rowsLimit

    def __str__(self):
        return 'Rows count (%d) is larger than rows limit (%d) for a single result' % (self.count, self.rowsLimit)


class RhApiPageSizeError(Exception):

    def __init__(self, count, rowsLimit, pageSize):
        self.count = count
        self.rowsLimit = rowsLimit
        self.pageSize = pageSize

    def __str__(self):
        return 'Page size (%d) is larger than rows limit (%d) for a single result' % (self.pageSize, self.rowsLimit)


class BadColumnNameError(Exception):

    def __init__(self, bad_column, columns_list, table_name):
        self.bad_column = bad_column
        self.columns_list = columns_list
        self.table_name = table_name

    def __str__(self):
        return 'Column name (%s) does not exist in the table (%s). Try these columns: (%s).' \
               % (self.bad_column, self.table_name, json.dumps(self.columns_list))


class RhApi:
    """
    RestHub API object
    """

    def __init__(self, url, debug=False, sso=None):
        """
        Construct API object.
        url: URL to RestHub endpoint, i.e. http://localhost:8080/api
        debug: should debug messages be printed out? Verbose!
        sso: use cookie provider from SSO_COOKIE_PROVIDER string
        """
        if re.match("/$", url) is None:
            url = url + "/"
        self.url = url
        self.debug = debug
        self.dprint("url = ", self.url)

        self.cprov = None
        if sso is not None and re.search("^https", url):
            if sso == 'login':
                self.cprov = lambda url, force_level: (
                CernSSO().login_sign_on(url, force_level=force_level), force_level)
            if sso == 'krb':
                self.cprov = lambda url, force_level: (CernSSO().krb_sign_on(url), 2)

    def _action(self, action, url, headers, data):
        force_level = 0
        while True:

            cookies = None
            if self.cprov is not None:
                cookies, force_level = self.cprov(self.url, force_level)

            with warnings.catch_warnings():
                r = action(url=url, headers=headers, data=data, cookies=cookies, verify=False)

            if r.status_code == 200 and r.url.startswith(SSO_LOGIN_URL):
                if force_level < 2:
                    force_level = force_level + 1
                    continue
                else:
                    if self.cprov is None:
                        raise Exception('Resource is secured by SSO. Please try --sso')
                    else:
                        raise Exception('Error while logging to HTTPS/SSO')

            return r

    def dprint(self, *args):
        """
        Print debug information
        """
        if self.debug:
            print("RhApi:", end='')
            for arg in args:
                print(arg, end='')
            print()

    def get(self, parts, data=None, headers=None, params=None, verbose=False, cols=False, inline_clobs=False,
            method=None):
        """
        General API call (do not use it directly!)
        """

        if type(params) != dict: params = {}
        if verbose: params["_verbose"] = True
        if cols: params["_cols"] = True
        if inline_clobs: params["_inclob"] = True

        #
        # Constructing request path
        #

        callurl = self.url + "/".join(requote_uri(str(p)) for p in parts)
        callurl = callurl + "?" + "&".join(p + "=" + requote_uri(str(params[p])) for p in params.keys())

        sdata = None
        if data != None:
            sdata = json.dumps(data)

        #
        # Do the query and respond
        #

        self.dprint(callurl, "with payload", sdata, "and headers", headers)

        if method is None:
            if data is None:
                method = 'get'
            else:
                method = 'post'
        else:
            method = method.lower()

        action = getattr(requests, method, None)
        if action:
            resp = self._action(action, headers=headers, url=callurl, data=data)
        else:
            raise NameError('Unknown HTTP method: ' + method)

        self.dprint("Response", resp.status_code, " ".join(str(resp.headers.get('content-type')).split("\r\n")))

        if resp.status_code == requests.codes.ok:
            rdata = resp.text
            if re.search("json", resp.headers.get('content-type')):
                try:
                    return json.loads(rdata)
                except TypeError as e:
                    self.dprint(e)
                    return rdata
            else:
                return rdata
        elif resp.status_code < 300:
            return None
        else:
            raise Exception('Response (' + str(resp.status_code) + '): ' + resp.text)

    def lob(self, url, file_name=None):
        """
        Retrieve blob from url.
        url: blob URL from query response
        file_name: (optional) file name to write lob content to
        returns:
            - lob content: if file_name is None
            - True: if file was written
            - None: if no content found
        Exception: if anything nasty happens
        """
        action = getattr(requests, "get", None)
        resp = self._action(action, url=url, headers=None, data=None)

        self.dprint("Response", resp.status_code, " ".join(str(resp.headers.get('content-type')).split("\r\n")))

        if resp.status_code == requests.codes.ok:
            if file_name is not None:
                with open(file_name, "wb") as f:
                    f.write(resp.content)
                return True
            else:
                return resp.content
        elif resp.status_code < 300:
            return None
        else:
            raise Exception('Response (' + str(resp.status_code) + '): ' + resp.text)

    def info(self, verbose=False):
        """
        Get server version information
        """
        return self.get(["info"], verbose=verbose)

    def folders(self, verbose=False):
        """
        Get list of folders
        """
        return list(self.get(["tables"], verbose=verbose).keys())

    def tables(self, folder, verbose=False):
        """
        Get tables for folder or all
        """
        raw = self.get(["tables"], verbose=verbose)
        d = []
        for t in raw[folder].keys():
            d.append(t)
        return d

    def table(self, folder, table, verbose=False):
        """
        Get info for table
        """
        return self.get(["table", folder, table], verbose=verbose)

    def qid(self, query):
        """
        Create query based on [query] and return its ID
        """
        return self.get(["query"], query)

    def query(self, qid, verbose=False):
        """
        Return qid metadata (assuming it exists..)
        """
        return self.get(["query", qid], verbose=verbose)

    def clean(self, qid, verbose=False):
        """
        Remove cache for query (assuming it exists..)
        """
        return self.get(["query", qid, "cache"], verbose=verbose, method='DELETE')

    def count(self, qid, params=None, verbose=False):
        """
        Get number of rows in a query
        """
        return int(self.get(["query", qid, "count"], params=params, verbose=verbose))

    def histo(self, qid, column, bins=None, bounds=None, params=None, verbose=False):
        """
        Get histogram bins for query column.
        """

        path = ["query", qid, "histo", column]

        assert bounds is None or (isinstance(bounds, (list, tuple)) and len(bounds) == 2 and all(
            isinstance(b, (int, float)) for b in bounds) and bounds[0] < bounds[1])
        if bounds: path += [",".join([str(i) for i in bounds])]

        assert bins is None or isinstance(bins, (int))
        if bins: path += [bins]

        return self.get(path, params=params, verbose=verbose)

    def data(self, qid, params=None, form='text/csv', pagesize=None, page=None, verbose=False, cols=False,
             inline_clobs=False):
        """
        Get data rows
        """

        rowsLimit = self.query(qid, verbose=True)["rowsLimit"]
        count = int(self.count(qid))

        ps = ["query", qid]
        if pagesize is None or page is None:
            if count > rowsLimit:
                raise RhApiRowLimitError(count, rowsLimit)
        else:
            if pagesize > rowsLimit:
                raise RhApiPageSizeError(count, rowsLimit, pagesize)
            else:
                ps.extend(["page", pagesize, page]);

        ps.append("data")
        return self.get(ps, None, {"Accept": form}, params, verbose=verbose, cols=cols, inline_clobs=inline_clobs)

    def csv(self, query, params=None, pagesize=None, page=None, verbose=False, inline_clobs=False):
        """
        Get rows in CSV format
        """
        qid = self.qid(query)
        return self.data(qid, params, 'text/csv', pagesize, page, verbose=verbose, inline_clobs=inline_clobs)

    def xml(self, query, params=None, pagesize=None, page=None, verbose=False, inline_clobs=False):
        """
        Get rows in XML format
        """
        qid = self.qid(query)
        return self.data(qid, params, 'text/xml', pagesize, page, verbose=verbose, inline_clobs=inline_clobs)

    def json(self, query, params=None, pagesize=None, page=None, verbose=False, cols=False, inline_clobs=False):
        """
        Get rows in JSON format (array of arrays)
        """
        qid = self.qid(query)
        return self.data(qid, params, 'application/json', pagesize, page, verbose=verbose, cols=cols,
                         inline_clobs=inline_clobs)

    def json_all(self, query, params=None, verbose=False, cols=False, inline_clobs=False):
        """
        Get all rows in JSON format (array of arrays)
        """

        rows = []

        qid = self.qid(query)
        rowsLimit = self.query(qid, verbose=True)["rowsLimit"]
        count = int(self.count(qid, params))
        pages = int(count / rowsLimit) + 1

        for page in range(1, (pages + 1)):
            data = self.data(qid, params, form="application/json", page=page, pagesize=rowsLimit, verbose=verbose,
                             cols=cols, inline_clobs=inline_clobs)
            rows.extend(data["data"])

        if count != len(rows):
            raise RhApiRowCountError(count, len(rows))

        return rows

    def json2(self, query, params=None, pagesize=None, page=None, verbose=False, cols=False, inline_clobs=False):
        """
        Get rows in JSON2 format (array or objects)
        """
        qid = self.qid(query)
        return self.data(qid, params, 'application/json2', pagesize, page, verbose=verbose, cols=cols,
                         inline_clobs=inline_clobs)


from optparse import OptionParser
import pprint

USAGE = 'usage: %prog [-v] [-u URL] [ FOLDER | FOLDER.TABLE | QUERY ]'
DEFAULT_URL = "http://vocms00170:2113"
DEFAULT_FORMAT = "csv"
DEFAULT_ROOT_FILE = "data.root"
FORMATS = ["csv", "xml", "json", "json2", "root"]
SSO_LOGIN_URL = "https://auth.cern.ch/"


class CLIClient:

    def __init__(self):

        self.pp = pprint.PrettyPrinter(indent=4)
        self.parser = OptionParser(USAGE)
        self.parser.add_option("-v", "--verbose", dest="verbose", help="verbose output. Default: %s" % False,
                               action="store_true", default=False)
        self.parser.add_option("-u", "--url", dest="url", help="service URL. Default: %s" % DEFAULT_URL, metavar="URL",
                               default=DEFAULT_URL)
        self.parser.add_option("-o", "--login", dest="login",
                               help="use simple login provider cache (requires selenium, stores pwd in not secure way!)",
                               metavar="login", action="store_true", default=False)
        self.parser.add_option("-k", "--krb", dest="krb", help="use kerberos login provider", metavar="krb",
                               action="store_true", default=False)
        self.parser.add_option("-f", "--format", dest="format",
                               help="data output format for QUERY data (%s). Default: %s" % (
                               ",".join(FORMATS), DEFAULT_FORMAT), metavar="FORMAT", default=DEFAULT_FORMAT)
        self.parser.add_option("-c", "--count", dest="count", help="instead of QUERY data return # of rows",
                               action="store_true", default=False)
        self.parser.add_option("-s", "--size", dest="size", help="number of rows per PAGE return for QUERY",
                               metavar="SIZE", type="int")
        self.parser.add_option("-g", "--page", dest="page", help="page number to return. Default 1", metavar="PAGE",
                               default=1, type="int")
        self.parser.add_option("-l", "--cols", dest="cols", help="add column metadata if possible. Default: False",
                               action="store_true", default=False)
        self.parser.add_option("-b", "--inclob", dest="inclob",
                               help="inline clobs directly into the output. Default: False (send as links)",
                               action="store_true", default=False)
        self.parser.add_option("-i", "--info", dest="info", help="print server version information. Default: False",
                               action="store_true", default=False)
        self.parser.add_option("-a", "--all", dest="all",
                               help="force to retrieve ALL data (can take long time). Default: False",
                               action="store_true", default=False)
        self.parser.add_option("-m", "--metadata", dest="metadata",
                               help="do not execute query but dump METADATA. Default: False", action="store_true",
                               default=False)
        self.parser.add_option("-n", "--clean", dest="clean",
                               help="clean cache before executing query (new results). Default: False",
                               action="store_true", default=False)
        self.parser.add_option("-p", dest="param", help="parameter for QUERY in form -pNAME=VALUE", metavar="PARAM",
                               action="append")
        self.parser.add_option("-r", "--root", dest="root",
                               help="ROOT file name, if format set to root. Default: " + DEFAULT_ROOT_FILE,
                               metavar="ROOT", default=DEFAULT_ROOT_FILE)
        self.parser.add_option("-t", "--histo", dest="histo",
                               help="histogram of BINS bins for COLUMN in LBOUND to RBOUND bounds. Possible combinations: COLUMN, COLUMN:BINS, COLUMN:BINS:LBOUND:RBOUND.",
                               metavar="COLUMN:BINS:LBOUND:RBOUND", default=None)

    def pprint(self, data):
        self.pp.pprint(data)

    def basicSelect(self, arg, api, param, verbose):
        split_arg = arg.split(".")
        table_metadata = api.table(split_arg[0], split_arg[1], verbose=verbose)
        table_name = split_arg[1]
        # get table names list from meta data
        column_names_list = []
        for i in table_metadata["columns"]:
            column_names_list.append(i["name"])

        arg = 'select * from ' + arg + ' a'
        # get and save to list values from p parameters
        params_length = 0
        if param is not None:
            param.sort()

        if param:
            param_column_names_list = []
            param_column_value_list = []
            split_where = []
            params_length = len(param)
            # assign param values to lists
            for i in param:
                split_where = i.split("=")
                param_column_names_list.append(split_where[0])
                param_column_value_list.append(split_where[1])

            # check if value of first parameter belongs to column names
            for i in param_column_names_list:
                if i not in column_names_list:
                    raise BadColumnNameError(i, column_names_list, table_name)

        if params_length != 0:
            # build where statements
            previous = next = None
            for index, obj in enumerate(param_column_names_list):
                if index > 0:
                    previous = param_column_names_list[index - 1]
                if index < (params_length - 1):
                    next = param_column_names_list[index + 1]
                if params_length == 1:
                    arg = arg + " where a." + param_column_names_list[index] + " = :" + param_column_names_list[index]
                else:
                    if index == 0:
                        if param_column_names_list[index] == next:
                            arg = arg + " where (a." + param_column_names_list[index] + " = :" + \
                                  param_column_names_list[index]
                        else:
                            arg = arg + " where a." + param_column_names_list[index] + " = :" + param_column_names_list[
                                index]
                    else:
                        if previous == param_column_names_list[index]:

                            if param_column_names_list[index] != next:
                                arg = arg + " or a." + param_column_names_list[index] + " = :" + \
                                      param_column_names_list[index] + str(index) + ")"
                                param[index] = param_column_names_list[index] + str(index) + '=' + \
                                               param_column_value_list[index]
                            else:
                                arg = arg + " or a." + param_column_names_list[index] + " = :" + \
                                      param_column_names_list[index] + str(index)
                                param[index] = param_column_names_list[index] + str(index) + '=' + \
                                               param_column_value_list[index]
                        else:
                            if param_column_names_list[index] != next:
                                arg = arg + " and a." + param_column_names_list[index] + " = :" + \
                                      param_column_names_list[index]
                            else:
                                arg = arg + " and ( a." + param_column_names_list[index] + " = :" + \
                                      param_column_names_list[index]

                previous = next = None

        return arg, param

    def run(self):

        try:

            (options, args) = self.parser.parse_args()

            sso = None
            if re.search("^https", options.url):

                if sum((options.login, options.krb)) != 1:
                    self.parser.error('For secure access please provide one of --krb or --login')
                    return 1

                if options.login:
                    sso = 'login'

                if options.krb:
                    sso = 'krb'

            api = RhApi(options.url, debug=options.verbose, sso=sso)

            # Info
            if options.info:
                self.pprint(api.info(verbose=options.verbose))
                return 0

            # Folders
            if len(args) == 0:
                self.pprint(api.folders(verbose=options.verbose))
                return 0

            if len(args) > 1:
                self.parser.error('More than one command found. Maybe double quotes are missing?')
                return 0

            arg = args[0]

            # FOLDER tables
            if re.match("^[a-zA-Z0-9_]+$", arg) is not None:
                self.pprint(api.tables(arg, verbose=options.verbose))
                return 0

            # FOLDER.TABLE
            if ((re.match("^[a-zA-Z0-9_]+\\.[a-zA-Z0-9_]+$", arg) is not None) and (options.format is None)):
                parts = arg.split(".")
                self.pprint(api.table(parts[0], parts[1], verbose=options.verbose))
                return 0

            # if format is Null, assign format to default format
            if options.format is None:
                options.format = DEFAULT_FORMAT

            # QUERY
            if re.match("^select ", arg, re.IGNORECASE) is not None or \
                    (re.match("^[a-zA-Z0-9_]+\\.[a-zA-Z0-9_]+$", arg) is not None and (options.format is not None)):
                if (re.match("^[a-zA-Z0-9_]+\\.[a-zA-Z0-9_]+$", arg) is not None and (options.format is not None)):
                    arg, options.param = self.basicSelect(arg, api, options.param, options.verbose)
                params = {}
                if options.param:
                    for ps in options.param:
                        m = re.match("^([^=]+)=(.*)$", ps)
                        if m:
                            params[m.group(1)] = m.group(2)

                if options.clean:
                    api.clean(api.qid(arg), verbose=options.verbose)

                if options.count:

                    print(api.count(api.qid(arg), params=params, verbose=options.verbose))

                elif options.histo:

                    if options.format not in ['json', 'json2', 'csv']:
                        self.parser.error('Histogram bins are possible in formats: json, json2, csv')

                    col = options.histo
                    bounds = None
                    bins = None

                    m = re.match("^([^:]+):?([0-9]+)?:?(-?[0-9\.]+)?:?(-?[0-9\.]+)?$", col)
                    if m:
                        col = m.group(1)
                        if m.group(2): bins = int(m.group(2))
                        if m.group(3) and m.group(4): bounds = (float(m.group(3)), float(m.group(4)))
                    else:
                        self.parser.error('Histogram pattern not recognized: ' + options.histo)

                    histo = api.histo(api.qid(arg), column=col, bins=bins, bounds=bounds, params=params,
                                      verbose=options.verbose)

                    if options.format in ['json', 'json2']:
                        print(histo)
                    else:
                        print('\t'.join(histo['cols']))
                        for b in histo['bins']:
                            print('\t'.join([str(n) for n in b]))

                elif options.metadata:

                    qid = api.qid(arg)
                    print(self.pprint(api.query(qid, verbose=options.verbose)))

                else:

                    if FORMATS.count(options.format) == 0:

                        self.parser.error(
                            'Format %s not understood: please use one of %s' % (options.format, ",".join(FORMATS)))

                    else:

                        if options.size and options.page and options.all:
                            self.parser.error('Wrong combination of options: ALL and SIZE both can not be defined')

                        if options.format == 'csv':
                            try:
                                print(api.csv(arg, params=params, pagesize=options.size, page=options.page,
                                              verbose=options.verbose, inline_clobs=options.inclob))
                            except RhApiRowLimitError as e:
                                if options.all:
                                    page = 0
                                    while (page * e.rowsLimit) < e.count:
                                        page = page + 1
                                        res = api.csv(arg, params=params, pagesize=e.rowsLimit, page=page,
                                                      verbose=options.verbose, inline_clobs=options.inclob)
                                        if page == 1:
                                            print(res, end='')
                                        else:
                                            print('\n'.join(res.split('\n')[1:]), end='')
                                else:
                                    raise e

                        if options.format == 'xml':
                            try:
                                print(api.xml(arg, params=params, pagesize=options.size, page=options.page,
                                              verbose=options.verbose, inline_clobs=options.inclob))
                            except RhApiRowLimitError as e:
                                if options.all:
                                    page = 0
                                    print('<?xml version="1.0" encoding="UTF-8" standalone="no"?><data>', end='')
                                    while (page * e.rowsLimit) < e.count:
                                        page = page + 1
                                        res = api.xml(arg, params=params, pagesize=e.rowsLimit, page=page,
                                                      verbose=options.verbose, inline_clobs=options.inclob)
                                        root = minidom.parseString(res).documentElement
                                        for row in root.getElementsByTagName('row'):
                                            print(row.toxml(), end='')
                                    print('</data>')
                                else:
                                    raise e

                        if options.format in ['json', 'json2', 'root']:
                            try:
                                method_name = options.format if options.format != 'root' else 'json'
                                method = getattr(api, method_name, None)
                                in_cols = options.cols if options.format != 'root' else True
                                data = method(arg, params=params, pagesize=options.size, page=options.page,
                                              verbose=options.verbose, cols=in_cols, inline_clobs=options.inclob)
                            except RhApiRowLimitError as e:
                                if options.all:
                                    page = 0
                                    data = None
                                    while (page * e.rowsLimit) < e.count:
                                        page = page + 1
                                        res = method(arg, params=params, pagesize=e.rowsLimit, page=page,
                                                     verbose=options.verbose, cols=in_cols, inline_clobs=options.inclob)
                                        if data is None:
                                            data = res
                                        else:
                                            data['data'].extend(res['data'])
                                else:
                                    raise e

                            if options.format == 'root':
                                self._to_root(data, options.root)
                            else:
                                print(data)

                return 0

            self.parser.error('Command %s not understood' % arg)

        except RhApiRowLimitError as e:

            print("ERROR: %s\nDetails: %s, consider --all option" % (type(e).__name__, e))

        except requests.exceptions.RequestException as e:
            reason = e.reason if hasattr(e, 'reason') else '%s' % e
            print("ERROR: %s\nDetails: %s" % (reason, e))

        except Exception as e:

            print("ERROR: %s\nDetails: %s" % (type(e).__name__, e))
            import traceback
            traceback.print_exc()

    def _root_column(self, data, ci, ct, cn):

        type_mapping = {
            str: 'Char_t',
            int: 'Int_t',
            float: 'Float_t'
        }

        if ct == 'NUMBER':
            t = int
            for r in data['data']:
                if r[ci] is not None and type(r[ci]) == float:
                    t = float
                    break
            return type_mapping[t] + ' ' + cn
        else:
            l = 1
            for r in data['data']:
                if r[ci] is None:
                    r[ci] = ''
                elif len(r[ci]) > l:
                    l = len(r[ci])
            return type_mapping[str] + ' ' + cn + '[' + str(l) + ']'

    def _to_root(self, data, filename):

        import ROOT
        from ROOT import TFile, TTree, gROOT, AddressOf

        columns = [self._root_column(data, i, c['type'], c['name']) for i, c in enumerate(data['cols'])]
        header = 'struct data_t { ' + ';'.join(columns) + '; };'

        gROOT.ProcessLine(header)
        row = ROOT.data_t()
        f = TFile(filename, 'RECREATE')
        tree = TTree('data', 'data from RHAPI')
        tree.Branch('data', row)

        for r in data['data']:
            for i, c in enumerate(data['cols']):
                v = r[i]
                if v is None:
                    if c['type'] == 'NUMBER':
                        v = -1
                    else:
                        v = ''
                try:
                    setattr(row, c['name'], v)
                except Exception as e:
                    print(c['name'], '=', v)
                    print(c, v)
                    print(e)
            tree.Fill()

        tree.Print()
        tree.Write()


if __name__ == '__main__':
    cli = CLIClient()
    sys.exit(cli.run())
