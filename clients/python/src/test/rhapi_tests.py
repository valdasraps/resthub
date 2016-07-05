from src.main.python.rhapi import RhApi, CLIClient
from os import system
import unittest
import json

# App URL
#URL  = "http://localhost:8888/api/"
URL  = "http://localhost:8113"
SCHEMA_NAME = u'gem_int2r'
TABLE = u'parts'
COUNT = 1

api = RhApi(URL, debug = False)
clicl = CLIClient()

class Files(object):
    
    def safeToFile(self,query_data, file_name):
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
    
    def test_get(self):
        self.assertIsNotNone(api.get(["info"], verbose = False))    
    
    def test_info(self):
        pprint = api.info()        
                
        files = Files()
        #files.safeToFile(pprint, './src/test/resources/test_info.json')
        data_from_file = files.loadFromFile('./src/test/resources/test_info.json', 'json')
        
        self.assertEquals(pprint, data_from_file)
        
    def test_folders(self):
        folders = ''.join(api.folders())
        
        self.assertEquals(SCHEMA_NAME, folders)
        
    def test_tables(self):
        tables = api.tables(SCHEMA_NAME)
        
        files = Files()        
        #files.safeToFile(tables, './src/test/resources/test_tables.json')
        data_from_file = files.loadFromFile('./src/test/resources/test_tables.json', 'json')
        
        self.assertEquals(data_from_file, tables)
        
    def test_table(self):
        table = api.table(folder = SCHEMA_NAME, table = TABLE)
        
        files = Files()        
        #files.safeToFile(table, './src/test/resources/test_table.json')
        data_from_file = files.loadFromFile('./src/test/resources/test_table.json', 'json')
        
        self.assertEquals(data_from_file, table)

    def test_qid(self):
        
        q = "select r.* from gem_int2r.parts r where r.id = :id"
        qid = api.qid(q)
        
        self.assertIsNotNone(qid)

    def test_query(self):
        q = "select r.* from gem_int2r.parts r where r.id = :id"
        qid = api.qid(q)
        query = api.query(qid)
        
        files = Files()        
        #files.safeToFile(query, './src/test/resources/test_query.json')
        data_from_file = files.loadFromFile('./src/test/resources/test_query.json', 'json')
        
        self.assertEquals(data_from_file, query)
            
    def test_count(self):
        q = "select r.* from gem_int2r.parts r where r.id = :id"
        p = {"id": 2701 }
        qid = api.qid(q)
        count_resp = api.count(qid, p)
        self.assertEqual(COUNT, count_resp)
        
    def test_data(self):
        q = "select r.* from gem_int2r.parts r where r.id = :id"
        p = {"id": 2701 }
        qid = api.qid(q)
        q_data = api.data(qid, p)
        
        files = Files()        
        #files.safeToFile(q_data, './src/test/resources/test_data.csv')
        data_from_file = files.loadFromFile('./src/test/resources/test_data.csv', 'csv')
        
        self.assertEquals(data_from_file, q_data)

    def test_csv(self):
        q = "select r.* from gem_int2r.parts r where r.id = :id"
        p = {"id": 2701 }
        qid = api.qid(q)
        q_csv = api.csv(q, p)
        
        files = Files()        
        #files.safeToFile(q_csv, './src/test/resources/test_csv.csv')
        data_from_file = files.loadFromFile('./src/test/resources/test_csv.csv', 'csv')
        
        self.assertEquals(data_from_file, q_csv)
        
    def test_xml(self):
        q = "select r.* from gem_int2r.parts r where r.id = :id"
        p = {"id": 2701 }
        qid = api.qid(q)
        q_xml = api.xml(q, p)
        
        files = Files()        
        #files.safeToFile(q_xml, './src/test/resources/test_xml.xml')
        data_from_file = files.loadFromFile('./src/test/resources/test_xml.xml', 'xml')
        
        self.assertEquals(data_from_file, q_xml) 
        
    def test_json(self):
        q = "select r.* from gem_int2r.parts r where r.id = :id"
        p = {"id": 2701 }
        qid = api.qid(q)
        q_json = api.json(q, p)
        
        files = Files()        
        #files.safeToFile(q_json, './src/test/resources/test_json.json')
        data_from_file = files.loadFromFile('./src/test/resources/test_json.json', 'json')
        
        self.assertEquals(data_from_file, q_json)     
               
    def test_json2(self):
        q = "select r.* from gem_int2r.parts r where r.id = :id"
        p = {"id": 2701 }
        qid = api.qid(q)
        q_json2 = api.json2(q, p)
        
        files = Files()        
        #files.safeToFile(q_json2, './src/test/resources/test_json2.json')
        data_from_file = files.loadFromFile('./src/test/resources/test_json2.json', 'json')
        
        self.assertEquals(data_from_file, q_json2)  

