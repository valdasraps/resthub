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

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

import lt.emasina.resthub.model.MdColumn;
import lt.emasina.resthub.server.cache.CcData;
import lt.emasina.resthub.server.handler.DataHandler;
import lt.emasina.resthub.server.query.Query;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;

/**
 * JSONExporter
 * @author valdo
 */
public class CSVConverter implements DataConverter {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private static final String FS = ",";
    private static final String NL = "\n";
    
    private static final Pattern SYMBOLS_TO_WRAP = Pattern.compile("[ ,;\"]");
    
    private static String escapeStr(String s) {
        String s1 = s.replaceAll("\"", "\"\"").replaceAll("\n", "").replaceAll("\t", "");
        if (SYMBOLS_TO_WRAP.matcher(s1).find()) {
            s1 = "\"".concat(s1).concat("\"");
        }
        return s1;
    }
    
    @Override
    public Representation convert(DataHandler handler, final Reference ref, CcData data) throws Exception {
        final StringBuilder sb = new StringBuilder();
        Query query = handler.getQuery();
        
        sb.setLength(0);
        String sep = "";
        for (MdColumn c : query.getColumns()) {
            sb.append(sep).append(escapeStr(c.getCName()));
            sep = FS;
        }
        sb.append(NL);
        
        new DataVisitor(handler) {
            
            @Override
            public void startRow() { }
            
            @Override
            public void visitCol() {
                
                if (colNumber > 0) {
                    sb.append(FS);
                }
                
                if (value != null) { 
                    switch (column.getType()) {
                        case DATE:
                            Calendar cal = (Calendar) value;
                            sb.append(DATE_FORMAT.format(cal.getTime())); 
                            break;
                        case STRING:
                            sb.append(escapeStr((String) value)); 
                            break;
                        case BLOB:
                        case CLOB:
                            sb.append("[file]"); 
                            break;
                        default:
                            sb.append(value); 
                    }
                }
            }
            
            @Override
            public void endRow() {
                sb.append(NL);
            }
            
        }.visit(data.getValue());
        
        return new WriterRepresentation(MediaType.TEXT_CSV) {
            @Override
            public void write(Writer writer) throws IOException {
                writer.write(sb.toString());
            }
        };
        
    }
    
}