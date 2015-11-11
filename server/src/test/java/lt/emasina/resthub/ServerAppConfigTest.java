package lt.emasina.resthub;

import java.net.MalformedURLException;
import java.net.URL;
import junit.framework.TestCase;
import lt.emasina.resthub.server.ServerAppConfig;
import lt.emasina.resthub.server.ServerAppConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.restlet.data.Reference;

/**
 * ServerAppConfigTest
 * @author valdo
 */
@RunWith(JUnit4.class)
public class ServerAppConfigTest extends TestCase {

    @Test
    public void referenceRewriteTest() throws MalformedURLException {
        ServerAppConfig cfg = new ServerAppConfig();
        cfg.addReferenceRewrite("^http:(.+)/(.+)$", "https:$1/prefix/$2");

        {
            URL url = new URL("http://some.web.server.domain/api");
            Reference baseRef = new Reference(url);
            URL r = cfg.getReference(baseRef, null);
            assertEquals("https://some.web.server.domain/prefix/api", r.toString());
        }
        {
            URL url = new URL("http://some.web.server.domain/api");
            Reference baseRef = new Reference(url);
            URL r = cfg.getReference(baseRef, null, "labas");
            assertEquals("https://some.web.server.domain/prefix/api/labas", r.toString());
        }
        
        cfg.setUpdateInterval(10);
        assertEquals(10, cfg.getUpdateInterval());
        
    }

}
