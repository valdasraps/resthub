package lt.emasina.resthub.server;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import lt.emasina.resthub.support.TestConnectionFactory;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.ConnectionFactory;
import lt.emasina.resthub.TableFactory;
import lt.emasina.resthub.factory.XmlFolderTableFactory;
import lt.emasina.resthub.factory.XmlResourceTableFactory;
import lt.emasina.resthub.support.TestSqlTableFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.restlet.Component;
import org.restlet.data.Protocol;

/**
 *
 * @author valdo
 */
@Log4j
@Ignore
public class ServerSetup {

    public static final String HOST = "http://localhost:8112";
    protected static final String[] EXCLUDE_HEADERS = {"Date", "Expires", "Accept-Ranges", "Allow"};
    
    private static final String XML_RESOURCE = "/lt/emasina/server/xml/tables.xml";
    protected static final String XML_FOLDER = "target/test/folder";

    protected static final Path FOLDER = Paths.get(XML_FOLDER);
    
    protected static Component comp;
    protected static ConnectionFactory cf;
    
    @BeforeClass
    public static void startServer() throws Exception {
        
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
        
        ServerAppConfig cfg = new ServerAppConfig();
        cfg.setUpdateInterval(10);
        cfg.setServiceVersion("1.11.11");
        
        cf = new TestConnectionFactory();
        
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
    
    @AfterClass
    public static void stopServer() throws Exception {
        comp.stop();
    }
    
    protected static void deleteFile(String fileName) throws IOException {
        Path file = FOLDER.resolve(fileName); 
        Files.deleteIfExists(file);
    }
    
    protected static void copyFile(String fileName) throws IOException {
        Path src = Paths.get("src/test/resources/lt/emasina/server/xml/" + fileName);
        Path tar = FOLDER.resolve(fileName); 
        deleteFile(fileName);
        Files.copy(src, tar);
    }
    
    public static void main(String[] args) {
        try {
            startServer();
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
    }
    
}
