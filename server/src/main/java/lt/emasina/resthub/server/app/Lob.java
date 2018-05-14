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


import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import lt.emasina.resthub.model.MdColumn;
import lt.emasina.resthub.model.MdType;
import lt.emasina.resthub.server.cache.CacheStats;
import lt.emasina.resthub.server.exception.ServerErrorException;

import org.restlet.data.MediaType;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.ResourceException;

import lt.emasina.resthub.server.converter.LobConverter;
import lt.emasina.resthub.server.exporter.LobExporter;
import lt.emasina.resthub.server.handler.LobHandler;
import static lt.emasina.resthub.server.util.ClientAssert.badRequestIfNot;
import org.restlet.data.Method;

/**
 * Lob
 * @author valdo
 */
public class Lob extends PagedData {
    
    private LobHandler handler;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();       
        
        Integer row = getAttr(Integer.class, "row");
        badRequestIfNot(row != null && row >= 0, "Row attribute must be set to the valid row number");
        
        Integer col = getAttr(Integer.class, "col");
        badRequestIfNot(col != null && col >= 0 && col < query.getColumns().size(), "Column attribute must be set to the valid column number");
        
        MdColumn column = query.getColumns().get(col);
        badRequestIfNot(column.getType() == MdType.BLOB || column.getType() == MdType.CLOB, "Column type must be BLOB or CLOB found %s", column.getType().name());
        
        this.handler = rf.createLobHandler(query, getQuery());
        handler.setPerPage(perPage);
        handler.setPage(page);
        handler.setColumn(col);
        handler.setRow(row);
        
        String mt = getParam(String.class, "mt");
        if (mt != null) {
            this.handler.setMediaType(MediaType.valueOf(mt));
        }
        
    }
    
    @Options
    public void define() {
        getResponse().setAccessControlAllowMethods(new HashSet<>(Arrays.asList(Method.GET, Method.OPTIONS)));
        getResponse().setAccessControlAllowHeaders(Collections.singleton("Content-Type"));
        addHeader(HEADER_CONTENT_TYPES, this.handler.getMediaType().toString());
    }
    
    @Get
    public void data() throws ResourceException {
        CacheStats stats = handler.getCacheStats();
        
        // Process "If-Modified-Since"
        if (respondNotModified(stats)) {
            addExpiresHeader(stats);
            return;
        }

        // Finally, return data
        try {
            
            LobExporter dexp = qf.getExporter(handler);
            getResponse().setEntity(new LobConverter().convert(handler, dexp.getValue()));
            addExpiresHeader(stats);

        } catch (Exception ex) {
            if (ResourceException.class.isAssignableFrom(ex.getClass())) {
                throw (ResourceException) ex;
            }
            throw new ServerErrorException(ex);
        }
  
    }

}