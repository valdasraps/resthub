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
package lt.emasina.resthub.server.parser.check;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * TableDef
 * @author valdo
 */
@Getter
@RequiredArgsConstructor
public abstract class SelectDef {

    private final SelectDef parent;
    
    private final List<String> columns = new ArrayList<>();
    
    public abstract boolean isSameConnectionName(String connectionName);

    public SubSelectDef getTop() {
        if (parent == null) {
            return (SubSelectDef) this;
        } else {
            return parent.getTop();
        }
    }
    
}
