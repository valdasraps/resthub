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

import com.google.inject.assistedinject.Assisted;
import lombok.Getter;
import lombok.Setter;
import lt.emasina.resthub.server.cache.CcBin;
import lt.emasina.resthub.server.exporter.BinExporter;
import lt.emasina.resthub.server.query.Query;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;
import java.util.List;

public class BinHandler extends PagedHandler<CcBin, BinExporter> {

    @Getter
    @Setter
    private static final MediaType JSON_MEDIA_TYPE = MediaType.APPLICATION_JSON;


    @Setter
    @Getter
    private String binCol;

    @Getter
    @Setter
    private Integer row;

    @Getter
    @Setter
    private Integer column;


    @Inject
    public BinHandler(@Assisted Query query, @Assisted Form form) throws ResourceException {
        super(query, form);
    }

    @Override
    public BinExporter createExporter() {

        return rf.createBinExporter(this);

    }

    @Override
    protected List getIdParts() {
        List parts = super.getIdParts();
        parts.add(column);
        parts.add(row);
        return parts;
    }
}
