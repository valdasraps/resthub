package lt.emasina.resthub.server.exporter;

import lt.emasina.resthub.server.cache.CcData;
import lt.emasina.resthub.server.handler.DataHandler;

import org.hibernate.Session;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class DataExporter extends Exporter<CcData> {

	@Inject
	public DataExporter(@Assisted DataHandler handler) {
		super(handler);
	}

	@Override
	protected CcData retrieveData(Session session) throws Exception {
		return getDf().getData(session, (DataHandler) getHandler());
	}

}
