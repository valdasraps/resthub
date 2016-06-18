package lt.emasina.resthub.server;

import java.io.IOException;
import java.nio.file.Files;
import junit.framework.TestCase;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.support.TestRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.restlet.data.MediaType;

@Log4j
@RunWith(JUnit4.class)
public class XmlServerTableFactoryTest extends ServerSetup {

    @Test
    public void refreshTests() throws Exception {
        JSONObject o;
        
        // No tables from files (no directory)
        
        testEmpty();
        
        // No tables from files (empty directory)
        
        Files.createDirectories(FOLDER);       
        Thread.sleep(11000);
        
        testEmpty();

        // Single table from files (1st file copied)
        
        copyFile("tables_1.xml");
        Thread.sleep(11000);
        
        o = getJSON(new TestRequest.Builder("r1","/tables").build());
        TestCase.assertEquals(2, o.length());
        TestCase.assertEquals(5, o.getJSONObject("store").length());
        TestCase.assertEquals(1, o.getJSONObject("folder").length());
        TestCase.assertNotNull(o.getJSONObject("folder").getString("table1"));
        
        o = getJSON(new TestRequest.Builder("r1","/blacklist").build());
        TestCase.assertEquals(0, o.length());
        
        // Two tables from files (2nd file copied)
        
        copyFile("tables_2.xml");
        Thread.sleep(11000);
        
        o = getJSON(new TestRequest.Builder("r1","/tables").build());
        TestCase.assertEquals(2, o.length());
        TestCase.assertEquals(5, o.getJSONObject("store").length());
        TestCase.assertEquals(2, o.getJSONObject("folder").length());
        TestCase.assertNotNull(o.getJSONObject("folder").getString("table1"));
        TestCase.assertNotNull(o.getJSONObject("folder").getString("table2"));
        
        o = getJSON(new TestRequest.Builder("r1","/blacklist").build());
        TestCase.assertEquals(0, o.length());
        
        // One table from files (1st file removed)
        
        deleteFile("tables_1.xml");
        Thread.sleep(11000);
        
        o = getJSON(new TestRequest.Builder("r1","/tables").build());
        TestCase.assertEquals(2, o.length());
        TestCase.assertEquals(5, o.getJSONObject("store").length());
        TestCase.assertEquals(1, o.getJSONObject("folder").length());
        TestCase.assertNotNull(o.getJSONObject("folder").getString("table2"));
        
        o = getJSON(new TestRequest.Builder("r1","/blacklist").build());
        TestCase.assertEquals(0, o.length());
        
        // Two tables from files (3rd file copied)
        
        copyFile("tables_3.xml");
        Thread.sleep(11000);
        
        o = getJSON(new TestRequest.Builder("r1","/tables").build());
        TestCase.assertEquals(2, o.length());
        TestCase.assertEquals(5, o.getJSONObject("store").length());
        TestCase.assertEquals(1, o.getJSONObject("folder").length());
        TestCase.assertNotNull(o.getJSONObject("folder").getString("table2"));
        
        o = getJSON(new TestRequest.Builder("r1","/blacklist").build());
        TestCase.assertEquals(1, o.length());
        TestCase.assertEquals(1, o.getJSONObject("folder").length());
        TestCase.assertNotNull(o.getJSONObject("folder").getString("table3"));
        
        // One table from files (3rd file removed)
        
        deleteFile("tables_3.xml");
        Thread.sleep(11000);
        
        o = getJSON(new TestRequest.Builder("r1","/tables").build());
        TestCase.assertEquals(2, o.length());
        TestCase.assertEquals(5, o.getJSONObject("store").length());
        TestCase.assertEquals(1, o.getJSONObject("folder").length());
        TestCase.assertNotNull(o.getJSONObject("folder").getString("table2"));
        
        o = getJSON(new TestRequest.Builder("r1","/blacklist").build());
        TestCase.assertEquals(0, o.length());
        
    }
    
    private void testEmpty() throws IOException, JSONException {
        
        JSONObject o = getJSON(new TestRequest.Builder("r1","/tables").build());
        
        TestCase.assertEquals(1, o.length());
        TestCase.assertEquals(5, o.getJSONObject("store").length());
        
        o = getJSON(new TestRequest.Builder("r1","/blacklist").build());
        
        TestCase.assertEquals(0, o.length());
    }
    
    private JSONObject getJSON(TestRequest tr) throws IOException, JSONException {
        return new JSONObject(tr.get(MediaType.APPLICATION_JSON).getResponseEntity().getText());
    }

    
}
