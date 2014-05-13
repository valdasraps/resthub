package net.resthub.server.converter;

import net.resthub.server.cache.CcData;
import net.resthub.server.handler.DataHandler;

import org.restlet.data.Reference;
import org.restlet.representation.Representation;

/**
 * Interface DataConverter
 * @author valdo
 */
public interface DataConverter {
    
    Representation convert(DataHandler handler, final Reference ref, CcData data) throws Exception;
    
}
