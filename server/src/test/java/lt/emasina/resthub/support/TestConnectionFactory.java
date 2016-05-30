package lt.emasina.resthub.support;

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
    
    private final Map<String, ConnectionDescription> conections = Maps.newHashMap();

    public TestConnectionFactory() {
        String url = System.getenv("TEST_DATABASE_URL");
        if (url == null) url = "oracle-cern.mif:1521/cerndev";
        //url = "localhost:1521/xe";
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException ex) {
            log.error("Error while retrieving OracleDriver", ex);
        }
        conections.put("default",  new ConnectionDescription("jdbc:oracle:thin:@" + url, "resthub_test", "test"));
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
