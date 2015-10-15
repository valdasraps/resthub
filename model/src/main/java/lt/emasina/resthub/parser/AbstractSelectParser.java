package lt.emasina.resthub.parser;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.OrderByVisitor;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * SelectParser
 * @author valdo
 */
public abstract class AbstractSelectParser implements SelectVisitor, FromItemVisitor, SelectItemVisitor, OrderByVisitor {

    // FromItemVisitor
    
    @Override
    public void visit(Table table) {
    }

    @Override
    public void visit(SubSelect ss) {
        ss.getSelectBody().accept(this);
    }

    @Override
    public void visit(SubJoin sj) {
        sj.getJoin().getRightItem().accept(this);
    }

    // SelectItemVisitor
    
    @Override
    public void visit(AllColumns ac) { }

    @Override
    public void visit(AllTableColumns atc) { }

}
