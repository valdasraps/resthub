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
package lt.emasina.resthub.server.parser.update;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lt.emasina.resthub.model.MdParameter;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import lt.emasina.resthub.server.table.ServerTable;
import lt.emasina.resthub.parser.AbstractAllParser;
import lt.emasina.resthub.server.query.QueryParameter;

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