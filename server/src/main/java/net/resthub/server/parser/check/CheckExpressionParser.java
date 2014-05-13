package net.resthub.server.parser.check;

import com.google.inject.assistedinject.Assisted;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.extern.log4j.Log4j;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.resthub.server.factory.ResourceFactory;
import net.resthub.exception.QueryException;
import net.resthub.parser.AbstractExpressionParser;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;

/**
 * ExpressionParser
 * @author valdo
 */
@Log4j
public class CheckExpressionParser extends AbstractExpressionParser {

    private static final String ALLOWED_FUNCTIONS_FILE = "/net/resthub/server/parser/functions.properties";
    private static final Set<String> ALLOWED_FUNCTIONS = new HashSet<>();
    static {
        Properties p = new Properties();
        try (InputStream is = CheckExpressionParser.class.getResourceAsStream(ALLOWED_FUNCTIONS_FILE)) {
            p.load(is);
        } catch (IOException ex) {
            log.error("Error while loading allowed functions", ex);
        }
        
        for (String k: p.stringPropertyNames()) {
            ALLOWED_FUNCTIONS.add(k.toUpperCase().trim());
        }
    }
    
    @Inject
    private ResourceFactory rf;
    
    private final SubSelectDef selectDef;

    @Inject
    public CheckExpressionParser(@Assisted SubSelectDef selectDef) {
        this.selectDef = selectDef;
    }

    @Override
    public void visit(Column column) {
        
        if (column.getTable() == null || column.getTable().getName() == null) {
            throw new QueryException("Column table can not be determined: %s", column.getFullyQualifiedName());
        }
        
        String alias = column.getTable().getName();
        if (!selectDef.getTables().containsKey(alias)) {
            throw new QueryException("Column table alias can not be determined: %s", column.getFullyQualifiedName());
        }
        
        SelectDef td = selectDef.getTables().get(alias);
        String name = fixColumnName(column.getColumnName());

        boolean found = Boolean.FALSE;
        for (String n: td.getColumns()) {
            if (name.equalsIgnoreCase(n)) {
                column.setColumnName("\"" + n + "\"");
                found = Boolean.TRUE;
            }
        }
        
        if (!found) {
            throw new QueryException("Column %s not found in table", column.getFullyQualifiedName());
        }
        
    }

    @Override
    public void visit(SubSelect subSelect) {
        CheckSelectParser checkParser = rf.createSelectParser((SubSelectDef) null);
        subSelect.getSelectBody().accept(checkParser);
        selectDef.getChildren().add(checkParser.getSelectDef());
    }

    @Override
    public void visit(JdbcNamedParameter p) {
        selectDef.getTop().getParameterNames().add(p.getName());
    }

    @Override
    public void visit(JdbcParameter jp) {
        throw new QueryException("Positional parameters are not supported: %s", jp.toString());
    }
    
    @Override
    public void visit(CaseExpression ce) {
        throw new QueryException("Case expression is not supported: %s", ce.toString());
    }

    @Override
    public void visit(WhenClause wc) {
        throw new QueryException("When clause is not supported: %s", wc.toString());
    }

    @Override
    public void visit(AllComparisonExpression ace) {
        throw new QueryException("All comparison expression is not supported: %s", ace.toString());
    }

    @Override
    public void visit(AnyComparisonExpression ace) {
        throw new QueryException("Any comparison expression is not supported: %s", ace.toString());
    }

    private static final Pattern QUOTTED_NAME = Pattern.compile("^\"(.*)\"$");
    
    public static String fixColumnName(String name) {
        Matcher m = QUOTTED_NAME.matcher(name);
        if (m.matches()) {
            return m.group(1);
        }
        return name;
    }

    @Override
    public void visit(SignedExpression se) {
        se.accept(this);
    }

    @Override
    public void visit(OracleHierarchicalExpression ohe) {
        if (ohe.getStartExpression() != null) {
            ohe.getStartExpression().accept(this);
        }
        if (ohe.getConnectExpression() != null) {
            ohe.getConnectExpression().accept(this);
        }
    }

    @Override
    public void visit(RegExpMatchOperator remo) {
        visitBinaryExpression(remo);
    }
    
    @Override
    public void visit(Function f) {
        super.visit(f);
        if (!ALLOWED_FUNCTIONS.contains(f.getName().trim().toUpperCase())) {
            throw new QueryException("Function is not in the allowed functions list: %s", f.getName());
        }
    }

}
