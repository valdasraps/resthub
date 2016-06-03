package lt.emasina.resthub.server;

import lt.emasina.resthub.support.TestRequest;
import lt.emasina.resthub.support.TestQuery;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.client.RestHubServer;
import lt.emasina.resthub.model.QueryManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Text;

@Log4j
@RunWith(JUnit4.class)
public class ServerTest extends ServerSetup {
    
    private final ServerChecks checks = new ServerChecks();
    
    @Test
    public void requestTest() throws IOException, URISyntaxException, org.json.JSONException {
        
        // Check Requests
       
        checks.check(new TestRequest.Builder("i0","/info").build());
        checks.check(new TestRequest.Builder("r1","/queries").build());
        checks.check(new TestRequest.Builder("r2","/tables").build());
        checks.check(new TestRequest.Builder("r3","/table/store/customer").build());
        checks.check(new TestRequest.Builder("r4","/table/store/sales").build());
        checks.check(new TestRequest.Builder("r5","/table/store/products").build());
        checks.check(new TestRequest.Builder("r6","/table/store/customer?_verbose").build());
        checks.check(new TestRequest.Builder("r7","/table/store/sales?_verbose").build());
        checks.check(new TestRequest.Builder("r8","/table/store/products?_verbose").build());
       
        // Check Queries
        checks.check(new TestQuery.Builder("q1", "SELECT * FROM (SELECT * FROM store.products c) a ORDER BY a.ID asc;", null, null).build());
        checks.check(new TestQuery.Builder("q2", "SELECT * FROM (SELECT c.ID,c.BRAND FROM store.products c WHERE c.ID > 100 ORDER BY c.BRAND desc) a ORDER BY a.ID asc;", "?p1=1000", null).build());
        checks.check(new TestQuery.Builder("q3", "SELECT * FROM (SELECT * FROM store.products c WHERE c.BRAND = :brand) a", "?brand=Bravo", new HashMap<String, String>() {{ put("Range", "rows=0-9"); }} ).build());
        checks.check(new TestQuery.Builder("q4", "SELECT * FROM store.sales a", null, null).build());
        checks.check(new TestQuery.Builder("q5", "SELECT * FROM store.products p", "?_cols", null).build());
    }
    
    @Test
    public void clientTest() throws Exception {
        
        RestHubServer rh = new RestHubServer(ServerSetup.HOST);
        QueryManager qm;
        
        // Simple query test
        
        qm = rh.newQueryManager("SELECT * FROM (SELECT c.ID,c.BRAND FROM store.products c WHERE c.ID > 1500 ORDER BY c.BRAND desc) a ORDER BY a.ID asc;");
        qm.addParameter("p1", 12);

        checks.check(qm);

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

}
