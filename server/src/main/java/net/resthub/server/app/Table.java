package net.resthub.server.app;

import net.resthub.server.exception.ServerErrorException;
import net.resthub.server.table.ServerTable;
import org.json.JSONException;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.ResourceException;

public class Table extends ServerBaseResource {

    private ServerTable tmd;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        this.tmd = getTableMd(true);
    }
    
    @Options
    public void define() {
        addHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        addHeader("Access-Control-Allow-Headers", "Content-Type");
        addHeader("Content-Type", "application/json");
    }

    @Get
    public void describe() throws ResourceException {
        try {
            getResponse().setEntity(new JsonRepresentation(tmd.getJSON(getHostRef(), verbose)));
        } catch (ResourceException | JSONException ex) {
            throw new ServerErrorException(ex);
        }
    }
    
}
