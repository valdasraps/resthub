from rhapi import RhApi

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

        qid = api.qid(q)
        print api.query(qid)
        print api.count(qid, p)

        print api.csv(qid, p)
        print api.xml(qid, p)
        print api.json(qid, p)

    except Exception, e:
        print e

if __name__ == '__main__':
    main()
