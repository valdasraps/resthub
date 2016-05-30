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

import java.net.MalformedURLException;
import lt.emasina.resthub.server.exception.ServerErrorException;
import lt.emasina.resthub.server.query.Query;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.ResourceException;

/**
 * Queries
 * @author valdo
 */
public class Queries extends ServerBaseResource {

    @Options
    public void define() {
        addHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        addHeader("Access-Control-Allow-Headers", "Content-Type");
        addHeader("Content-Type", "application/json");
    }
    
    @Get
    public void describe() throws MalformedURLException {
        try {
            JSONObject ret = new JSONObject();
            for (Query qmd: qf.getQueries()) {
                ret.put(qmd.getQid().getId(), qmd.getReference(getHostRef(), null));
            }
            getResponse().setEntity(new JsonRepresentation(ret));
            
        } catch (JSONException ex) {
            throw new ServerErrorException(ex);
        }
    }
    
    @Delete
    public void remove() throws ResourceException {
        for (Query qmd: qf.getQueries()) {
            qf.removeQuery(qmd.getQid().getId());
        }
        getResponse().setEntity(new StringRepresentation(Boolean.toString(qf.getQueries().isEmpty())));
    }
    
}
