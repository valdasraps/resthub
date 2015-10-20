package lt.emasina.server.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lt.emasina.server.test.support.TestConnectionFactory;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.factory.XmlResourceTableFactory;
import lt.emasina.resthub.server.ServerApp;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.engine.header.Header;
import org.restlet.util.Series;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 *
 * @author valdo
 */
@Log4j
@Ignore
public class ServerSetup {

    public static final String HOST = "http://localhost:8112";
    protected static final String[] EXCLUDE_HEADERS = {"Date", "Expires", "Accept-Ranges", "Allow"};
    
    private static final String XML_RESOURCE = "/lt/emasina/server/xml/tables.xml";
    private static Component comp;
    
    @BeforeClass
    public static void startServer() throws Exception {
        String url = System.getenv("TEST_DATABASE_URL");
        if (url == null) url = "192.168.54.1:1521/cerndev";
        
        ServerApp app = new ServerApp(new TestConnectionFactory(url), new XmlResourceTableFactory(XML_RESOURCE));
        comp = new Component();
        comp.getServers().add(Protocol.HTTP, 8112);
        comp.getDefaultHost().attach(app);
        comp.start();
    }
    
    @AfterClass
    public static void stopServer() throws Exception {
        comp.stop();
    }

    protected InputStream getResultStream(String fileName) {
        return ServerSetup.class.getResourceAsStream("/lt/emasina/server/results/" + fileName);
    }
    
    protected File getResultFile(String fileName) {
        return new File("src/test/resources/lt/emasina/server/results/" + fileName);
    }
    
    protected void compareData(String contentType, String expected, String received) throws JSONException {
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
    
    protected void headersToFile(Series headers, String fileName) throws IOException {
        Map map = new HashMap();
        for (Object header1 : headers) {
            Header header = (Header) header1;
            map.put(header.getName(), header.getValue());
        }
        headersToFile(map, fileName);
    }
    
    protected void headersToFile(Map headers, String fileName) throws IOException {
        Properties prop = new Properties();
        for (Object key: headers.keySet()) {
            String name = (String) key;
            String value = (String) headers.get(key);
            prop.setProperty(name, value);
        }
        prop.store(new FileOutputStream(getResultFile(fileName)), "Headers");
    }
    
    protected void compareHeaders(Series headers, InputStream inputStream) throws IOException {
        Map map = new HashMap();
        for (Object header1 : headers) {
            Header header = (Header) header1;
            map.put(header.getName(), header.getValue());
        }
        compareHeaders(map, inputStream);
    }
    
    protected void compareHeaders(Map headers, InputStream inputStream) throws IOException {
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
