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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lt.emasina.resthub.server.table.ServerTable;

/**
 * SubSelectDef
 * @author valdo
 */
@Getter
public class SubSelectDef extends SelectDef {

    private Map<String, SelectDef> tables = new HashMap<>();
    private List<SubSelectDef> children = new ArrayList<>();
    private Set<String> parameterNames = new HashSet<>();

    public SubSelectDef() {
        super(null);
    }

    public SubSelectDef(SelectDef parent) {
        super(parent);
    }

    public boolean hasAlias(String alias) {
        if (tables.containsKey(alias)) {
            return true;
        }
        
        for (SelectDef t: tables.values()) {
            if (t instanceof SubSelectDef) {
                if (((SubSelectDef) t).hasAlias(alias)) {
                    return true;
                }
            }
        }

        for (SubSelectDef t: children) {
            if (t.hasAlias(alias)) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean isSameConnectionName(String connectionName) {
        for (SelectDef t: tables.values()) {
            if (!t.isSameConnectionName(connectionName)) {
                return false;
            }
        }
        for (SubSelectDef t: children) {
            if (!t.isSameConnectionName(connectionName)) {
                return false;
            }
        }
        return true;
    }

    public ServerTable getServerTable(String alias) {
        
        if (tables.containsKey(alias)) {
            SelectDef td = tables.get(alias);
            if (td instanceof TableDef) {
                return ((TableDef) td).getTableMd();
            }
        }
        
        for (SelectDef td: tables.values()) {
            if (td instanceof SubSelectDef) {
                ServerTable tableMd = ((SubSelectDef) td).getServerTable(alias);
                if (tableMd != null) {
                    return tableMd;
                }
            }
        }
        
        for (SubSelectDef ss: children) {
            ServerTable tableMd = ss.getServerTable(alias);
            if (tableMd != null) {
                return tableMd;
            }
        }
        
        return null;
    }
    
}
