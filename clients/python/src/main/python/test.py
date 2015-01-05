import re, simplejson as json
from rhapi import RhApi, RhApiError

# App URL
URL  = "http://localhost:8888/api/"

def main():
    try:

        # Construct API object
        api = RhApi(URL, debug = True)

        print api.folders()
        for f in api.folders():
            print api.tables(f)

        #for f in api.folders():
        #    for t in api.tables(f):
        #        print api.table(f, t)

        q = "select * from hcal.runs r where r.runnumber > :run"
        p = {"run": 231700 }

        print api.query(q)
        print api.count(q, p)

        print api.csv(q, p)
        print api.xml(q, p)
        print api.json(q, p)

    except RhApiError, e:
        print e

if __name__ == '__main__':
    main()
