/*
 * #%L
 * server
 * %%
 * Copyright (C) 2012 - 2020 valdasraps
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

import lt.emasina.resthub.server.exporter.HistoExporter;
import lt.emasina.resthub.server.handler.HistoHandler;
import org.restlet.data.Method;
import lt.emasina.resthub.server.query.Query;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.ResourceException;
import java.util.*;
import lt.emasina.resthub.model.MdColumn;
import lt.emasina.resthub.server.cache.CacheStats;
import lt.emasina.resthub.server.converter.HistoConverter;
import lt.emasina.resthub.server.exception.ClientErrorException;
import lt.emasina.resthub.server.exception.ServerErrorException;
import org.restlet.data.MediaType;
import org.restlet.data.Status;

public class Histo extends PagedData {

    private static final HistoConverter CONVERTER = new HistoConverter();
    private HistoHandler handler;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        Query q = getQueryMd(true);
        handler = rf.createHistoHandler(q, getQuery());
        
        MdColumn column = q.getColumn(super.getAttr(String.class, "column"));
        if (column == null) {
            throw new ClientErrorException(Status.CLIENT_ERROR_NOT_FOUND, "Column not found?");
        } else {
            handler.setColumn(column);
        }

        Integer bins = super.getAttr(Integer.class, "bins");
        if (bins != null) {
            if (bins <= 0) {
                throw new ClientErrorException(Status.CLIENT_ERROR_BAD_REQUEST, "Bad number of bins, must be > 0");
            } else {
                handler.setBins(bins);
            }
        }

        Double minValue = super.getAttr(Double.class, "min");
        Double maxValue = super.getAttr(Double.class, "max");
        if (minValue != null && maxValue != null && maxValue > minValue) {
            handler.setMinValue(minValue);
            handler.setMaxValue(maxValue);
        }
    }

    @Options
    public void define() {
        getResponse().setAccessControlAllowMethods(new HashSet<>(Arrays.asList(Method.GET, Method.OPTIONS)));
        getResponse().setAccessControlAllowHeaders(Collections.singleton("Content-Type"));
        addHeader(HEADER_CONTENT_TYPES, MediaType.APPLICATION_JSON.toString());
    }

    @Get
    public void histo() throws ResourceException {
        CacheStats stats = handler.getCacheStats();
        
        // Process "If-Modified-Since"
        if (respondNotModified(stats)) {
            addExpiresHeader(stats);
            return;
        }

        // Finally, return data
        try {
            
            HistoExporter de = qf.getExporter(handler);
            getResponse().setEntity(CONVERTER.convert(handler, getHostRef(), de.getValue()));
            addExpiresHeader(stats);

        } catch (Exception ex) {
            if (ResourceException.class.isAssignableFrom(ex.getClass())) {
                throw (ResourceException) ex;
            }
            throw new ServerErrorException(ex);
        }

    }

}