package net.resthub.server.query;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * QueryMap
 * @author valdo
 */
public class QueryMap {
    
    private final Map<String, Query> queries = new ConcurrentHashMap<>();
    private final Map<String, String> ids = new ConcurrentHashMap<>();
    private final Map<String, Date> lastAccess = new ConcurrentHashMap<>();

    public synchronized void add(Query q) {
        QueryId qid = q.getQid();
        queries.put(qid.getId(), q);
        ids.put(qid.getMd5(), qid.getId());
        lastAccess.put(qid.getId(), new Date());
    }
    
    public synchronized boolean remove(String id) {
        Query q = queries.remove(id);
        if (q != null) {
            ids.remove(q.getQid().getMd5());
            lastAccess.remove(q.getQid().getId());
            return true;
        } else {
            return false;
        }
    }
    
    public synchronized Query get(String id) {
        lastAccess.put(id, new Date());
        return queries.get(id);
    }
    
    public synchronized String getId(String md5) {
        return ids.get(md5);
    }
    
    public Date getLastAccess(String id) {
        return lastAccess.get(id);
    }
    
    public Collection<Query> getQueries() {
        return Collections.unmodifiableCollection(queries.values());
    }
    
}
