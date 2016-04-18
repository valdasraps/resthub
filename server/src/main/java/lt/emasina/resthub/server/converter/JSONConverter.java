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
package lt.emasina.resthub.server.converter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import lt.emasina.resthub.server.cache.CcData;
import lt.emasina.resthub.server.handler.DataHandler;
import lt.emasina.resthub.server.query.Query;

import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.data.Reference;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

/**
 * JSONExporter
 * @author valdo
 */
public class JSONConverter implements DataConverter {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Representation convert(final DataHandler handler, final Reference ref, final CcData data) throws Exception {
        JSONObject obj = new JSONObject();
        final JSONArray arr = new JSONArray();
        Query query = handler.getQuery();
        
        if (handler.isPrintColumns()) {
            obj.put("cols", query.getColumnsJSON());
        }
        
        new DataVisitor(handler) {
            
            private JSONArray o;
            
            @Override
            public void startRow() {
                o = new JSONArray();
            }
            
            @Override
            public void visitCol() {
                switch (column.getType()) {
                    case DATE:
                        Calendar cal = (Calendar) value;
                        o.put(value != null ? DATE_FORMAT.format(cal.getTime()) : null);
                        break;
                    case CLOB:
                    case BLOB:
                        o.put(getLobReference(ref));
                        break;
                    default:
                        o.put(value);
                }
            }
            
            @Override
            public void endRow() {
                arr.put(o);
            }
            
        }.visit(data.getValue());
        
        obj.put("data", arr);
        
        return new JsonRepresentation(obj);
    }
    
}