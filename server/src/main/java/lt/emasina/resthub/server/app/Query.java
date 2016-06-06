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
package lt.emasina.resthub.server.app;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import lt.emasina.resthub.server.exception.ClientErrorException;
import lt.emasina.resthub.server.exception.ServerErrorException;
import lt.emasina.resthub.exception.QueryException;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

/**
 * Queries
 * @author valdo
 */
public class Query extends ServerBaseResource {

    private lt.emasina.resthub.server.query.Query queryMd;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        this.queryMd = getQueryMd(false);
    }
    
    @Options
    public void define() {
        getResponse().setAccessControlAllowMethods(new HashSet<>(Arrays.asList(Method.DELETE, Method.GET, Method.OPTIONS, Method.POST)));
        getResponse().setAccessControlAllowHeaders(Collections.singleton("Content-Type"));
        addHeader("X-Content-Types", "application/json");
    }
    
    @Post("text")
    public void save(Representation entity) throws ResourceException, IOException {
        String sql = entity.getText();
        if (sql != null) {
            try {
                String id = qf.createQuery(sql);
                getResponse().setEntity(new StringRepresentation(id));
            } catch (QueryException ex) {
                throw new ClientErrorException(Status.CLIENT_ERROR_BAD_REQUEST, ex.getMessage());
            }
        } else {
            throw new ClientErrorException(Status.CLIENT_ERROR_BAD_REQUEST, "Query missing?");
        }        
    }
    
    @Get
    public void describe() {
        if (queryMd == null) {
            throw new ClientErrorException(Status.CLIENT_ERROR_BAD_REQUEST, "Query ID missing?");
        }
        try {
            JSONObject o = queryMd.getJSON(getHostRef(), verbose);
            getResponse().setEntity(new JsonRepresentation(o));           
        } catch (ResourceException | JSONException ex) {
            throw new ServerErrorException(ex);
        }
    }
    
    @Delete
    public void remove() throws ResourceException {
        Boolean result = false;
        if (queryMd != null) {
            result = qf.removeQuery(queryMd.getQid().getId());
        }
        getResponse().setEntity(new StringRepresentation(result.toString()));
    }
    
}
