package net.resthub.server.factory;

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
import net.resthub.exception.QueryException;
import net.resthub.server.exporter.Exporter;
import net.resthub.server.handler.Handler;
import net.resthub.server.query.Query;
import net.resthub.server.query.QueryId;
import net.resthub.server.query.QueryMap;
import net.resthub.server.table.TableId;

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
        // Create id and search for existing query
        QueryId qid = rf.create(sql);
        String id = queries.getId(qid.getMd5());
        if (id != null) {
            return id;
        }

        // Not found. Create new query and add to list
        Query q = rf.create(qid);
        queries.add(q);

        // Create query cache
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
        return Long.toHexString(UID.incrementAndGet());
    }
}
