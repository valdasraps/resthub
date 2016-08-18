package lt.emasina.resthub.server;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import junit.framework.TestCase;
import lombok.extern.log4j.Log4j;
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
    public void refreshTests() throws Exception {
        JSONObject o;
        
        testEmpty();
        
        execDdl("create view allobjs_sqltest as select owner, object_name from all_objects");
        Thread.sleep(11000);
        
        o = getJSON(new TestRequest.Builder("/tables").build());
        
        TestCase.assertEquals(2, o.length());
        TestCase.assertEquals(5, o.getJSONObject("store").length());
        TestCase.assertEquals(1, o.getJSONObject("folder").length());
        TestCase.assertNotNull(o.getJSONObject("folder").getString("allobjs"));
        
        checks.check(new TestRequest.Builder("sqltf1","/table/folder/allobjs").build());
        
        execDdl("CREATE OR REPLACE VIEW ALLOBJS_SQLTEST AS select owner, object_name, 'dummy' as dummy from all_objects");
        Thread.sleep(11000);
        
        checks.check(new TestRequest.Builder("sqltf2","/table/folder/allobjs").build());
        
        execDdl("drop view allobjs_sqltest");
        Thread.sleep(11000);

        testEmpty();
        
    }
    
    private void execDdl(String ddl) throws SQLException {
        try (OracleConnection con = WORKER.getCf().getConnection("default")) {
            try (Statement st = con.createStatement()) {
                st.execute(ddl);
            }
        }
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
