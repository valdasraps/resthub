package lt.emasina.resthub;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
    
	CheckParserTest.class,
    ServerAppConfigTest.class,
    UpdateParserTest.class
        
})
public class TestSuite {

}
