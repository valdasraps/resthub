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
package lt.emasina.resthub.model;

import java.sql.Types;

/**
 * MdType
 * @author valdo
 */
public enum MdType {

    STRING,
    NUMBER,
    DATE,
    CLOB,
    BLOB;
    
    public static MdType getMdType(int t) {
        switch (t) {
            
            case Types.BIGINT:
            case Types.INTEGER:
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.NUMERIC:
                return NUMBER;

            case Types.TIMESTAMP:
            case Types.DATE:
                return DATE;
                
            case Types.VARCHAR:
            case Types.CHAR:
                return STRING;
                
            case Types.CLOB:
                return CLOB;
                
            case Types.BLOB:
                return BLOB;
                
            default:
                throw new IllegalArgumentException(String.format("Unsupported SQL type: %d", t));
        }
    }

}
