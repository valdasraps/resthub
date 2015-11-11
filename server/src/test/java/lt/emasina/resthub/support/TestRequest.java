package lt.emasina.resthub.support;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lt.emasina.resthub.server.ServerSetup;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.restlet.data.MediaType;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

@Getter
@Setter(AccessLevel.PROTECTED)
public class TestRequest {

    private String prefix;
    private String path;
    private String entity;
    private HashMap headers;
    
    public String getUrl() {
        return ServerSetup.HOST + path;
    }
    
    public ClientResource post() throws IOException {
        ClientResource client = new ClientResource(getUrl());
        client.post(this.entity);
        client.release();
        
        assertTrue(client.getStatus().isSuccess());
        assertEquals(200, client.getStatus().getCode());
        
        return client;
    }
    
    public ClientResource get() throws IOException {
        ClientResource client = new ClientResource(getUrl());
        if (headers != null) addHeaders(client);
        client.get();
        client.release();

        assertTrue(client.getStatus().isSuccess());
        assertEquals(200, client.getStatus().getCode());
        
        return client;
    }
    
    public ClientResource get(MediaType type) throws IOException {
        ClientResource client = new ClientResource(getUrl());
        if (headers != null) addHeaders(client);
        client.get(type);
        client.release();

        assertTrue(client.getStatus().isSuccess());
        assertEquals(200, client.getStatus().getCode());
        
        return client;
    }
    
    public ClientResource options() throws IOException {
        ClientResource client = new ClientResource(getUrl());
        client.options();
        client.release();

        assertTrue(client.getStatus().isSuccess());
        assertEquals(200, client.getStatus().getCode());
        
        return client;
    }
    
    public ClientResource deleteQuery() throws IOException { 
        return null;
    } 
   
    public ClientResource cache() throws IOException { 
        return null;
    }
    
    public ClientResource count() throws IOException { 
        return null;
    } 
    
    public ClientResource deleteCache() throws IOException { 
        return null;
    }
    
    public void addHeaders(ClientResource client) {
        Series<Header> reqHeaders = (Series<Header>)
        client.getRequestAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
        if (reqHeaders == null) {
            reqHeaders = new Series(Header.class);
            client.getRequestAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, reqHeaders);
        }
     
        Iterator it = headers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            reqHeaders.add(new Header(pairs.getKey().toString(), pairs.getValue().toString() ));
        }
        
    }
    
    public static class Builder {
        
        private final TestRequest req = new TestRequest();
        
        public Builder(String prefix, String path) {
            req.prefix = prefix;
            req.path = path;
        }
        
        public Builder entity(String entity) {
            req.entity = entity;
            return this;
        }
        
        public TestRequest build() {
            return req;
        }
        
    }

}