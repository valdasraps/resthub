import requests
import re
import json
import sys
from requests.utils import requote_uri
import xml.dom.minidom as minidom
"""
Python object that enables connection to RestHub API.
Errors, fixes and suggestions to be sent to project 
website in GitHub: https://github.com/valdasraps/resthub
"""

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

    def __init__(self, url, debug = False):
        """
        Construct API object.
        url: URL to RestHub endpoint, i.e. http://localhost:8080/api
        debug: should debug messages be printed out? Verbose!
        """
        if re.match("/$", url) is None:
            url = url + "/"
        self.url = url
        self.debug = debug
        self.dprint("url = ", self.url)

    def dprint(self, *args):
        """
        Print debug information
        """
        if self.debug: 
            print "RhApi:",
            for arg in args:
                print arg, 
            print

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

        # Check https and gab cookies if needed

        cookies = None
        if re.search("^https", callurl):
            cookies = krb_sign_on(callurl)

        action = getattr(requests, method, None)
        if action:
            resp = action(headers = headers, url = callurl, data = data, cookies = cookies)
        else:
            raise NameError('Unknown HTTP method: ' + method)

        has_getcode = "status_code" in dir(resp)
        if self.debug: 
            if has_getcode:
                self.dprint("Response", resp.status_code, " ".join(str(resp.headers.get('content-type')).split("\r\n")))
            else:
                self.dprint("Response", " ".join(str(resp.headers.get('content-type')).split("\r\n")))

        if resp.status_code == requests.codes.ok:
            rdata = resp.text
            if re.search("json", resp.headers.get('content-type')):
                try:
                    return json.loads(rdata)
                except TypeError, e:
                    self.dprint(e)
                    return rdata
            else:
                return rdata

    def info(self, verbose = False):
        """
        Get server version information
        """
        return self.get(["info"], verbose = verbose)
    
    def folders(self, verbose = False):
        """
        Get list of folders
        """
        return self.get(["tables"], verbose = verbose).keys()

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
FORMATS = [ "csv", "xml", "json", "json2" ]

