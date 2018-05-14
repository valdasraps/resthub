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
package lt.emasina.resthub.server.app;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import javax.inject.Inject;

import lt.emasina.resthub.server.exporter.CountExporter;
import lt.emasina.resthub.server.factory.ResourceFactory;
import lt.emasina.resthub.server.handler.CountHandler;
import lt.emasina.resthub.server.query.Query;
import org.restlet.data.Method;

import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.ResourceException;

/**
 * Count
 * @author valdo
 */
public class Count extends ServerBaseResource {

    @Inject
    private ResourceFactory rf;
    
    private CountHandler handler;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        Query q = getQueryMd(true);
        this.handler = rf.createCountHandler(q, getQuery());
    }
    
    @Options
    public void define() {
        getResponse().setAccessControlAllowMethods(new HashSet<>(Arrays.asList(Method.GET, Method.OPTIONS)));
        getResponse().setAccessControlAllowHeaders(Collections.singleton("Content-Type"));
        addHeader(HEADER_CONTENT_TYPES, "text/plain");
    }

    @Get
    public void count() throws ResourceException {
        CountExporter de = qf.getExporter(handler);
        getResponse().setEntity(new StringRepresentation(de.getValue().getValue().toString()));     
    }
}