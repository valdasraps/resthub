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
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SubSelect;
import lt.emasina.resthub.exception.QueryException;
import lt.emasina.resthub.server.table.ServerTable;
import lt.emasina.resthub.server.parser.check.CheckSelectParser;
import lt.emasina.resthub.parser.AbstractAllParser;
import lt.emasina.resthub.server.query.QueryParameter;

/**
 * SelectParser
 * @author valdo
 */
@RequiredArgsConstructor
public class UpdateSelectParser extends AbstractAllParser {
    
    private final CheckSelectParser selectParser;
    
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

        if (ps.getWhere() != null) {
            ps.getWhere().accept(this);
        }
        
        if (ps.getOracleHierarchical() != null) {
            ps.getOracleHierarchical().accept(this);
        }

    }

    // FromItemVisitor
    
    @Override
    public void visit(Table table) {
        String alias = table.getAlias().getName();
        ServerTable t = selectParser.getSelectDef().getServerTable(alias);
        
        try {
            
            UpdateParamParser paramParser = new UpdateParamParser(alias, t);
            Select select = t.getSelect();
            select.getSelectBody().accept(paramParser);
            
            table.setSchemaName(null);
            table.setName("(" + select.toString() + ")");
            
            parameters.putAll(paramParser.getParameters());
            
        } catch (JSQLParserException ex) {
            throw new QueryException(ex.getMessage());
        }
    }

    @Override
    public void visit(SubSelect ss) {
        ss.getSelectBody().accept(this);
    }

    /**
     * RESTful query parameters
     * @param p 
     */
    @Override
    public void visit(JdbcNamedParameter p) {
        parameters.put(p.getName(), new QueryParameter(p.getName()));
    }

}