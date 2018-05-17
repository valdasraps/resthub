package lt.emasina.resthub.test;


import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.client.RestHubServer;
import lt.emasina.resthub.model.QueryManager;
import lt.emasina.resthub.server.ServerSetup;
import lt.emasina.resthub.server.TunnelSetup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.restlet.resource.ResourceException;
import static junit.framework.TestCase.assertEquals;

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
		assertEquals("1998-01-17 23:00:49",qm.getDataJSON().getJSONArray("data").getJSONArray(0).getString(0));

		try {

			WORKER.stopTunnel();
			// Checking output without tcp tunnel stopped
			qm = rh.newQueryManager("SELECT * FROM (SELECT c.ID,c.BRAND FROM store.products c WHERE c.ID > 1500 ORDER BY c.BRAND desc) a ORDER BY a.ID asc;");
			qm.addParameter("p1", 12);
			assertEquals(1501, qm.getDataJSON().getJSONArray("data").getJSONArray(0).getInt(0));

		} catch (ResourceException ex) {
			log.error("Error while stopping a tunnel", ex);
		}

		WORKER.startTunnel();

		// Checking output with tcp tunnel started again
		qm = rh.newQueryManager("SELECT * FROM (SELECT c.ID,c.BRAND FROM store.products c WHERE c.ID > 1500 ORDER BY c.BRAND desc) a ORDER BY a.ID asc;");
		qm.addParameter("p1", 12);
		assertEquals(1501, qm.getDataJSON().getJSONArray("data").getJSONArray(0).getInt(0));

	}

}
