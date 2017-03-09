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
package lt.emasina.resthub.server.table;

import com.google.common.collect.Lists;
import com.google.inject.assistedinject.Assisted;
import java.io.StringReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lt.emasina.resthub.TableFactory;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import lt.emasina.resthub.server.factory.MetadataFactory;
import lt.emasina.resthub.model.MdColumn;
import lt.emasina.resthub.model.MdParameter;
import lt.emasina.resthub.model.MdTable;
import lt.emasina.resthub.server.ServerAppConfig;
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
    private final TableFactory tf;

    @Inject
    public ServerTable(@Assisted MdTable table, @Assisted TableFactory tf) {
        this.id = new TableId(table);
        this.table = table;
        this.tf = tf;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public URL getReference(String prefix, Reference ref, Object... parts) {
        List list = Lists.newArrayList(prefix, id.getNamespace(), id.getName());
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
                ret.put("cache", getReference("table", ref, "cache"));
            }

            ret.put("tableFactory", tf.getClass().getSimpleName());
            
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

    /**
     * Determine if this is the same based on data.
     * @param stToCompare ServerTable to compare with.
     * @return true if is the same, false - otherwise.
     */
    public boolean isSame(ServerTable stToCompare) {


        MdTable newest = getTable();
        MdTable old = stToCompare.getTable();

        return newest.getColumns().equals(old.getColumns()) &&
                newest.getMetadata().equals(old.getMetadata()) &&
                newest.getName().equals(old.getName()) &&
                newest.getNamespace().equals(old.getNamespace()) &&
                newest.getParameters().equals(old.getParameters());

    }
}
