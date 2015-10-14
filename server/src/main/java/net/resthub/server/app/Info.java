package net.resthub.server.app;

import net.resthub.server.exception.ServerErrorException;
import net.resthub.server.parser.check.CheckExpressionParser;
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
            
            JSONObject o = new JSONObject();
            o.put("query", q);
            
            getResponse().setEntity(new JsonRepresentation(o));           
            
        } catch (JSONException ex) {
            throw new ServerErrorException(ex);
        }
    }
        
}
