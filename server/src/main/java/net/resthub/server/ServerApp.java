package net.resthub.server;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.resthub.ConnectionFactory;
import net.resthub.TableFactory;
import net.resthub.server.app.BlacklistTable;
import net.resthub.server.app.BlacklistTables;
import net.resthub.server.app.Cache;
import net.resthub.server.app.Queries;
import net.resthub.server.app.Query;
import net.resthub.server.app.Count;
import net.resthub.server.app.Data;
import net.resthub.server.app.Lob;
import net.resthub.server.app.Table;
import net.resthub.server.app.Tables;
import net.resthub.server.factory.CacheFactory;
import net.resthub.server.factory.MetadataFactory;
import net.resthub.server.factory.MetadataFactoryIf;
import net.resthub.server.factory.InjectorJobFactory;
import net.resthub.server.factory.ResourceFactory;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;
import org.restlet.Restlet;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;

public class ServerApp extends BaseApp {
    
    private final AbstractModule[] applicationModules;
    
    public ServerApp(final ConnectionFactory connectionFactory, final TableFactory tableFactory) throws Exception {
        this(connectionFactory, tableFactory, new ServerAppConfig());
    }
    
    public ServerApp(final ConnectionFactory connectionFactory, final TableFactory tableFactory, final ServerAppConfig cfg) throws Exception {
        
        setName("JDBC-Restlet Application");
        setDescription("Application that provides RESTful API to JDBC queries");
        setAuthor("Valdas Rapsevicius, valdas.rapsevicius@cern.ch");
        
        final SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        final Scheduler scheduler = schedulerFactory.getScheduler();

        this.applicationModules = new AbstractModule[] { 
            new AbstractModule() {

                @Override
                protected void configure() {
                    
                    bind(ConnectionFactory.class).toInstance(connectionFactory);
                    bind(TableFactory.class).toInstance(tableFactory);
                    bind(CCJSqlParserManager.class).toInstance(new CCJSqlParserManager());
                    install(new FactoryModuleBuilder().build(ResourceFactory.class));
                    bind(JobFactory.class).to(InjectorJobFactory.class);  
                    bind(MetadataFactoryIf.class).to(MetadataFactory.class);
                    bind(Scheduler.class).toInstance(scheduler);
                    bind(ServerAppConfig.class).toInstance(cfg);
                    
                }

            }
        };
        
        getInjector().getInstance(MetadataFactoryIf.class).refresh();
        InjectorJobFactory.startUpdateJob(scheduler, getInjector().getInstance(InjectorJobFactory.class));
        
        getInjector().getInstance(CacheFactory.class);
        scheduler.start();
        
    }

    @Override
    public synchronized void stop() throws Exception {
        super.stop();
        getInjector().getInstance(Scheduler.class).shutdown();
        getInjector().getInstance(CacheFactory.class).close();
        getInjector().getInstance(TableFactory.class).close();
    }
    
    @Override
    protected AbstractModule[] getApplicationModules() {
        return applicationModules;
    }

    @Override
    public Restlet createInboundRoot() {
        
        Router router = new Router(getContext());

        // GET
        router.attach("/", Tables.class);
        router.attach("/tables", Tables.class);
        router.attach("/tables/{tableNs}", Tables.class);
        router.attach("/table/{tableNs}/{tableName}", Table.class);

        // GET, DELETE
        router.attach("/blacklist", BlacklistTables.class);
        router.attach("/blacklist/{tableNs}", BlacklistTables.class);
        router.attach("/blacklist/{tableNs}/{tableName}", BlacklistTable.class);
        
        // GET, DELETE
        router.attach("/table/{tableNs}/{tableName}/cache", Cache.class);

        // GET
        router.attach("/queries", Queries.class);

        // POST
        router.attach("/query", Query.class);

        // GET, DELETE
        router.attach("/query/{queryId}", Query.class);

        // GET
        router.attach("/query/{queryId}/count", Count.class);

        // GET
        router.attach("/query/{queryId}/data", Data.class);
        router.attach("/query/{queryId}/page/{perPage}/{page}/data", Data.class);
        router.attach("/query/{queryId}/{row}/{col}/lob", Lob.class);
        router.attach("/query/{queryId}/page/{perPage}/{page}/{row}/{col}/lob", Lob.class);
        
        // GET, DELETE
        router.attach("/query/{queryId}/cache", Cache.class);

        Filter filter = new RequestFilter(getContext());
        filter.setNext(router);

        return filter;

    }

}