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
package lt.emasina.resthub.server;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.server.app.BaseResource;
import lt.emasina.resthub.util.ResourceInjector;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.restlet.Application;

/**
 * BaseApp
 * @author valdo
 */
@Log4j
public abstract class BaseApp extends Application {

    private static final Class<?> SQL_BINDER_CLASS = org.hibernate.type.descriptor.sql.BasicBinder.class;
    private Injector injector = null;
    
    protected abstract AbstractModule[] getApplicationModules();
    
    public BaseApp() {
        if (log.isDebugEnabled()) {
            Logger logger = log.getLoggerRepository().getLogger(SQL_BINDER_CLASS.getName());
            logger.setLevel(Level.TRACE);
        }
        setStatusService(new ResponseStatusService());
    }
    
    public Injector getInjector() {
        if (injector == null) {
            List<AbstractModule> modules = new ArrayList<>();
            modules.addAll(Arrays.asList(getApplicationModules()));
            modules.add(                
                new AbstractModule() {

                    @Override
                    protected void configure() {
                        requestStaticInjection(BaseResource.class);
                    }

                    @Provides
                    ResourceInjector memberInjector(final Injector injector) {
                        return new ResourceInjector() {

                            @Override
                            public void injectMembers(Object object) {
                                injector.injectMembers(object);
                            }

                        };
                    }
                }
            );
            this.injector = Guice.createInjector(modules);
        }
        return injector;
    }
    
}
