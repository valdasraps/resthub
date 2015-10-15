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
