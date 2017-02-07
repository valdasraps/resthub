package lt.emasina.resthub.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.model.DataResponse;
import lt.emasina.resthub.model.QueryManager;
import static lt.emasina.resthub.server.ServerSetup.EXCLUDE_HEADERS;
import lt.emasina.resthub.support.TestRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;
import org.skyscreamer.jsonassert.JSONAssert;
import static junit.framework.TestCase.assertEquals;

@Log4j
public class ServerChecks {

    private final static String HEADER_FILE = "client_header";
    private final static String DATA_FILE = "client_data";
    
    public void check(QueryManager qm) throws IOException, JSONException {
        
        log.debug("Cheking query headers");
        Map headers = qm.options();
        
        // Checking headers file
        InputStream inputStream = getResultStream(HEADER_FILE);
        
        if (inputStream == null) {
            headersToFile(headers, HEADER_FILE);
        } else {
            compareHeaders(headers, inputStream);
        }

        log.debug("Cheking query data");
        inputStream = getResultStream(DATA_FILE);
        boolean loaded = inputStream != null;
        Properties prop = new Properties();
        if (loaded) {
            prop.load(inputStream);
        }
        
        // Getting available content types 
        String ct = (String) headers.get("X-Content-Types");
        String[] contentTypes = ct.split(",");     
        
        // Check each contentType   
        for (String contentType : contentTypes) { 
            
            DataResponse dr = qm.getData(contentType);
            String data = dr.getString();
            
            if (!loaded) {
                prop.setProperty(contentType, data);
            } else {
                log.info(String.format("Comparing data: %s @ %s...", contentType, DATA_FILE));
                compareData(contentType, prop.getProperty(contentType), data);
            }
        }
       
        if (!loaded) {
            prop.store(new FileOutputStream(getResultFile(DATA_FILE)), "Query data");
        }

    }
    
    public void check(TestRequest query) throws IOException, URISyntaxException, org.json.JSONException {
        check(query, Boolean.FALSE);
    }
    
    public void check(TestRequest query, Boolean skipData) throws IOException, URISyntaxException, org.json.JSONException {
                
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
        
        if (skipData) return;
             
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
        
        String[] contentTypes = headers.getValues("X-Content-Types").split(",");
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
                
                log.info(String.format("Comparing data: %s @ %s...", get.getMediaType().toString(), dataFile));
                compareData(get.getMediaType().toString(), prop.getProperty(contentType), data);
                
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
    
    private InputStream getResultStream(String fileName) {
        return ServerSetup.class.getResourceAsStream("/lt/emasina/server/results/" + fileName);
    }
    
    private File getResultFile(String fileName) {
        return new File("src/test/resources/lt/emasina/server/results/" + fileName);
    }
    
    private void compareData(String contentType, String expected, String received) throws JSONException {
        if (contentType.equals("application/json")) {
            Object je = new JSONTokener(expected).nextValue();
            Object jr = new JSONTokener(received).nextValue();

            assertEquals(je.getClass(), jr.getClass());
            
            if (je instanceof JSONObject) {
                JSONAssert.assertEquals((JSONObject) je, (JSONObject) jr, false);
            } else if (je instanceof JSONArray) {
                JSONAssert.assertEquals((JSONArray) je, (JSONArray) jr, false);
            }
        } else {
            assertEquals(expected.length(), received.length());
        }
    }
    
    private void headersToFile(Series headers, String fileName) throws IOException {
        Map map = new HashMap();
        for (Object header1 : headers) {
            Header header = (Header) header1;
            map.put(header.getName(), header.getValue());
        }
        headersToFile(map, fileName);
    }
    
    private void headersToFile(Map headers, String fileName) throws IOException {
        Properties prop = new Properties();
        for (Object key: headers.keySet()) {
            String name = (String) key;
            String value = (String) headers.get(key);
            prop.setProperty(name, value);
        }
        prop.store(new FileOutputStream(getResultFile(fileName)), "Headers");
    }
    
    private void compareHeaders(Series headers, InputStream inputStream) throws IOException {
        Map map = new HashMap();
        for (Object header1 : headers) {
            Header header = (Header) header1;
            map.put(header.getName(), header.getValue());
        }
        compareHeaders(map, inputStream);
    }
    
    private void compareHeaders(Map headers, InputStream inputStream) throws IOException {
        Properties prop = new Properties();
        prop.load(inputStream);
        for (Object key: headers.keySet()) {
            String name = (String) key;
            String current_value = (String) headers.get(key);
            String test_value = prop.getProperty(name);
            if (!Arrays.asList(EXCLUDE_HEADERS).contains(name)) {
                assertEquals(String.format("[%s] header value", name), test_value, current_value);
            }
        }
    }
       
}
