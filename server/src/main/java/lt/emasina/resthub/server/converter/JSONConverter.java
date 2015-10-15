package lt.emasina.resthub.server.converter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import lt.emasina.resthub.server.cache.CcData;
import lt.emasina.resthub.server.handler.DataHandler;
import lt.emasina.resthub.server.query.Query;

import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.data.Reference;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

/**
 * JSONExporter
 * @author valdo
 */
public class JSONConverter implements DataConverter {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("y-M-d H:ms:d");

    @Override
    public Representation convert(final DataHandler handler, final Reference ref, final CcData data) throws Exception {
        JSONObject obj = new JSONObject();
        final JSONArray arr = new JSONArray();
        Query query = handler.getQuery();
        
        if (handler.isPrintColumns()) {
            obj.put("cols", query.getColumnsJSON());
        }
        
        new DataVisitor(handler) {
            
            private JSONArray o;
            
            @Override
            public void startRow() {
                o = new JSONArray();
            }
            
            @Override
            public void visitCol() {
                switch (column.getType()) {
                    case DATE:
                        o.put(value != null ? DATE_FORMAT.format((Date) value) : null);
                        break;
                    case CLOB:
                    case BLOB:
                        o.put(getLobReference(ref));
                        break;
                    default:
                        o.put(value);
                }
            }
            
            @Override
            public void endRow() {
                arr.put(o);
            }
            
        }.visit(data.getValue());
        
        obj.put("data", arr);
        
        return new JsonRepresentation(obj);
    }
    
}