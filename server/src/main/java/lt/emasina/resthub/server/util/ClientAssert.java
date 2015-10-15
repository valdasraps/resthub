package lt.emasina.resthub.server.util;

import lt.emasina.resthub.server.exception.ClientErrorException;
import org.restlet.data.Status;

/**
 * ClientAssert
 * @author valdo
 */
public class ClientAssert {

    public static void badRequestIfNot(boolean value, String message, Object... args) throws ClientErrorException {
        if (!value) {
            throw new ClientErrorException(Status.CLIENT_ERROR_BAD_REQUEST, message, args);
        }
    }
    
}
