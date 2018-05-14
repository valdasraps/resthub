package lt.emasina.resthub;

import lt.emasina.resthub.server.XmlServerTableFactoryTest;
import lt.emasina.resthub.server.ServerTest;
import lt.emasina.resthub.server.SqlServerCleanupTest;
import lt.emasina.resthub.server.SqlServerTableFactoryTest;
import lt.emasina.resthub.server.TunnelTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
    
    SqlServerCleanupTest.class,
    XmlServerTableFactoryTest.class,
    SqlServerTableFactoryTest.class,
    
    ServerTest.class,
	
    TunnelTest.class,
    PythonClientTest.class
        
})
public class TestSuite {

}
