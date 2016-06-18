package lt.emasina.resthub;

import lt.emasina.resthub.server.XmlServerTableFactoryTest;
import lt.emasina.resthub.server.ServerTest;
import lt.emasina.resthub.server.SqlServerTableFactoryTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
    
    CheckParserTest.class,
    ServerAppConfigTest.class,
    UpdateParserTest.class,
    
    XmlServerTableFactoryTest.class,
    SqlServerTableFactoryTest.class,
    
    ServerTest.class
        
})
public class TestSuite {

}
