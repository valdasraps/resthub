package net.resthub.server.handler;

import javax.inject.Inject;

import net.resthub.server.cache.CcLob;
import net.resthub.server.exporter.LobExporter;
import net.resthub.server.query.Query;

import org.restlet.data.Form;
import org.restlet.resource.ResourceException;

import com.google.inject.assistedinject.Assisted;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import net.resthub.model.MdColumn;
import net.resthub.server.exception.ClientErrorException;
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
