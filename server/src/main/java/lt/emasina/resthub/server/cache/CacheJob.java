package lt.emasina.resthub.server.cache;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.server.exporter.Exporter;
import lt.emasina.resthub.server.factory.CacheFactory;
import lt.emasina.resthub.server.factory.InjectorJobFactory;
import lt.emasina.resthub.server.factory.QueryFactory;
import lt.emasina.resthub.server.handler.Handler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * CleanJob
 * @author valdo
 */
@Log4j
public class CacheJob implements Job {

    @Inject
    private CacheFactory ccf;
    
    @Inject
    private QueryFactory qf;
        
    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        
        if (jec.getTrigger().mayFireAgain()) {
            return;
        }       
        Integer id = jec.getJobDetail().getJobDataMap().getInt(InjectorJobFactory.HANDLER_ID_ATTR);
        CacheFactory.CacheJobData data = ccf.getCacheJobData(id);
        if (data == null) {
            return;
        }
        Handler<?,?> handler = data.getHandler();
        
        CacheStats cacheStats = handler.getCacheStats();
        long hitCount = cacheStats.getHitCount();
        int queryHitCount = handler.getQuery().getHitCount();
        
        long currentExpTime = cacheStats.getExpTime();
        long expectedExpTime = data.getExpTime();
        
        log.info(String.format("Cache Job: id = (%d), tableHitCount = (%d), hitCount: (%d)",
                id, queryHitCount, (hitCount - 1) / 2));
        log.info(String.format("Cache Job: id = (%d), currentExpTime = (%d), expectedExpTime: (%d)", 
                id, currentExpTime, expectedExpTime));
        
        if (currentExpTime > expectedExpTime) {
            return;
        } 
        
        // each element hit increments hitCount value by 2 and adds 1 at creation time
        if (queryHitCount >= (hitCount - 1) / 2 ) {
            return;
        }
        
        Exporter<?> dataExporter = qf.getExporter(handler);
        dataExporter.refreshValue();
   
    }

}
