package net.resthub.parser;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import lombok.Getter;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.WithItem;
import net.resthub.exception.QueryException;

/**
 * SqlParser
 * @author valdo
 */
public class SqlParser extends AbstractAllParser {

    @Getter
    private final Set<String> parameterNames = new HashSet<>();
    
    @Override
    public void visit(PlainSelect ps) {

        // First collect FROM and JOIN items
        
        ps.getFromItem().accept(this);
        
        if (ps.getJoins() != null) {
            for (Join j : ps.getJoins()) {
                j.getRightItem().accept(this);
                if (j.getOnExpression() != null) {
                    j.getOnExpression().accept(this);
                }
                if (j.getUsingColumns() != null) {
                    for (Iterator it1 = j.getUsingColumns().iterator(); it1.hasNext();) {
                        ((Expression) it1.next()).accept(this);
                    }
                }
            }
        }
        
       // Next iterate over SELECT, WHERE and ORDER BY items
        
        for (Iterator it = ps.getSelectItems().iterator(); it.hasNext();) {
            Object o = it.next();
            if (o instanceof SelectExpressionItem) {
                ((SelectExpressionItem) o).accept(this);
            }
            if (o instanceof AllColumns) {
                ((AllColumns) o).accept(this);
            }
            if (o instanceof AllTableColumns) {
                ((AllTableColumns) o).accept(this);
            }
        }

        if (ps.getWhere() != null) {
            ps.getWhere().accept(this);
        }
        
        if (ps.getOrderByElements() != null) {
            for (Iterator it = ps.getOrderByElements().iterator(); it.hasNext();) {
                ((OrderByElement) it.next()).accept(this);
            }
        }
        
        if (ps.getOracleHierarchical() != null) {
            ps.getOracleHierarchical().accept(this);
        }
        
    }

    @Override
    public void visit(JdbcNamedParameter p) {
        String name = p.getName().toLowerCase();
        p.setName(name);
        parameterNames.add(name);
    }

    @Override
    public void visit(JdbcParameter jp) {
        throw new RuntimeException(String.format("Positional parameters are not allowed: %s", jp));
    }

    @Override
    public void visit(WithItem wi) {
        throw new QueryException("With expressions are not supported", wi);
    }

}