package lt.emasina.resthub.server.handler;

import javax.inject.Inject;

import lt.emasina.resthub.server.cache.CcCount;
import lt.emasina.resthub.server.exporter.CountExporter;
import lt.emasina.resthub.server.query.Query;

import org.restlet.data.Form;
import org.restlet.resource.ResourceException;

import com.google.inject.assistedinject.Assisted;
import java.util.Collections;
import java.util.List;

public class CountHandler extends Handler<CcCount, CountExporter> {

    @Inject
    public CountHandler(@Assisted Query query, @Assisted Form form) throws ResourceException {
        super(query, form);
    }

    @Override
    public CountExporter createExporter() {
        return rf.createCountExporter(this);
    }

    @Override
    protected List getIdParts() {
        return Collections.singletonList("count");
    }

}
