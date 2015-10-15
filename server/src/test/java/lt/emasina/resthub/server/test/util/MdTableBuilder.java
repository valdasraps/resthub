package lt.emasina.resthub.server.test.util;

import java.util.concurrent.atomic.AtomicLong;
import lt.emasina.resthub.model.MdColumn;
import lt.emasina.resthub.model.MdParameter;
import lt.emasina.resthub.model.MdTable;
import lt.emasina.resthub.model.MdType;

/**
 * TableMdBuilder
 * @author valdo
 */
public class MdTableBuilder {

    private static final AtomicLong ID = new AtomicLong();
    private final MdTable t;
    
    public MdTableBuilder(String namespace, String name, String connectionName, String sql) {
        t = new MdTable();
        t.setId(ID.incrementAndGet());
        t.setNamespace(namespace);
        t.setName(name);
        t.setConnectionName(connectionName);
        t.setSql(sql);
    }
    
    public MdTableBuilder column(String name, MdType type) {
        MdColumn c = new MdColumn();
        c.setId(ID.incrementAndGet());
        c.setName(name);
        c.setType(type);
        c.setTable(t);
        t.getColumns().add(c);
        return this;
    }

    public MdTableBuilder parameter(String name, MdType type, Boolean array) {
        MdParameter p = new MdParameter();
        p.setId(ID.incrementAndGet());
        p.setArray(array);
        p.setName(name);
        p.setType(type);
        p.setTable(t);
        t.getParameters().add(p);
        return this;
    }

    public MdTableBuilder cacheTime(int ct) {
        t.setCacheTime(ct);
        return this;
    }
    
    public MdTableBuilder rowsLimit(int rl) {
        t.setRowsLimit(rl);
        return this;
    }
    
    public MdTable build() {
        return t;
    }
    
}
