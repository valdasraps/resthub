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
import java.io.OutputStream;
import lt.emasina.resthub.server.cache.CcLob;
import lt.emasina.resthub.server.exception.ClientErrorException;
import lt.emasina.resthub.server.handler.LobHandler;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 * LobConverter class
 * @author valdo
 */
public class LobConverter {

    public Representation convert(final LobHandler handler, final CcLob data) throws Exception {
        
        if (data.getValue() == null) {
            return new EmptyRepresentation();
        }
        
        switch (handler.getMdColumn().getType()) {
            case BLOB:
                
                return new OutputRepresentation(handler.getMediaType()) {
                    
                    @Override
                    public void write(OutputStream out) throws IOException {
                        out.write(data.getValue());
                    }
                };
                
            case CLOB:
                
                return new StringRepresentation(new String(data.getValue()), handler.getMediaType());
                
            default:
                
                throw new ClientErrorException(Status.CLIENT_ERROR_BAD_REQUEST);
                
        }
        
    }
    
}
