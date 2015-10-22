package lt.emasina.server.test;

import lt.emasina.server.test.support.TestConnectionFactory;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.factory.XmlResourceTableFactory;
import lt.emasina.resthub.server.ServerApp;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.restlet.Component;
import org.restlet.data.Protocol;

/**
 *
 * @author valdo
 */
@Log4j
@Ignore
public class ServerSetup {

    public static final String HOST = "http://localhost:8112";
    protected static final String[] EXCLUDE_HEADERS = {"Date", "Expires", "Accept-Ranges", "Allow"};
    
    private static final String XML_RESOURCE = "/lt/emasina/server/xml/tables.xml";
    private static Component comp;
    
    @BeforeClass
    public static void startServer() throws Exception {       
        ServerApp app = new ServerApp(new TestConnectionFactory(), new XmlResourceTableFactory(XML_RESOURCE));
        comp = new Component();
        comp.getServers().add(Protocol.HTTP, 8112);
        comp.getDefaultHost().attach(app);
        comp.start();
    }
    
    @AfterClass
    public static void stopServer() throws Exception {
        comp.stop();
    }

}
