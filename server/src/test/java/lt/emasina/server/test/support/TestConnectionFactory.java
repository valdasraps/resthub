package lt.emasina.server.test.support;

import com.google.common.collect.Maps;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.ConnectionFactory;
import oracle.jdbc.OracleConnection;

/**
 * ConnectionFactory
 * @author valdo
 */
@Log4j
@Singleton
public class TestConnectionFactory implements ConnectionFactory {
    
    private static final Map<String, ConnectionDescription> CONNECTIONS = Maps.newHashMap();
    static {
        CONNECTIONS.put("default",  new ConnectionDescription("jdbc:oracle:thin:@localhost:1521/XE", "resthub_test", "test"));
    }

    public TestConnectionFactory() {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException ex) {
            log.error("Error while retrieving OracleDriver", ex);
        }
    }
    
    @Override
    public Collection<String> getConnectionNames() {
        return CONNECTIONS.keySet();
    }
    
    @Override
    public OracleConnection getConnection(String name) throws SQLException {
        if (CONNECTIONS.containsKey(name)) {
            return CONNECTIONS.get(name).getConnection();
        }
        throw new IllegalArgumentException(String.format("Connection %s not found.", name));
    }

    @Override
    public String getUrl(String name) {
        return CONNECTIONS.get(name).getUrl();
    }

    @Override
    public String getUsername(String name) {
        return CONNECTIONS.get(name).getUser();
    }

    @Override
    public String getPassword(String name) {
        return CONNECTIONS.get(name).getPasswd();
    }

    @Getter
    @RequiredArgsConstructor
    private static class ConnectionDescription {
        
        private final String url;
        private final String user;
        private final String passwd;
        
        public OracleConnection getConnection() throws SQLException {
            log.debug(String.format("Connecting to %s...", url));
            return (OracleConnection) DriverManager.getConnection(url, user, passwd);
        }
        
    }
    
}
