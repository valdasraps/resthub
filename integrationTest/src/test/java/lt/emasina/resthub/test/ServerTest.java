package lt.emasina.resthub.test;

import lt.emasina.resthub.support.TestRequest;
import lt.emasina.resthub.support.TestQuery;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.client.RestHubServer;
import lt.emasina.resthub.model.QueryManager;
import lt.emasina.resthub.server.ServerChecks;
import lt.emasina.resthub.server.ServerSetup;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Text;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import org.json.JSONObject;
import org.restlet.data.MediaType;

@Log4j
@RunWith(JUnit4.class)
public class ServerTest extends ServerSetup {
    
    private final ServerChecks checks = new ServerChecks();
    
    @Test
    public void requestTest() throws IOException, URISyntaxException, org.json.JSONException {
        
        // Check Requests

        checks.check(new TestRequest.Builder("/info").build());
        checks.check(new TestRequest.Builder("/queries").build(), Boolean.TRUE);
        checks.check(new TestRequest.Builder("/tables").build());
        
        checks.check(new TestRequest.Builder("/table/store/customer").build());
        checks.check(new TestRequest.Builder("/table/store/sales").build());
        checks.check(new TestRequest.Builder("/table/store/products").build());
        checks.check(new TestRequest.Builder("/table/store/customers").build());
        checks.check(new TestRequest.Builder("/table/store/texts").build());
        
        checks.check(new TestRequest.Builder("/table/store/customer?_verbose").build());
        checks.check(new TestRequest.Builder("/table/store/sales?_verbose").build());
        checks.check(new TestRequest.Builder("/table/store/products?_verbose").build());
        checks.check(new TestRequest.Builder("/table/store/customers?_verbose").build());
        checks.check(new TestRequest.Builder("/table/store/texts?_verbose").build());

        checks.check(new TestRequest.Builder("/table/store/customer/data").build());
        checks.check(new TestRequest.Builder("/table/store/sales/data").build());
        checks.check(new TestRequest.Builder("/table/store/products/data").build());
        checks.check(new TestRequest.Builder("/table/store/customers/data").build());
        checks.check(new TestRequest.Builder("/table/store/texts/data").build());
        checks.check(new TestRequest.Builder("/table/store/texts/data?_inclob").build());
        
        // Check Queries
        checks.check(new TestQuery.Builder("SELECT * FROM (SELECT * FROM store.products c) a ORDER BY a.ID asc;")
                .build());
        checks.check(new TestQuery.Builder("SELECT * FROM (SELECT c.ID,c.BRAND FROM store.products c WHERE c.ID > 100 ORDER BY c.BRAND desc) a ORDER BY a.ID asc;")
                .params("?p1=1000")
                .build());
        checks.check(new TestQuery.Builder("SELECT * FROM (SELECT * FROM store.products c WHERE c.BRAND = :brand) a")
                .params("?brand=Bravo")
                .headers(new HashMap<String, String>() {{ put("Range", "rows=0-9"); }})
                .build());
        checks.check(new TestQuery.Builder("SELECT * FROM store.sales a")
                .build());
        checks.check(new TestQuery.Builder("SELECT * FROM store.products p ORDER BY p.id desc")
                .params("?_cols")
                .build());
        checks.check(new TestQuery.Builder("SELECT * FROM store.products c")
                .build());
        checks.check(new TestQuery.Builder("SELECT * FROM store.products c")
                .page(10, 1)
                .build());
        checks.check(new TestQuery.Builder("SELECT * FROM store.products c")
                .page(10, 5)
                .build());
        
        // Checking LOBs
        {
            String sql = "SELECT * FROM store.texts t WHERE t.id = 1";
            
            // Checking request
            checks.check(new TestQuery.Builder(sql).build());
            
            // Retrieving data URL, then data
            TestQuery q = new TestQuery.Builder(sql).build();
            JSONObject o = new JSONObject(q.get(new MediaType("application/json2")).getResponseEntity().getText());
            // {"data":[{"descr":"http://localhost:8112/query/o1636da089b3/0/1/lob","name":"simple","id":1}]}
            String lobUrl = o.getJSONArray("data").getJSONObject(0).getString("descr").replaceFirst("^.*:[0-9]+/", "/");
            TestRequest r = new TestRequest.Builder(lobUrl).build();
            checks.check(r, true);
            r = new TestRequest.Builder(lobUrl).build();
            String data = r.get().getResponseEntity().getText();
            
            // Retrieving data in a single call
            checks.check(new TestQuery.Builder(sql).params("?_inclob").build());
            
            q = new TestQuery.Builder(sql).params("?_inclob").build();
            o = new JSONObject(q.get(new MediaType("application/json2")).getResponseEntity().getText());
            // {"data":[{"descr":"This is some short description","name":"simple","id":1}]}
            
            assertEquals(data, o.getJSONArray("data").getJSONObject(0).getString("descr"));
            
        }
        
        // Check page size larger than rowsLimit
        try {
            
            new TestQuery.Builder("SELECT * FROM store.products c")
                    .page(1005, 1)
                    .build().get();
            
        } catch (ResourceException ex) {
            assertEquals("Bad Request (400) - The request could not be understood by the server due to malformed syntax", ex.getMessage());
        }
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
        } catch (ResourceException ex) {
            Assert.assertSame(ResourceException.class, ex.getClass());
            ResourceException rex = (ResourceException) ex;
            assertEquals(400, rex.getStatus().getCode());
        }
        
    }

}