class CLIClient:
    
    def __init__(self):
        
        self.pp = pprint.PrettyPrinter(indent=4)
        self.parser = OptionParser(USAGE)
        self.parser.add_option("-v", "--verbose",  dest = "verbose",  help = "verbose output", action = "store_true", default = False)
        self.parser.add_option("-u", "--url",      dest = "url",      help = "service URL", metavar = "URL", default=DEFAULT_URL)
        self.parser.add_option("-f", "--format",   dest = "format",   help = "data output format for QUERY data (%s)" % ",".join(FORMATS), metavar = "FORMAT")
        self.parser.add_option("-c", "--count",    dest = "count",    help = "instead of QUERY data return # of rows", action = "store_true", default = False)
        self.parser.add_option("-s", "--size",     dest = "size",     help = "number of rows per PAGE return for QUERY", metavar = "SIZE", type="int")
        self.parser.add_option("-g", "--page",     dest = "page",     help = "page number to return. Default 1", metavar = "PAGE", default = 1, type="int")
        self.parser.add_option("-l", "--cols",     dest = "cols",     help = "add column metadata if possible. Default: false", action = "store_true", default = False)
        self.parser.add_option("-b", "--inclob",   dest = "inclob",   help = "inline clobs directly into the output. Default: false (send as links)", action = "store_true", default = False)
        self.parser.add_option("-i", "--info",     dest = "info",     help = "print server version information", action = "store_true", default = False)
        self.parser.add_option("-a", "--all",      dest = "all",      help = "force to retrieve ALL data (can take long time)", action = "store_true", default = False)
        self.parser.add_option("-m", "--metadata", dest = "metadata", help = "do not execute query but dump METADATA", action = "store_true", default = False)
        self.parser.add_option("-n", "--clean",    dest = "clean",    help = "clean cache before executing query (new results)", action = "store_true", default = False)
        self.parser.add_option("-p",               dest = "param",    help = "parameter for QUERY in form -pNAME=VALUE", metavar = "PARAM", action="append")

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

            api = RhApi(options.url, debug = options.verbose)

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
                    
                    print api.count(api.qid(arg), params = params, verbose = options.verbose)
                    
                elif options.metadata:
                    
                    qid = api.qid(arg)
                    print self.pprint(api.query(qid, verbose = options.verbose))
                        
                else:
                    
                    if FORMATS.count(options.format) == 0:
                        
                        self.parser.error('Format %s not understood: please use one of %s' % (options.format, ",".join(FORMATS)))

                    else:
                        
                        if options.size and options.page and options.all:
                            self.parser.error('Wrong combination of options: ALL and SIZE both can not be defined')
                        
                        if options.format == 'csv':
                            try:
                                print api.csv(arg, params = params, pagesize = options.size, page = options.page, verbose = options.verbose, inline_clobs = options.inclob)
                            except RhApiRowLimitError, e:
                                if options.all:
                                    page = 0
                                    while (page * e.rowsLimit) < e.count:
                                        page = page + 1
                                        res = api.csv(arg, params = params, pagesize = e.rowsLimit, page = page, verbose = options.verbose, inline_clobs = options.inclob)
                                        if page == 1:
                                            print res,
                                        else:
                                            print '\n'.join(res.split('\n')[1:]),
                                else:
                                    raise e

                        if options.format == 'xml':
                            try:
                                print api.xml(arg, params = params, pagesize = options.size, page = options.page, verbose = options.verbose, inline_clobs = options.inclob)
                            except RhApiRowLimitError, e:
                                if options.all:
                                    page = 0
                                    print '<?xml version="1.0" encoding="UTF-8" standalone="no"?><data>', 
                                    while (page * e.rowsLimit) < e.count:
                                        page = page + 1
                                        res = api.xml(arg, params = params, pagesize = e.rowsLimit, page = page, verbose = options.verbose, inline_clobs = options.inclob)
                                        root = minidom.parseString(res).documentElement
                                        for row in root.getElementsByTagName('row'):
                                            print row.toxml(),
                                    print '</data>'
                                else:
                                    raise e

                        if options.format in ['json','json2']:
                            try:
                                if options.format == 'json':
                                    print api.json(arg, params = params, pagesize = options.size, page = options.page, verbose = options.verbose, cols = options.cols, inline_clobs = options.inclob)
                                else:
                                    print api.json2(arg, params = params, pagesize = options.size, page = options.page, verbose = options.verbose, cols = options.cols, inline_clobs = options.inclob)
                            except RhApiRowLimitError, e:
                                if options.all:
                                    page = 0
                                    print '{"data": [', 
                                    while (page * e.rowsLimit) < e.count:
                                        page = page + 1
                                        res = api.json(arg, params = params, pagesize = e.rowsLimit, page = page, verbose = options.verbose, inline_clobs = options.inclob)
                                        comma = ','
                                        if page == 1: comma = ''
                                        for d in res['data']:
                                            print comma, d,
                                            comma = ','
                                    print "]}"
                                else:
                                    raise e
                        
                return 0

            self.parser.error('Command %s not understood' % arg)

        except RhApiRowLimitError, e:
            
            print "ERROR: %s\nDetails: %s, consider --all option" % (type(e).__name__, e)

        except requests.exceptions.RequestException, e:
	    reason = e.reason if hasattr(e, 'reason') else '%d %s' % (e.code, e.msg)
	    print "ERROR: %s\nDetails: %s" % (reason, e)
            
        except Exception, e:
            
            print "ERROR: %s\nDetails: %s" % (type(e).__name__, e)

##
##  Pasting in code from https://pypi.org/project/python-cern-sso-krb/
##

# Copyright (C) 2017, CERN
# This software is distributed under the terms of the GNU General Public
# Licence version 3 (GPL Version 3), copied verbatim in the file "LICENSE".
# In applying this license, CERN does not waive the privileges and immunities
# granted to it by virtue of its status as Intergovernmental Organization
# or submit itself to any jurisdiction.

from six.moves.urllib.parse import urlparse, urljoin

