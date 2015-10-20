package lt.emasina.server.test.support;

import java.io.IOException;
import java.util.HashMap;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import lt.emasina.server.test.ServerSetup;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.restlet.data.MediaType;
import org.restlet.resource.ClientResource;

@Log4j
@Getter
public class TestQuery extends TestRequest {

    private String id;
    private String params;  

    public void setParams(String params) {
        this.params = params;
    }
   
    @Override
    public ClientResource deleteQuery() throws IOException {
        String url = ServerSetup.HOST + "/query/" + this.id;
        
        ClientResource client = new ClientResource(url);
        client.delete();
        
        log.debug("Deleting query: " + url);
 
        assertTrue(client.getStatus().isSuccess());
        assertEquals(200, client.getStatus().getCode());
        
        return client;
    } 
    
    @Override
    public ClientResource deleteCache() throws IOException {
        String url = ServerSetup.HOST + "/query/" + this.id + "/cache";
        
        ClientResource client = new ClientResource(url);
        client.delete();
        
        log.debug("Deleting cache: " + url);
 
        assertTrue(client.getStatus().isSuccess());
        assertEquals(200, client.getStatus().getCode());
        
        return client;
    } 
    
    @Override
    public ClientResource count() throws IOException { 
        String url = ServerSetup.HOST + "/query/" + this.id + "/count";
        if (this.params != null) url += this.params;
   
        ClientResource client = new ClientResource(url);
        client.get();
        
        log.debug("Checking query count: " + url);
        
        assertTrue(client.getStatus().isSuccess());
        assertEquals(200, client.getStatus().getCode());
        
        return client;
    }
    
    @Override
    public ClientResource cache() throws IOException {         
        String url = ServerSetup.HOST + "/query/" + this.id + "/cache";
        
        log.debug("Checking query cache: "+getUrl());

        ClientResource client = new ClientResource(url);
        client.get();
        
        log.debug("Checking query count: " + url);
        
        assertTrue(client.getStatus().isSuccess());
        assertEquals(200, client.getStatus().getCode());
        
        return client;
    }
        
    @Override
    public ClientResource get(MediaType type) throws IOException {    
        String url = ServerSetup.HOST + "/query/" + this.id + "/data";       
        if (this.params != null) url += this.params;
   
        ClientResource client = new ClientResource(url);
        client.get(type);
        
        assertTrue(client.getStatus().isSuccess());
        assertEquals(200, client.getStatus().getCode());
        
        return client;
    } 
    
    @Override
    public ClientResource options() throws IOException {
        setPath("/query/" + this.id + "/data");
        return super.options();
    } 
    
    public static class Builder {
        
        private final TestQuery q = new TestQuery();
        
        public Builder(String prefix, String sql, String params, HashMap headers) {
            q.setPrefix(prefix);
            q.setPath("/query");
            q.setEntity(sql);
            q.setParams(params);
            q.setHeaders(headers);
        }
        
        public TestQuery build() {
            try {
                ClientResource client = q.post();
                q.id = client.getResponseEntity().getText();              
                q.setPath("/query/" + q.id + "/data");
            } catch (IOException ex) {
                fail(ex.getMessage());
            }
            return q;
        }
        
    }
    
}
