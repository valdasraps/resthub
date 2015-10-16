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
package lt.emasina.resthub.server.handler;

import javax.inject.Inject;

import lt.emasina.resthub.server.cache.CcLob;
import lt.emasina.resthub.server.exporter.LobExporter;
import lt.emasina.resthub.server.query.Query;

import org.restlet.data.Form;
import org.restlet.resource.ResourceException;

import com.google.inject.assistedinject.Assisted;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lt.emasina.resthub.model.MdColumn;
import lt.emasina.resthub.server.exception.ClientErrorException;
import org.restlet.data.MediaType;
import org.restlet.data.Status;

public class LobHandler extends PagedHandler<CcLob, LobExporter> {

    private static final MediaType BLOB_MEDIA_TYPE = MediaType.APPLICATION_ALL;
    private static final MediaType CLOB_MEDIA_TYPE = MediaType.TEXT_PLAIN;
    
    @Getter
    @Setter
    private Integer row;
    
    @Getter
    @Setter
    private Integer column;
    
    @Setter
    private MediaType mediaType;
    
    @Inject
    public LobHandler(@Assisted Query query, @Assisted Form form) throws ResourceException {
    	super(query, form);
    }

    @Override
    public LobExporter createExporter() {
        return rf.createLobExporter(this);
    }
    
    public MdColumn getMdColumn() {
        return getQuery().getColumns().get(column);
    }
    
    public MediaType getMediaType() {
        if (mediaType == null) {
            switch (getMdColumn().getType()) {
                case BLOB:
                    mediaType = BLOB_MEDIA_TYPE;
                    break;
                case CLOB:
                    mediaType = CLOB_MEDIA_TYPE;
                    break;
                default:
                    throw new ClientErrorException(Status.CLIENT_ERROR_BAD_REQUEST);
            }
        }
        return mediaType;
    }
    
    @Override
    protected List getIdParts() {
        List parts = super.getIdParts();
        parts.add(column);
        parts.add(row);
        return parts;
    }
	
}
