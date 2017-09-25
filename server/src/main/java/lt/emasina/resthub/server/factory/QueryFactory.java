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
package lt.emasina.resthub.server.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.exception.QueryException;
import lt.emasina.resthub.server.exporter.Exporter;
import lt.emasina.resthub.server.handler.Handler;
import lt.emasina.resthub.server.query.Query;
import lt.emasina.resthub.server.query.QueryId;
import lt.emasina.resthub.server.query.QueryMap;
import lt.emasina.resthub.server.table.TableId;

import org.restlet.resource.ResourceException;

/**
 * QueryFactory
 *
 * @author valdo
 */
@Log4j
@Singleton
public class QueryFactory {

    private static final int TIME_TO_LIVE_SECS = 60 * 60;
    
    @Inject
    private CacheFactory ccf;
    
    @Inject
    private ResourceFactory rf;
    
    private final QueryMap queries = new QueryMap();

    public Collection<Query> getQueries() {
        return queries.getQueries();
    }

    public Query getQuery(String id) {
        return queries.get(id);
    }

    public String createQuery(String sql) throws QueryException {
        
        // 0: search for query by MD5 (fast-forward)
        String id = queries.getId(QueryId.getMD5(sql));
        if (id != null) {
            return id;
        }
        
        // 1: Not found. Create QID, normalize query and search again
        QueryId qid = rf.create(sql);
        id = queries.getId(qid.getMd5());
        if (id != null) {
            return id;
        }

        // 2: Not found. Create new query and add to list
        Query q = rf.create(qid);
        queries.add(q);

        // 3: Create query cache
        if (q.isCacheable()) {
            ccf.add(q);
        }

        return qid.getId();
    }
    
    private final Map<Integer, Exporter<?>> exporters = new ConcurrentHashMap<>();
    
    @SuppressWarnings("unchecked")
    public <E extends Exporter<?>, H extends Handler<?, E>> E getExporter(H handler) throws ResourceException {
        Integer id = handler.getId();
        
        E de = (E) exporters.get(id);
        if (de != null) {

            if (log.isDebugEnabled()) {
                log.debug(String.format("got DataExporter from map %d for %d", de.hashCode(), id));
            }
            
            return de;
        }
        
        synchronized (this) {
            de = (E) exporters.get(id);
            if (de == null) {
                
            	de = handler.createExporter();
                exporters.put(id, de);

                if (log.isDebugEnabled()) {
                    log.debug(String.format("created DataExporter %d for %s", de.hashCode(), id));
                }
                
            } else {
                log.debug(String.format("got DataExporter from map after lock %d for %s", de.hashCode(), id));
            }
            
            return de;
            
        }
    }
    
    @SuppressWarnings("unchecked")
	public <E extends Exporter<?>, H extends Handler<?, E>> void removeExporter(H handler) {
        E removed = (E) exporters.remove(handler.getId());

        if (log.isDebugEnabled() && removed != null) {
            log.debug(String.format("removed DataExporter %d for %s", removed.hashCode(), handler.getId()));
        }
        
    }   
    
    public boolean removeQuery(String id) {
        
        if (log.isDebugEnabled()) {
            log.debug(String.format("removing query: %s", id));
        }
        
        Query q = queries.get(id);
        if (q != null) {
            ccf.remove(q);
        }
        
        return queries.remove(id);
    }

    public void removeQueries(TableId id) {
        for (String qid: getQueries(id)) {
            removeQuery(qid);
        }
    }

    public Collection<String> getQueries(TableId id) {
        Set<String> qids = new HashSet<>();
        for (Query q: queries.getQueries()) {
            if (q.getTables().contains(id)) {
                qids.add(q.getQid().getId());
            }
        }
        return qids;
    }
    
    public synchronized void cleanQueries() {
        List<String> toRemove = new ArrayList<>();
        Date threshold = new Date(new Date().getTime() - (TIME_TO_LIVE_SECS * 1000));
        
        for (Query q: queries.getQueries()) {
            String id = q.getQid().getId();
            Date lastAccess = queries.getLastAccess(id);
            if (lastAccess.before(threshold)) {
                toRemove.add(id);
                
                if (log.isDebugEnabled()) {
                    log.debug(String.format("removing query %s due to expired access time: %s", id, lastAccess));
                }
                
            }
        }
        
        for (String id: toRemove) {
            removeQuery(id);
        }
        
    }
    
    private static final AtomicLong UID = new AtomicLong(new Date().getTime());

    public static String nextUID() {
        // Prefix "o" to remove conversion to number 1e99999 problem
        // Based on Onuskis village in Lithunia
        return "o".concat(Long.toHexString(UID.incrementAndGet()));
    }
}
