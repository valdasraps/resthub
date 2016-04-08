/*
 * #%L
 * server
 * %%
 * Copyright (C) 2012 - 2015 valdasraps
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package lt.emasina.resthub.server.app;

import javax.inject.Inject;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.server.exception.ClientErrorException;
import lt.emasina.resthub.server.factory.MetadataFactory;
import lt.emasina.resthub.server.factory.QueryFactory;
import lt.emasina.resthub.server.query.Query;
import lt.emasina.resthub.server.table.TableId;
import lt.emasina.resthub.server.table.ServerTable;
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
        this.verbose = isParam("_verbose");
        
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
