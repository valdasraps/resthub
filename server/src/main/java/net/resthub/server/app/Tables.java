package net.resthub.server.app;

import java.util.Collection;
import net.resthub.server.table.ServerTable;
import org.restlet.resource.Options;

public class Tables extends AbstractTables {

    @Options
    public void define() {
        addHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        addHeader("Access-Control-Allow-Headers", "Content-Type");
        addHeader("Content-Type", "application/json");
    }

    @Override
    protected Collection<ServerTable> getTables() {
        return mf.getTables();
    }
    
}
