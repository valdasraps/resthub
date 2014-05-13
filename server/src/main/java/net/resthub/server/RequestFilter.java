package net.resthub.server;

import net.resthub.server.app.BaseResource;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Filter;

/**
 * RequestFilter
 * @author valdo
 */
public class RequestFilter extends Filter {

    public RequestFilter(Context context) {
        super(context);
    }
    
    @Override
    protected void afterHandle(Request request, Response response) {
        BaseResource.addHeader(response, "Access-Control-Allow-Origin", "*");
        super.afterHandle(request, response);
    }

}
