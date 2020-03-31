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
package lt.emasina.resthub.server.cache;

import lt.emasina.resthub.server.query.Query;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CcHisto extends CcBase<List<Object[]>> {

    private static final long serialVersionUID = 1L;

    public void addRow(Query query, Object row) throws SQLException {
        if (getValue() == null) {
            setValue(new ArrayList<>());
        }

        if (row == null || ! row.getClass().isArray()) {
            getValue().add(new Object[] { row });
        } else {
            getValue().add((Object[]) row);
        }

    }

}