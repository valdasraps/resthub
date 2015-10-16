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
package lt.emasina.resthub.server.query;

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
