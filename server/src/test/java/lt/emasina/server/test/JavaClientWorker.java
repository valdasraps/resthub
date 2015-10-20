package lt.emasina.server.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.client.RestHubServer;
import lt.emasina.resthub.model.DataResponse;
import lt.emasina.resthub.model.QueryManager;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@Log4j
@RunWith(JUnit4.class)
public class JavaClientWorker extends ServerSetup {
    
    private final static String HEADER_FILE = "client_header";
    private final static String DATA_FILE = "client_data";
    private final Random random = new Random();
    
    @Test
    public void checkQueries() throws IOException, URISyntaxException, org.json.JSONException {
        
        RestHubServer rh = new RestHubServer(ServerSetup.HOST);

        QueryManager qm = rh.newQueryManager("SELECT * FROM (SELECT c.ID,c.BRAND FROM store.products c WHERE c.ID > 1500 ORDER BY c.BRAND desc) a ORDER BY a.ID asc;");
        qm.addParameter("p1", random.nextInt());

        check(qm);
        
    }
    
    private void check(QueryManager qm) throws IOException, JSONException {
        
        log.debug("Cheking query headers");
        Map headers = qm.options();
        
        // Checking headers file
        InputStream inputStream = getResultStream(HEADER_FILE);
        
        if (inputStream == null) {
            headersToFile(headers, HEADER_FILE);
        } else {
            compareHeaders(headers, inputStream);
        }

        log.debug("Cheking query data");
        inputStream = getResultStream(DATA_FILE);
        boolean loaded = inputStream != null;
        Properties prop = new Properties();
        if (loaded) {
            prop.load(inputStream);
        }
        
        // Getting available content types 
        String ct = (String) headers.get("Content-Type");
        String[] contentTypes = ct.split(",");     
        
        // Check each contentType   
        for (String contentType : contentTypes) { 
            
            DataResponse dr = qm.getData(contentType);
            String data = dr.getString();
            
            if (!loaded) {
                prop.setProperty(contentType, data);
            } else {
                log.info(String.format("Comparing data: %s @ %s...", contentType, DATA_FILE));
                compareData(contentType, prop.getProperty(contentType), data);
            }
        }
       
        if (!loaded) {
            prop.store(new FileOutputStream(getResultFile(DATA_FILE)), "Query data");
        }

    }
       
}
