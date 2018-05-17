package lt.emasina.resthub;

import lt.emasina.resthub.test.PythonClientTest;
import lt.emasina.resthub.test.XmlServerTableFactoryTest;
import lt.emasina.resthub.test.ServerTest;
import lt.emasina.resthub.test.SqlServerTableFactoryTest;
import lt.emasina.resthub.test.TunnelTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
    
    PythonClientTest.class,
    XmlServerTableFactoryTest.class,
    SqlServerTableFactoryTest.class,
    ServerTest.class,
    TunnelTest.class
        
})
public class TestSuite {

}
