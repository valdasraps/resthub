package lt.emasina.resthub.server;

import lt.emasina.resthub.support.TestRequest;
import lt.emasina.resthub.support.TestQuery;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.logging.Level;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.client.RestHubServer;
import lt.emasina.resthub.model.QueryManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Text;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

@Log
@Log4j
@RunWith(JUnit4.class)
public class TunnelTest extends TunnelSetup {

	@Test
	public void clientTest() throws Exception {

		RestHubServer rh = new RestHubServer(ServerSetup.HOST);
		QueryManager qm;

		// Checking output with tcp tunnel started
		qm = rh.newQueryManager("SELECT s.SAL_TIME FROM store.sales s WHERE s.SAL_ID = :n__id");
		qm.addParameter("id", 173);
		assertEquals("1998-01-17 23:00:49",
				qm.getDataJSON().getJSONArray("data").getJSONArray(0)
						.getString(0));

		try {

			WORKER.stopTunnel();
			// Checking output without tcp tunnel stopped
			qm = rh.newQueryManager("SELECT * FROM (SELECT c.ID,c.BRAND FROM store.products c WHERE c.ID > 1500 ORDER BY c.BRAND desc) a ORDER BY a.ID asc;");
			qm.addParameter("p1", 12);
			assertEquals("1501", qm.getDataJSON().getJSONArray("data")
					.getJSONArray(0).getString(0));

		} catch (ResourceException ex) {
			log.log(Level.ALL, "Error while stopping a tunnel", ex);
		}

		WORKER.startTunnel();

		// Checking output with tcp tunnel started again
		qm = rh.newQueryManager("SELECT * FROM (SELECT c.ID,c.BRAND FROM store.products c WHERE c.ID > 1500 ORDER BY c.BRAND desc) a ORDER BY a.ID asc;");
		qm.addParameter("p1", 12);
		assertEquals("1501", qm.getDataJSON().getJSONArray("data")
				.getJSONArray(0).getString(0));

	}

}
