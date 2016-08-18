package lt.emasina.resthub.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.log4j.Log4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;

/**
 *
 * @author valdo
 */
@Log4j
@Ignore
public class ServerSetup {

    public static final String HOST = "http://localhost:8112";
    protected static final String[] EXCLUDE_HEADERS = {"Date", "Expires", "Accept-Ranges", "Allow"};
    
    protected static final ServerWorker WORKER = new ServerWorker();
    
    @BeforeClass
    public static void startServer() throws Exception {
        //WORKER.startTunnel();
        WORKER.startServer();
    }
    
    @AfterClass
    public static void stopServer() throws Exception {
        //WORKER.stopServer();
        WORKER.stopTunnel();
    }
    
    protected static void deleteFile(String fileName) throws IOException {
        Path file = ServerWorker.FOLDER.resolve(fileName); 
        Files.deleteIfExists(file);
    }
    
    protected static void copyFile(String fileName) throws IOException {
        Path src = Paths.get("src/test/resources/lt/emasina/server/xml/" + fileName);
        Path tar = ServerWorker.FOLDER.resolve(fileName); 
        deleteFile(fileName);
        Files.copy(src, tar);
    }
    
    public static void main(String[] args) {
        try {
            
            WORKER.startTunnel();
            startServer();
            
            
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
    }
    
}
