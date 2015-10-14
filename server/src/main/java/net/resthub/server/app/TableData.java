package net.resthub.server.app;

import java.net.URL;
import net.resthub.model.MdTable;
import net.resthub.server.table.ServerTable;
import org.restlet.data.MediaType;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.ResourceException;

public class TableData extends ServerBaseResource {

    private static final String SQL = "select * from %s.%s t";
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
        StringBuilder sb = new StringBuilder();
        for (MediaType mt: Data.SUPPORTED_TYPES) {
            sb.append(sb.length() > 0 ? "," : "").append(mt);
        }
        addHeader("Content-Type", sb.toString());
    }
    
    @Get
    public void data() throws ResourceException {
        MdTable t = tmd.getTable();
        String sql = String.format(SQL, t.getNamespace(), t.getName());
        String id = qf.createQuery(sql);
        String path = getReference().getPath().replaceFirst("/table/" + t.getNamespace() + "/" + t.getName() + "/", 
                                                            "/query/" + id + "/");
        URL ref = cfg.getReference(getHostRef(), getReference().getQuery(), path);
        getResponse().redirectTemporary(ref.toString());
    }
    
}
