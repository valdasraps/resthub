package net.resthub.server;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.log4j.Log4j;
import net.resthub.server.app.BaseResource;
import net.resthub.util.ResourceInjector;
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
