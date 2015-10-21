package lt.emasina.server.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import junit.framework.TestCase;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.client.RestHubServer;
import lt.emasina.resthub.model.DataResponse;
import lt.emasina.resthub.model.QueryManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Text;

@Log4j
@RunWith(JUnit4.class)
public class JavaClientTest extends ServerSetup {

    private final static String HEADER_FILE = "client_header";
    private final static String DATA_FILE = "client_data";
    private final Random random = new Random();
    
    @Test
    public void checkQueries() throws Exception {
        
        RestHubServer rh = new RestHubServer(ServerSetup.HOST);
        QueryManager qm;
        
        // Simple query test
        
        qm = rh.newQueryManager("SELECT * FROM (SELECT c.ID,c.BRAND FROM store.products c WHERE c.ID > 1500 ORDER BY c.BRAND desc) a ORDER BY a.ID asc;");
        qm.addParameter("p1", random.nextInt());

        check(qm);

        // Checking timestamp output
        
        qm = rh.newQueryManager("SELECT s.SAL_TIME FROM store.sales s WHERE s.SAL_ID = :n__id");
        qm.addParameter("id", 173);
        
        assertEquals("1998-01-17 23:00:49", qm.getDataJSON().getJSONArray("data").getJSONArray(0).getString(0));
        
        Text timeText = (Text) qm.getDataXML().getDocumentElement().getFirstChild().getFirstChild().getFirstChild();
        assertEquals("1998-01-17T23:00:49", timeText.getData());
        
        // Checking same column names to fail
        
        qm = rh.newQueryManager("SELECT * FROM store.sales s1, store.sales s2 WHERE s1.SAL_ID = s2.SAL_ID and s1.SAL_ID = :n__id");
        qm.addParameter("id", 173);
        
        try {
            qm.refresh();
            fail("Should have failed due to same columns...");
        } catch (Exception ex) {
            Assert.assertSame(ResourceException.class, ex.getClass());
            ResourceException rex = (ResourceException) ex;
            assertEquals(400, rex.getStatus().getCode());
        }
        
    }
    
    private void check(QueryManager qm) throws IOException, JSONException {
        
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
        String ct = (String) headers.get("Content-Type");
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
       
}
