package net.resthub.server.parser.update;

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
import net.resthub.exception.QueryException;
import net.resthub.server.table.ServerTable;
import net.resthub.server.parser.check.CheckSelectParser;
import net.resthub.parser.AbstractAllParser;
import net.resthub.server.query.QueryParameter;

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