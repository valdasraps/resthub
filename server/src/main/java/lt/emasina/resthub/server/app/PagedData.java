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
        badRequestIfNot(perPage == null || (perPage > 0 && perPage <= query.getRowsLimit()), "Per page parameter must be between 0 and %d", query.getRowsLimit());

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