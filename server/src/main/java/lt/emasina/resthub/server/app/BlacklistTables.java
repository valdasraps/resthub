package lt.emasina.resthub.server.app;

import lt.emasina.resthub.server.exception.ServerErrorException;
import lt.emasina.resthub.server.table.ServerTable;
import lt.emasina.resthub.server.table.TableId;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Options;

public class BlacklistTables extends ServerBaseResource {
    
    private String namespace = null;
    
    @Override
    public void doInit() {
        super.doInit();
        this.namespace = super.getAttr(String.class, "tableNs");
    }
    
    @Options
    public void define() {
        addHeader("Access-Control-Allow-Methods", "GET, DELETE, OPTIONS");
        addHeader("Access-Control-Allow-Headers", "Content-Type");
        addHeader("Content-Type", "application/json");
    }
    
    @Get
    public void describe() {
        try {
            JSONObject ret = new JSONObject();
            for (ServerTable tmd : mf.getBlacklist()) {
                TableId id = tmd.getId();
                if (namespace != null) {
                    if (namespace.equals(id.getNamespace())) {
                        ret.put(id.getName(), tmd.getReference("blacklist", getHostRef()));
                    }
                } else {
                    if (!ret.has(id.getNamespace())) {
                        ret.put(id.getNamespace(), new JSONObject());
                    }
                    JSONObject nso = (JSONObject) ret.get(id.getNamespace());
                    nso.put(id.getName(), tmd.getReference("blacklist", getHostRef()));
                }
            }
            getResponse().setEntity(new JsonRepresentation(ret));
            
        } catch (JSONException ex) {
            throw new ServerErrorException(ex);
        }
    }
    
    @Delete
    public void remove() {
        if (namespace == null) {
            mf.clearBlacklist();
        } else {
            mf.clearBlacklist(namespace);
        }
        getResponse().setStatus(Status.SUCCESS_OK);
    }
    
}
