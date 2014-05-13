package net.resthub.server.query;

import lombok.Getter;
import net.resthub.model.MdParameter;
import net.resthub.model.MdType;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * QueryParameter class
 * @author valdo
 */
public class QueryParameter extends MdParameter {
    
	private static final long serialVersionUID = 1L;

	/**
     * Constructor for RESTful query parameter
     * @param name
     */
    public QueryParameter(String name) {
        this.setArray(Boolean.FALSE);
        this.setId(0L);
        this.setName(name);
        this.setType(MdType.STRING);
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
    
    @Getter
    private final String sqlName;
    
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