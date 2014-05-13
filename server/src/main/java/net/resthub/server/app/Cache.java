package net.resthub.server.app;

import javax.inject.Inject;
import net.sf.ehcache.statistics.StatisticsGateway;
import net.resthub.server.exception.ClientErrorException;
import net.resthub.server.factory.CacheFactory;
import net.resthub.server.query.Query;
import net.resthub.server.table.ServerTable;
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
