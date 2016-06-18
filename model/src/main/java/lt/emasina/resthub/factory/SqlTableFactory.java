/*
 * #%L
 * model
 * %%
 * Copyright (C) 2012 - 2016 valdasraps
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
package lt.emasina.resthub.factory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.TableFactory;
import lt.emasina.resthub.model.MdTable;
import oracle.jdbc.OracleConnection;

@Log4j
@RequiredArgsConstructor
public abstract class SqlTableFactory extends TableFactory {

    /**
     * MdTable + Metadata hashcode
     */
    private final Map<MdTable, Integer> tableCodes = new HashMap<>();

    public abstract String getTablesSql();
    public abstract String getConnectionName();
    public abstract MdTable getMdTable(ResultSet rs) throws SQLException;
    
    protected void applyParameters(PreparedStatement ps) throws SQLException { }
    
    @Override
    public boolean isRefresh() {
        boolean refresh = false;
        try (OracleConnection conn = getCf().getConnection(getConnectionName())) {
            
            Map<MdTable, Integer> tables= new HashMap<>();
            tables.putAll(tableCodes);
            tableCodes.clear();
            
            for (MdTable t: this.getTablesInternal()) {
                
                Integer hc = 0;
                try {
                    hc = getMdTableHashcode(conn, t);
                } catch (SQLException ex) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Error while reading metadata for %s", t), ex);
                    }
                }
                
                if (tables.containsKey(t)) {
                    if (!Objects.equals(tables.get(t), hc)) {
                        refresh = true;
                    }
                    tables.remove(t);
                } else {
                    refresh = true;
                }
                
                tableCodes.put(t, hc);
                
            }
            
            if (!tables.isEmpty()) {
                refresh = true;
            }
            
        } catch (SQLException ex) {
            log.error("Error while connecting to DB", ex);
        }
        return refresh;
    }
    
    private Integer getMdTableHashcode(OracleConnection conn, MdTable t) throws SQLException {
        StringBuilder sb = new StringBuilder();
        try (PreparedStatement ps = conn.prepareStatement(t.getSql())) {
            ResultSetMetaData md = ps.getMetaData();
            for (int c = 1; c <= md.getColumnCount(); c++) {
                sb.append(md.getColumnName(c))
                  .append(" ")
                  .append(md.getColumnTypeName(c))
                  .append("(")
                  .append(md.getScale(c))
                  .append(",")
                  .append(md.getPrecision(c))
                  .append(");");
            }
        }
        return sb.toString().hashCode();
    }
    
    private List<MdTable> getTablesInternal() throws SQLException {
        List<MdTable> tables = new ArrayList<>();
        try (OracleConnection conn = getCf().getConnection(getConnectionName())) {
            try (PreparedStatement ps = conn.prepareStatement(getTablesSql())) {
                applyParameters(ps);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        tables.add(getMdTable(rs));
                    }
                }
            }
        }
        return tables;
    }

    @Override
    public List<MdTable> getTables() throws Exception {
        return getTablesInternal();
    }

    @Override
    public void close() throws Exception {
        tableCodes.clear();
    }

}
