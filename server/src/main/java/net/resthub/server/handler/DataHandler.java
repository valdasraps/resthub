package net.resthub.server.handler;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import net.resthub.server.cache.CcData;
import net.resthub.server.exporter.DataExporter;
import net.resthub.server.query.Query;

import org.restlet.data.Form;
import org.restlet.resource.ResourceException;

import com.google.inject.assistedinject.Assisted;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.restlet.data.Reference;

public class DataHandler extends PagedHandler<CcData, DataExporter> {

    @Getter
    @Setter
    private boolean printColumns;
    
    @Inject
    public DataHandler(@Assisted Query query, @Assisted Form form) throws ResourceException {
    	super(query, form);
    }

    @Override
    public DataExporter createExporter() {
        return rf.createDataExporter(this);
    }
    
    public URL getReference(Reference ref, Object... parts) {
        List<Object> myparts = new ArrayList<>();
        if (getPerPage() != null && getPage() != null) {
            myparts.add("page");
            myparts.add(getPerPage());
            myparts.add(getPage());
        }
        myparts.addAll(Arrays.asList(parts));
        return getQuery().getReference(ref, getQueryString(), myparts.toArray());
    }

}