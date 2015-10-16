/*
 * #%L
 * server
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