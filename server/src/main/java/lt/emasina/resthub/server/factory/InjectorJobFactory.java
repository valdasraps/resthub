/*
 * #%L
 * server
 * %%
 * Copyright (C) 2012 - 2015 valdasraps
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package lt.emasina.resthub.server.factory;

import com.google.inject.Injector;
import javax.inject.Inject;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.server.ServerAppConfig;
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
    
    public static void startUpdateJob(Scheduler scheduler, InjectorJobFactory jobFactory, ServerAppConfig cfg) throws SchedulerException {

        JobDetail jobDetail = JobBuilder.newJob(UpdateJob.class).withIdentity(UPDATE_JOB_ID, UPDATE_JOB_GROUP_ID).build();
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(UPDATE_TRIGGER_ID).withSchedule(
                SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(cfg.getUpdateInterval()).repeatForever())
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
