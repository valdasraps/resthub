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

import lt.emasina.resthub.server.exception.ServerErrorException;
import lt.emasina.resthub.server.parser.check.CheckExpressionParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.ResourceException;

/**
 * Queries
 * @author valdo
 */
public class Info extends ServerBaseResource {

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
    }
    
    @Options
    public void define() {
        addHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        addHeader("Access-Control-Allow-Headers", "Content-Type");
        addHeader("Content-Type", "application/json");
    }
    
    @Get
    public void info() {
        try {
            
            JSONObject q = new JSONObject();
            q.put("allowed_functions", new JSONArray(CheckExpressionParser.getAllowedFunctions()));

            JSONObject v = new JSONObject();
            v.put("service", cfg.getServiceVersion());
            v.put("resthub", cfg.getResthubVersion());
            
            JSONObject o = new JSONObject();
            o.put("query", q);
            o.put("version", v);
            
            getResponse().setEntity(new JsonRepresentation(o));           
            
        } catch (JSONException ex) {
            throw new ServerErrorException(ex);
        }
    }
        
}
