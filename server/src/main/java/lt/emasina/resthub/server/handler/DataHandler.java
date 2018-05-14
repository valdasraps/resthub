/*
 * #%L
 * server
 * %%
 * Copyright (C) 2012 - 2015 valdasraps
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

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lt.emasina.resthub.server.cache.CcData;
import lt.emasina.resthub.server.exporter.DataExporter;
import lt.emasina.resthub.server.query.Query;

import org.restlet.data.Form;
import org.restlet.resource.ResourceException;

import com.google.inject.assistedinject.Assisted;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.restlet.data.Reference;

public class DataHandler extends PagedHandler<CcData, DataExporter> {

    @Getter
    @Setter
    private boolean printColumns;

    @Getter
    @Setter
    private boolean inlineClobs;
    
    @Inject
    public DataHandler(@Assisted Query query, @Assisted Form form) throws ResourceException {
    	super(query, form);
    }

    @Override
    public DataExporter createExporter() {
        return rf.createDataExporter(this);
    }
    
    public URL getReference(Reference ref, Object... parts) {
        List<Object> myparts = new ArrayList<>();
        if (getPerPage() != null && getPage() != null) {
            myparts.add("page");
            myparts.add(getPerPage());
            myparts.add(getPage());
        }
        myparts.addAll(Arrays.asList(parts));
        return getQuery().getReference(ref, getQueryString(), myparts.toArray());
    }

}