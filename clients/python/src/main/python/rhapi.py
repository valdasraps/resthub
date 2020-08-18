from __future__ import print_function

import requests
import re
import os
import json
import sys
import logging
from requests.utils import requote_uri
import xml.dom.minidom as minidom
import importlib
"""
Python object that enables connection to RestHub API.
Errors, fixes and suggestions to be sent to project 
website in GitHub: https://github.com/valdasraps/resthub
"""

import urllib3
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

class CernSSO:

    DEFAULT_TIMEOUT_SECONDS = 10

    def _init_session(self, s, url, cookiejar, auth_url_fragment):
        """
        Internal helper function: initialise the sesion by trying to access
        a given URL, setting up cookies etc.

        :param: auth_url_fragment: a URL fragment which will be joined to
        the base URL after the redirect, before the parameters. Examples are
        auth/integrated/ (kerberos) and auth/sslclient/ (SSL)
        """

        from six.moves.urllib.parse import urlparse, urljoin

        # Try getting the URL we really want, and get redirected to SSO
        logging.debug("Fetching URL: %s" % url)
        r1 = s.get(url, timeout=self.DEFAULT_TIMEOUT_SECONDS, verify = False, cookies = cookiejar)

        # Parse out the session keys from the GET arguments:
        redirect_url = urlparse(r1.url)
        logging.debug("Was redirected to SSO URL: %s" % str(redirect_url))

        # ...and inject them into the Kerberos authentication URL
        final_auth_url = "{auth_url}?{parameters}".format(
            auth_url=urljoin(r1.url, auth_url_fragment),
            parameters=redirect_url.query)

        return final_auth_url

    def _finalise_login(self, s, auth_results):
        """
        Perform the final POST authentication steps to fully authenticate
        the session, saving any cookies in s' cookie jar.
        """

        import xml.etree.ElementTree as ET

        r2 = auth_results

        # Did it work? Raise Exception otherwise.
        r2.raise_for_status()

        # Get the contents
        try:
            tree = ET.fromstring(r2.content)
        except ET.ParseError as e:
            logging.error("Could not parse response from server!")
            logging.error("The contents returned was:\n{}".format(r2.content))
            raise e

        action = tree.findall("body/form")[0].get('action')

        # Unpack the hidden form data fields
        form_data = dict((
            (elm.get('name'), elm.get('value'))
            for elm in tree.findall("body/form/input")))

        # ...and submit the form (WHY IS THIS STEP EVEN HERE!?)
        logging.debug("Performing final authentication POST to %s" % action)
        r3 = s.post(url = action, data = form_data, timeout = self.DEFAULT_TIMEOUT_SECONDS, allow_redirects=False)

        # Did _that_ work?
        r3.raise_for_status()

        # The session cookie jar should now contain the necessary cookies.
        logging.debug("Cookie jar now contains: %s" % str(s.cookies))

        return s.cookies

    def krb_sign_on(self, url, cookiejar={}, force_level = 0):
        """
        Perform Kerberos-backed single-sign on against a provided
        (protected) URL.

        It is assumed that the current session has a working Kerberos
        ticket.

        Returns a Requests `CookieJar`, which can be accessed as a
        dictionary, but most importantly passed directly into a request or
        session via the `cookies` keyword argument.

        If a cookiejar-like object (such as a dictionary) is passed as the
        cookiejar keword argument, this is passed on to the Session.
        """

        from requests_kerberos import HTTPKerberosAuth, OPTIONAL

        kerberos_auth = HTTPKerberosAuth(mutual_authentication=OPTIONAL)

        with requests.Session() as s:

            krb_auth_url = self._init_session(s=s, url=url, cookiejar=cookiejar,
                                        auth_url_fragment=u"auth/integrated/")

            # Perform actual Kerberos authentication
            logging.debug("Performing Kerberos authentication against %s"
                    % krb_auth_url)

            r2 = s.get(krb_auth_url, auth=kerberos_auth,
                      timeout=self.DEFAULT_TIMEOUT_SECONDS)

            return self._finalise_login(s, auth_results=r2)


    def cert_sign_on(self, url, cert_file, key_file, cookiejar={}):
        """
        Perform Single-Sign On with a robot/user certificate specified by
        cert_file and key_file agains the target url. Note that the key
        needs to be passwordless. cookiejar, if provided, will be used to
        store cookies, and can be a Requests CookieJar, or a
        MozillaCookieJar. Or even a dict.

        Cookies will be returned on completion, but cookiejar will also be
        modified in-place.

        If you have a PKCS12 (.p12) file, you need to convert it. These
        steps will not work for passwordless keys.

        `openssl pkcs12 -clcerts -nokeys -in myCert.p12 -out ~/private/myCert.pem`

        `openssl pkcs12 -nocerts -in myCert.p12 -out ~/private/myCert.tmp.key`

        `openssl rsa -in ~/private/myCert.tmp.key -out ~/private/myCert.key`

        Note that the resulting key file is *unencrypted*!

        """

        with requests.Session() as s:

            # Set up the certificates (this needs to be done _before_ any
            # connection is opened!)
            s.cert = (cert_file, key_file)

            cert_auth_url = self._init_session(s=s, url=url, cookiejar=cookiejar,
                                          auth_url_fragment=u"auth/sslclient/")

            logging.debug("Performing SSL Cert authentication against %s"
                    % cert_auth_url)

            r2 = s.get(cert_auth_url, cookies = cookiejar, verify = False, timeout = self.DEFAULT_TIMEOUT_SECONDS)

            return self._finalise_login(s, auth_results=r2)

