package net.resthub.server.parser.update;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.resthub.model.MdParameter;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.resthub.server.table.ServerTable;
import net.resthub.parser.AbstractAllParser;
import net.resthub.server.query.QueryParameter;

/**
 * SelectParser
 * @author valdo
 */
@RequiredArgsConstructor
public class UpdateParamParser extends AbstractAllParser {
    
    private final String alias;
    private final ServerTable tmd;
    
    @Getter
    private final Map<String, QueryParameter> parameters = new HashMap<>();
    
    // SelectVisitor
    
    @Override
    public void visit(PlainSelect ps) {

        ps.getFromItem().accept(this);
        if (ps.getJoins() != null) {
            for (Join j : ps.getJoins()) {
                j.getRightItem().accept(this);
            }
        }
        
        if (ps.getJoins() != null) {
            for (Join j : ps.getJoins()) {
                if (j.getOnExpression() != null) {
                    j.getOnExpression().accept(this);
                }
                if (j.getUsingColumns() != null) {
                    for (Iterator<?> it1 = j.getUsingColumns().iterator(); it1.hasNext();) {
                        ((Expression) it1.next()).accept(this);
                    }
                }
            }
        }
        
        // Next iterate over SELECT, WHERE and ORDER BY items
        
        for (Object o: ps.getSelectItems()) {
            if (o instanceof SelectExpressionItem) {
                ((SelectExpressionItem) o).accept(this);
            }
        }

        if (ps.getWhere() != null) {
            ps.getWhere().accept(this);
        }
        
        if (ps.getOrderByElements() != null) {
            for (Iterator<?> it = ps.getOrderByElements().iterator(); it.hasNext();) {
                ((OrderByElement) it.next()).accept(this);
            }
        }
        
        if (ps.getOracleHierarchical() != null) {
            ps.getOracleHierarchical().accept(this);
        }

    }

    @Override
    public void visit(JdbcNamedParameter p) {
        MdParameter parameter = tmd.getParameter(p.getName());
        QueryParameter qp = new QueryParameter(parameter, alias);
        p.setName(qp.getSqlName());
        parameters.put(qp.getName(), qp);
    }
    
}