package lt.emasina.resthub;

import lt.emasina.resthub.server.XmlServerTableFactoryTest;
import lt.emasina.resthub.server.ServerTest;
import lt.emasina.resthub.server.SqlServerTableFactoryTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
    
    XmlServerTableFactoryTest.class,
    SqlServerTableFactoryTest.class,
    
    ServerTest.class,
	
	PythonClientTest.class
        
})
public class TestSuite {

}
