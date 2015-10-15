package lt.emasina.resthub.server.factory;

import com.google.inject.persist.Transactional;
import java.util.Collection;
import lt.emasina.resthub.server.table.TableId;
import lt.emasina.resthub.server.table.ServerTable;

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
