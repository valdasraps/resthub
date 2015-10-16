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
package lt.emasina.resthub.server.parser.check;

import lombok.Getter;
import lt.emasina.resthub.server.table.ServerTable;
import lt.emasina.resthub.model.MdColumn;

/**
 * SimpleTableDef
 * @author valdo
 */
@Getter
public class TableDef extends SelectDef {

    private final ServerTable tableMd;

    public TableDef(ServerTable tableMd, SelectDef parent) {
        super(parent);
        this.tableMd = tableMd;
        for(MdColumn pc: tableMd.getTable().getColumns()) {
            getColumns().add(pc.getName());
        }
    }

    @Override
    public boolean isSameConnectionName(String connectionName) {
        return connectionName.equals(tableMd.getTable().getConnectionName());
    }
    
}
