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
import lt.emasina.resthub.model.MdColumn;
import lt.emasina.resthub.model.MdParameter;
import lt.emasina.resthub.model.MdType;
import lt.emasina.resthub.util.CNameUtil;

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
                    if (col == null) {
                        col = new MdColumn();
                        col.setName(name);
                        col.setCName(CNameUtil.normalize(name));
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
