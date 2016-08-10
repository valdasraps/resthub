package lt.emasina.resthub.debug;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import lt.emasina.resthub.debug.DebugServer;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class StartDebugServer {

	@Test
	public void runServer() throws Exception{
		DebugServer startServer = new DebugServer();
		
	}
	
	@Test
	public void dummyTest() {
		assertTrue(true);
	}


}
