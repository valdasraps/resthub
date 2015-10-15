package lt.emasina.resthub.server.exporter;

import lt.emasina.resthub.server.cache.CcLob;
import lt.emasina.resthub.server.handler.LobHandler;

import org.hibernate.Session;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class LobExporter extends Exporter<CcLob> {

	@Inject
	public LobExporter(@Assisted LobHandler handler) {
		super(handler);
	}

	@Override
	protected CcLob retrieveData(Session session) throws Exception {
		return getDf().getLob(session, (LobHandler) getHandler());
	}

}
