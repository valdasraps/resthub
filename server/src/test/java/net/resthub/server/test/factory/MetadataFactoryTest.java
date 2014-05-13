package net.resthub.server.test.factory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import net.resthub.model.MdType;
import net.resthub.server.factory.MetadataFactoryIf;
import net.resthub.server.factory.ResourceFactory;
import net.resthub.server.table.TableId;
import net.resthub.server.table.ServerTable;
import net.resthub.server.test.util.MdTableBuilder;

/**
 * MetadataFactoryTest
 * @author valdo
 */
public class MetadataFactoryTest implements MetadataFactoryIf {

    private final Map<TableId, ServerTable> tables = new ConcurrentHashMap<>();
    
    @Inject
    public MetadataFactoryTest(ResourceFactory rf) {
        tables.put(new TableId("test", "customer"), rf.create(
                        new MdTableBuilder("test", "customer", "test", "select * from customer")
                            .column("id", MdType.NUMBER)
                            .column("name", MdType.STRING)
                            .column("country", MdType.STRING)
                            .build()));
        tables.put(new TableId("test", "product"), rf.create(
                        new MdTableBuilder("test", "product", "test", "select * from product")
                            .column("id", MdType.NUMBER)
                            .column("name", MdType.STRING)
                            .column("brand", MdType.STRING)
                            .build()));
        tables.put(new TableId("test", "customer_with_param"), rf.create(
                        new MdTableBuilder("test", "customer_with_param", "test", "select * from customer where id = :id")
                            .column("id", MdType.NUMBER)
                            .column("name", MdType.STRING)
                            .column("country", MdType.STRING)
                            .parameter("id", MdType.NUMBER, Boolean.FALSE)
                            .build()));
        tables.put(new TableId("test", "customer_with_array_param"), rf.create(
                        new MdTableBuilder("test", "customer_with_array_param", "test", "select * from customer where id in (:ids)")
                            .column("id", MdType.NUMBER)
                            .column("name", MdType.STRING)
                            .column("country", MdType.STRING)
                            .parameter("ids", MdType.NUMBER, Boolean.TRUE)
                            .build()));
    }
    
    @Override
    public ServerTable getTable(TableId id) {
        return tables.get(id);
    }

    @Override
    public Collection<ServerTable> getTables() {
        return tables.values();
    }

    @Override
    public boolean hasTable(TableId id) {
        return tables.containsKey(id);
    }

    @Override
    public void refresh() {  }

}