class CernLoginSSO:

    def file_mtime(self, file_path):
        try:
            return os.path.getmtime(file_path)
        except OSError:
            return -1

    def login(self, url, cache_file = ".session.cache", force_level = 0):

        import json
        from getpass import getpass
        from os import remove, path
        import requests
        import warnings
        from base64 import b64encode, b64decode
        from ilock import ILock
        from selenium import webdriver
        from selenium.webdriver.firefox.options import Options
        from selenium.webdriver.support.ui import WebDriverWait
        from selenium.webdriver.support import expected_conditions as EC

        cache = None
        cache_file = path.abspath(cache_file)
        cache_lock_id = b64encode(cache_file.encode('utf-8')).decode()
        cache_time = self.file_mtime(cache_file)

        with ILock(cache_lock_id):

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

                options = Options()
                options.headless = True
                driver = webdriver.Firefox(options = options)
                driver.get(url)

                in_login = driver.find_element_by_xpath("//td[@class='box_login']//input")
                in_login.clear()
                in_login.send_keys(username)
                in_passw = driver.find_element_by_xpath("//td[@class='box_password']//input")
                in_passw.clear()
                in_passw.send_keys(password)
                in_submit = driver.find_element_by_xpath("//td[@class='box_signinbutton']//input")
                in_submit.click()

                WebDriverWait(driver, 10).until(EC.url_to_be(url))

                cache = {
                    'secret': b64encode((username + '/' + b64encode(password.encode()).decode()).encode()).decode(),
                    'location': driver.current_url,
                    'cookies': { i['name']: i['value'] for i in driver.get_cookies() }
                }

                driver.close()

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
        return 'Column name (%s) does not exist in the table (%s). Try these columns: (%s).'\
             % (self.bad_column, self.table_name, json.dumps(self.columns_list))

