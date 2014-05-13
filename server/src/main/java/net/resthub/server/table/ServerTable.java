package net.resthub.server.table;

import com.google.common.collect.Lists;
import com.google.inject.assistedinject.Assisted;
import java.io.StringReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.resthub.server.factory.MetadataFactory;
import net.resthub.model.MdColumn;
import net.resthub.model.MdParameter;
import net.resthub.model.MdTable;
import net.resthub.server.ServerAppConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Reference;

@Getter
@EqualsAndHashCode(of = "id")
public class ServerTable {

    @Inject
    private CCJSqlParserManager pm;
    
    @Inject
    private ServerAppConfig cfg;
    
    private final TableId id;
    private final MdTable table;

    @Inject
    public ServerTable(@Assisted MdTable table) {
        this.id = new TableId(table);
        this.table = table;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public URL getReference(Reference ref, Object... parts) {
        List list = Lists.newArrayList("table", id.getNamespace(), id.getName());
        list.addAll(Arrays.asList(parts));
        return cfg.getReference(ref, null, list);
    }
    
    public Select getSelect() throws JSQLParserException {
        Statement stmt = pm.parse(new StringReader(table.getSql()));
        return (Select) stmt;
    }
    
    public JSONObject getJSON(Reference ref, boolean verbose) throws JSONException {
        JSONObject ret = new JSONObject();
        ret.put("metadata", MetadataFactory.mapToJSONObject(table.getMetadata()));
        ret.put("connection", table.getConnectionName());
        
        if (verbose) {
            ret.put("sql", table.getSql());
            ret.put("id", id);        
            ret.put("cacheTime", table.getCacheTime());
            ret.put("rowsLimit", table.getRowsLimit());
            ret.put("hitCount", table.getHitCount());
            if (isCacheable()) {
                ret.put("cache", getReference(ref, "cache"));
            }
        }

        JSONArray cols = new JSONArray();
        for (MdColumn c: table.getColumns()) {
            JSONObject col = new JSONObject();
            col.put("name", c.getName());
            col.put("type", c.getType());
            col.put("metadata", MetadataFactory.mapToJSONObject(c.getMetadata()));
            cols.put(col);
        }
        if (cols.length() > 0) {
            ret.put("columns", cols);
        }

        JSONArray pars = new JSONArray();
        for (MdParameter p: table.getParameters()) {
            JSONObject par = new JSONObject();
            par.put("name", p.getName());
            par.put("type", p.getType());
            par.put("array", p.getArray());
            par.put("metadata", MetadataFactory.mapToJSONObject(p.getMetadata()));
            pars.put(par);
        }
        if (pars.length() > 0) {
            ret.put("parameters", pars);
        }
        return ret;
    }
    
    public MdColumn getColumn(String name) {
        for (MdColumn cmd: table.getColumns()) {
            if (cmd.getName().equalsIgnoreCase(name)) {
                return cmd;
            }
        }
        return null;
    }
    
    public MdParameter getParameter(String name) {
        for (MdParameter p: table.getParameters()) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        return null;
    }
    
    public boolean isCacheable() {
        return table.getCacheTime() != MdTable.SKIP_CACHE_TIME;
    }

}
