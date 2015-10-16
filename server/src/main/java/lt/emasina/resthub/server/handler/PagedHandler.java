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

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lt.emasina.resthub.server.query.Query;

import org.restlet.data.Form;
import org.restlet.resource.ResourceException;

import lt.emasina.resthub.server.cache.CcBase;
import lt.emasina.resthub.server.exporter.Exporter;

public abstract class PagedHandler<C extends CcBase<?>, E extends Exporter<C>> extends Handler<C,E> {
	
    @Getter
    @Setter
    private Integer perPage;
    
    @Getter
    @Setter
    private Integer page;
    
    public PagedHandler(Query query, Form form) throws ResourceException {
    	super(query, form);
    }
    
    @Override
    protected List getIdParts() {
        List parts = new ArrayList();
        parts.add(perPage);
        parts.add(page);
        return parts;
    }
    
}