import logging
import xml.etree.ElementTree as ET

import requests
from requests_kerberos import HTTPKerberosAuth, OPTIONAL

try:  # Python 2.7+
    from logging import NullHandler
except ImportError:
    # Hello, you are using a 10 year old software. :(
    class NullHandler(logging.Handler):
        def emit(self, record):
            pass


log = logging.getLogger(__name__)
log.addHandler(NullHandler())

DEFAULT_TIMEOUT_SECONDS = 10


def _init_session(s, url, cookiejar, auth_url_fragment):
    """
    Internal helper function: initialise the sesion by trying to access
    a given URL, setting up cookies etc.


    :param: auth_url_fragment: a URL fragment which will be joined to
    the base URL after the redirect, before the parameters. Examples are
    auth/integrated/ (kerberos) and auth/sslclient/ (SSL)
    """

    if cookiejar is not None:
        log.debug("Using provided cookiejar")
        s.cookies = cookiejar

    # Try getting the URL we really want, and get redirected to SSO
    log.info("Fetching URL: %s" % url)
    r1 = s.get(url, timeout=DEFAULT_TIMEOUT_SECONDS)

    # Parse out the session keys from the GET arguments:
    redirect_url = urlparse(r1.url)
    log.debug("Was redirected to SSO URL: %s" % str(redirect_url))

    # ...and inject them into the Kerberos authentication URL
    final_auth_url = "{auth_url}?{parameters}".format(
        auth_url=urljoin(r1.url, auth_url_fragment),
        parameters=redirect_url.query)

    return final_auth_url


def _finalise_login(s, auth_results):
    """
    Perform the final POST authentication steps to fully authenticate
    the session, saving any cookies in s' cookie jar.
    """

    r2 = auth_results

    # Did it work? Raise Exception otherwise.
    r2.raise_for_status()

    # Get the contents
    try:
        tree = ET.fromstring(r2.content)
    except ET.ParseError as e:
        log.error("Could not parse response from server!")
        log.error("The contents returned was:\n{}".format(r2.content))
        raise e

    action = tree.findall("body/form")[0].get('action')

    # Unpack the hidden form data fields
    form_data = dict((
        (elm.get('name'), elm.get('value'))
        for elm in tree.findall("body/form/input")))

    # ...and submit the form (WHY IS THIS STEP EVEN HERE!?)
    log.debug("Performing final authentication POST to %s" % action)
    r3 = s.post(url=action, data=form_data, timeout=DEFAULT_TIMEOUT_SECONDS)

    # Did _that_ work?
    r3.raise_for_status()

    # The session cookie jar should now contain the necessary cookies.
    log.debug("Cookie jar now contains: %s" % str(s.cookies))

    return s.cookies


def krb_sign_on(url, cookiejar=None):
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

    kerberos_auth = HTTPKerberosAuth(mutual_authentication=OPTIONAL)

    with requests.Session() as s:

        krb_auth_url = _init_session(s=s, url=url, cookiejar=cookiejar,
                                     auth_url_fragment=u"auth/integrated/")

        # Perform actual Kerberos authentication
        log.info("Performing Kerberos authentication against %s"
                 % krb_auth_url)

        r2 = s.get(krb_auth_url, auth=kerberos_auth,
                   timeout=DEFAULT_TIMEOUT_SECONDS)

        return _finalise_login(s, auth_results=r2)


def cert_sign_on(url, cert_file, key_file, cookiejar={}):
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

        cert_auth_url = _init_session(s=s, url=url, cookiejar=cookiejar,
                                      auth_url_fragment=u"auth/sslclient/")

        log.info("Performing SSL Cert authentication against %s"
                 % cert_auth_url)

        r2 = s.get(cert_auth_url, cookies=cookiejar, verify=False,
                   timeout=DEFAULT_TIMEOUT_SECONDS)

        return _finalise_login(s, auth_results=r2)

##
##  End of pasting in code from https://pypi.org/project/python-cern-sso-krb/
##

if __name__ == '__main__':

    cli = CLIClient()
    sys.exit(cli.run())
