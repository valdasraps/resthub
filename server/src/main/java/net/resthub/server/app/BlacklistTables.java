package net.resthub.server.app;

import java.util.Collection;
import net.resthub.server.table.ServerTable;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Options;

public class BlacklistTables extends AbstractTables {
    
    @Options
    public void define() {
        addHeader("Access-Control-Allow-Methods", "GET, DELETE, OPTIONS");
        addHeader("Access-Control-Allow-Headers", "Content-Type");
        addHeader("Content-Type", "application/json");
    }
    
    @Override
    protected Collection<ServerTable> getTables() {
        return mf.getBlacklist();
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
