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
