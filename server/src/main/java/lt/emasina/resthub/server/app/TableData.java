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

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import lt.emasina.resthub.model.MdTable;
import lt.emasina.resthub.server.table.ServerTable;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.ResourceException;

public class TableData extends ServerBaseResource {

    private static final String SQL = "select * from %s.%s t";
    private ServerTable tmd;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        this.tmd = getTableMd(true);
    }
    
    @Options
    public void define() {
        getResponse().setAccessControlAllowMethods(new HashSet<>(Arrays.asList(Method.GET, Method.OPTIONS)));
        getResponse().setAccessControlAllowHeaders(Collections.singleton("Content-Type"));
        StringBuilder sb = new StringBuilder();
        for (MediaType mt: Data.SUPPORTED_TYPES) {
            sb.append(sb.length() > 0 ? "," : "").append(mt);
        }
        addHeader("X-Content-Types", sb.toString());
    }
    
    @Get
    public void data() throws ResourceException {
        MdTable t = tmd.getTable();
        String sql = String.format(SQL, t.getNamespace(), t.getName());
        String id = qf.createQuery(sql);
        String path = getReference().getPath().replaceFirst("/table/" + t.getNamespace() + "/" + t.getName() + "/", 
                                                            "/query/" + id + "/");
        URL ref = cfg.getReference(getHostRef(), getReference().getQuery(), path);
        getResponse().redirectTemporary(ref.toString());
    }
    
}
