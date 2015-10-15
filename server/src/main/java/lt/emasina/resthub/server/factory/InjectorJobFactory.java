package lt.emasina.resthub.server.factory;

import com.google.inject.Injector;
import javax.inject.Inject;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.server.cache.CacheJob;
import lt.emasina.resthub.server.handler.Handler;
import lt.emasina.resthub.server.query.QueryStats;
import lt.emasina.resthub.server.query.UpdateJob;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

/**
 * SchedulerFactory
 *
 * @author valdo
 */
@Log4j
public class InjectorJobFactory implements JobFactory {
    
    @Inject
    private Injector injector;

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        return (Job) injector.getInstance(bundle.getJobDetail().getJobClass());
    }
    private static final String UPDATE_JOB_ID = "updateJob";
    private static final String UPDATE_JOB_GROUP_ID = "resthubUpdateJobs";
    private static final String UPDATE_TRIGGER_ID = "updateJobTrigger";
    private static final int UPDATE_INTERVAL_SEC = 120;

    public static void startUpdateJob(Scheduler scheduler, InjectorJobFactory jobFactory) throws SchedulerException {

        JobDetail jobDetail = JobBuilder.newJob(UpdateJob.class).withIdentity(UPDATE_JOB_ID, UPDATE_JOB_GROUP_ID).build();
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(UPDATE_TRIGGER_ID).withSchedule(
                SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(UPDATE_INTERVAL_SEC).repeatForever())
                .build();

        scheduler.setJobFactory(jobFactory);
        scheduler.scheduleJob(jobDetail, trigger);

    }
    
    //private static final String CACHE_JOB_GROUP_ID = "resthubCacheJobs";
    public static final String HANDLER_ID_ATTR = "id";

    public static void startCacheJob(Scheduler scheduler, InjectorJobFactory jobFactory, Handler<?,?> qh) throws SchedulerException {
        QueryStats stats = qh.getQuery().getStats();
        long cacheTime = stats.getCacheTime();

        JobDetail jobDetail = JobBuilder.newJob(CacheJob.class).build();
        jobDetail.getJobDataMap().put(HANDLER_ID_ATTR, qh.getId());
        Trigger trigger = TriggerBuilder.newTrigger().withSchedule(
                SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(qh.getQuery().getCacheTimeInMilliseconds() - cacheTime).withRepeatCount(1))
                .build();

        log.debug(String.format("JOB time: (%d)", qh.getQuery().getCacheTimeInMilliseconds() - cacheTime));
        scheduler.setJobFactory(jobFactory);
        scheduler.scheduleJob(jobDetail, trigger);

    }
}
