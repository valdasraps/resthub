package net.resthub.server.query;

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
