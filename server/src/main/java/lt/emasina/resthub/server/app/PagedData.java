package lt.emasina.resthub.server.app;

import static lt.emasina.resthub.server.util.ClientAssert.badRequestIfNot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;
import lt.emasina.resthub.server.cache.CacheStats;

import lt.emasina.resthub.server.factory.ResourceFactory;
import lt.emasina.resthub.server.query.Query;

import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;


/**
 * PagedData
 * @author valdo
 */
public abstract class PagedData extends ServerBaseResource {
    
    @Inject
    protected ResourceFactory rf;
    
    protected Query query;
    protected Integer perPage;
    protected Integer page;
    
    protected static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE,dd MMM yyyy HH:mm:ss zzz");

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        
        boolean postMethod = getMethod().equals(Method.POST);
        this.query = getQueryMd(! postMethod);

        this.perPage = getAttr(Integer.class, "perPage");
        badRequestIfNot(perPage == null || perPage > 0, "Per page parameter must be > 0");

        this.page = getAttr(Integer.class, "page");
        badRequestIfNot(page == null || page > 0, "Page number parameter must be > 0");
    }
    
    protected void addExpiresHeader(final CacheStats cacheStats) {       
        if (query.isCacheable()){
            long cacheExpTime = cacheStats.getExpTime();

            if (cacheExpTime == 0) {
                cacheExpTime = new Date().getTime() + query.getCacheTime() * 1000;
            }
            
            Date expDate = new Date(cacheExpTime);
            String cacheExpDate = DATE_FORMAT.format(expDate);

            addHeader("Expires", cacheExpDate);
        }
    }
    
    protected boolean respondNotModified(final CacheStats cacheStats) {
        Date modifiedSince = getHeaderValue(Date.class, "If-Modified-Since");
        if (modifiedSince != null && query.isCacheable()){
            long cacheLastUpdateTime = cacheStats.getLastUpdate();
            if (modifiedSince.getTime() > cacheLastUpdateTime && cacheLastUpdateTime > 0) {
                getResponse().setStatus(Status.REDIRECTION_NOT_MODIFIED);
                return Boolean.TRUE;
            } 
        }
        return Boolean.FALSE;
    }
    
}