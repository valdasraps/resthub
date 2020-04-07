package lt.emasina.resthub.debug;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lt.emasina.resthub.ConnectionFactory;
import lt.emasina.resthub.TableFactory;
import lt.emasina.resthub.model.MdTable;
import lt.emasina.resthub.server.ServerApp;
import lt.emasina.resthub.server.ServerAppConfig;
import oracle.jdbc.OracleConnection;
import org.restlet.Component;
import org.restlet.data.Protocol;

public class DebugServer {

    // Setup these constants before running!
    /*
    Prisijungimai pries BIN funkcinoaluma

    private static final String URL = "jdbc:oracle:thin:@oracle-mif:1521/xe";
    private static final String USER = "RESTHUB_TEST";
    private static final String PASSWD = "tesT2018";

     */

    private static final String URL = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String USER = "RESTHUB_TEST";
    private static final String PASSWD = "testing";


    private static final String NAMESPACE = "RESTHUB_TEST";
    private static final String NAME = "TEXTS";
    private static final String SQL = "select * from TEXTS";

/*
Kodas testavimui pries bin funkcionaluma


    private static final String NAMESPACE = "RESTHUB_TEST";
    private static final String NAME = "CUSTOMER";
    private static final String SQL = "select count(1) as c from CUSTOMER";

 */


    //private static final String URL = "jdbc:oracle:thin:@cmsr_lb";
    //private static final String USER = "cms_dqm_run_registry_off_r";
    //private static final String PASSWD = "";
    //private static final String NAMESPACE = "rr_global";
    //private static final String NAME = "runs";
    //private static final String SQL = "select * from RR3_RUS_ROW_GLOBAL where RR3_VER_PKG.GOTO_TAG_R('LATEST') = 1";
    // Thats it!
    private static final String CONNECTION_NAME = "test";

    private final Component comp;

    public DebugServer() throws Exception {

        ServerApp app = new ServerApp(new ConnectionFactory() {

            @Override
            public Collection<String> getConnectionNames() {
                return Collections.singletonList(CONNECTION_NAME);
            }

            @Override
            public OracleConnection getConnection(String name) throws SQLException {
                return (OracleConnection) DriverManager.getConnection(URL, USER, PASSWD);
            }

            @Override
            public String getUrl(String name) {
                return URL;
            }

            @Override
            public String getUsername(String name) {
                return USER;
            }

            @Override
            public String getPassword(String name) {
                return PASSWD;
            }
        },
                new TableFactory() {

            private boolean refresh = true;

            @Override
            public boolean isRefresh() {
                if (refresh) {
                    refresh = false;
                    return true;
                }
                return false;
            }

            @Override
            public List<MdTable> getTables() throws Exception {
                MdTable t = new MdTable();

                t.setNamespace(NAMESPACE);
                t.setName(NAME);
                t.setConnectionName(CONNECTION_NAME);
                t.setSql(SQL);

                return Collections.singletonList(t);
            }

            @Override
            public void close() throws Exception {
            }

        },
                new ServerAppConfig());
        comp = new Component();
        comp.getServers().add(Protocol.HTTP, 8112);
        comp.getDefaultHost().attach(app);
        comp.start();
    }

    public static void main(String[] args) throws Exception {
        Class.forName("oracle.jdbc.OracleDriver");
        System.setProperty("oracle.net.tns_admin", "/etc");
        DebugServer debugServer = new DebugServer();
    }

}
