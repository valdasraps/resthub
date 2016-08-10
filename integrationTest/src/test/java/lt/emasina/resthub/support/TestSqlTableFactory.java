package lt.emasina.resthub.support;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lt.emasina.resthub.factory.SqlTableFactory;
import lt.emasina.resthub.model.MdTable;

public class TestSqlTableFactory extends SqlTableFactory {

    @Override
    public String getTablesSql() {
        return "select owner, object_name, REGEXP_REPLACE(object_name, '_SQLTEST', '') from all_objects where owner = :1 and object_name like '%_SQLTEST'";
    }

    @Override
    protected void applyParameters(PreparedStatement ps) throws SQLException {
        ps.setString(1, "RESTHUB_TEST");
    }

    @Override
    public String getConnectionName() {
        return "default";
    }

    @Override
    public MdTable getMdTable(ResultSet rs) throws SQLException {
        MdTable t = new MdTable();
        t.setConnectionName("default");
        
        t.setSql(new StringBuilder("select * from ")
                .append(rs.getString(1))
                .append(".")
                .append(rs.getString(2))
                .toString());
        t.setName(rs.getString(3).toLowerCase());
        t.setNamespace("folder");
        return t;
    }

}
