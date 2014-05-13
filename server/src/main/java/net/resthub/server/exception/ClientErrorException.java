package net.resthub.server.exception;

import lombok.extern.log4j.Log4j;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

/**
 * ClientException
 * @author valdo
 */
@Log4j
public class ClientErrorException extends ResourceException {

	private static final long serialVersionUID = 1L;

	public ClientErrorException(Status status) {
        super(status);
    }

    public ClientErrorException(Status status, Throwable cause) {
        this(status, cause.getMessage());
    }
    
    public ClientErrorException(Status status, String message, Object... args) {
        this(status, String.format(message, args));
    }
    
    public ClientErrorException(Status status, String message) {
        super(status, message);
        log.warn(message);
    }
    
}
