package lt.emasina.resthub.support;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
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
    
    private final Map<String, ConnectionDescription> conections = Maps.newHashMap();
    private final Properties testing = new Properties();

    public TestConnectionFactory() throws IOException {
        
        try (InputStream is = TestConnectionFactory.class.getResourceAsStream("/testing.properties")) {
            testing.load(is);
        }
        
        String url = System.getenv("TEST_DATABASE_URL");
        if (url == null) {
            url = testing.getProperty("test.server.url");
        }
        
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException ex) {
            log.error("Error while retrieving OracleDriver", ex);
        }
        conections.put("default",  new ConnectionDescription("jdbc:oracle:thin:@" + url, 
                testing.getProperty("test.server.user"), testing.getProperty("test.server.passwd")));
    }
    
    @Override
    public Collection<String> getConnectionNames() {
        return conections.keySet();
    }
    
    @Override
    public OracleConnection getConnection(String name) throws SQLException {
        if (conections.containsKey(name)) {
            return conections.get(name).getConnection();
        }
        throw new IllegalArgumentException(String.format("Connection %s not found.", name));
    }

    @Override
    public String getUrl(String name) {
        return conections.get(name).getUrl();
    }

    @Override
    public String getUsername(String name) {
        return conections.get(name).getUser();
    }

    @Override
    public String getPassword(String name) {
        return conections.get(name).getPasswd();
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
