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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * QueryStats
 * @author valdo
 */
public class QueryStats {

    private int count = 0;
    private int cached = 0;
    private int sqlSuccesses = 0;
    private int sqlErrors = 0;
    private int sqlTimeAverage = 0;
    private long cacheTime = 0L;
    
    public void setCached() {
        synchronized(this) {
            count += 1;
            cached += 1;
        }
    }
    
    public void setSqlSuccess(int sqlElapsed) {
        synchronized(this) {
            count += 1;
            this.sqlSuccesses += 1;
            sqlTimeAverage = (sqlTimeAverage * (count - 1) + sqlElapsed) / count;
        }
    }
    
    public void setSqlError() {
        synchronized(this) {
            count += 1;
            sqlErrors += 1;
        }
    }
    
    public JSONObject getJSON() throws JSONException {
        JSONObject o = new JSONObject();
        synchronized(this) {
            o.put("count", count);
            o.put("cached", cached);
            o.put("sqlSuccesses", sqlSuccesses);
            o.put("sqlErrors", sqlErrors);
            o.put("sqlTimeAverage", sqlTimeAverage);
        }
        return o;
    }

    public void setCacheTime(long cacheTime) {
        this.cacheTime = cacheTime;
    }
    
    public long getCacheTime() {
        return this.cacheTime;
    }

}
