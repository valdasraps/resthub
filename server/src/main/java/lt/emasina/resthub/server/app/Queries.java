package lt.emasina.resthub.server.app;

import java.net.MalformedURLException;
import lt.emasina.resthub.server.exception.ServerErrorException;
import lt.emasina.resthub.server.query.Query;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Options;

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
    
}
