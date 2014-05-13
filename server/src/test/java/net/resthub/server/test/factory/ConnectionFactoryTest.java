package net.resthub.server.test.factory;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

import net.resthub.ConnectionFactory;
import oracle.jdbc.OracleConnection;

/**
 * ConnectionFactoryTest
 * @author valdo
 */
public class ConnectionFactoryTest implements ConnectionFactory {

	private static final long serialVersionUID = 1L;

	@Override
    public Collection<String> getConnectionNames() {
        return Collections.singleton("test");
    }

    @Override
    public OracleConnection getConnection(String name) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getUrl(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getUsername(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getPassword(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
