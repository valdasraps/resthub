package net.resthub.model;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import net.resthub.util.Helper;
import org.json.JSONException;
import org.json.JSONObject;

@Getter
public class Table {

    private final String name;
    private final String connection;
    private final List<Column> columns;
    private final List<Parameter> parameters;
    
    private String id;
    private String sql;
    private long cacheTime;
    private long rowsLimit;
    
    private final Map<String, String> metadata;

    public Table(String name, JSONObject ob, boolean v) throws JSONException {

        this.name = name;
        this.connection = ob.getString("connection");

        if (v) {
            this.sql = ob.getString("sql");
            this.cacheTime = ob.getLong("cacheTime");
            this.rowsLimit = ob.getLong("rowsLimit");
            this.id = ob.getString("id");
        }

        this.columns = Helper.getColumns(ob);
        this.parameters = Helper.getParameters(ob);
        this.metadata = Helper.getMetadata(ob);
    }
}
