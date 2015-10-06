package net.resthub.parser;

import java.util.Iterator;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.KeepExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.MySQLGroupConcat;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.NumericBind;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.WithinGroupExpression;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.OrderByVisitor;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.statement.select.WithItem;

/**
 * SelectParser
 * @author valdo
 */
public abstract class AbstractAllParser implements SelectVisitor, FromItemVisitor, SelectItemVisitor, ExpressionVisitor, OrderByVisitor, ItemsListVisitor {

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

    @Override
    public void visit(SelectExpressionItem sei) {
        sei.getExpression().accept(this);
    }

    // ExpressionVisitor

    @Override
    public void visit(NullValue nv) {
        // Fine
    }

    @Override
    public void visit(Function fnctn) {
        if (fnctn.getParameters() != null) {
            for (Iterator it = fnctn.getParameters().getExpressions().iterator(); it.hasNext();) {
                ((Expression) it.next()).accept(this);
            }
        }
    }

    @Override
    public void visit(JdbcParameter jp) { }

    @Override
    public void visit(DoubleValue dv) { }

    @Override
    public void visit(LongValue lv) { }

    @Override
    public void visit(DateValue dv) { }

    @Override
    public void visit(TimeValue tv) { }

    @Override
    public void visit(TimestampValue tv) { }

    @Override
    public void visit(Parenthesis prnths) {
        prnths.getExpression().accept(this);
    }

    @Override
    public void visit(StringValue sv) { }

    @Override
    public void visit(Addition adtn) {
        visitBinaryExpression(adtn);
    }

    @Override
    public void visit(Division dvsn) {
        visitBinaryExpression(dvsn);
    }

    @Override
    public void visit(Multiplication m) {
        visitBinaryExpression(m);
    }

    @Override
    public void visit(Subtraction s) {
        visitBinaryExpression(s);
    }

    @Override
    public void visit(AndExpression ae) {
        visitBinaryExpression(ae);
    }

    @Override
    public void visit(OrExpression oe) {
        visitBinaryExpression(oe);
    }

    @Override
    public void visit(Between btwn) {
        btwn.getLeftExpression().accept(this);
        btwn.getBetweenExpressionStart().accept(this);
        btwn.getBetweenExpressionEnd().accept(this);
    }

    @Override
    public void visit(EqualsTo et) {
        visitBinaryExpression(et);
    }

    @Override
    public void visit(GreaterThan gt) {
        visitBinaryExpression(gt);
    }

    @Override
    public void visit(GreaterThanEquals gte) {
        visitBinaryExpression(gte);
    }

    @Override
    public void visit(InExpression ie) {
        if (ie.getLeftExpression() != null) {
            ie.getLeftExpression().accept(this);
        }
        if (ie.getLeftItemsList() != null) {
            ie.getLeftItemsList().accept(this);
        }
        ie.getRightItemsList().accept(this);
    }

    @Override
    public void visit(IsNullExpression ine) {
        ine.getLeftExpression().accept(this);
    }

    @Override
    public void visit(LikeExpression le) {
        visitBinaryExpression(le);
    }

    @Override
    public void visit(MinorThan mt) {
        visitBinaryExpression(mt);
    }

    @Override
    public void visit(MinorThanEquals mte) {
        visitBinaryExpression(mte);
    }

    @Override
    public void visit(NotEqualsTo net) {
        visitBinaryExpression(net);
    }

    @Override
    public void visit(Column column) { }

    @Override
    public void visit(CaseExpression ce) {
        for (Iterator it = ce.getWhenClauses().iterator(); it.hasNext();) {
            ((WhenClause) it.next()).accept(this);
        }
        ce.getSwitchExpression().accept(this);
        ce.getElseExpression().accept(this);
    }

    @Override
    public void visit(WhenClause wc) {
        wc.getWhenExpression().accept(this);
        wc.getThenExpression().accept(this);
    }

    @Override
    public void visit(ExistsExpression ee) {
        ee.getRightExpression().accept(this);
    }

    @Override
    public void visit(AllComparisonExpression ace) {
        ace.getSubSelect().getSelectBody().accept(this);
    }

    @Override
    public void visit(AnyComparisonExpression ace) {
        ace.getSubSelect().getSelectBody().accept(this);
    }

