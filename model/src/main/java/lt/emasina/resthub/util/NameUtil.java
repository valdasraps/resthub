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
package lt.emasina.resthub.util;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CNameUtil
 * @author valdo
 */
public class NameUtil {
    
    private static final Pattern CNAME_PATTERN = Pattern.compile(
            "^[A-Z_a-z\\u00C0\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02ff\\u0370-\\u037d"
          + "\\u037f-\\u1fff\\u200c\\u200d\\u2070-\\u218f\\u2c00-\\u2fef\\u3001-\\ud7ff"
          + "\\uf900-\\ufdcf\\ufdf0-\\ufffd]"
          + "[A-Z_a-z\\u00C0\\u00D6\\u00D8-\\u00F6"
          + "\\u00F8-\\u02ff\\u0370-\\u037d\\u037f-\\u1fff\\u200c\\u200d\\u2070-\\u218f"
          + "\\u2c00-\\u2fef\\u3001-\\udfff\\uf900-\\ufdcf\\ufdf0-\\ufffd\\-\\.0-9"
          + "\\u00b7\\u0300-\\u036f\\u203f-\\u2040]*\\Z");

    private static final Pattern FIRST_CHAR_PATTERN = Pattern.compile(
            "^[A-Z_a-z\\u00C0\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02ff\\u0370-\\u037d"
          + "\\u037f-\\u1fff\\u200c\\u200d\\u2070-\\u218f\\u2c00-\\u2fef\\u3001-\\ud7ff"
          + "\\uf900-\\ufdcf\\ufdf0-\\ufffd]");
    
    private static final Pattern BAD_CHAR_PATTERN = Pattern.compile(
            "[^A-Z_a-z\\u00C0\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02ff\\u0370-\\u037d"
          + "\\u037f-\\u1fff\\u200c\\u200d\\u2070-\\u218f\\u2c00-\\u2fef\\u3001-\\udfff"
          + "\\uf900-\\ufdcf\\ufdf0-\\ufffd\\-\\.0-9\\u00b7\\u0300-\\u036f"
          + "\\u203f-\\u2040]");
    
    public static boolean isCName(String name) {
        return CNAME_PATTERN.matcher(name).matches();
    }
    
    public static boolean isOraName(String name) {
        if (name != null) {
            for (char c: name.toCharArray()) {
                if (!Character.isJavaIdentifierPart(c)) {
                    return false;
                }
            }           
            return true;
        }
        return false;
    }
    
    public static String getCName(String name) {
        if (isCName(name)) {
            return name;
        }
        
        Matcher firstName = FIRST_CHAR_PATTERN.matcher(name);
        if (!firstName.find()) {
            name = "_" + name;
        }
        
        Matcher badChar = BAD_CHAR_PATTERN.matcher(name);
        if (badChar.find()) {
            name = badChar.replaceAll("_");
        }
        
        return name;
    }
    
    public static String getJName(String name) {
        String ename = "";
        StringTokenizer tok = new StringTokenizer(name, " _");
        while (tok.hasMoreTokens()) {
            String t = tok.nextToken();
            ename += t.substring(0, 1).toUpperCase().concat(t.substring(1).toLowerCase());
        }
        return ename.substring(0, 1).toLowerCase().concat(ename.substring(1));
    }
    
}
