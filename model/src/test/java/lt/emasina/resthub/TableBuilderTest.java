package lt.emasina.resthub;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import static junit.framework.Assert.assertEquals;
import lt.emasina.resthub.ConnectionFactory;
import lt.emasina.resthub.factory.TableBuilder;
import lt.emasina.resthub.model.MdParameter;
import oracle.jdbc.OracleConnection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * ParserTest
 * @author valdo
 */
@RunWith(JUnit4.class)
public class TableBuilderTest {

    public final TableBuilder tb;
    
    public TableBuilderTest() {
        Injector injector = Guice.createInjector(
            new AbstractModule() {
                @Override
                protected void configure() {
                    bind(ConnectionFactory.class).toInstance(new ConnectionFactory() {

                        @Override
                        public Collection<String> getConnectionNames() {
                            return Collections.EMPTY_SET;
                        }

                        @Override
                        public OracleConnection getConnection(String name) throws SQLException {
                            return null;
                        }

                        @Override
                        public String getUrl(String name) {
                            return null;
                        }

                        @Override
                        public String getUsername(String name) {
                            return null;
                        }

                        @Override
                        public String getPassword(String name) {
                            return null;
                        }
                    });
                }
            }
        );
        this.tb = injector.getInstance(TableBuilder.class);
    }
    
    @Test
    public void connectByTest() throws Exception {
        Set<MdParameter> params = new HashSet<>();
        tb.collectParameters("SELECT" +
"        CONDS.VERSION," +
"        PXLDET.ROC_NAME," +
"        PXLDET.ROC_STATUS," +
"        PXLDET.RECORD_ID" +
"    FROM" +
"        CMS_PXL_PIXEL_COND.PIXEL_DETECTOR_CONFIG PXLDET" +
"    INNER JOIN" +
"        CMS_PXL_CORE_COND.COND_DATA_SETS CONDS" +
"            ON PXLDET.CONDITION_DATA_SET_ID = CONDS.CONDITION_DATA_SET_ID" +
"    WHERE" +
"        CONDS.IS_RECORD_DELETED = 'F'" +
"        AND (" +
"            CONDS.VERSION = :version" +
"            OR :version is null" +
"        )" +
"        AND (" +
"            PXLDET.ROC_STATUS = :status" +
"            OR :status is null" +
"        )" +
"        AND exists (" +
"            select" +
"                null" +
"            from" +
"                (select" +
"                    decode(level," +
"                    '0'," +
"                    '0'," +
"                    :rocs) n" +
"                from" +
"                    dual connect by" +
"                    level <= :rocs_number) l" +
"            where" +
"                PXLDET.ROC_NAME like l.n" +
"            )", params);
        assertEquals(4, params.size());
    }
    
}
