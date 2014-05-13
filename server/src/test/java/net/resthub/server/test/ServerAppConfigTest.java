package net.resthub.server.test;

import java.net.MalformedURLException;
import java.net.URL;
import junit.framework.TestCase;
import net.resthub.server.ServerAppConfig;
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
            URL url = new URL("http://cmswbmoffdev.web.cern.ch/api");
            Reference baseRef = new Reference(url);
            URL r = cfg.getReference(baseRef, null);
            assertEquals("https://cmswbmoffdev.web.cern.ch/prefix/api", r.toString());
        }
        {
            URL url = new URL("http://cmswbmoffdev.web.cern.ch/api");
            Reference baseRef = new Reference(url);
            URL r = cfg.getReference(baseRef, null, "labas");
            assertEquals("https://cmswbmoffdev.web.cern.ch/prefix/api/labas", r.toString());
        }
    }

}
