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
