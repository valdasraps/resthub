package lt.emasina.resthub.server.app;

import lt.emasina.resthub.server.exception.ClientErrorException;
import lt.emasina.resthub.server.exception.ServerErrorException;
import lt.emasina.resthub.server.table.TableId;
import org.json.JSONException;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.ResourceException;

public class BlacklistTable extends ServerBaseResource {

    private lt.emasina.resthub.server.table.ServerTable tmd;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        String tableNs = super.getAttr(String.class, "tableNs");
        String tableName = super.getAttr(String.class, "tableName");
        this.tmd = mf.getBlacklistTable(new TableId(tableNs, tableName));
        if (tmd == null) {
            throw new ClientErrorException(Status.CLIENT_ERROR_NOT_FOUND, "table [%s.%s] not found.", tableNs, tableName);
        }
    }
    
    @Options
    public void define() {
        addHeader("Access-Control-Allow-Methods", "GET, DELETE, OPTIONS");
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
    
    @Delete
    public void remove() {
        mf.removeBlacklistTable(tmd.getId());
    }
    
}
