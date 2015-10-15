package lt.emasina.resthub.server.handler;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.server.app.BaseResource;
import lt.emasina.resthub.server.cache.CacheStats;
import lt.emasina.resthub.server.cache.CcBase;
import lt.emasina.resthub.server.exception.ClientErrorException;
import lt.emasina.resthub.server.exporter.Exporter;
import lt.emasina.resthub.server.factory.CacheFactory;
import lt.emasina.resthub.server.factory.ResourceFactory;
import lt.emasina.resthub.server.query.Query;
import lt.emasina.resthub.server.query.QueryParameter;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.commons.lang.builder.HashCodeBuilder;

import org.hibernate.SQLQuery;
import org.hibernate.type.BigDecimalType;
import org.hibernate.type.DateType;
import org.hibernate.type.StringType;
import org.json.JSONArray;
import org.json.JSONException;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

/**
 * QueryHandler
 * @param <C>
 * @param <E>
 * @author valdo
 */
@Log4j
@ToString(of = "query")
public abstract class Handler<C extends CcBase<?>, E extends Exporter<C>> {

    @Inject
    private CacheFactory ccf;
    
    @Inject
    protected ResourceFactory rf;
    
    @Getter
    private final Query query;
    
    @Getter
    private final Map<QueryParameter, Object> parameters = new HashMap<>();
    
    @Getter
    private final String queryString;

    public Handler(Query query, Form form) throws ResourceException {
        this.query = query;
        this.queryString = form.getQueryString();
        for (QueryParameter p : query.getParameters()) {

            String name = p.getName();
            String svalue = form.getFirstValue(name, true);

            switch (p.getType()) {
                
                case DATE:
                    parameters.put(p, readParameterValue(Date.class, p, svalue));
                    break;
                    
                case NUMBER:
                    parameters.put(p, readParameterValue(BigDecimal.class, p, svalue));
                    break;
                    
                case STRING:
                    parameters.put(p, readParameterValue(String.class, p, svalue));
                    break;
                    
                case CLOB:
                case BLOB:
                    throw new ClientErrorException(Status.CLIENT_ERROR_BAD_REQUEST, String.format("LOBs are not supported as parameters: %s", name));
                    
            }
            
        }
        
        if (log.isDebugEnabled()) {
            for (QueryParameter qp: parameters.keySet()) {
                log.debug(qp.toString(parameters.get(qp)));
            }
        }
        
    }

    private static <T> Object readParameterValue(Class<T> clazz, QueryParameter param, String svalue) throws ResourceException {
        if (svalue == null) {
            return null;
        } else {
            try {
                if (param.getArray()) {
                    JSONArray ja = new JSONArray(svalue);
                    List<T> a = new ArrayList<>();
                    for (int i = 0; i < ja.length(); i++) {
                        a.add(BaseResource.convertValue(clazz, ja.getString(i)));
                    }
                    return a.toArray();
                } else {
                    return BaseResource.convertValue(clazz, svalue);
                }
            } catch (JSONException ex) {
                throw new ClientErrorException(Status.CLIENT_ERROR_BAD_REQUEST, ex);
            }
        }
    }

    public void applyParameters(SQLQuery query) throws SQLException {
        for (Map.Entry<QueryParameter, Object> e : parameters.entrySet()) {
            
            QueryParameter p = e.getKey();
            Object value = e.getValue();
            String name = p.getSqlName();
            
            if (value != null && p.getArray()) {
                
                switch (p.getType()) {
                    case DATE:
                        query.setParameterList(name, (Object[]) value, new DateType());
                        break;
                    case NUMBER:
                        query.setParameterList(name, (Object[]) value, new BigDecimalType());
                        break;
                    case STRING:
                        query.setParameterList(name, (Object[]) value, new StringType());
                        break;
                    case CLOB:
                    case BLOB:
                        throw new ClientErrorException(Status.CLIENT_ERROR_BAD_REQUEST, String.format("LOBs are not supported as parameters: %s", name));
                }
                
            } else {
                
                switch (p.getType()) {
                    case DATE:
                        query.setDate(name, (Date) value);
                        break;
                    case NUMBER:
                        query.setBigDecimal(name, (BigDecimal) value);
                        break;
                    case STRING:
                        query.setString(name, (String) value);
                        break;
                    case CLOB:
                    case BLOB:
                        throw new ClientErrorException(Status.CLIENT_ERROR_BAD_REQUEST, String.format("LOBs are not supported as parameters: %s", name));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
	public C getCached() {
        if (query.isCacheable()) {
            Cache cache = ccf.get(query);
            if (cache == null) {

                if (log.isDebugEnabled()) {
                    log.debug(String.format("Cache for %s not found", this));
                }

            } else {

                if (log.isDebugEnabled()) {
                    log.debug(String.format("Cache for %s found", this));
                }

                if (cache.isKeyInCache(getId())) {

                    Element el = cache.get(getId());
                    boolean expired = (el == null || el.isExpired());

                    if (log.isDebugEnabled()) {

                        log.debug(String.format("Element %d found in %s cache (expired = %s)",
                                getId(), query.getQid(), expired));
                    }

                    if (el != null && !expired) {
                        return (C) el.getObjectValue();
                    }

                } else {

                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Element %d not found in %s cache", getId(), query.getQid()));
                    }

                }
            }

        }

        return (C) null;

    }

    public void setCached(C data) {
        if (query.isCacheable()) {
            Cache cache = ccf.get(query);
            if (cache != null) {

                cache.put(new Element(getId(), data));
                
                if (query.getHitCount() > 0 && !query.isEternal()) {
                    CacheStats cs = getCacheStats();
                    ccf.createCacheJob(this, cs.getExpTime());
                }
                
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Element %s put into %s cache", getId(), query.getQid()));
                }

            }
        }
    }

    public CacheStats getCacheStats() {
        CacheStats cs = new CacheStats();

        if (query.isCacheable()) {
            Cache cache = ccf.get(query);
            if (cache != null) {
                if (cache.isKeyInCache(getId())) {
                    Element el = cache.get(getId());
                    cs.setExpired(el == null || el.isExpired());

                    if (el != null && !cs.isExpired()) {
                        cs.setLastUpdate(el.getLastUpdateTime());
                        cs.setExpTime(el.getExpirationTime());
                        cs.setHitCount(el.getHitCount());
                    }
                }
            }
        }
        return cs;
    }

    private Integer id = null;
    
    public final Integer getId() {
        if (id == null) {
            HashCodeBuilder hcb = new HashCodeBuilder(17, 37)
                .append(this.getClass())
                .append(getQuery().getQid().getId());

            for (Object part: getIdParts()) {
                hcb.append(part);
            }
            
            for (Map.Entry<QueryParameter, Object> e : getParameters().entrySet()) {
                hcb.append(e.getKey().getName());
                hcb.append(e.getValue());
            }
            
            this.id = hcb.toHashCode();
        }
        return id;
    }
    
    protected abstract List getIdParts();
    public abstract E createExporter();
    
}