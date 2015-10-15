package lt.emasina.resthub.server.query;

import javax.inject.Inject;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.TableFactory;
import lt.emasina.resthub.server.factory.CacheFactory;
import lt.emasina.resthub.server.factory.MetadataFactory;
import lt.emasina.resthub.server.factory.QueryFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * CleanJob
 * @author valdo
 */
@Log4j
public class UpdateJob implements Job {

    @Inject
    private MetadataFactory mf;
    
    @Inject
    private CacheFactory ccf;
    
    @Inject
    private QueryFactory qf;
    
    @Inject
    private TableFactory tf;
    
    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        
        if (tf.isRefreshable()) {
            try {
                mf.refresh(); 
            } catch (Exception ex) {
                throw new JobExecutionException(ex);
            }
        }
        
        qf.cleanQueries();
        
        if (log.isDebugEnabled()) {
            ccf.logStats();
        }
        
    }

}
