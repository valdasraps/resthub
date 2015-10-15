package lt.emasina.resthub.server.query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lt.emasina.resthub.model.MdParameter;
import lt.emasina.resthub.model.MdType;

/**
 * QueryParameter class
 * @author valdo
 */
public class QueryParameter extends MdParameter {
    
    private static final Pattern TYPED_PARAM_NAME = Pattern.compile("^([snd])__(.+)$", Pattern.CASE_INSENSITIVE);
    private static final long serialVersionUID = 1L;

    @Getter
    private final String sqlName;
    
    /**
     * Constructor for RESTful query parameter
     * @param name
     */
    public QueryParameter(String name) {
        this.setArray(Boolean.FALSE);
        this.setId(0L);
        
        Matcher m = TYPED_PARAM_NAME.matcher(name);
        if (m.matches()) {
            this.setName(m.group(2));
            switch (m.group(1)) {
                case "s":
                case "S":
                    this.setType(MdType.STRING);
                    break;
                case "n":
                case "N":
                    this.setType(MdType.NUMBER);
                    break;
                case "d":
                case "D":
                    this.setType(MdType.DATE);
                    break;
            }
        } else {
            this.setName(name);
            this.setType(MdType.STRING);
        }
        this.sqlName = name;
    }
    
    /**
     * Constructor for table query parameter
     * @param p 
     * @param alias 
     */
    public QueryParameter(MdParameter p, String alias) {
        this.setArray(p.getArray());
        this.setCreateTime(p.getCreateTime());
        this.setCreateUser(p.getCreateUser());
        this.setId(p.getId());
        this.setMetadata(p.getMetadata());        
        this.setName(alias + "." + p.getName());
        this.setType(p.getType());
        this.setUpdateTime(p.getUpdateTime());
        this.setUpdateUser(p.getUpdateUser());
        this.sqlName = alias + "_" + p.getName();
    }
    
    public String toString(Object value) {
        StringBuilder svalue = new StringBuilder();
        if (value == null) {
            svalue.append(value);
        } else {
            svalue.append(getArray() ? "[" : "");
            boolean first = true;
            for (Object v: getArray() ? (Object[]) value : new Object[] { value }) {
                svalue
                    .append(!first ? "," : "")
                    .append(v)
                    .append(" (")
                    .append(v.getClass().getSimpleName())
                    .append(")");
                first = false;
            }
            svalue.append(getArray() ? "]" : "");
        }
        return String.format("%s: sql=%s, array=%s, value=%s", getName(), getSqlName(), getArray(), svalue);
    }

}