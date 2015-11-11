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

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lt.emasina.resthub.model.MdTable;

/**
 * Table factory class
 * @author audrius
 */
public abstract class TableFactory implements AutoCloseable{
    
    protected static final String TABLE_SOURCE_KEY = "Source";

    public abstract boolean isRefresh();
    public abstract List<MdTable> getTables() throws Exception;
    
    @Getter @Setter
    private TableFactory next = null;

    public void closeAll() throws Exception {
        TableFactory tf = this;
        while (tf != null) {
            tf.close();
            tf = tf.getNext();
        }
    }
    
    public static class Builder {
        
        private TableFactory head;
        
        public Builder add(TableFactory tf) {
            TableFactory c = this.head;
            if (c == null) this.head = tf;
            else {
                while (c.getNext() != null) c = c.getNext();
                c.setNext(tf);
            }
            return this;
        }
        
        public TableFactory build() {
            return this.head;
        }
        
    }
    
}
