package net.resthub.server.cache;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.resthub.server.query.Query;

public class CcData extends CcBase<List<Object[]>> {

    private static final long serialVersionUID = 1L;

    public void addRow(Query query, Object row) throws SQLException {
    	if (getValue() == null) {
            setValue(new ArrayList<Object[]>());
    	}
        
        if (row == null || ! row.getClass().isArray()) {
            getValue().add(new Object[] { row });
        } else {
            getValue().add((Object[]) row);
        }
        
    }
	
}
