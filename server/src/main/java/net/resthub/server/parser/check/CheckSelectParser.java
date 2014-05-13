package net.resthub.server.parser.check;

import com.google.inject.assistedinject.Assisted;
import net.resthub.parser.AbstractSelectParser;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.Getter;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.statement.select.WithItem;
import net.resthub.server.factory.ResourceFactory;
import net.resthub.exception.QueryException;
import net.resthub.server.factory.MetadataFactoryIf;
import net.resthub.server.table.TableId;
import net.resthub.server.table.ServerTable;

/**
 * SelectParser
 * @author valdo
 */
public class CheckSelectParser extends AbstractSelectParser {
    
    private final ResourceFactory rf;
    
    @Inject
    private MetadataFactoryIf mf;
    
    @Getter
    private final SubSelectDef selectDef;
    
    private final CheckExpressionParser expParser;
    
    @Inject
    public CheckSelectParser(@Assisted @Nullable SubSelectDef parent, ResourceFactory rf) {
        this.rf = rf;
        this.selectDef = new SubSelectDef(parent);
        this.expParser = rf.createExpressionParser(this.selectDef);
    }
    
    public List<ServerTable> getTables() {
        List<ServerTable> tables = new ArrayList<>();
        List<SelectDef> cache = new ArrayList<>();
        cache.addAll(selectDef.getTables().values());
        while (!cache.isEmpty()) {
            SelectDef td = cache.remove(0);
            if (td instanceof SubSelectDef) {
                cache.addAll(((SubSelectDef) td).getTables().values());
            } else
            if (td instanceof TableDef) {
                tables.add(((TableDef) td).getTableMd());
            }
        }
        return tables;
    }
    
    /**
     * SelectVisitor
     * @param ps
     */
    @Override
    public void visit(PlainSelect ps) {
        
        // First collect FROM and JOIN items
        
        ps.getFromItem().accept(this);
        
        if (ps.getJoins() != null) {
            for (Join j : ps.getJoins()) {
                j.getRightItem().accept(this);
                if (j.getOnExpression() != null) {
                    j.getOnExpression().accept(expParser);
                }
                if (j.getUsingColumns() != null) {
                    for (Iterator<?> it1 = j.getUsingColumns().iterator(); it1.hasNext();) {
                        ((Expression) it1.next()).accept(expParser);
                    }
                }
            }
        }
        
       // Next iterate over SELECT, WHERE and ORDER BY items
        
        for (Object o: ps.getSelectItems()) {
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
            ps.getWhere().accept(expParser);
        }
        
        if (ps.getOrderByElements() != null) {
            for (Iterator<?> it = ps.getOrderByElements().iterator(); it.hasNext();) {
                ((OrderByElement) it.next()).accept(this);
            }
        }
        
        if (ps.getOracleHierarchical() != null) {
            ps.getOracleHierarchical().accept(expParser);
        }
        
    }

    /**
     * FromItemVisitor
     * @param table
     */
    
    @Override
    public void visit(Table table) {
        
        if (table.getSchemaName() == null) {
            throw new QueryException("Table namespace not defined: %s", table.getFullyQualifiedName());
        }
        
        if (table.getAlias() == null || table.getAlias().getName() == null) {
            throw new QueryException("Table alias not defined: %s", table.getFullyQualifiedName());
        }
        
        String alias = table.getAlias().getName();
        if (selectDef.getTop().hasAlias(alias)) {
            throw new QueryException("Duplicate table alias: %s", alias);
        }
        
        TableId tid = new TableId(table.getSchemaName().toLowerCase(), table.getName().toLowerCase());
        ServerTable tmd = mf.getTable(tid);
        
        if (tmd == null) {
            throw new QueryException("Table not found: %s", table.getFullyQualifiedName());
        }
        
        if (!selectDef.isSameConnectionName(tmd.getTable().getConnectionName())) {
            throw new QueryException("Table connections do not match: %s", tmd.getId());
        }
        
        selectDef.getTables().put(alias, new TableDef(tmd, selectDef));        

    }

    @Override
    public void visit(SubSelect ss) {
        if (ss.getAlias() == null || ss.getAlias().getName() == null) {
            throw new QueryException("Subselect alias not defined: %s", ss.toString());
        }

        String alias = ss.getAlias().getName();
        if (selectDef.getTop().hasAlias(alias)) {
            throw new QueryException("Duplicate table alias: %s", alias);
        }
        
        CheckSelectParser sp = rf.createSelectParser(selectDef);
        selectDef.getTables().put(alias, sp.getSelectDef());
        ss.getSelectBody().accept(sp);
        
    }

    @Override
    public void visit(SubJoin sj) {
        throw new QueryException("SubJoins are not supported: %s", sj.toString());
    }
    
    // SelectItemVisitor
    
    @Override
    public void visit(AllTableColumns atc) {
        String alias = atc.getTable().getName();
        if (!selectDef.getTables().containsKey(alias)) {
            throw new QueryException("Table alias %s not defined", alias);
        }
        
        SelectDef td = selectDef.getTables().get(alias);
        selectDef.getColumns().addAll(td.getColumns());
    }

    @Override
    public void visit(AllColumns ac) {
        for (SelectDef td: selectDef.getTables().values()) {
            selectDef.getColumns().addAll(td.getColumns());
        }
    }

    @Override
    public void visit(SelectExpressionItem sei) {
        String alias = sei.getAlias() != null ? sei.getAlias().getName() : null;
        if (alias != null) {
            selectDef.getColumns().add(alias);
        } else {
            if (sei.getExpression() instanceof Column) {
                Column col = (Column) sei.getExpression();
                selectDef.getColumns().add(CheckExpressionParser.fixColumnName(col.getColumnName()));
            } else {
                throw new QueryException("Complex return expressions must have alias defined: %s", sei.getExpression());
            }
        }
        sei.getExpression().accept(expParser);
    }

    @Override
    public void visit(OrderByElement ob) {
        ob.getExpression().accept(expParser);
    }

    @Override
    public void visit(SetOperationList setOpList) {
        throw new QueryException("Set operations are not supported: %s", setOpList.toString());
    }

    @Override
    public void visit(WithItem withItem) {
        throw new QueryException("With construct is not supported: %s", withItem.toString());
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        throw new QueryException("Lateral subselects are not supported: %s", lateralSubSelect.toString());
    }

    @Override
    public void visit(ValuesList valuesList) {
        valuesList.getMultiExpressionList().accept(expParser);
    }
    
}
