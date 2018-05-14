package lt.emasina.resthub.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import junit.framework.TestCase;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.model.DataResponse;
import lt.emasina.resthub.model.QueryManager;
import static lt.emasina.resthub.server.ServerSetup.EXCLUDE_HEADERS;
import lt.emasina.resthub.support.TestRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
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
        
        Map headers = qm.options();
        compareHeaders(headers, HEADER_FILE);

        boolean loaded;
        Properties prop = new Properties();
        
        try (InputStream in = getResultStream(DATA_FILE)) {
            loaded = in != null;
            if (loaded) {
                prop.load(in);
            }
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
                
        // Checking options
        
        ClientResource client = query.options();

        Map<String,String> headers = getHeaders(client);
        compareHeaders(headers, query.getFilename("options"));
        
        if (skipData) return;
        
        Properties prop = new Properties();
        boolean loaded;
        String[] contentTypes = headers.get("X-Content-Types").split(",");
        ArrayList<String> mimeTypes = new ArrayList<>();   
        
        // Loading previous results
        
        String dataFile = query.getFilename("data");
        try (InputStream inputStream = getResultStream(dataFile)) {
            loaded = inputStream != null;
            if (loaded) {
                prop.load(inputStream);
            }
        }

        // Check query count
        client = query.count();
        if (client != null) {
            compareHeaders(getHeaders(client), query.getFilename("count_headers"));
            String count = client.getResponseEntity().getText();
            if (!loaded) {
                prop.setProperty("count", count);
            } else {
                assertEquals(prop.getProperty("count"), count);
            }
        }
                
        // Delete query cache
        
        query.deleteCache(); 
        
        // Check each contentType
        for (String contentType : contentTypes) {
            
            long t = System.currentTimeMillis();
            client = query.get(MediaType.valueOf(contentType));
            log.info(String.format("GET took: %d ms @%d", System.currentTimeMillis() - t, System.currentTimeMillis()));
            
            compareHeaders(getHeaders(client), query.getFilename("data_headers_" + contentType.replaceAll("/", "")));
            
            Representation get = client.getResponseEntity();
            String data = get.getText();
            get.exhaust();
            
            String qid = getHeaders(client).get("X-Query-Id");
            if (qid != null) {
                data = data.replaceAll(qid, "QUERYID");
            }

            if (!loaded) {
                prop.setProperty(contentType, data);
            } else {
                
                log.info(String.format("Comparing data: %s @ %s...", get.getMediaType().toString(), dataFile));
                compareData(get.getMediaType().toString(), prop.getProperty(contentType), data);
                
                // Get unique content types
                String type = getHeaders(client).get("Content-Type");
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
    
    private void headersToFile(Map<String,String> headers, String fileName) throws IOException {
        Properties prop = new Properties();
        for (String name: headers.keySet()) {
            prop.setProperty(name, headers.get(name));
        }
        prop.store(new FileOutputStream(getResultFile(fileName)), "Headers");
    }
    
    private void compareHeaders(Map<String,String> headers, String filename) throws IOException {
        try (InputStream in = getResultStream(filename)) {
            if (in == null) {
                headersToFile(headers, filename);
            } else {
                Properties prop = new Properties();
                prop.load(in);
                for (String name: headers.keySet()) {
                    String current_value = headers.get(name);
                    String test_value = prop.getProperty(name);
                    if (Arrays.asList(EXCLUDE_HEADERS).contains(name)) {
                        TestCase.assertTrue(String.format("[%s] header existence", name), test_value != null);
                    } else {
                        assertEquals(String.format("[%s] header value", name), test_value, current_value);
                    }
                }
            }
        }
    }
       
    private static Map<String,String> getHeaders(ClientResource client) {
        return ((Series) client.getResponse().getAttributes().get("org.restlet.http.headers")).getValuesMap();
    }
    
}
