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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import lombok.Getter;
import lombok.ToString;
import lt.emasina.resthub.exception.QueryException;
import lt.emasina.resthub.factory.TableBuilder;
import lt.emasina.resthub.model.MdColumn;
import lt.emasina.resthub.model.MdTable;
import lt.emasina.resthub.server.factory.MetadataFactory;
import lt.emasina.resthub.server.factory.ResourceFactory;
import lt.emasina.resthub.server.parser.check.CheckSelectParser;
import lt.emasina.resthub.server.parser.check.SubSelectDef;
import lt.emasina.resthub.server.parser.update.UpdateSelectParser;
import lt.emasina.resthub.server.table.ServerTable;
import lt.emasina.resthub.server.table.TableId;
import net.sf.jsqlparser.statement.select.Select;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Reference;

import com.google.common.collect.Lists;
import com.google.inject.assistedinject.Assisted;
import lt.emasina.resthub.server.ServerAppConfig;

/**
 * QueryMd
 * @author valdo
 */
@Getter
@ToString(of = {"qid"})
public class Query {

    @Inject
    private ServerAppConfig cfg;
    
    private final QueryId qid;
    private final String sql;
    private final Date createTime = new Date();
    
    private String connectionName;
    private Integer hitCount = 0;
    private Integer cacheTime = MdTable.ETERNAL_CACHE_TIME;
    private Integer rowsLimit = MdTable.MAX_ROWS_LIMIT;
    private Integer timeOut = 0;
    private final List<MdColumn> columns = new ArrayList<>();
    private final List<QueryParameter> parameters = new ArrayList<>();
    private final Set<TableId> tables = new HashSet<>();
    private final QueryStats stats = new QueryStats();

    @Inject
    public Query(@Assisted QueryId qid, ResourceFactory rf, TableBuilder tf) throws QueryException, Exception {
        this.qid = qid;
        
        CheckSelectParser checkParser = rf.createSelectParser((SubSelectDef) null);
        
        // Check syntax
        qid.getSelect().getSelectBody().accept(checkParser);
        
        Boolean hitCountWasZero = false;
        
        // Collect aggregate info from tables
        for (ServerTable t: checkParser.getTables()) {
            
            this.tables.add(t.getId());
            this.connectionName = t.getTable().getConnectionName();

            Integer ct = t.getTable().getCacheTime();
            if (ct != MdTable.ETERNAL_CACHE_TIME && this.cacheTime != MdTable.SKIP_CACHE_TIME && 
                    !Objects.equals(ct, this.cacheTime)) {
                if (this.cacheTime == MdTable.ETERNAL_CACHE_TIME || ct < this.cacheTime) {
                    this.cacheTime = t.getTable().getCacheTime();
                }
            }

            if (t.getTable().getRowsLimit() < this.rowsLimit) {
                this.rowsLimit = t.getTable().getRowsLimit();
            }
            
            if (!hitCountWasZero) {
                if (t.getTable().getHitCount() == 0) {
                    hitCountWasZero = true;
                } else {
                    this.hitCount += t.getTable().getHitCount();
                }
            }
            
            if (this.timeOut < t.getTable().getTimeout()) {
                this.timeOut = t.getTable().getTimeout();
            }
            
        }
        
        if (hitCountWasZero) {
            this.hitCount = 0;
        } else {
            this.hitCount /= checkParser.getTables().size();
        }
        
        // Create exec version
        UpdateSelectParser execParser = new UpdateSelectParser(checkParser);
        Select select = qid.getSelect();
        select.getSelectBody().accept(execParser);
        this.parameters.addAll(execParser.getParameters().values());
        
        String tsql = select.toString();
        
        // Collect columns
        tf.collectColumns(this.connectionName, tsql, columns);
        
        StringBuilder cols = new StringBuilder();
        cols.append("select ");
        boolean firstPass = Boolean.TRUE;
        for (MdColumn c: columns) {
            if (!firstPass) {
                cols.append(",");
            }
            cols.append("\"").append(c.getName()).append("\"");
            firstPass = Boolean.FALSE;
        }
        cols.append(" from (").append(tsql).append(")");
        
        this.sql = cols.toString();
        
    }
    
    public boolean isEternal() {
        return cacheTime == MdTable.ETERNAL_CACHE_TIME;
    }
    
    public int getCacheTime() {
        return isEternal() ? MdTable.SKIP_CACHE_TIME : cacheTime;
    }
    
    public int getCacheTimeInMilliseconds() {
        return isEternal() ? MdTable.SKIP_CACHE_TIME : cacheTime * 1000;
    }
        
    public boolean isCacheable() {
        return cacheTime != MdTable.SKIP_CACHE_TIME;
    }
    
    public URL getReference(Reference ref, Integer perpage, Integer page, String query) {
        return getReference(ref, "page", query, perpage, page, "data");
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public URL getReference(Reference ref, String query, Object... parts) {
        List list = Lists.newArrayList("query", qid.getId());
        list.addAll(Arrays.asList(parts));
        return cfg.getReference(ref, query, list);
    }
    
    public JSONObject getJSON(Reference ref, boolean verbose) throws JSONException {
        JSONObject ret = new JSONObject();
        ret.put("connection", connectionName);
        ret.put("query", qid.getSql());
        
        if (verbose) {
            ret.put("sql", sql);
            ret.put("cacheTime", cacheTime);
            ret.put("md5", qid.getMd5());
            ret.put("md5Raw", qid.getMd5Raw());
            ret.put("id", qid.getId());
            ret.put("rowsLimit", rowsLimit);
            ret.put("hitCount", hitCount);
            ret.put("timeOut", timeOut);
            if (isCacheable()) {
                ret.put("eternal", isEternal());
                ret.put("cache", getReference(ref, null, "cache"));
            }
            ret.put("stats", stats.getJSON());
        }
        
        ret.put("columns", getColumnsJSON());
        
        JSONArray pars = getParamsJSON(verbose);
        if (pars.length() > 0) {
            ret.put("parameters", pars);
        }
        
        return ret;
    }

    public JSONArray getColumnsJSON() throws JSONException {
        JSONArray cols = new JSONArray();
        for (MdColumn c: columns) {
            JSONObject col = new JSONObject();
            col.put("name", c.getName());
            col.put("type", c.getType());
            col.put("cname", c.getCName());
            col.put("jname", c.getJName());
            cols.put(col);
        }
        return cols;
    }

    public JSONArray getParamsJSON(boolean verbose) throws JSONException {
        JSONArray pars = new JSONArray();
        for (QueryParameter p: parameters) {
            JSONObject par = new JSONObject();
            par.put("name", p.getName());
            par.put("type", p.getType());
            par.put("array", p.getArray());
            if (verbose) {
                par.put("sqlName", p.getSqlName());
            }
            par.put("metadata", MetadataFactory.mapToJSONObject(p.getMetadata()));
            pars.put(par);
        }
        return pars;
    }

}