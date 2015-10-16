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
package lt.emasina.resthub.server.table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lt.emasina.resthub.model.MdTable;

/**
 * ResourceId
 * @author valdo
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(of = {"namespace", "name"})
public class TableId {

    private final String namespace;
    private final String name;
    
    public TableId(MdTable t) {
        this(t.getNamespace(), t.getName());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.append(namespace).append(".").append(name).toString();
    }
    
}
