package lt.emasina.resthub.test;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import junit.framework.TestCase;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.server.ServerChecks;
import lt.emasina.resthub.server.ServerSetup;
import lt.emasina.resthub.support.TestRequest;
import oracle.jdbc.OracleConnection;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.restlet.data.MediaType;

@Log4j
@RunWith(JUnit4.class)
public class SqlServerTableFactoryTest extends ServerSetup {

    private final ServerChecks checks = new ServerChecks();

    @Test
    public void testRemoveQueries() throws Exception {
        //check or json object is empty
        JSONObject o;
        testEmpty();

        //Build tables and test data
        o = getJSON(new TestRequest.Builder("/tables").build());
        TestCase.assertEquals(1, o.length());
        TestCase.assertEquals(6, o.getJSONObject("store").length());

        //Create customers view
        execDdl("create view cussimple_sqltest as select cus_id, cus_lname from customer");
        Thread.sleep(11000);

        //check or queries is empty before fill it
        o = getJSON(new TestRequest.Builder("/queries").build());
        TestCase.assertEquals(0, o.length());
        Thread.sleep(1000);

        //should find one query after this
        o = getJSON(new TestRequest.Builder("/table/folder/cussimple/data").build());
        o = getJSON(new TestRequest.Builder("/queries").build());
        TestCase.assertEquals(1, o.length());

        //We edit existing view, so it should force to delete existing query.
        execDdl("create or replace view cussimple_sqltest as select cus_id, cus_lname, cus_country from customer");
        Thread.sleep(12000);
        o = getJSON(new TestRequest.Builder("/queries").build());
        TestCase.assertEquals(0, o.length());

        //Create one querie and after that create another view to check or it affect existing query. IT SHOULDN'T
        o = getJSON(new TestRequest.Builder("/table/folder/cussimple/data").build());
        o = getJSON(new TestRequest.Builder("/queries").build());
        TestCase.assertEquals(1, o.length());
        execDdl("create or replace view cussimple1_sqltest as select cus_id, cus_lname from customer");
        Thread.sleep(12000);
        o = getJSON(new TestRequest.Builder("/queries").build());
        TestCase.assertEquals(1, o.length());

        //Delete created views.
        execDdl("drop view cussimple_sqltest");
        execDdl("drop view cussimple1_sqltest");
        Thread.sleep(11000);
    }

    @Test
    public void refreshTests() throws Exception {
        JSONObject o;

        testEmpty();
        
        execDdl("create view allobjs_sqltest as select owner, object_name from all_objects");
        Thread.sleep(11000);
        
        o = getJSON(new TestRequest.Builder("/tables").build());
        
        TestCase.assertEquals(2, o.length());
        TestCase.assertEquals(6, o.getJSONObject("store").length());
        TestCase.assertEquals(1, o.getJSONObject("folder").length());
        TestCase.assertNotNull(o.getJSONObject("folder").getString("allobjs"));

        checks.check(new TestRequest.Builder("/table/folder/allobjs").build());

        execDdl("CREATE OR REPLACE VIEW ALLOBJS_SQLTEST AS select owner, object_name, 'dummy' as dummy from all_objects");
        Thread.sleep(11000);
        
        checks.check(new TestRequest.Builder("/table/folder/allobjs").build());
        
        execDdl("drop view allobjs_sqltest");
        Thread.sleep(11000);

        testEmpty();
        
    }
    
    protected void execDdl(String ddl) throws SQLException {
        try (OracleConnection con = WORKER.getCf().getConnection("default")) {
            try (Statement st = con.createStatement()) {
                st.execute(ddl);
            }
        }
    }
    
    protected void testEmpty() throws IOException, JSONException {
        
        JSONObject o = getJSON(new TestRequest.Builder("/tables").build());

        TestCase.assertEquals(1, o.length());
        TestCase.assertEquals(6, o.getJSONObject("store").length());
        
        o = getJSON(new TestRequest.Builder("/blacklist").build());
        
        TestCase.assertEquals(0, o.length());
    }
    
    protected JSONObject getJSON(TestRequest tr) throws IOException, JSONException {
        return new JSONObject(tr.get(MediaType.APPLICATION_JSON).getResponseEntity().getText());
    }

}
