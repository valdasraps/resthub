/*
 * #%L
 * server
 * %%
 * Copyright (C) 2012 - 2016 valdasraps
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
package lt.emasina.resthub.server;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.service.StatusService;

public class ResponseStatusService extends StatusService {

    @Override
    public Representation getRepresentation(Status status, Request request, Response response) {
        if (status.isError()) {
            StringRepresentation ret = new StringRepresentation(status.getDescription());
            ret.setMediaType(MediaType.TEXT_PLAIN);
            return ret;
        }
        return super.toRepresentation(status, request, response);
    }

}
