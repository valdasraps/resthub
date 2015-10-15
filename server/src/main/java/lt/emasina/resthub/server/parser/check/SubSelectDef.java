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
