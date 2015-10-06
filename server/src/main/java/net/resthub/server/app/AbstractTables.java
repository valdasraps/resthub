package net.resthub.server.app;

import java.util.Collection;

import net.resthub.server.exception.ServerErrorException;
import net.resthub.server.table.TableId;
import net.resthub.server.table.ServerTable;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

public abstract class AbstractTables extends ServerBaseResource {
    
    protected String namespace = null;
    protected String name = null;

    protected abstract Collection<ServerTable> getTables();
    
    @Override
    public void doInit() {
        super.doInit();
        this.namespace = super.getAttr(String.class, "tableNs");
        this.name = super.getAttr(String.class, "tableName");
    }
    
    @Get
    public void describe() {
        try {
            JSONObject ret = new JSONObject();
            for (ServerTable tmd : getTables()) {
                TableId id = tmd.getId();
                if (namespace != null) {
                    if (namespace.equals(id.getNamespace())) {
                        ret.put(id.getName(), tmd.getReference(getHostRef()));
                    }
                } else {
                    if (!ret.has(id.getNamespace())) {
                        ret.put(id.getNamespace(), new JSONObject());
                    }
                    JSONObject nso = (JSONObject) ret.get(id.getNamespace());
                    nso.put(id.getName(), tmd.getReference(getHostRef()));
                }
            }
            getResponse().setEntity(new JsonRepresentation(ret));
            
        } catch (JSONException ex) {
            throw new ServerErrorException(ex);
        }
    }
    
    @Put
    public void refresh() throws Exception {
        if (namespace != null && name != null) { 
        	mf.refreshTable(namespace, name);
        } else if (namespace != null) {
        	mf.refreshNamespace(namespace);        	
        } else {
            mf.refreshAllTables();
        }
    }
    
}
