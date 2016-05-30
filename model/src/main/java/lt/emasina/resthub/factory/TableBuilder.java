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
package lt.emasina.resthub.factory;

import lt.emasina.resthub.parser.SqlParser;
import com.google.inject.Inject;
import java.io.Serializable;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import oracle.jdbc.OracleConnection;
import lt.emasina.resthub.ConnectionFactory;
import lt.emasina.resthub.exception.QueryException;
import lt.emasina.resthub.model.MdColumn;
import lt.emasina.resthub.model.MdParameter;
import lt.emasina.resthub.model.MdType;
import lt.emasina.resthub.util.NameUtil;

public class TableBuilder implements Serializable {

    @Inject
    private CCJSqlParserManager pm;
    
    @Inject
    private ConnectionFactory cf;

    public void collectParameters(final String sql, Collection<MdParameter> parameters) throws Exception {
        SqlParser parser = new SqlParser();
        
        Statement stmt = pm.parse(new StringReader(sql));
        if (stmt instanceof Select) {
            ((Select) stmt).getSelectBody().accept(parser);
        } else {
            throw new Exception("Only SELECT statements allowed!");
        }        
        
        List<MdParameter> tempParameters = new ArrayList<>();

        for (String name: parser.getParameterNames()) {
            MdParameter param = null;

            for (MdParameter p: parameters) {
                if (p.getName().equalsIgnoreCase(name)) {
                    param = p;
                    break;
                }
            }

            if (param == null) {
                param = new MdParameter();
                param.setName(name);
                param.setType(MdType.STRING);
                param.setArray(Boolean.FALSE);
            } else {
                param.setName(name.toLowerCase());
            }

            param.beforeSave();
            tempParameters.add(param);

        }

        parameters.clear();
        parameters.addAll(tempParameters);
        
    }
    
    public void collectColumns(final String connectionName, final String sql, List<MdColumn> columns) throws Exception {
        
        try (OracleConnection con = cf.getConnection(connectionName)) {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                
                List<String> columnNames = new ArrayList<>();
                List<MdColumn> tempColumns = new ArrayList<>();
                
                // Create missing columns
                ResultSetMetaData md = ps.getMetaData();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    
                    String name = md.getColumnName(i);
                    MdColumn col = null;
                    for (MdColumn c: columns) {
                        if (c.getName().equals(name)) {
                            col = c;
                            break;
                        }
                    }
                    
                    // Check repeating column names
                    if (columnNames.contains(name)) {
                        throw new QueryException("Non unique column names defined!");
                    }
                    columnNames.add(name);
                    
                    if (col == null) {
                        col = new MdColumn();
                        col.setName(name);
                        col.setCName(NameUtil.getCName(name));
                        col.setJName(NameUtil.getJName(name));
                    }

                    col.setNumber(i);
                    col.setType(MdType.getMdType(md.getColumnType(i)));
                    col.beforeSave();
                    
                    tempColumns.add(col);
                    
                }

                columns.clear();
                columns.addAll(tempColumns);
                
            }
        }
    }
    
}
