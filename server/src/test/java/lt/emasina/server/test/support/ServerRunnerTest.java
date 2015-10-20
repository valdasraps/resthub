package lt.emasina.server.test.support;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lt.emasina.server.test.ServerRunnerWorker;

public class ServerRunnerTest {
    
    private static final int NTHREDS = 5;
    
    public void testQueries() throws InterruptedException {
    
        ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
        for (int i = 0; i < 10; i++) {
          executor.execute(new ServerRunnerWorker());
        }
        
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.DAYS);

    }
}
