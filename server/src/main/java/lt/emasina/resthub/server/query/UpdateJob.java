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
        
        try {
            mf.refresh(); 
        } catch (Exception ex) {
            throw new JobExecutionException(ex);
        }
        
        qf.cleanQueries();
        
        if (log.isDebugEnabled()) {
            ccf.logStats();
        }
        
    }

}
