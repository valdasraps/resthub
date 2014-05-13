package net.resthub.server.app;

import java.io.IOException;
import net.resthub.server.exception.ClientErrorException;
import net.resthub.server.exception.ServerErrorException;
import net.resthub.exception.QueryException;
import org.json.JSONException;
import org.json.JSONObject;
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

    private net.resthub.server.query.Query queryMd;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        this.queryMd = getQueryMd(false);
    }
    
    @Options
    public void define() {
        addHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, OPTIONS");
        addHeader("Access-Control-Allow-Headers", "Content-Type");
        addHeader("Content-Type", "application/json");
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