class RhApi:
    """
    RestHub API object
    """

    def __init__(self, url, debug = False, sso = None, sso_cert = None):
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
                self.cprov = lambda url, force_level: (CernLoginSSO().login(url, force_level = force_level), force_level)
            if sso == 'krb':
                self.cprov = lambda url, force_level: (CernSSO().krb_sign_on(url), 2)
            if sso == 'cert' and sso_cert is not None:
                cert_file, key_file = sso_cert.split(':')
                self.cprov = lambda url, force_level: (CernSSO().cert_sign_on(url, cert_file, key_file), 2)

    def _action(self, action, url, headers, data):
        force_level = 0
        while True:

            cookies = None
            if self.cprov is not None:
                cookies, force_level = self.cprov(self.url, force_level)

            r = action(url = url, headers = headers, data = data, cookies = cookies, verify = False)

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
            print("RhApi:", end = '')
            for arg in args:
                print(arg, end = '')
            print()

    def get(self, parts, data = None, headers = None, params = None, verbose = False, cols = False, inline_clobs = False, method = None):
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
            resp = self._action(action, headers = headers, url = callurl, data = data)
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

    def info(self, verbose = False):
        """
        Get server version information
        """
        return self.get(["info"], verbose = verbose)
    
    def folders(self, verbose = False):
        """
        Get list of folders
        """
        return list(self.get(["tables"], verbose = verbose).keys())

    def tables(self, folder, verbose = False):
        """
        Get tables for folder or all
        """
        raw = self.get(["tables"], verbose = verbose)
        d = []
        for t in raw[folder].keys(): 
            d.append(t)
        return d

    def table(self, folder, table, verbose = False):
        """
        Get info for table
        """
        return self.get(["table", folder, table], verbose = verbose)

    def qid(self, query):
        """
        Create query based on [query] and return its ID
        """
        return self.get(["query"], query)

    def query(self, qid, verbose = False):
        """
        Return qid metadata (assuming it exists..)
        """
        return self.get(["query", qid], verbose = verbose)

    def clean(self, qid, verbose = False):
        """
        Remove cache for query (assuming it exists..)
        """
        return self.get(["query", qid, "cache"], verbose = verbose, method = 'DELETE')

    def count(self, qid, params = None, verbose = False):
        """
        Get number of rows in a query 
        """
        return int(self.get(["query", qid, "count"], params = params, verbose = verbose))

    def data(self, qid, params = None, form = 'text/csv', pagesize = None, page = None, verbose = False, cols = False, inline_clobs = False):
        """
        Get data rows
        """

        rowsLimit = self.query(qid, verbose = True)["rowsLimit"]
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
        return self.get(ps, None, { "Accept": form }, params, verbose = verbose, cols = cols, inline_clobs = inline_clobs)

    def csv(self, query, params = None, pagesize = None, page = None, verbose = False, inline_clobs = False):
        """
        Get rows in CSV format 
        """
        qid = self.qid(query)
        return self.data(qid, params, 'text/csv', pagesize, page, verbose = verbose, inline_clobs = inline_clobs)

    def xml(self, query, params = None, pagesize = None, page = None, verbose = False, inline_clobs = False):
        """
        Get rows in XML format 
        """
        qid = self.qid(query)
        return self.data(qid, params, 'text/xml', pagesize, page, verbose = verbose, inline_clobs = inline_clobs)

    def json(self, query, params = None, pagesize = None, page = None, verbose = False, cols = False, inline_clobs = False):
        """
        Get rows in JSON format (array of arrays)
        """
        qid = self.qid(query)
        return self.data(qid, params, 'application/json', pagesize, page, verbose = verbose, cols = cols, inline_clobs = inline_clobs)
    
    def json_all(self, query, params = None, verbose = False, cols = False, inline_clobs = False):
        """
        Get all rows in JSON format (array of arrays)
        """
        
        rows = []
        
        qid = self.qid(query)     
        rowsLimit = self.query(qid, verbose = True)["rowsLimit"]
        count = int(self.count(qid, params))
        pages = int(count/rowsLimit) + 1
        
        for page in range(1, (pages + 1)):
            data = self.data(qid, params, form="application/json", page = page, pagesize = rowsLimit, verbose = verbose, cols = cols, inline_clobs = inline_clobs)
            rows.extend(data["data"])
        
        if count != len(rows):
            raise RhApiRowCountError(count, len(rows))
        
        return rows
    
    def json2(self, query, params = None, pagesize = None, page = None, verbose = False, cols = False, inline_clobs = False):
        """
        Get rows in JSON2 format (array or objects)
        """
        qid = self.qid(query)
        return self.data(qid, params, 'application/json2', pagesize, page, verbose = verbose, cols = cols, inline_clobs = inline_clobs)

from optparse import OptionParser
import pprint

USAGE = 'usage: %prog [-v] [-u URL] [ FOLDER | FOLDER.TABLE | QUERY ]'
DEFAULT_URL = "http://vocms00170:2113"
DEFAULT_FORMAT = "csv"
DEFAULT_ROOT_FILE = "data.root"
FORMATS = [ "csv", "xml", "json", "json2", "root" ]
SSO_LOGIN_URL = "https://login.cern.ch/"

