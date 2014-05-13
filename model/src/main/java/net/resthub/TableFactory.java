package net.resthub;

import java.util.Date;
import java.util.List;
import net.resthub.model.MdTable;

/**
 * Table factory interface
 * @author audrius
 */
public interface TableFactory extends AutoCloseable{
    
    public boolean isRefreshable();
    public boolean isRefresh(Date lastUpdate);
    public List<MdTable> getTables() throws Exception;
    
}
