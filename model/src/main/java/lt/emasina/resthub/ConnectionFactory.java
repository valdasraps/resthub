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
package lt.emasina.resthub;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import oracle.jdbc.OracleConnection;

/**
 * ConnectionFactory
 * @author valdo
 */
public interface ConnectionFactory extends Serializable {
    
    public Collection<String> getConnectionNames();
    public OracleConnection getConnection(String name) throws SQLException;
    public String getUrl(String name);
    public String getUsername(String name);
    public String getPassword(String name);
    
}