class CLIClient:
    
    def __init__(self):
        
        self.pp = pprint.PrettyPrinter(indent=4)
        self.parser = OptionParser(USAGE)
        self.parser.add_option("-v", "--verbose",  dest = "verbose",  help = "verbose output. Default: %s" % False, action = "store_true", default = False)
        self.parser.add_option("-u", "--url",      dest = "url",      help = "service URL. Default: %s" % DEFAULT_URL, metavar = "URL", default=DEFAULT_URL)
        self.parser.add_option("-o", "--login",   dest = "login",   help = "use simple login provider cache (requires selenium, stores pwd in not secure way!)", metavar = "login", action = "store_true", default = False)
        self.parser.add_option("-k", "--krb",     dest = "krb",     help = "use kerberos login provider", metavar = "krb", action = "store_true", default = False)
        self.parser.add_option("-t", "--cert",    dest = "cert",    help = "pem certificate and key files in form cert_file:key_file", metavar = "cert")
        self.parser.add_option("-f", "--format",   dest = "format",   help = "data output format for QUERY data (%s). Default: %s" % (",".join(FORMATS), DEFAULT_FORMAT), metavar = "FORMAT", default = DEFAULT_FORMAT)
        self.parser.add_option("-c", "--count",    dest = "count",    help = "instead of QUERY data return # of rows", action = "store_true", default = False)
        self.parser.add_option("-s", "--size",     dest = "size",     help = "number of rows per PAGE return for QUERY", metavar = "SIZE", type="int")
        self.parser.add_option("-g", "--page",     dest = "page",     help = "page number to return. Default 1", metavar = "PAGE", default = 1, type="int")
        self.parser.add_option("-l", "--cols",     dest = "cols",     help = "add column metadata if possible. Default: False", action = "store_true", default = False)
        self.parser.add_option("-b", "--inclob",   dest = "inclob",   help = "inline clobs directly into the output. Default: False (send as links)", action = "store_true", default = False)
        self.parser.add_option("-i", "--info",     dest = "info",     help = "print server version information. Default: False", action = "store_true", default = False)
        self.parser.add_option("-a", "--all",      dest = "all",      help = "force to retrieve ALL data (can take long time). Default: False", action = "store_true", default = False)
        self.parser.add_option("-m", "--metadata", dest = "metadata", help = "do not execute query but dump METADATA. Default: False", action = "store_true", default = False)
        self.parser.add_option("-n", "--clean",    dest = "clean",    help = "clean cache before executing query (new results). Default: False", action = "store_true", default = False)
        self.parser.add_option("-p",               dest = "param",    help = "parameter for QUERY in form -pNAME=VALUE", metavar = "PARAM", action="append")
        self.parser.add_option("-r", "--root",     dest = "root",     help = "ROOT file name, if format set to root. Default: " + DEFAULT_ROOT_FILE, metavar = "ROOT", default=DEFAULT_ROOT_FILE)

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
                    previous = param_column_names_list[index-1]
                if index < (params_length -1):
                    next = param_column_names_list[index+1]
                if params_length == 1:
                    arg = arg + " where a." + param_column_names_list[index] + " = :" + param_column_names_list[index]                
                else:
                    if index == 0:
                        if param_column_names_list[index] == next:
                            arg = arg + " where (a." + param_column_names_list[index] + " = :" + param_column_names_list[index]
                        else:
                            arg = arg + " where a." + param_column_names_list[index] + " = :" + param_column_names_list[index]
                    else:
                        if previous == param_column_names_list[index]:
                            
                            if param_column_names_list[index] != next:
                                arg = arg + " or a." + param_column_names_list[index] + " = :" + param_column_names_list[index] + str(index) + ")"
                                param[index] = param_column_names_list[index] + str(index) + '=' + param_column_value_list[index]
                            else:
                                arg = arg + " or a." + param_column_names_list[index] + " = :" + param_column_names_list[index] + str(index)
                                param[index] = param_column_names_list[index] + str(index) + '=' + param_column_value_list[index]
                        else:
                            if param_column_names_list[index] != next:
                                arg = arg + " and a." + param_column_names_list[index] + " = :" + param_column_names_list[index]
                            else:
                                arg = arg + " and ( a." + param_column_names_list[index] + " = :" + param_column_names_list[index]                  
                
                previous = next = None
                
        return arg, param

    def run(self):

        try:

            (options, args) = self.parser.parse_args()

            sso = None
            sso_cert = None
            if re.search("^https", options.url):
                
                if sum((options.login, options.krb, options.cert is not None)) != 1:
                    self.parser.error('For secure access please provide one of --krb, --cert or --login')
                    return 1

                if options.login:
                    sso = 'login'

                if options.krb:
                    sso = 'krb'

                if options.cert is not None:
                    sso = 'cert'
                    sso_cert = options.cert

            api = RhApi(options.url, debug = options.verbose, sso = sso, sso_cert = sso_cert)

            # Info
            if options.info:
                self.pprint(api.info(verbose = options.verbose))
                return 0

            # Folders
            if len(args) == 0:
                self.pprint(api.folders(verbose = options.verbose))
                return 0

            if len(args) > 1:
                self.parser.error('More than one command found. Maybe double quotes are missing?')
                return 0

            arg = args[0]

            # FOLDER tables
            if re.match("^[a-zA-Z0-9_]+$", arg) is not None:
                self.pprint(api.tables(arg, verbose = options.verbose))
                return 0


            # FOLDER.TABLE
            if ((re.match("^[a-zA-Z0-9_]+\\.[a-zA-Z0-9_]+$", arg) is not None) and (options.format is None)):
                parts = arg.split(".")
                self.pprint(api.table(parts[0], parts[1], verbose = options.verbose))
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
                    api.clean(api.qid(arg), verbose = options.verbose)

                if options.count:
                    
                    print(api.count(api.qid(arg), params = params, verbose = options.verbose))
                    
                elif options.metadata:
                    
                    qid = api.qid(arg)
                    print(self.pprint(api.query(qid, verbose = options.verbose)))
                        
                else:
                    
                    if FORMATS.count(options.format) == 0:
                        
                        self.parser.error('Format %s not understood: please use one of %s' % (options.format, ",".join(FORMATS)))

                    else:
                        
                        if options.size and options.page and options.all:
                            self.parser.error('Wrong combination of options: ALL and SIZE both can not be defined')
                        
                        if options.format == 'csv':
                            try:
                                print(api.csv(arg, params = params, pagesize = options.size, page = options.page, verbose = options.verbose, inline_clobs = options.inclob))
                            except RhApiRowLimitError as e:
                                if options.all:
                                    page = 0
                                    while (page * e.rowsLimit) < e.count:
                                        page = page + 1
                                        res = api.csv(arg, params = params, pagesize = e.rowsLimit, page = page, verbose = options.verbose, inline_clobs = options.inclob)
                                        if page == 1:
                                            print(res, end = '')
                                        else:
                                            print('\n'.join(res.split('\n')[1:]), end = '')
                                else:
                                    raise e

                        if options.format == 'xml':
                            try:
                                print(api.xml(arg, params = params, pagesize = options.size, page = options.page, verbose = options.verbose, inline_clobs = options.inclob))
                            except RhApiRowLimitError as e:
                                if options.all:
                                    page = 0
                                    print('<?xml version="1.0" encoding="UTF-8" standalone="no"?><data>', end = '')
                                    while (page * e.rowsLimit) < e.count:
                                        page = page + 1
                                        res = api.xml(arg, params = params, pagesize = e.rowsLimit, page = page, verbose = options.verbose, inline_clobs = options.inclob)
                                        root = minidom.parseString(res).documentElement
                                        for row in root.getElementsByTagName('row'):
                                            print(row.toxml(), end = '')
                                    print('</data>')
                                else:
                                    raise e

                        if options.format in ['json','json2','root']:
                            try:
                                method_name = options.format if options.format != 'root' else 'json'
                                method = getattr(api, method_name, None)
                                in_cols = options.cols if options.format != 'root' else True
                                data = method(arg, params = params, pagesize = options.size, page = options.page, verbose = options.verbose, cols = in_cols, inline_clobs = options.inclob)
                            except RhApiRowLimitError as e:
                                if options.all:
                                    page = 0
                                    data = None
                                    while (page * e.rowsLimit) < e.count:
                                        page = page + 1
                                        res = method(arg, params = params, pagesize = e.rowsLimit, page = page, verbose = options.verbose, cols = in_cols, inline_clobs = options.inclob)
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
            str:   'Char_t',
            int:   'Int_t',
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

        columns = [ self._root_column(data, i, c['type'], c['name']) for i,c in enumerate(data['cols']) ]
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
                    if c['type'] == 'NUMBER': v = -1
                    else: v = ''
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

