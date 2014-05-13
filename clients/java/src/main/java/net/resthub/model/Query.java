package net.resthub.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import net.resthub.util.Helper;
import org.json.JSONException;
import org.json.JSONObject;

@Getter
public class Query {

    private final String id;
    private final String connection;
    private final String query;
    private final List<Column> columns;
    private final List<Parameter> parameters;
    
    private String sql;
    private long cacheTime;
    private long rowsLimit;
    private String md5;
    private boolean eternal;
    
    private Map<String, String> metadata;
    private Map<String, Long> stats;

    public Query(String id, JSONObject ob, boolean v) throws JSONException {
        this.id = id;
        this.connection = ob.getString("connection");
        this.query = ob.getString("query");

        if (v) {
            this.sql = ob.getString("sql");
            this.cacheTime = ob.getLong("cacheTime");
            this.rowsLimit = ob.getLong("rowsLimit");
            this.md5 = ob.getString("md5");
            this.eternal = ob.getBoolean("eternal");
            this.stats = new HashMap<>();
            JSONObject get = ob.getJSONObject("stats");
            Iterator i = ob.keys();
            while (i.hasNext()) {
                String statName = i.next().toString();
                long statValue = get.getLong(statName);
                stats.put(statName, statValue);
            }
        } 

        this.columns = Helper.getColumns(ob);
        this.parameters = Helper.getParameters(ob);
        this.metadata = Helper.getMetadata(ob);
    }
}
