import urllib2, re, simplejson as json, socket

"""
Python object that enables connection to RestHub API.
Errors, fixes and suggestions to be sent to project 
website in GitHub: https://github.com/valdasraps/resthub
"""

class RhApiError(Exception):
    """
    API Exception class
    """

    def __init__(self, resp):
        """
        Construct exception by providing response object.
        """
        if type(resp) == str:
            self.message = resp
        else:
            self.url = resp.geturl()
            self.code = resp.getcode()
            self.stack = None
            for line in resp.read().split("\n"):
                if self.stack == None:
                    m = re.search("<pre>(.*)", line)
                    if m != None:
                        self.stack = m.group(1)
                        m = re.search("^.+\.([^\.]+: .*)$", self.stack)
                        if m != None:
                            self.message = m.group(1)
                        else:
                            self.message = line
                else:
                    m = re.search("(.*)</pre>", line)
                    if m != None:
                        self.stack = self.stack + "\n" + m.group(1)
                        break
                    else:
                        self.stack = self.stack + "\n" + line

    def __str__(self):
        """ Get message """
        return self.message

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

    def get(self, parts, data = None, headers = None, params = None):
        """
        General API call (do not use it directly!)
        """

        #
        # Constructing request path
        #

        callurl = self.url + "/".join(urllib2.quote(p) for p in parts)
        if params != None:
            callurl = callurl + "?" + "&".join(p + "=" + urllib2.quote(str(params[p])) for p in params.keys())

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
        else:
            raise RhApiError(resp)

    def folders(self):
        """
        Get list of folders
        """
        return self.get(["tables"]).keys()

    def tables(self, folder):
        """
        Get tables for folder or all
        """
        raw = self.get(["tables"])
        d = []
        for t in raw[folder].keys(): 
            d.append(t)
        return d

    def table(self, folder, table):
        """
        Get info for table
        """
        return self.get(["table", folder, table])

    def query(self, query):
        """
        Create query and return its ID
        """
        return self.get(["query"], query)

    def count(self, query, params = None):
        """
        Get number of rows in a query 
        """
        qid = self.query(query)
        return self.get(["query", qid, "count"], params = params)

    def data(self, query, params = None, form = 'text/csv', pagesize = None, page = None):
        """
        Get data rows
        """
        qid = self.query(query)
        ps = ["query", qid]
        if pagesize != None and page != None:
            ps.extend(["page", pagesize, page]);
        ps.append("data")
        return self.get(ps, None, { "Accept": form }, params)

    def csv(self, query, params = None, pagesize = None, page = None):
        """
        Get rows in CSV format 
        """
        return self.data(query, params, 'text/csv', pagesize, page)

    def xml(self, query, params = None, pagesize = None, page = None):
        """
        Get rows in XML format 
        """
        return self.data(query, params, 'text/xml', pagesize, page)

    def json(self, query, params = None, pagesize = None, page = None):
        """
        Get rows in JSON format 
        """
        return self.data(query, params, 'application/json', pagesize, page)

if __name__ == '__main__':

    print "RestHub API library."
