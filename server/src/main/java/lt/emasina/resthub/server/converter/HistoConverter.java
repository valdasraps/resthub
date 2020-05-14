/*
 * #%L
 * server
 * %%
 * Copyright (C) 2012 - 2020 valdasraps
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
package lt.emasina.resthub.server.converter;

import java.util.List;
import java.util.Map;
import lt.emasina.resthub.server.cache.CcHisto;
import lt.emasina.resthub.server.handler.HistoHandler;
import org.hibernate.type.Type;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.data.Reference;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

/**
 * Convert Histo data to JSON representation.
 * @author valdo
 */
public class HistoConverter {

    public Representation convert(HistoHandler handler, Reference ref, CcHisto data) throws Exception {
        JSONObject root = new JSONObject();
        
        JSONArray cols = new JSONArray();
        Map<String, Type> allCols = handler.getColumns();
        allCols.remove("mean");
        allCols.remove("rms");
        for (String colname: allCols.keySet()) {
            cols.put(colname);
        }
        root.put("cols", cols);

        final JSONArray bins = new JSONArray();
        final List<Object[]> values = data.getValue();
        
        if (values != null) {
            for (int rowNumber = 0; rowNumber < values.size(); rowNumber++) {
                JSONArray val = new JSONArray(values.get(rowNumber));
                if (val.length() > 5) {
                    val.remove(val.length()-1);
                    val.remove(val.length()-1);
                }
                bins.put(val);
            }
        }
        
        root.put("bins", bins);

        JSONObject mean = new JSONObject();
        JSONObject rms = new JSONObject();

        if (values.size() > 5) {
            final JSONArray stats = new JSONArray();
            JSONArray val = new JSONArray(values.get(0));

            rms.put("rms", val.get(5));
            mean.put("mean", val.get(6));

            stats.put(rms);
            stats.put(mean);
            root.put("stats", stats);
        }

        return new JsonRepresentation(root);
    }
    
}
