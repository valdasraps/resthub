package lt.emasina.resthub.server.test.util;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import junit.framework.TestCase;
import lt.emasina.resthub.ConnectionFactory;
import lt.emasina.resthub.server.factory.MetadataFactoryIf;
import lt.emasina.resthub.server.factory.ResourceFactory;
import lt.emasina.resthub.server.query.QueryId;
import lt.emasina.resthub.server.parser.check.CheckSelectParser;
import lt.emasina.resthub.server.parser.check.SubSelectDef;
import lt.emasina.resthub.server.parser.update.UpdateSelectParser;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.Select;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

/**
 * SelectParserTestSuite
 * @author valdo
 */
public abstract class AbstractParser extends TestCase {
    
    protected final Injector injector;
    protected final ResourceFactory rf;
    protected final MetadataFactoryIf mf;

    public AbstractParser() throws SchedulerException {
        
        final SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        final Scheduler scheduler = schedulerFactory.getScheduler();
        
        this.injector = Guice.createInjector(new AbstractModule() {
            
            @Override
            protected void configure() {
                install(new FactoryModuleBuilder().build(ResourceFactory.class));
                bind(MetadataFactoryIf.class).to(MetadataFactoryTest.class);
                bind(CCJSqlParserManager.class).toInstance(new CCJSqlParserManager());
                bind(ConnectionFactory.class).toInstance(new ConnectionFactoryTest());
                bind(Scheduler.class).toInstance(scheduler);
            }
        });
        this.rf = injector.getInstance(ResourceFactory.class);
        this.mf = injector.getInstance(MetadataFactoryIf.class);
    }
    
    protected SubSelectDef getSubSelectDef(String sql) {
        return getCheckParser(sql).getSelectDef();
    }
    
    protected CheckSelectParser getCheckParser(String sql) {
        CheckSelectParser checkParser = rf.createSelectParser((SubSelectDef) null);
        QueryId qid = rf.create(sql);
        qid.getSelect().getSelectBody().accept(checkParser);
        return checkParser;
    }
    
    protected UpdateSelectParser getUpdateParser(String sql) {
        UpdateSelectParser updParser = new UpdateSelectParser(getCheckParser(sql));
        QueryId qid = rf.create(sql);
        qid.getSelect().getSelectBody().accept(updParser);
        return updParser;
    }
    
    protected Select getUpdateSelect(String sql) {
        UpdateSelectParser updParser = new UpdateSelectParser(getCheckParser(sql));
        QueryId qid = rf.create(sql);
        Select select = qid.getSelect();
        select.getSelectBody().accept(updParser);
        return select;
    }
    
}
