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
import lt.emasina.resthub.server.exception.ClientErrorException;
import lt.emasina.resthub.server.exception.ServerErrorException;
import lt.emasina.resthub.server.table.TableId;
import org.json.JSONException;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.ResourceException;

public class BlacklistTable extends ServerBaseResource {

    private lt.emasina.resthub.server.table.ServerTable tmd;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        String tableNs = super.getAttr(String.class, "tableNs");
        String tableName = super.getAttr(String.class, "tableName");
        this.tmd = mf.getBlacklistTable(new TableId(tableNs, tableName));
        if (tmd == null) {
            throw new ClientErrorException(Status.CLIENT_ERROR_NOT_FOUND, "table [%s.%s] not found.", tableNs, tableName);
        }
    }
    
    @Options
    public void define() {
        getResponse().setAccessControlAllowMethods(new HashSet<>(Arrays.asList(Method.DELETE, Method.GET, Method.OPTIONS)));
        getResponse().setAccessControlAllowHeaders(Collections.singleton("Content-Type"));
        addHeader("X-Content-Types", "application/json");
    }

    @Get
    public void describe() throws ResourceException {
        try {
            getResponse().setEntity(new JsonRepresentation(tmd.getJSON(getHostRef(), verbose)));
        } catch (ResourceException | JSONException ex) {
            throw new ServerErrorException(ex);
        }
    }
    
    @Delete
    public void remove() {
        mf.removeBlacklistTable(tmd.getId());
    }
    
}
