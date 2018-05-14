from os import system, chdir, getcwd
import time
import unittest
import json
import sys

test_source_dir = getcwd()
chdir("../clients")
main_source_dir = getcwd()

sys.path.insert(0, main_source_dir)
from python.src.main.python.rhapi import RhApi, CLIClient
chdir(test_source_dir)

# App URL
URL  = "http://localhost:8112"
SCHEMA_NAME = u'store'
TABLE = u'customer'
COUNT = 1
RESOURCES_PATH = './python/src/test/resources/'
RHAPI_PATH = main_source_dir +  '/python/src/main/python/rhapi.py'
api = RhApi(URL, debug = False)
clicl = CLIClient()

class Files(object):
    
    def saveToFile(self,query_data, file_name):
        if type(query_data) is dict or type(query_data) is list:
            with open(file_name, 'w') as f:
                json.dump(query_data, f)
            f.close()
        else:
            output_file = open(file_name, 'w+')
            output_file.write(query_data)
            output_file.close()
    
    def loadFromFile(self, file_name, t):
        input_file_data = ''
        
        if t == 'json':
            with open(file_name, 'r') as f:
                try:
                    input_file_data = json.load(f)
                # if the file is empty the ValueError will be thrown
                except ValueError:
                    input_file_data = {}
            f.close()
        else:
            with open(file_name) as fp:
                for line in fp:
                    input_file_data += str(line)
                fp.close()
        return input_file_data

class Rhapi_test_cases(unittest.TestCase):
    
    def checkEqual(self, o1, o2):
        if type(o1) == type(o2) and type(o1) == type([]):
            return len(o1) == len(o2) and sorted(o1) == sorted(o2)
        else:
            return self.assertEquals(o1, o2)
    
    #function for sort dict in dict
    def deep_sort(self, obj):
        if isinstance(obj, dict):
            _sorted = {}
            for key in sorted(obj):
                _sorted[key] = self.deep_sort(obj[key])
        elif isinstance(obj, list):
            new_list = []
            for val in obj:
                new_list.append(self.deep_sort(val))
            _sorted = sorted(new_list)
        else:
            _sorted = obj
        return _sorted
    
    def test_get(self):
        self.assertIsNotNone(api.get(["info"], verbose = False))    
    
    def test_info(self):
        pprint = api.info()        
        files = Files()
        #files.saveToFile(pprint, RESOURCES_PATH + 'test_info.json')
        data_from_file = files.loadFromFile(RESOURCES_PATH+'test_info.json', 'json')

        self.assertEquals(self.deep_sort(pprint), self.deep_sort(data_from_file))
        
    def test_folders(self):
        folders = ''.join(api.folders())
        
        self.assertEquals(SCHEMA_NAME, folders)
        
    def test_tables(self):
        tables = api.tables(SCHEMA_NAME)
        
        files = Files()        
        #files.saveToFile(tables, RESOURCES_PATH+'test_tables.json')
        data_from_file = files.loadFromFile(RESOURCES_PATH+'test_tables.json', 'json')
        
        self.checkEqual(data_from_file, tables)
        
    def test_table(self):
        table = api.table(folder = SCHEMA_NAME, table = TABLE)
        
        files = Files()        
        #files.saveToFile(table, RESOURCES_PATH+'test_table.json')
        data_from_file = files.loadFromFile(RESOURCES_PATH+'test_table.json', 'json')
        
        self.assertEquals(data_from_file, table)

    def test_qid(self):
        
        q = "select r.* from store.customer r where r.id = :id"
        qid = api.qid(q)
        
        self.assertIsNotNone(qid)

    def test_query(self):
        q = "select r.* from store.customer r where r.id = :id"
        qid = api.qid(q)
        query = api.query(qid)
        
        files = Files()        
        #files.saveToFile(query, RESOURCES_PATH+'test_query.json')
        data_from_file = files.loadFromFile(RESOURCES_PATH+'test_query.json', 'json')
        
        self.assertEquals(self.deep_sort(data_from_file), self.deep_sort(query))
            
    def test_count(self):
        q = "select r.* from store.customer r where r.id = :id"
        p = {"id": 373 }
        qid = api.qid(q)
        count_resp = api.count(qid, p)
        self.assertEqual(COUNT, count_resp)
        
    def test_data(self):
        q = "select r.* from store.customer r where r.id = :id"
        p = {"id": 373 }
        qid = api.qid(q)
        q_data = api.data(qid, p)
        
        files = Files()        
        #files.saveToFile(q_data, RESOURCES_PATH+'test_data.csv')
        data_from_file = files.loadFromFile(RESOURCES_PATH+'test_data.csv', 'csv')
        
        self.assertEquals(data_from_file, q_data)

    def test_csv(self):
        q = "select r.* from store.customer r where r.id = :id"
        p = {"id": 373 }
        qid = api.qid(q)
        q_csv = api.csv(q, p)
        
        files = Files()        
        #files.saveToFile(q_csv, RESOURCES_PATH+'test_csv.csv')
        data_from_file = files.loadFromFile(RESOURCES_PATH+'test_csv.csv', 'csv')
        
        self.assertEquals(data_from_file, q_csv)
        
    def test_xml(self):
        q = "select r.* from store.customer r where r.id = :id"
        p = {"id": 373 }
        qid = api.qid(q)
        q_xml = api.xml(q, p)
        
        files = Files()        
        #files.saveToFile(q_xml, RESOURCES_PATH+'test_xml.xml')
        data_from_file = files.loadFromFile(RESOURCES_PATH+'test_xml.xml', 'xml')
        
        self.assertEquals(data_from_file, q_xml) 
        
    def test_json(self):
        q = "select r.* from store.customer r where r.id = :id"
        p = {"id": 373 }
        qid = api.qid(q)
        q_json = api.json(q, p)
        
        files = Files()        
        #files.saveToFile(q_json, RESOURCES_PATH+'test_json.json')
        data_from_file = files.loadFromFile(RESOURCES_PATH+'test_json.json', 'json')
        
        self.assertEquals(data_from_file, q_json)     
               
    def test_json2(self):
        q = "select r.* from store.customer r where r.id = :id"
        p = {"id": 373 }
        qid = api.qid(q)
        q_json2 = api.json2(q, p)
        
        files = Files()        
        #files.saveToFile(q_json2, RESOURCES_PATH+'test_json2.json')
        data_from_file = files.loadFromFile(RESOURCES_PATH+'test_json2.json', 'json')
        
        self.assertEquals(data_from_file, q_json2)  