class CLIClient_test_cases(unittest.TestCase):
    
    def test_basicSelect(self):
        p=['ID=2701']
        b_select, args = clicl.basicSelect(arg='gem_int2r.parts', api=api, param=p,verbose=False)
        files = Files()        
        
        #files.safeToFile(b_select, './src/test/resources/test_basics.txt')
        data_from_file = files.loadFromFile('./src/test/resources/test_basics.txt', 'txt')
        self.assertEquals(data_from_file, b_select)
        
    def test_CLIClient_info(self):
        print 'CLIClient info--------------------------------------------------' 
        t_info = system('python ./src/main/python/rhapi.py -u http://localhost:8113 -i')  
        
        self.assertEquals(0,t_info)    
        print '----------------------------------------------------------------' 

    def test_CLIClient_folders(self):
        print 'CLIClient folders-----------------------------------------------' 
        t_folders = system('python ./src/main/python/rhapi.py -u http://localhost:8113')  
        
        self.assertEquals(0,t_folders)    
        print '----------------------------------------------------------------' 

    def test_CLIClient_folder_tables(self):
        print 'CLIClient folder tables-----------------------------------------' 
        t_f_tables = system('python ./src/main/python/rhapi.py -u http://localhost:8113 gem_int2r')  
        
        self.assertEquals(0,t_f_tables)    
        print '----------------------------------------------------------------' 

    def test_CLIClient_table_metadata(self):
        print 'CLIClient table metadata----------------------------------------' 
        t_f_table = system('python ./src/main/python/rhapi.py -u http://localhost:8113 gem_int2r.parts')  
        
        self.assertEquals(0,t_f_table)    
        print '----------------------------------------------------------------'
        
    def test_CLIClient_basic_select(self):
        print 'CLIClient basic select------------------------------------------' 
        
        #no parameters
        print 'Query with no parameters with limited size - 3:'
        t_b_select = system('python ./src/main/python/rhapi.py -u http://localhost:8113 gem_int2r.parts -f csv -s 3')  
        self.assertEquals(0,t_b_select)
        #1 parameter
        print 'Query with 1 parameter ID=1280'
        t_b_select_param = system('python ./src/main/python/rhapi.py -u http://localhost:8113 gem_int2r.parts -f csv -s 3 -p ID=1280')   
        self.assertEquals(0,t_b_select_param)
        #2 same parameters
        print 'Query with 2 same parameters ID=1280 ID=1582'
        t_b_select_same_param = system('python ./src/main/python/rhapi.py -u http://localhost:8113 gem_int2r.parts -f csv -s 3 -p ID=1280 -p ID=1582')   
        self.assertEquals(0,t_b_select_same_param)
        
        #2 diff parameters
        print 'Query with 2 different parameters ID=1582 MANUFACTURER=IBM'
        t_b_select_diff_param = system("python ./src/main/python/rhapi.py -u http://localhost:8113 gem_int2r.parts -f csv -s 3 -p ID=1582 -p MANUFACTURER='IBM'")   
        self.assertEquals(0,t_b_select_diff_param)
        
        #2 same and 1 diff parameter
        print 'Query with 2 same and 1 different parameters ID=1582 and MANUFACTURER=IBM ID=1280'
        t_b_select_diff_param = system("python ./src/main/python/rhapi.py -u http://localhost:8113 gem_int2r.parts -f csv -s 3 -p ID=1582 -p MANUFACTURER='IBM' -p ID=1280")   
        self.assertEquals(0,t_b_select_diff_param)        
        print 'Query with 2 same and other 2 same parameters ID=1582 MANUFACTURER=IBM ID=1280 MANUFACTURER=IBM2'
        
        #2 same and 2 same parameter
        t_b_select_diff_param = system("python ./src/main/python/rhapi.py -u http://localhost:8113 gem_int2r.parts -f csv -s 3 -p ID=1582 -p MANUFACTURER='IBM' -p ID=1280 -p MANUFACTURER='IBM2'")   
        self.assertEquals(0,t_b_select_diff_param)   
        
        print 'Query with 2 same and 2 different parameters ID=1582 MANUFACTURER=IBM ID=1280 BARCODE=LMNOPQRSTUVW78901244'        
        #2 same and 2 diff parameter
        t_b_select_diff_param = system("python ./src/main/python/rhapi.py -u http://localhost:8113 gem_int2r.parts -f csv -s 3 -p ID=1582 -p MANUFACTURER='IBM' -p ID=1280 -p BARCODE='LMNOPQRSTUVW78901244'")   
        self.assertEquals(0,t_b_select_diff_param)           

        print 'Query with 3 same and 2 different parameters ID=1582 ID=500 MANUFACTURER=IBM ID=1280 BARCODE=LMNOPQRSTUVW78901244'                
        #3 same and 2 diff parameter
        t_b_select_diff_param = system("python ./src/main/python/rhapi.py -u http://localhost:8113 gem_int2r.parts -f csv -s 3 -p ID=1582 -p ID=500 -p MANUFACTURER='IBM' -p ID=1280 -p BARCODE='LMNOPQRSTUVW78901244'")   
        self.assertEquals(0,t_b_select_diff_param)

        print '----------------------------------------------------------------'
        
    def test_CLIClient_select(self):
        print 'CLIClient select------------------------------------------------' 
        t_f_select = system('python ./src/main/python/rhapi.py -u http://localhost:8113 "select r.* from gem_int2r.parts r where r.id = :a or r.id = :b" -p a=1280 -p b=1582')  
        
        self.assertEquals(0,t_f_select)    
        print '----------------------------------------------------------------'          
        
def main():
    try:
        print api.folders()
        for f in api.folders():
            print api.tables(f)

        q = "select r.* from gem_int2r.parts r where r.id > :id"
        p = {"id": 2700 }
        s = 5
        
        qid = api.qid(q)
        print api.query(qid)
        print api.count(qid, p)
        print api.csv(q, p)
        csv_data = api.csv(q, p)
        print api.xml(q, p)
        print api.json(q, p)
        
    except Exception, e:
        print e

if __name__ == '__main__':
    #start old test module
    #main()
    
    #start unit tests cases
    unittest.main()
