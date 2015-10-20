package lt.emasina.server.test.support;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lt.emasina.server.test.JavaClientWorker;

public class JavaClientTest {

    private static final int NTHREDS = 3;

    public void testQueries() throws InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
        for (int i = 0; i < 50; i++) {
            executor.execute(new JavaClientWorker());
        }
        
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        
    }
    
}
