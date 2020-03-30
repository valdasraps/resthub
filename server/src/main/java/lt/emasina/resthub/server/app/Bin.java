package lt.emasina.resthub.server.app;

import lt.emasina.resthub.server.exporter.BinExporter;
import lt.emasina.resthub.server.handler.BinHandler;
import org.restlet.data.Method;
import lt.emasina.resthub.server.query.Query;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.ResourceException;
import java.util.*;

public class Bin extends PagedData {

    private BinHandler handler;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        Query q = getQueryMd(true);
        handler = rf.createBinHandler(q, getQuery());
        handler.setBinCol(super.getAttr(String.class, "column"));
        handler.setPerPage(perPage);
        handler.setPage(page);
    }

    @Options
    public void define() {
        getResponse().setAccessControlAllowMethods(new HashSet<>(Arrays.asList(Method.GET, Method.OPTIONS)));
        getResponse().setAccessControlAllowHeaders(Collections.singleton("Content-Type"));
        addHeader(HEADER_CONTENT_TYPES, "application/json");
    }

    @Get
    public void bin() throws ResourceException {

        BinExporter de = qf.getExporter(handler);

        // Cia pagal mane nera gerai, bet nesuprantu, nurodau tipa, kaip ir gerai turetų būti
        getResponse().setEntity(new StringRepresentation(new String(de.getValue().getValue().toString()), handler.getJSON_MEDIA_TYPE() ));
    }

}