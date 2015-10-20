package lt.emasina.server.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.client.RestHubServer;
import lt.emasina.resthub.model.DataResponse;
import lt.emasina.resthub.model.QueryManager;
import org.json.JSONException;
import static org.junit.Assert.assertEquals;

@Log4j
public class JavaClientWorker {
    
    private static final String HEADER_FILE = "/lt/emasina/server/results/client_headers";
    private static final String DATA_FILE = "/lt/emasina/server/results/client_data";
    private static final String[] EXCLUDE_HEADERS = {"Date", "Expires", "Accept-Ranges", "Allow"};
    
    private final Random random = new Random();
    
    public void setupQueries() throws IOException, URISyntaxException, org.json.JSONException {
        
        RestHubServer rh = new RestHubServer(TestSuite.HOST);

        QueryManager qm = rh.newQueryManager("SELECT * FROM (SELECT c.ID,c.BRAND FROM store.products c WHERE c.ID > 100 ORDER BY c.BRAND desc) a ORDER BY a.ID asc;");
        qm.addParameter("p1", random.nextInt());

        check(qm);
        
    }
    
    private void check(QueryManager qm) throws IOException, JSONException {
        
        log.debug("Cheking query headers");
        Map headers = qm.options();
        
        // Checking headers file
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(HEADER_FILE);
        
        // Getting available content types 
        String ct = (String) headers.get("Content-Type");
        String[] contentTypes = ct.split(",");     

        if (inputStream == null) {
            headersToFile(headers);
        } else {
            compareHeaders(headers, inputStream);
        }

        log.debug("Cheking query data");
        inputStream = getClass().getClassLoader().getResourceAsStream(DATA_FILE);
        Properties prop = new Properties();
        
        // Check each contentType   
        for (String contentType : contentTypes) { 
            
            DataResponse dr = qm.getData(contentType);
            String data = dr.getString();
            
            if (inputStream == null) {
                dataToFile(data, contentType, prop);
            } else {
                log.debug("Comparing " + contentType + " data");
                prop.load(inputStream);
                
                String expected_value = prop.getProperty(contentType);
                assertEquals(expected_value, data);

            }
       }

    }
    
    private void headersToFile(Map headers) throws IOException {

        log.debug("Writing headers to file");
        Properties prop = new Properties();
        
        Iterator it = headers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String name = (String) pairs.getKey();
            String value = (String) pairs.getValue();
            prop.setProperty(name, value);
            it.remove(); 
        }
 
        File file = new File("src/test/resources" + HEADER_FILE);
        prop.store(new FileOutputStream(file), "Query headers");
    }
    
    private void compareHeaders(Map headers, InputStream inputStream) throws IOException {

        log.debug("Reading headers from file");
        Properties prop = new Properties();
        prop.load(inputStream);
        
        Iterator it = headers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String name = (String) pairs.getKey();
            String current_value = (String) pairs.getValue();
            String test_value = prop.getProperty(name);
            
            if (!Arrays.asList(EXCLUDE_HEADERS).contains(name)) {
                assertEquals(String.format("[%s] header value", name), test_value, current_value);
            }
            it.remove(); 
        }

    }
    
     private void dataToFile(String data, String dataType, Properties prop) throws IOException {

        log.debug("Writing data to file");
        prop.setProperty(dataType, data);

        File file = new File("src/test/resources" + DATA_FILE);
        prop.store(new FileOutputStream(file), "Query data");
    }
       
}
