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
package lt.emasina.resthub.server.app;

import javax.inject.Inject;
import net.sf.ehcache.statistics.StatisticsGateway;
import lt.emasina.resthub.server.exception.ClientErrorException;
import lt.emasina.resthub.server.factory.CacheFactory;
import lt.emasina.resthub.server.query.Query;
import lt.emasina.resthub.server.table.ServerTable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.ResourceException;

/**
 * Queries
 * @author valdo
 */
public class Cache extends ServerBaseResource {

    @Inject
    private CacheFactory ccf;
    
    private Query qmd;
    private ServerTable tmd;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        this.qmd = getQueryMd(false);
        this.tmd = getTableMd(false);
    }
    
    @Options
    public void define() {
        addHeader("Access-Control-Allow-Methods", "GET, DELETE, OPTIONS");
        addHeader("Access-Control-Allow-Headers", "Content-Type");
        addHeader("Content-Type", "application/json");
    }
    
    @Get
    public void describe() throws ResourceException {
        if (qmd != null) {
            
            net.sf.ehcache.Cache c = ccf.get(qmd);
            if (c == null) {
                throw new ClientErrorException(Status.CLIENT_ERROR_NOT_FOUND);
            }
            StatisticsGateway st = c.getStatistics();
            JSONObject ret = new JSONObject(st);
            getResponse().setEntity(new JsonRepresentation(ret));
            
        } else
        if (tmd != null) {

            if (!tmd.isCacheable()) {
                throw new ClientErrorException(Status.CLIENT_ERROR_NOT_FOUND);
            }

            JSONArray ret = new JSONArray();
            for (String qid: qf.getQueries(tmd.getId())) {
                ret.put(getReference("query", qid, "cache"));
            }
            getResponse().setEntity(new JsonRepresentation(ret));
            
        }
    }
    
    @Delete
    public void remove() throws ResourceException {
        if (qmd != null) {
            
            net.sf.ehcache.Cache c = ccf.get(qmd);
            if (c == null) {
                throw new ClientErrorException(Status.CLIENT_ERROR_NOT_FOUND);
            }
            c.removeAll();
            getResponse().setStatus(Status.SUCCESS_OK);
            
        } else
        if (tmd != null) {

            if (!tmd.isCacheable()) {
                throw new ClientErrorException(Status.CLIENT_ERROR_NOT_FOUND);
            }

            for (String qid: qf.getQueries(tmd.getId())) {
                Query q = qf.getQuery(qid);
                if (q.isCacheable()) {
                    net.sf.ehcache.Cache c = ccf.get(q);
                    if (c != null) {
                        c.removeAll();
                    }
                }
            }
            getResponse().setStatus(Status.SUCCESS_OK);
            
        }
    }
    
}
