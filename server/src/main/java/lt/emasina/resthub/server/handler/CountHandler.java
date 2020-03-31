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

import javax.inject.Inject;

import lt.emasina.resthub.server.cache.CcCount;
import lt.emasina.resthub.server.exporter.CountExporter;
import lt.emasina.resthub.server.query.Query;

import org.restlet.data.Form;
import org.restlet.resource.ResourceException;

import com.google.inject.assistedinject.Assisted;
import java.util.Collections;
import java.util.List;

public class CountHandler extends Handler<CcCount, CountExporter> {

    @Inject
    public CountHandler(@Assisted Query query, @Assisted Form form) throws ResourceException {
        super(query, form);
    }

    @Override
    public CountExporter createExporter() {
        return rf.createCountExporter(this);
    }

    @Override
    protected List getIdParts() {
        return Collections.singletonList("count");
    }

}
