import urllib2
import re
import simplejson as json
import sys
import xml.dom.minidom as minidom

"""
Python object that enables connection to RestHub API.
Errors, fixes and suggestions to be sent to project 
website in GitHub: https://github.com/valdasraps/resthub
"""

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

    def get(self, parts, data = None, headers = None, params = None, verbose = False):
        """
        General API call (do not use it directly!)
        """

        #
        # Constructing request path
        #

        callurl = self.url + "/".join(urllib2.quote(str(p)) for p in parts)
        if params != None:
            callurl = callurl + "?" + "&".join(p + "=" + urllib2.quote(str(params[p])) for p in params.keys())
            if verbose:
                callurl = callurl + "&_verbose"
        else:
            if verbose:
                callurl = callurl + "?_verbose"

        sdata = None
        if data != None:
            sdata = json.dumps(data)

        #
        # Do the query and respond
        #

        self.dprint(callurl, "with payload", sdata, "and headers", headers)

        req = urllib2.Request(url = callurl, data = data)
        if headers != None:
            for h in headers.keys():
                req.add_header(h, headers[h])

        resp = urllib2.urlopen(req)

        has_getcode = "getcode" in dir(resp)
        if self.debug: 
            if has_getcode:
                self.dprint("Response", resp.getcode(), " ".join(str(resp.info()).split("\r\n")))
            else:
                self.dprint("Response", " ".join(str(resp.info()).split("\r\n")))

        if not has_getcode or resp.getcode() == 200:
            rdata = resp.read()
            if re.search("json", resp.info().gettype()):
                try:
                    return json.loads(rdata)
                except TypeError, e:
                    self.dprint(e)
                    return rdata
            else:
                return rdata

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

    def count(self, qid, params = None, verbose = False):
        """
        Get number of rows in a query 
        """
        return int(self.get(["query", qid, "count"], params = params, verbose = verbose))

    def data(self, qid, params = None, form = 'text/csv', pagesize = None, page = None, verbose = False):
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
        return self.get(ps, None, { "Accept": form }, params, verbose = verbose)

    def csv(self, query, params = None, pagesize = None, page = None, verbose = False):
        """
        Get rows in CSV format 
        """
        qid = self.qid(query)
        return self.data(qid, params, 'text/csv', pagesize, page, verbose = verbose)

    def xml(self, query, params = None, pagesize = None, page = None, verbose = False):
        """
        Get rows in XML format 
        """
        qid = self.qid(query)
        return self.data(qid, params, 'text/xml', pagesize, page, verbose = verbose)

    def json(self, query, params = None, pagesize = None, page = None, verbose = False):
        """
        Get rows in JSON format 
        """
        qid = self.qid(query)
        return self.data(qid, params, 'application/json', pagesize, page, verbose = verbose)

from optparse import OptionParser
import pprint

USAGE = 'usage: %prog [-v] [-u URL] [ FOLDER | FOLDER.TABLE | QUERY ]'
DEFAULT_URL = "http://vocms00169:2113"
DEFAULT_FORMAT = "csv"
FORMATS = [ "csv", "xml", "json" ]

class CLIClient:
    
    def __init__(self):
        
        self.pp = pprint.PrettyPrinter(indent=4)
        self.parser = OptionParser(USAGE)
        self.parser.add_option("-v", "--verbose",  dest = "verbose",  help = "verbose output", action = "store_true", default = False)
        self.parser.add_option("-u", "--url",      dest = "url",      help = "service URL", metavar = "URL", default=DEFAULT_URL)
        self.parser.add_option("-f", "--format",   dest = "format",   help = "data output format for QUERY data (%s)" % ",".join(FORMATS), metavar = "FORMAT", default=DEFAULT_FORMAT)
        self.parser.add_option("-c", "--count",    dest = "count",    help = "instead of QUERY data return # of rows", action = "store_true", default = False)
        self.parser.add_option("-s", "--size",     dest = "size",     help = "number of rows per PAGE return for QUERY", metavar = "SIZE", type="int")
        self.parser.add_option("-g", "--page",     dest = "page",     help = "page number to return. Default 1", metavar = "PAGE", default = 1, type="int")
        self.parser.add_option("-a", "--all",      dest = "all",      help = "force to retrieve ALL data (can take long time)", action = "store_true", default = False)
        self.parser.add_option("-m", "--metadata", dest = "metadata", help = "do not execute query but dump METADATA", action = "store_true", default = False)
        self.parser.add_option("-p",               dest = "param",    help = "parameter for QUERY in form -pNAME=VALUE", metavar = "PARAM", action="append")

    def pprint(self, data):
        self.pp.pprint(data)

    def run(self):

        try:

            (options, args) = self.parser.parse_args()

            api = RhApi(options.url, debug = options.verbose)

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
            if re.match("^[a-zA-Z0-9_]+\\.[a-zA-Z0-9_]+$", arg) is not None:
                parts = arg.split(".")
                self.pprint(api.table(parts[0], parts[1], verbose = options.verbose))
                return 0

            # QUERY
            if re.match("^select ", arg, re.IGNORECASE) is not None:

                params = {}
                if options.param:
                    for ps in options.param:
                        m = re.match("^([^=]+)=(.*)$", ps)
                        if m:
                            params[m.group(1)] = m.group(2)
                            
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
                                print api.csv(arg, params = params, pagesize = options.size, page = options.page, verbose = options.verbose)
                            except RhApiRowLimitError, e:
                                if options.all:
                                    page = 0
                                    while (page * e.rowsLimit) < e.count:
                                        page = page + 1
                                        res = api.csv(arg, params = params, pagesize = e.rowsLimit, page = page, verbose = options.verbose)
                                        if page == 1:
                                            print res,
                                        else:
                                            print '\n'.join(res.split('\n')[1:]),
                                else:
                                    raise e

                        if options.format == 'xml':
                            try:
                                print api.xml(arg, params = params, pagesize = options.size, page = options.page, verbose = options.verbose)
                            except RhApiRowLimitError, e:
                                if options.all:
                                    page = 0
                                    print '<?xml version="1.0" encoding="UTF-8" standalone="no"?><data>', 
                                    while (page * e.rowsLimit) < e.count:
                                        page = page + 1
                                        res = api.xml(arg, params = params, pagesize = e.rowsLimit, page = page, verbose = options.verbose)
                                        root = minidom.parseString(res).documentElement
                                        for row in root.getElementsByTagName('row'):
                                            print row.toxml(),
                                    print '</data>'
                                else:
                                    raise e

                        if options.format == 'json':
                            try:
                                print api.json(arg, params = params, pagesize = options.size, page = options.page, verbose = options.verbose)
                            except RhApiRowLimitError, e:
                                if options.all:
                                    page = 0
                                    print '{"data": [', 
                                    while (page * e.rowsLimit) < e.count:
                                        page = page + 1
                                        res = api.json(arg, params = params, pagesize = e.rowsLimit, page = page, verbose = options.verbose)
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

        except urllib2.HTTPError, e:
            
            print "ERROR: %s\nDetails: %s" % (e.reason, e.read())
            
        except Exception, e:
            
            print "ERROR: %s\nDetails: %s" % (type(e).__name__, e)

if __name__ == '__main__':

    cli = CLIClient()
    sys.exit(cli.run())
