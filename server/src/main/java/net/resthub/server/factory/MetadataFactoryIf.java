package net.resthub.server.factory;

import com.google.inject.persist.Transactional;
import java.util.Collection;
import net.resthub.server.table.TableId;
import net.resthub.server.table.ServerTable;

/**
 * MetadataFactoryIf
 * @author valdo
 */
public interface MetadataFactoryIf {

    ServerTable getTable(TableId id);

    Collection<ServerTable> getTables();

    boolean hasTable(TableId id);

    @Transactional
    void refresh() throws Exception;
    
}
