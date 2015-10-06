package net.resthub.parser;

import java.util.Iterator;
import net.sf.jsqlparser.expression.AnalyticExpression;
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
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.RowConstructor;
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
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import net.sf.jsqlparser.schema.Column;

/**
 * SelectParser
 * @author valdo
 */
public abstract class AbstractExpressionParser implements ExpressionVisitor, ItemsListVisitor {

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
        aexpr.getExpression().accept(this);
    }

    @Override
    public void visit(ExtractExpression eexpr) {
        eexpr.getExpression().accept(this);
    }

    @Override
    public void visit(IntervalExpression iexpr) {
    }
    
    // ItemsListVisitor
    
    @Override
    public void visit(ExpressionList el) {
        for (Iterator it = el.getExpressions().iterator(); it.hasNext();) {
            ((Expression) it.next()).accept(this);
        }
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        for (ExpressionList expl: multiExprList.getExprList()) {
            expl.accept(this);
        }
    }
    
    // Utility
    
    protected void visitBinaryExpression(BinaryExpression be) {
        be.getLeftExpression().accept(this);
        be.getRightExpression().accept(this);
    }

    @Override
    public void visit(HexValue hv) { }

    @Override
    public void visit(WithinGroupExpression wge) {
        wge.getExprList().accept(this);
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
    public void visit(KeepExpression ke) { }

    @Override
    public void visit(MySQLGroupConcat msqlgc) {
        msqlgc.getExpressionList().accept(this);
    }

    @Override
    public void visit(RowConstructor rc) {
        rc.getExprList().accept(this);
    }
    
}
