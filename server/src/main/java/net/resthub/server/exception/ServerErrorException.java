package net.resthub.server.exception;

import lombok.extern.log4j.Log4j;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

/**
 * ClientException
 * @author valdo
 */
@Log4j
public class ServerErrorException extends ResourceException {

	private static final long serialVersionUID = 1L;

	public ServerErrorException(Throwable cause) {
        this(Status.SERVER_ERROR_INTERNAL, cause);
    }
    
    public ServerErrorException(Status status, Throwable cause) {
        super(status, cause.getMessage());
        log.error("Server error!", cause);
    }
    
}