class CLIClient_test_cases(unittest.TestCase):
    
    def test_basicSelect(self):
        p=['ID=2701']
        b_select, args = clicl.basicSelect(arg='store.customer', api=api, param=p,verbose=False)
        files = Files()        
        
        #files.saveToFile(b_select, RESOURCES_PATH+'test_basics.txt')
        data_from_file = files.loadFromFile(RESOURCES_PATH+'test_basics.txt', 'txt')
        self.assertEquals(data_from_file, b_select)
        
    def test_CLIClient_info(self):
        print 'CLIClient info--------------------------------------------------' 
        sys.stdout.flush()
        t_info = system('python '+RHAPI_PATH+' -u '+URL+' -i')  
        
        self.assertEquals(0,t_info)    
        print '----------------------------------------------------------------' 
        sys.stdout.flush()

    def test_CLIClient_folders(self):
        print 'CLIClient folders-----------------------------------------------' 
        sys.stdout.flush()
        t_folders = system('python '+RHAPI_PATH+' -u '+URL)  
        
        self.assertEquals(0,t_folders)    
        print '----------------------------------------------------------------' 
        sys.stdout.flush()

    def test_CLIClient_folder_tables(self):
        print 'CLIClient folder tables-----------------------------------------' 
        sys.stdout.flush()
        t_f_tables = system('python '+RHAPI_PATH+' -u '+URL+' store')  
        
        self.assertEquals(0,t_f_tables)    
        print '----------------------------------------------------------------' 
        sys.stdout.flush()

    def test_CLIClient_table_metadata(self):
        print 'CLIClient table metadata----------------------------------------' 
        sys.stdout.flush()
        t_f_table = system('python '+RHAPI_PATH+' -u '+URL+' store.customer')  
        
        self.assertEquals(0,t_f_table)    
        print '----------------------------------------------------------------'
        sys.stdout.flush()

    def test_CLIClient_basic_select(self):
        print 'CLIClient basic select------------------------------------------' 
        sys.stdout.flush()
        #no parameters
        print 'Query with no parameters with limited size - 3:'
        sys.stdout.flush()
        t_b_select = system('python '+RHAPI_PATH+' -u '+URL+' store.customer -f csv -s 3')  
        self.assertEquals(0,t_b_select)

        #1 parameter
        print 'Query with 1 parameter ID=373'
        sys.stdout.flush()
        t_b_select_param = system('python '+RHAPI_PATH+' -u '+URL+' store.customer -f csv -s 3 -p ID=373')   
        self.assertEquals(0,t_b_select_param)

        #2 same parameters
        print 'Query with 2 same parameters ID=373 ID=371'
        sys.stdout.flush()
        t_b_select_same_param = system('python '+RHAPI_PATH+' -u '+URL+' store.customer -f csv -s 3 -p ID=373 -p ID=371')   
        self.assertEquals(0,t_b_select_same_param)
        
        #2 diff parameters
        print 'Query with 2 different parameters ID=373 CITY=Corvallis'
        sys.stdout.flush()
        t_b_select_diff_param = system("python "+RHAPI_PATH+" -u "+URL+" store.customer -f csv -s 3 -p ID=373 -p CITY='Corvallis'")   
        self.assertEquals(0,t_b_select_diff_param)
        
        #2 same and 1 diff parameter
        print 'Query with 2 same and 1 different parameters ID=373 and CITY=Corvallis ID=371'
        sys.stdout.flush()
        t_b_select_diff_param = system("python "+RHAPI_PATH+" -u "+URL+" store.customer -f csv -s 3 -p ID=373 -p CITY='Corvallis' -p ID=371")   
        self.assertEquals(0,t_b_select_diff_param)        
        print 'Query with 2 same and other 2 same parameters ID=373 CITY=Corvallis ID=371 CITY=Berkeley'
        sys.stdout.flush()

        #2 same and 2 same parameter
        t_b_select_diff_param = system("python "+RHAPI_PATH+" -u "+URL+" store.customer -f csv -s 3 -p ID=373 -p CITY='Corvallis' -p ID=371 -p CITY='Berkeley'")   
        self.assertEquals(0,t_b_select_diff_param)   
        
        print 'Query with 2 same and 2 different parameters ID=373 CITY=Corvallis ID=371 COUNTRY=USA'        
        sys.stdout.flush()
        #2 same and 2 diff parameter
        t_b_select_diff_param = system("python "+RHAPI_PATH+" -u "+URL+" store.customer -f csv -s 3 -p ID=373 -p CITY='Corvallis' -p ID=371 -p COUNTRY='USA'")   
        self.assertEquals(0,t_b_select_diff_param)           

        print 'Query with 3 same and 2 different parameters ID=373 ID=371 CITY=Corvallis ID=372 COUNTRY=USA'                
        sys.stdout.flush()
        #3 same and 2 diff parameter
        t_b_select_diff_param = system("python "+RHAPI_PATH+" -u "+URL+" store.customer -f csv -s 3 -p ID=373 -p ID=372 -p CITY='Corvallis' -p ID=371 -p COUNTRY='USA'")   
        self.assertEquals(0,t_b_select_diff_param)

        print '----------------------------------------------------------------'
        sys.stdout.flush()

    def test_CLIClient_select(self):
        print 'CLIClient select------------------------------------------------' 
        sys.stdout.flush()
        t_f_select = system('python '+RHAPI_PATH+' -u '+URL+' "select r.* from store.customer r where r.id = :a or r.id = :b" -p a=373 -p b=371')  
        
        self.assertEquals(0,t_f_select)    
        print '----------------------------------------------------------------'          
        sys.stdout.flush()

def main():
    try:
        print api.folders()
        sys.stdout.flush()
        for f in api.folders():
            print api.tables(f)
            sys.stdout.flush()
        q = "select r.* from store.customer r where r.id > :id"
        p = {"id": 373 }
        s = 5
        
        qid = api.qid(q)
        print api.query(qid)
        sys.stdout.flush()
        print api.count(qid, p)
        sys.stdout.flush()
        print api.csv(q, p)
        sys.stdout.flush()
        csv_data = api.csv(q, p)
        print api.xml(q, p)
        sys.stdout.flush()
        print api.json(q, p)
        sys.stdout.flush()

    except Exception, e:
        print e
        sys.stdout.flush()

if __name__ == '__main__':
    #start old test module
    #main()
    
    #start unit tests cases
    unittest.main()
