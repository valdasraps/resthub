package lt.emasina.server.test;

import lt.emasina.server.test.support.TestRequest;
import lt.emasina.server.test.support.TestQuery;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

import org.restlet.data.MediaType;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;
import lombok.extern.log4j.Log4j;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.restlet.representation.Representation;

@Log4j
@RunWith(JUnit4.class)
public class ServerRunnerWorker extends ServerSetup {

    @Test
    public void doTest() throws IOException, URISyntaxException, org.json.JSONException {
        
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
        check(new TestQuery.Builder("q4", "SELECT * FROM store.sales a", null, null).build());
    }

    private void check(TestRequest query) throws IOException, URISyntaxException, org.json.JSONException {
                
        ClientResource client = query.options();

        String headerFile = query.getPrefix() + "_headers";
        Series headers = (Series) client.getResponse().getAttributes().get("org.restlet.http.headers");
        
        // Checking headers
        
        InputStream inputStream = getResultStream(headerFile);
        if (inputStream == null) {
            headersToFile(headers, headerFile);
        } else {
            compareHeaders(headers, inputStream);
        }
             
        // Checking data
        
        Properties prop = new Properties();
        String dataFile = query.getPrefix() + "_data";
        inputStream = getResultStream(dataFile);
        boolean loaded = inputStream != null;
        if (loaded) {
            prop.load(inputStream);
        }

        // Check query count 
        client = query.count();
        if (client != null) {
            String count = client.getResponseEntity().getText();
            if (!loaded) {
                prop.setProperty("count", count);
            } else {
                assertEquals(prop.getProperty("count"), count);
            }
        }
                
        // Delete query cache
        
        query.deleteCache(); 
        
        String[] contentTypes = headers.getValues("Content-Type").split(",");
        ArrayList<String> mimeTypes = new ArrayList<>();   
        
        // Check each contentType
        for (String contentType : contentTypes) {
            
            long t = System.currentTimeMillis();
            client = query.get(MediaType.valueOf(contentType));
            log.info(String.format("GET took: %d ms @%d", System.currentTimeMillis() - t, System.currentTimeMillis()));
            
            Representation get = client.getResponseEntity();
            String data = get.getText();
            get.exhaust();

            if (!loaded) {
                prop.setProperty(contentType, data);
            } else {
                
                log.info(String.format("Comparing data: %s @ %s...", contentType, dataFile));
                compareData(contentType, prop.getProperty(contentType), data);
                
                // Get unique content types
                headers = (Series) client.getResponse().getAttributes().get("org.restlet.http.headers");
                String type = headers.getValues("Content-Type");

                if(!mimeTypes.contains(type)) {
                    mimeTypes.add(type);
                }              
            }
        }
                       
        // Check cache size    
        client = query.cache();
       
        if (client != null) {
            String data = client.getResponseEntity().getText();
            JSONObject jsonObj = new JSONObject(data);
            log.debug("Comparing cache:" + jsonObj);
            int test_value = Integer.parseInt(jsonObj.getString("size"));
                    
            assertEquals((int) 1, test_value);
        }

        // Delete query
        query.deleteQuery();
        
        if (!loaded) {
            prop.store(new FileOutputStream(getResultFile(dataFile)), "Query data");
        }

    }

}
