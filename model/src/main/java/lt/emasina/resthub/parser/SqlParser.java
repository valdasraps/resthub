/*
 * #%L
 * model
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
package lt.emasina.resthub.parser;

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
import lt.emasina.resthub.exception.QueryException;

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