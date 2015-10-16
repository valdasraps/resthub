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
package lt.emasina.resthub.server.exception;

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
