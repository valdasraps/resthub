package lt.emasina.resthub.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.ConnectionFactory;
import lt.emasina.resthub.TableFactory;
import lt.emasina.resthub.factory.XmlFolderTableFactory;
import lt.emasina.resthub.factory.XmlResourceTableFactory;
import lt.emasina.resthub.model.MdTable;
import lt.emasina.resthub.support.TestConnectionFactory;
import lt.emasina.resthub.support.TestSqlTableFactory;
import lt.emasina.resthub.support.TcpTunnel;
import oracle.jdbc.OracleConnection;
import org.restlet.Component;
import org.restlet.data.Protocol;

@Log4j
public class ServerWorker {
    
    public static final String XML_RESOURCE = "/lt/emasina/server/xml/tables.xml";
    public static final String XML_FOLDER = "target/test/folder";
    public static final Path FOLDER = Paths.get(XML_FOLDER);
    private static final String TUNNEL_PORT = "15222";
    private static final Pattern URL_PATTERN = Pattern.compile("^([^:/]+):([0-9]+)/(.*)$");
    
    private final String url;
    private final String host;
    private final String port;
    private final String sid;
    private final String tunnel_url;
    private final String username;
    private final String password;
    
    private Component comp;
    
    @Getter
    private ConnectionFactory cf;
    
    private ExecutorService tunnelExecutor;
    
    private TcpTunnel tcp;
    
    public ServerWorker() {
        Properties testing = new Properties();
        try (InputStream is = TestConnectionFactory.class.getResourceAsStream("/testing.properties")) {
            testing.load(is);
        } catch (IOException ex) {
            log.fatal(ex);
        }
        if (System.getenv("TEST_DATABASE_URL") != null) {
            this.url = System.getenv("TEST_DATABASE_URL");
        } else {
            this.url = testing.getProperty("test.server.url");
        }
        this.username = testing.getProperty("test.server.user");
        this.password = testing.getProperty("test.server.passwd");
        Matcher m = URL_PATTERN.matcher(url);
        if (!m.matches()) throw new IllegalArgumentException(String.format("Wrong DB url? %s", url));
        host = m.group(1);
        port = m.group(2);
        sid = m.group(3);
        tunnel_url = "localhost:".concat(TUNNEL_PORT).concat("/").concat(sid);
    }
    
    public void startTunnel() {
    	this.tunnelExecutor = Executors.newSingleThreadExecutor();
    	this.tunnelExecutor.submit(new Runnable() {
            @Override
            public void run() {
                
                try {
                	tcp = new TcpTunnel(Integer.parseInt(TUNNEL_PORT), host, Integer.parseInt(port));
                	tcp.start();
                } catch (IOException ex) {
                    log.fatal(ex);
                }
            }
        });
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            log.error(ex);
        }
    }
    
    public void stopTunnel() throws InterruptedException {
        if (this.tunnelExecutor != null) {
            this.tunnelExecutor.shutdownNow();
            this.tunnelExecutor = null;
            
            tcp.stop();
            Thread.sleep(10000);
        }
    }
    
    public void startServer() throws Exception {
        
        // Cleaning up folder
        
        if (Files.exists(FOLDER)) {
            Files.walkFileTree(FOLDER, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                }

            });
        }
        
        // Setup ConnectionFactory
        
        if (tunnelExecutor != null) {
            cf = new TestConnectionFactory(tunnel_url, username, password);
        } else {
            cf = new TestConnectionFactory(url, username, password);
        }
        
        // Cleaning up views
        
        try (OracleConnection con = getCf().getConnection("default")) {
            
            TestSqlTableFactory tf = new TestSqlTableFactory();
            tf.init(getCf());
            try (Statement st = con.createStatement()) {
                for (MdTable t: tf.getTables()) {
                    st.execute("DROP VIEW ".concat(t.getName().concat("_SQLTEST")));
                }
            }
        }
        
        ServerAppConfig cfg = new ServerAppConfig();
        cfg.setUpdateInterval(10);
        cfg.setServiceVersion("1.11.11");
        
        ServerApp app = new ServerApp(cf, 
            new TableFactory.Builder()
                .add(new XmlResourceTableFactory(XML_RESOURCE))
                .add(new XmlFolderTableFactory(XML_FOLDER))
                .add(new TestSqlTableFactory())
                    .build(), cfg);
        comp = new Component();
        comp.getServers().add(Protocol.HTTP, 8112);
        comp.getDefaultHost().attach(app);
        comp.start();
    }
    
    public void stopServer() throws Exception {
        if (comp != null) {
            comp.stop();
            comp = null;
        }
    }
    
}
