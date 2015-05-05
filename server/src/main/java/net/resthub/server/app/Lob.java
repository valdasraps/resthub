package net.resthub.server.app;


import net.resthub.model.MdColumn;
import net.resthub.model.MdType;
import net.resthub.server.cache.CacheStats;
import net.resthub.server.exception.ServerErrorException;

import org.restlet.data.MediaType;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.ResourceException;

import net.resthub.server.converter.LobConverter;
import net.resthub.server.exporter.LobExporter;
import net.resthub.server.handler.LobHandler;
import static net.resthub.server.util.ClientAssert.badRequestIfNot;

/**
 * Lob
 * @author valdo
 */
public class Lob extends PagedData {
    
    private LobHandler handler;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();       
        
        Integer row = getAttr(Integer.class, "row");
        badRequestIfNot(row != null && row >= 0, "Row attribute must be set to the valid row number");
        
        Integer col = getAttr(Integer.class, "col");
        badRequestIfNot(col != null && col >= 0 && col < query.getColumns().size(), "Column attribute must be set to the valid column number");
        
        MdColumn column = query.getColumns().get(col);
        badRequestIfNot(column.getType() == MdType.BLOB || column.getType() == MdType.CLOB, "Column type must be BLOB or CLOB found %s", column.getType().name());
        
        this.handler = rf.createLobHandler(query, getQuery());
        handler.setPerPage(perPage);
        handler.setPage(page);
        handler.setColumn(col);
        handler.setRow(row);
        
        String mt = getParam(String.class, "mt");
        if (mt != null) {
            this.handler.setMediaType(MediaType.valueOf(mt));
        }
        
    }
    
    @Options
    public void define() {
        addHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        addHeader("Access-Control-Allow-Headers", "Content-Type");     
        addHeader("Content-Type", this.handler.getMediaType().toString());
    }
    
    @Get
    public void data() throws ResourceException {
        CacheStats stats = handler.getCacheStats();
        
        // Process "If-Modified-Since"
        if (respondNotModified(stats)) {
            addExpiresHeader(stats);
            return;
        }

        // Finally, return data
        try {
            
            LobExporter dexp = qf.getExporter(handler);
            getResponse().setEntity(new LobConverter().convert(handler, dexp.getValue()));
            addExpiresHeader(stats);

        } catch (Exception ex) {
            if (ResourceException.class.isAssignableFrom(ex.getClass())) {
                throw (ResourceException) ex;
            }
            throw new ServerErrorException(ex);
        }
  
    }

}