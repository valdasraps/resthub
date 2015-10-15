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
