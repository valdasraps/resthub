package lt.emasina.resthub.support;

import java.io.IOException;
import java.util.HashMap;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.server.ServerSetup;
import org.restlet.data.MediaType;
import org.restlet.resource.ClientResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Log4j
@Getter
public class TestQuery extends TestRequest {

    private String id;
    private String params;
    private Integer perPage;
    private Integer page;

    public void setParams(String params) {
        this.params = params;
    }
    
    protected String getPathData() {
        StringBuilder sb = new StringBuilder();
        sb.append("/query/").append(id);
        if (perPage != null && page != null) {
            sb.append("/page/").append(perPage).append("/").append(page);
        }
        sb.append("/data");
        return sb.toString();
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
        assertEquals(204, client.getStatus().getCode());
        
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
        String url = ServerSetup.HOST + getPathData();       
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
        
        public Builder(String sql) {
            q.setPath("/query");
            q.setEntity(sql);
        }
        
        public Builder params(String params) {
            q.setParams(params);
            return this;
        }
        
        public Builder headers(HashMap headers) {
            q.setHeaders(headers);
            return this;
        }
        
        public Builder page(int perPage, int page) {
            q.perPage = perPage;
            q.page = page;
            return this;
        }
        
        public TestQuery build() {
            try {
                
                ClientResource client = q.post();
                q.id = client.getResponseEntity().getText();
                q.setPath(q.getPathData());
                
            } catch (IOException ex) {
                fail(ex.getMessage());
            }
            return q;
        }
        
    }
    
}
