package lt.emasina.resthub.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CNameUtil
 * @author valdo
 */
public class CNameUtil {
    
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
    
    public static boolean isValid(String name) {
        return CNAME_PATTERN.matcher(name).matches();
    }
    
    public static String normalize(String name) {
        if (isValid(name)) {
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
    
    
}
