package lt.emasina.resthub.server.table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lt.emasina.resthub.model.MdTable;

/**
 * ResourceId
 * @author valdo
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(of = {"namespace", "name"})
public class TableId {

    private final String namespace;
    private final String name;
    
    public TableId(MdTable t) {
        this(t.getNamespace(), t.getName());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.append(namespace).append(".").append(name).toString();
    }
    
}