    @Override
    public void visit(Concat concat) {
        visitBinaryExpression(concat);
    }

    @Override
    public void visit(Matches mtchs) {
        visitBinaryExpression(mtchs);
    }

    @Override
    public void visit(BitwiseAnd ba) {
        visitBinaryExpression(ba);
    }

    @Override
    public void visit(BitwiseOr bo) {
        visitBinaryExpression(bo);
    }

    @Override
    public void visit(BitwiseXor bx) {
        visitBinaryExpression(bx);
    }
    
    // OrderByVisitor
    
    @Override
    public void visit(OrderByElement obe) {
        obe.getExpression().accept(this);
    }
    
    // ItemsListVisitor
    
    @Override
    public void visit(ExpressionList el) {
        for (Iterator it = el.getExpressions().iterator(); it.hasNext();) {
            ((Expression) it.next()).accept(this);
        }
    }

    // Utility
    
    protected void visitBinaryExpression(BinaryExpression be) {
        be.getLeftExpression().accept(this);
        be.getRightExpression().accept(this);
    }

    @Override
    public void visit(SetOperationList sol) {
        if (sol.getOrderByElements() != null) {
            for (OrderByElement ob: sol.getOrderByElements()) {
                ob.accept(this);
            }
        }
        if (sol.getSelects() != null) {
            for (SelectBody ps: sol.getSelects()) {
                ps.accept(this);
            }
        }
    }

    @Override
    public void visit(WithItem wi) {
        if (wi.getWithItemList() != null) {
            for (SelectItem si: wi.getWithItemList()) {
                si.accept(this);
            }
        }
        wi.getSelectBody().accept(this);
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        lateralSubSelect.getSubSelect().getSelectBody().accept(this);
    }

    @Override
    public void visit(ValuesList vl) {
        vl.getMultiExpressionList().accept(this);
    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
    }

    @Override
    public void visit(CastExpression cast) {
        cast.getLeftExpression().accept(this);
    }

    @Override
    public void visit(Modulo modulo) {
        visitBinaryExpression(modulo);
    }

    @Override
    public void visit(AnalyticExpression aexpr) {
        if (aexpr.getDefaultValue() != null) {
            aexpr.getDefaultValue().accept(this);
        }
        if (aexpr.getExpression() != null) {
            aexpr.getExpression().accept(this);
        }
        if (aexpr.getOffset() != null) {
            aexpr.getOffset().accept(this);
        }
        if (aexpr.getPartitionExpressionList() != null) {
            for (Expression el: aexpr.getPartitionExpressionList().getExpressions()) {
                el.accept(this);
            }
        }
        if (aexpr.getOrderByElements() != null) {
            for (OrderByElement ob: aexpr.getOrderByElements()) {
                ob.accept(this);
            }
        }
    }

    @Override
    public void visit(ExtractExpression eexpr) {
        eexpr.getExpression().accept(this);
    }

    @Override
    public void visit(IntervalExpression iexpr) {
        System.out.println("Interval!");
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        for (ExpressionList el: multiExprList.getExprList()) {
            el.accept(this);
        }
    }

    @Override
    public void visit(SignedExpression se) {
        se.getExpression().accept(this);
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
    public void visit(HexValue hv) { }

    @Override
    public void visit(WithinGroupExpression wge) {
        wge.getExprList().accept(this);
        for (OrderByElement obe: wge.getOrderByElements()) {
            obe.accept(this);
        }
    }

    @Override
    public void visit(JsonExpression je) {
        je.getColumn().accept(this);
    }

    @Override
    public void visit(RegExpMySQLOperator rmsql) { 
        rmsql.getLeftExpression().accept(this);
        rmsql.getRightExpression().accept(this);
    }

    @Override
    public void visit(UserVariable uv) { }

    @Override
    public void visit(NumericBind nb) { }

    @Override
    public void visit(KeepExpression ke) {
        for (OrderByElement obe: ke.getOrderByElements()) {
            obe.accept(this);
        }
    }

    @Override
    public void visit(MySQLGroupConcat msqlgc) {
        msqlgc.getExpressionList().accept(this);
        for (OrderByElement obe: msqlgc.getOrderByElements()) {
            obe.accept(this);
        }
    }

    @Override
    public void visit(RowConstructor rc) {
        rc.getExprList().accept(this);
    }
    
}
