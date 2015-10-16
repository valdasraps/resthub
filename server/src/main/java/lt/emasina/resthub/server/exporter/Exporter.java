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
package lt.emasina.resthub.server.exporter;

import javax.inject.Inject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.factory.ConnectionManager;
import lt.emasina.resthub.server.cache.CcBase;
import lt.emasina.resthub.server.exception.ServerErrorException;
import lt.emasina.resthub.server.factory.DataFactory;
import lt.emasina.resthub.server.factory.QueryFactory;
import lt.emasina.resthub.server.handler.Handler;
import lt.emasina.resthub.server.query.QueryStats;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.restlet.resource.ResourceException;

/**
 * DataExporter class
 * @author valdo
 * @param <C>
 */
@Log4j
@Getter
@RequiredArgsConstructor
public abstract class Exporter <C extends CcBase<?>> {
    
    @Inject
    private ConnectionManager cm;

    @Inject
    private DataFactory df;
    
    @Inject
    private QueryFactory qf;
    
    private final Handler<C,?> handler;
    
    private volatile C value = null;
    
    public C getValue() {
        setValue(Boolean.TRUE);
        return value;
    }
    
    public void refreshValue() {
        setValue(Boolean.FALSE);
    }
    
    private void setValue(boolean useCache) {
        Integer id = handler.getId();

        if (useCache && value != null) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("got value from class property, 1LC (%d)", id));
            }
            return;  
        }

        QueryStats stats = handler.getQuery().getStats();
        synchronized(this) {
        
            if (useCache && value != null) {
                
                if (log.isDebugEnabled()) {
                    log.debug(String.format("got value from class property after lock, 1LC (%d)", id));
                }
                
            } else {
            
                if (useCache) {
                    value = handler.getCached();
                }
                
                if (useCache && value != null) {
                    
                    stats.setCached();

                    if (log.isDebugEnabled()) {
                        log.debug(String.format("got value from cache, 2LC (%d)", id));
                    }
                    
                } else {
                    
                    long startTime = System.currentTimeMillis();

                    Session session = cm.getSession(handler.getQuery().getConnectionName());
                    Transaction tr = session.beginTransaction();
                    try {
                    	
                        value = retrieveData(session);
                        
                    } catch (Exception ex) {
                        stats.setSqlError();
                        if (ResourceException.class.isAssignableFrom(ex.getClass())) {
                            throw (ResourceException) ex;
                        } else {
                            throw new ServerErrorException(ex);
                        }
                    } finally {
                        tr.commit();
                        session.close();
                    }

                    Long elapsed = System.currentTimeMillis() - startTime;
                    stats.setSqlSuccess(elapsed.intValue());
                    stats.setCacheTime(elapsed);
                    
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("got value from database (%d), time: (%d)", id, elapsed));
                    }

                    handler.setCached(value);
                    
                } 

                qf.removeExporter(handler);
                 
            }
            
        }

    }

    protected abstract C retrieveData(Session session) throws Exception;
    
}
