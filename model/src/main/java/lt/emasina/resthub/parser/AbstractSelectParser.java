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
