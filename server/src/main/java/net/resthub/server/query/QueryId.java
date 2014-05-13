package net.resthub.server.query;

import net.resthub.exception.QueryException;
import com.google.inject.assistedinject.Assisted;
import java.io.StringReader;
import javax.inject.Inject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.commons.codec.digest.DigestUtils;
import net.resthub.server.factory.QueryFactory;
import org.hibernate.engine.jdbc.internal.BasicFormatterImpl;
import org.hibernate.engine.jdbc.internal.Formatter;

/**
 * QueryId
 * @author valdo
 */
@Getter
@EqualsAndHashCode(of = "id")
@ToString(of = { "id" })
public class QueryId {

    private static final Formatter SQL_FORMATTER = new BasicFormatterImpl();
    
    private final String id;
    private final String md5;
    private final String sql;
    private final Select select;
    
    @Inject
    public QueryId(@Assisted String sql, CCJSqlParserManager pm) throws QueryException {
        try {
            Statement stmt = pm.parse(new StringReader(sql));
            if (stmt instanceof Select) {
                this.select = (Select) stmt;
                this.sql = SQL_FORMATTER.format(this.select.toString());
                this.md5 = DigestUtils.md5Hex(this.sql);
                this.id = QueryFactory.nextUID();
            } else {
                throw new QueryException("Only SELECT statements allowed!");
            }
        } catch (JSQLParserException ex) {
            throw new QueryException(ex.getCause().getMessage());
        }
    }
    
}
