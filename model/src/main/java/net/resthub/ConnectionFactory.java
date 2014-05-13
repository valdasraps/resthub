package net.resthub;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import oracle.jdbc.OracleConnection;

/**
 * ConnectionFactory
 * @author valdo
 */
public interface ConnectionFactory extends Serializable {
    
    public Collection<String> getConnectionNames();
    public OracleConnection getConnection(String name) throws SQLException;
    public String getUrl(String name);
    public String getUsername(String name);
    public String getPassword(String name);
    
}
