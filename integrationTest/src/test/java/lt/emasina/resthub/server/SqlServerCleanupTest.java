package lt.emasina.resthub.server;

import java.sql.SQLException;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.support.SqlServerTableFactoryBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@Log4j
@RunWith(JUnit4.class)
public class SqlServerCleanupTest extends SqlServerTableFactoryBase {

    @Test
    public void testCleanup() throws Exception {

        execDdlIgnore("drop view cussimple_sqltest");
        execDdlIgnore("drop view cussimple1_sqltest");
        execDdlIgnore("drop view allobjs_sqltest");
        Thread.sleep(11000);

        testEmpty();

    }

    private void execDdlIgnore(String ddl) {
        try {
            execDdl(ddl);
        } catch (SQLException ex) { /* Ignore */ }

    }
    
}
