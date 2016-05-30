package lt.emasina.resthub;

import lt.emasina.resthub.server.ServerTableFactoryTest;
import lt.emasina.resthub.server.ServerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
    
    CheckParserTest.class,
    ServerAppConfigTest.class,
    UpdateParserTest.class,
    
    ServerTableFactoryTest.class,
    ServerTest.class
        
})
public class TestSuite {

}
