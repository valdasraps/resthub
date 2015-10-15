package lt.emasina.resthub.server.converter;

import lt.emasina.resthub.server.cache.CcData;
import lt.emasina.resthub.server.handler.DataHandler;

import org.restlet.data.Reference;
import org.restlet.representation.Representation;

/**
 * Interface DataConverter
 * @author valdo
 */
public interface DataConverter {
    
    Representation convert(DataHandler handler, final Reference ref, CcData data) throws Exception;
    
}
