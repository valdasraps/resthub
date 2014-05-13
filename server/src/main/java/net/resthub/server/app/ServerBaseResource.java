package net.resthub.server.app;

import javax.inject.Inject;
import lombok.extern.log4j.Log4j;
import net.resthub.server.exception.ClientErrorException;
import net.resthub.server.factory.MetadataFactory;
import net.resthub.server.factory.QueryFactory;
import net.resthub.server.query.Query;
import net.resthub.server.table.TableId;
import net.resthub.server.table.ServerTable;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

/**
 * JdbcBaseResource
 * @author valdo
 */
@Log4j
public abstract class ServerBaseResource extends BaseResource {

    @Inject
    protected MetadataFactory mf;
    
    @Inject
    protected QueryFactory qf;

    protected boolean verbose = false;
    
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        this.verbose = getParam(Boolean.class, "v", false);
        
        if (log.isDebugEnabled()) {
            log.debug(String.format("Init app: %s, %s in %s", this.getClass().getName(), this.hashCode(), Thread.currentThread()));
        }
        
    }
    
    protected ServerTable getTableMd(boolean dieIfNotFound) throws ResourceException {
        String tableNs = super.getAttr(String.class, "tableNs");
        String tableName = super.getAttr(String.class, "tableName");
        if (tableNs != null && tableName != null) {
            ServerTable rmd = mf.getTable(new TableId(tableNs, tableName));
            if (rmd == null && dieIfNotFound) {
                throw new ClientErrorException(Status.CLIENT_ERROR_NOT_FOUND, "table [%s.%s] not found.", tableNs, tableName);
            }
            return rmd;
        } else {
            return null;
        }
    }

    protected Query getQueryMd(boolean dieIfNotFound) throws ResourceException {
        String queryId = super.getAttr(String.class, "queryId");
        if (queryId != null) {
            Query q = qf.getQuery(queryId);
            if (q == null && dieIfNotFound) {
                throw new ClientErrorException(Status.CLIENT_ERROR_NOT_FOUND, "query [%s] not found.", queryId);
            }
            return q;
        } else {
            return null;
        }
    }
    
}
