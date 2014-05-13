package net.resthub.server.converter;

import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import lombok.RequiredArgsConstructor;
import net.resthub.model.MdColumn;
import net.resthub.server.handler.DataHandler;

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
    public abstract void visitCol();
    public abstract void endRow();
    
    public void visit(List<Object[]> data) {
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