package lt.emasina.resthub.support;

import lt.emasina.resthub.server.*;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import junit.framework.TestCase;
import lombok.extern.log4j.Log4j;
import oracle.jdbc.OracleConnection;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;

@Log4j
public abstract class SqlServerTableFactoryBase extends ServerSetup {

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
