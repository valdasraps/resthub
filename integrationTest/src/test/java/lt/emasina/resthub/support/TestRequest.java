package lt.emasina.resthub.support;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lt.emasina.resthub.server.ServerSetup;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Getter
@Setter(AccessLevel.PROTECTED)
public class TestRequest {

    private static final Map<String,Integer> COUNTERS = new HashMap<>();

    private String className;
    private Integer prefix;
    private String path;
    private String entity;
    private HashMap headers;

    public TestRequest() {
        
        StackTraceElement[] stack = new Exception().getStackTrace();
        StackTraceElement el = stack[1];
        for (int i = 2; i < stack.length; i++) {
            el = stack[i];
            if (!el.getClassName().contains(".support.")) break;
        }
        
        System.out.println("Request for " + el);
        
        this.className = el.getClassName().replaceFirst("^.*\\.", "");
        
        synchronized (COUNTERS) {
            if (!COUNTERS.containsKey(this.className)) {
                COUNTERS.put(this.className, 1);
            }
            this.prefix = COUNTERS.get(className);
            COUNTERS.put(this.className, this.prefix + 1);
        }
    }
    
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
        assertEquals(204, client.getStatus().getCode());
        
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
    
    public String getFilename(String suffix) {
        return String.format("%s_%03d_%s", className, prefix, suffix);
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
        
        public Builder(String path) {
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