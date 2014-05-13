package net.resthub.server.parser.check;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * TableDef
 * @author valdo
 */
@Getter
@RequiredArgsConstructor
public abstract class SelectDef {

    private final SelectDef parent;
    
    private final List<String> columns = new ArrayList<>();
    
    public abstract boolean isSameConnectionName(String connectionName);

    public SubSelectDef getTop() {
        if (parent == null) {
            return (SubSelectDef) this;
        } else {
            return parent.getTop();
        }
    }
    
}
