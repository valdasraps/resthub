package lt.emasina.server.test;

import lt.emasina.server.test.support.TestRequest;
import lt.emasina.server.test.support.TestQuery;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

import org.restlet.data.MediaType;
import org.restlet.engine.header.Header;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;
import lombok.extern.log4j.Log4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.representation.Representation;

@Log4j
public class ServerRunnerWorker implements Runnable {

    private static final String[] EXCLUDE_HEADERS = { "Date", "Expires", "Accept-Ranges", "Allow" };

    @Override
    public void run() {
        try {
            setupQueries();
        } catch (IOException | URISyntaxException | JSONException ex) {
            Logger.getLogger(ServerRunnerWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setupQueries() throws IOException, URISyntaxException, org.json.JSONException {
        
        // Check Requests
       
        check(new TestRequest.Builder("r1","/queries").build());
        check(new TestRequest.Builder("r2","/tables").build());
        check(new TestRequest.Builder("r3","/table/store/customer").build());
        check(new TestRequest.Builder("r4","/table/store/sales").build());
        check(new TestRequest.Builder("r5","/table/store/products").build());
       
        // Check Queries
        check(new TestQuery.Builder("q1", "SELECT * FROM (SELECT * FROM store.products c) a ORDER BY a.ID asc;", null, null).build());
        check(new TestQuery.Builder("q2", "SELECT * FROM (SELECT c.ID,c.BRAND FROM store.products c WHERE c.ID > 100 ORDER BY c.BRAND desc) a ORDER BY a.ID asc;", "?p1=1000", null).build());
        check(new TestQuery.Builder("q3", "SELECT * FROM (SELECT * FROM store.products c WHERE c.BRAND = :brand) a", "?brand=Bravo", new HashMap<String, String>() {{ put("Range", "rows=0-9"); }} ).build());
    }

    private void check(TestRequest query) throws IOException, URISyntaxException, org.json.JSONException {
                
        log.debug("Cheking query headers");
        
        ClientResource client = query.options();

        String headerFile = query.getPrefix() + "_headers";
        Series headers = (Series) client.getResponse().getAttributes().get("org.restlet.http.headers");
        
        // Checking headers file
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("/lt/emasina/server/results/" + headerFile);
        
        if (inputStream == null) {
            headersToFile(headers, headerFile);
        } else {
            compareHeaders(headers, inputStream);
        }
             
        log.debug("Cheking query data");

        Properties prop = new Properties();
        String dataFile = query.getPrefix() + "_data";
        // Checking data file
        inputStream = getClass().getClassLoader().getResourceAsStream("/lt/emasina/server/results/" + dataFile);

        // Check query count 
        client = query.count();
        if (client != null) 
        {
            String count = client.getResponseEntity().getText();
            if (inputStream == null) {
                dataToFile(count, dataFile, "count", prop);
            } else {
                prop.load(inputStream);
                String exp_value = prop.getProperty("count");
                assertEquals(exp_value, count);
            }
        }
                
        // Delete query cache
        // client = query.deleteCache(); 
        
        String[] contentTypes = headers.getValues("Content-Type").split(",");
        ArrayList<String> mimeTypes = new ArrayList<>();   
        
        // Check each contentType
        
        for (String contentType : contentTypes) {        
            
            long t = System.currentTimeMillis();
            client = query.get(MediaType.valueOf(contentType));
            log.debug(String.format("GET took: %d ms @%d", System.currentTimeMillis() - t, System.currentTimeMillis()));
            
            Representation get = client.getResponseEntity();
            String data = get.getText();
            get.exhaust();

            if (inputStream == null) {
                dataToFile(data, dataFile, contentType, prop);
            } else {

                log.debug("Comparing " + contentType + " data");

                prop.load(inputStream);
                String expected_value = prop.getProperty(contentType);
                assertEquals(expected_value, data);
                
                // Get unique content types
                headers = (Series) client.getResponse().getAttributes().get("org.restlet.http.headers");
                String type = headers.getValues("Content-Type");

                if(!mimeTypes.contains(type))
                    mimeTypes.add(type);              
            }
        }
                       
        // Check cache size    
        client = null; //client = query.cache();
       
        if (client != null) {
            String data = client.getResponseEntity().getText();
            JSONObject jsonObj = new JSONObject(data);
            log.debug("Comparing cache:" + jsonObj);
            String test_value = jsonObj.getString("size");
            String expected_value = Integer.toString( mimeTypes.size() );
                    
            assertEquals(expected_value, test_value);
        }

        // Delete query
        // client = query.deleteQuery();

    }

    private void compareHeaders(Series headers, InputStream inputStream) throws IOException {

        log.debug("Reading headers from file");
        Properties prop = new Properties();
        prop.load(inputStream);

        for (Object header1 : headers) {
            Header header = (Header) header1;
            String name = header.getName();
            String current_value = header.getValue();
            String test_value = prop.getProperty(name);
            if (!Arrays.asList(EXCLUDE_HEADERS).contains(name)) {
                assertEquals(String.format("[%s] header value", name), test_value, current_value);
            }
        }

    }

    private void headersToFile(Series headers, String fileName) throws IOException {

        log.debug("Writing headers to file");
        Properties prop = new Properties();

        for (Object header1 : headers) {
            Header header = (Header) header1;
            String name = header.getName();
            String value = header.getValue();
            prop.setProperty(name, value);
        }
        
        File file = new File("src/test/resources/lt/emasina/server/results/" + fileName);
        prop.store(new FileOutputStream(file), "Query headers");
    }

    private void dataToFile(String data, String fileName, String dataType, Properties prop) throws IOException {

        log.debug("Writing data to file");

        prop.setProperty(dataType, data);

        File file = new File("src/test/resources/lt/emasina/server/results/" + fileName);
        prop.store(new FileOutputStream(file), "Query data");
    }

}
