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
package lt.emasina.resthub.server.converter;

import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lt.emasina.resthub.model.MdColumn;
import lt.emasina.resthub.server.handler.DataHandler;

import org.restlet.data.Reference;

/**
 * RowColIterator
 * @author valdo
 */
@RequiredArgsConstructor
public abstract class DataVisitor {

    private final DataHandler handler;
    
    protected Integer rowNumber;
    protected Integer colNumber;
    protected MdColumn column;
    protected Object value;
    
    public abstract void startRow();
    public abstract void visitCol() throws Exception;
    public abstract void endRow();
    
    public void visit(List<Object[]> data) throws Exception {
        if (data != null) {
            for (rowNumber = 0; rowNumber < data.size(); rowNumber++) {
                Object[] row = data.get(rowNumber);
                startRow();

                Iterator<Object> it = Arrays.asList(row).iterator();
                for (colNumber = 0; colNumber < handler.getQuery().getColumns().size(); colNumber++) {
                    column = handler.getQuery().getColumns().get(colNumber);
                    value = it.next();
                    visitCol();
                }

                endRow();

            }
        }
    }

    public URL getLobReference(Reference ref) {
        if (value == null) {
            return null;
        } else {
            return handler.getReference(ref, rowNumber, colNumber, "lob");
        }
    }

}