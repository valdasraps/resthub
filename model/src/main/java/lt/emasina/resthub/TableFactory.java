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

import java.util.Date;
import java.util.List;
import lt.emasina.resthub.model.MdTable;

/**
 * Table factory interface
 * @author audrius
 */
public interface TableFactory extends AutoCloseable{
    
    public boolean isRefreshable();
    public boolean isRefresh(Date lastUpdate);
    public List<MdTable> getTables() throws Exception;
    
}
