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


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import lt.emasina.resthub.exception.QueryException;
import lt.emasina.resthub.server.cache.CacheStats;
import lt.emasina.resthub.server.converter.CSVConverter;
import lt.emasina.resthub.server.converter.DataConverter;
import lt.emasina.resthub.server.converter.JSONConverter;
import lt.emasina.resthub.server.converter.XMLConverter;
import lt.emasina.resthub.server.exception.ClientErrorException;
import lt.emasina.resthub.server.exception.ServerErrorException;
import lt.emasina.resthub.server.exporter.DataExporter;
import lt.emasina.resthub.server.handler.DataHandler;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import com.google.common.collect.Maps;
import lt.emasina.resthub.server.converter.JSON2Converter;

/**
 * Data
 * @author valdo
 */
public class Data extends PagedData {
    
    public final static Map<MediaType, DataConverter> CONVERTERS = Maps.newLinkedHashMap();
    public final static List<MediaType> SUPPORTED_TYPES;
    static {
        CONVERTERS.put(new MediaType("application/json2"), new JSON2Converter());
        CONVERTERS.put(MediaType.APPLICATION_JSON, new JSONConverter());
        CONVERTERS.put(MediaType.APPLICATION_XML, new XMLConverter());
        CONVERTERS.put(MediaType.TEXT_XML, new XMLConverter());
        CONVERTERS.put(MediaType.TEXT_CSV, new CSVConverter());
        CONVERTERS.put(MediaType.TEXT_PLAIN, new CSVConverter());
        SUPPORTED_TYPES = new ArrayList<>(CONVERTERS.keySet());
    }
    
    private Boolean printColumns;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();       
        this.printColumns = getParam(Boolean.class, "cols", false);
    }
    
    @Options
    public void define() {
        addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        addHeader("Access-Control-Allow-Headers", "Content-Type");     
        StringBuilder sb = new StringBuilder();
        for (MediaType mt: SUPPORTED_TYPES) {
            sb.append(sb.length() > 0 ? "," : "").append(mt);
        }
        addHeader("Content-Type", sb.toString());
    }
    
    @Post("text")
    public void save(Representation entity) throws ResourceException, IOException {
        if (query != null) {
            getResponse().redirectTemporary(getOriginalRef());
        } else {
            String sql = entity.getText();
            if (sql != null) {
                try {
                    
                    String id = qf.createQuery(sql);
                    String oldId = super.getAttr(String.class, "queryId");
                    String url = getOriginalRef().toString();
                    
                    url = url.replaceFirst("/query/" + oldId + "/", "/query/" + id + "/");
                    
                    getResponse().redirectTemporary(url);
                } catch (QueryException ex) {
                    throw new ClientErrorException(Status.CLIENT_ERROR_BAD_REQUEST, ex.getMessage());
                }
            } else {
                throw new ClientErrorException(Status.CLIENT_ERROR_BAD_REQUEST, "Query missing?");
            }        
        }
    }
    
    @Get
    public void data() throws ResourceException {
        
        // Check media type
        MediaType preferredMediaType = getClientInfo().getPreferredMediaType(SUPPORTED_TYPES);
        if (preferredMediaType == null) {
            throw new ClientErrorException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, "Unsupported media types");
        }
        
        // Create query handler
        DataHandler handler = rf.createDataHandler(query, getQuery());
        handler.setPerPage(perPage);
        handler.setPage(page);
        handler.setPrintColumns(printColumns);
        
        CacheStats stats = handler.getCacheStats();
        
        // Process "If-Modified-Since"
        if (respondNotModified(stats)) {
            addExpiresHeader(stats);
            return;
        }

        // Finally, return data
        try {
            
            DataExporter dexp = qf.getExporter(handler);
            getResponse().setEntity(CONVERTERS.get(preferredMediaType).convert(handler, getHostRef(), dexp.getValue()));
            addExpiresHeader(stats);

        } catch (Exception ex) {
            if (ResourceException.class.isAssignableFrom(ex.getClass())) {
                throw (ResourceException) ex;
            }
            throw new ServerErrorException(ex);
        }
  
    }

}