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
package lt.emasina.resthub.server.handler;

import com.google.inject.assistedinject.Assisted;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;
import lt.emasina.resthub.server.cache.CcHisto;
import lt.emasina.resthub.server.exporter.HistoExporter;
import lt.emasina.resthub.server.query.Query;
import org.restlet.data.Form;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import lt.emasina.resthub.model.MdColumn;
import org.hibernate.type.Type;

@Getter
public class HistoHandler extends Handler<CcHisto, HistoExporter> {

    private static final int DEFAULT_BINS = 10;
            
    @Setter
    private MdColumn column;
    
    @Setter
    private int bins = DEFAULT_BINS;

    @Setter
    @Getter
    private double minValue;

    @Setter
    @Getter
    private double maxValue;
    
    private final Map<String, Type> columns = new LinkedHashMap<>();

    @Inject
    public HistoHandler(@Assisted Query query, @Assisted Form form) throws ResourceException {
        super(query, form);
    }

    @Override
    public HistoExporter createExporter() {
        return rf.createHistoExporter(this);
    }

    @Override
    protected List getIdParts() {
        List parts = new ArrayList();
        parts.add(column.getName());
        parts.add(bins);
        parts.add(minValue);
        parts.add(maxValue);
        return parts;
    }
    
}
