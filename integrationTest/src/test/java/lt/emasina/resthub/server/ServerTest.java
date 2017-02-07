package lt.emasina.resthub.server;

import lt.emasina.resthub.support.TestRequest;
import lt.emasina.resthub.support.TestQuery;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.client.RestHubServer;
import lt.emasina.resthub.model.QueryManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Text;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

@Log4j
@RunWith(JUnit4.class)
public class ServerTest extends ServerSetup {
    
    private final ServerChecks checks = new ServerChecks();
    
    @Test
    public void requestTest() throws IOException, URISyntaxException, org.json.JSONException {
        
        // Check Requests

        checks.check(new TestRequest.Builder("i00","/info").build());
        checks.check(new TestRequest.Builder("r01","/queries").build(), Boolean.TRUE);
        checks.check(new TestRequest.Builder("r02","/tables").build());
        
        checks.check(new TestRequest.Builder("r03","/table/store/customer").build());
        checks.check(new TestRequest.Builder("r04","/table/store/sales").build());
        checks.check(new TestRequest.Builder("r05","/table/store/products").build());
        checks.check(new TestRequest.Builder("r09","/table/store/customers").build());
        
        checks.check(new TestRequest.Builder("r06","/table/store/customer?_verbose").build());
        checks.check(new TestRequest.Builder("r07","/table/store/sales?_verbose").build());
        checks.check(new TestRequest.Builder("r08","/table/store/products?_verbose").build());
        checks.check(new TestRequest.Builder("r10","/table/store/customers?_verbose").build());

        checks.check(new TestRequest.Builder("r11","/table/store/customer/data").build());
        checks.check(new TestRequest.Builder("r12","/table/store/sales/data").build());
        checks.check(new TestRequest.Builder("r13","/table/store/products/data").build());
        checks.check(new TestRequest.Builder("r14","/table/store/customers/data").build());
        
        // Check Queries
        checks.check(new TestQuery.Builder("q01", "SELECT * FROM (SELECT * FROM store.products c) a ORDER BY a.ID asc;")
                .build());
        checks.check(new TestQuery.Builder("q02", "SELECT * FROM (SELECT c.ID,c.BRAND FROM store.products c WHERE c.ID > 100 ORDER BY c.BRAND desc) a ORDER BY a.ID asc;")
                .params("?p1=1000")
                .build());
        checks.check(new TestQuery.Builder("q03", "SELECT * FROM (SELECT * FROM store.products c WHERE c.BRAND = :brand) a")
                .params("?brand=Bravo")
                .headers(new HashMap<String, String>() {{ put("Range", "rows=0-9"); }})
                .build());
        checks.check(new TestQuery.Builder("q04", "SELECT * FROM store.sales a")
                .build());
        checks.check(new TestQuery.Builder("q05", "SELECT * FROM store.products p ORDER BY p.id desc")
                .params("?_cols")
                .build());
        checks.check(new TestQuery.Builder("q06", "SELECT * FROM store.products c")
                .build());
        checks.check(new TestQuery.Builder("q06_10_1", "SELECT * FROM store.products c")
                .page(10, 1)
                .build());
        checks.check(new TestQuery.Builder("q06_10_5", "SELECT * FROM store.products c")
                .page(10, 5)
                .build());
        
        // Check page size larger than rowsLimit
        try {
            
            new TestQuery.Builder("q06_1005_1", "SELECT * FROM store.products c")
                    .page(1005, 1)
                    .build().get();
            
        } catch (Exception ex) {
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
        } catch (Exception ex) {
            Assert.assertSame(ResourceException.class, ex.getClass());
            ResourceException rex = (ResourceException) ex;
            assertEquals(400, rex.getStatus().getCode());
        }
        
    }

}
