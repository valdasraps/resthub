package lt.emasina.resthub.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import net.sf.ehcache.util.concurrent.ConcurrentHashMap;
import org.restlet.data.Reference;

/**
 * ServerAppConfig
 * @author valdo
 */
@Log4j
@Getter
public class ServerAppConfig {

    private final List<PatternPair> refRewritePats = new ArrayList<>();
    private final Map<Reference, Reference> refCache = new ConcurrentHashMap<>();
    
    public void addReferenceRewrite(String searchStr, String replacement) {
        refRewritePats.add(new PatternPair(Pattern.compile(searchStr), replacement));
    }
    
    public URL getReference(final Reference baseRef, String query, Object... pathElement) {
        return getReference(baseRef, query, Arrays.asList(pathElement));
    }
    
    public URL getReference(final Reference baseRef, String query, List<?> parts) {

        if (!refCache.containsKey(baseRef)) {
            refCache.put(baseRef, prepareReference(baseRef));
        }
        Reference r = new Reference(refCache.get(baseRef));
        
        String currPath = r.getPath();
        StringBuilder sb = new StringBuilder(currPath == null ? "" : currPath);
        for (Object pe : parts) {
            if (pe instanceof String && ((String) pe).startsWith("/")) {
                sb.append(pe);
            } else {
                sb.append("/").append(pe);
            }
        }
        r.setPath(sb.toString());
        
        if (query != null) {
            r.setQuery(query);
        }
        
        return r.toUrl();
    }
    
    private Reference prepareReference(final Reference baseRef) {
        if (refRewritePats.isEmpty()) {
            return new Reference(baseRef);
        }
        
        String u = baseRef.toUrl().toString();
        boolean replaced = Boolean.FALSE;
        for(PatternPair pp: refRewritePats) {
            Matcher m = pp.getSearch().matcher(u);
            if (m.matches()) {
                u = m.replaceAll(pp.getReplacement());
                replaced = Boolean.TRUE;
            }
        }

        if (replaced) {
            try {
                return new Reference(new URL(u));
            } catch (MalformedURLException ex) {
                log.warn(String.format("Problem while rewritting URL: %s", u), ex);
                return new Reference(baseRef);
            }
        } else {
            return new Reference(baseRef);
        }

    }
    
    @Getter
    @RequiredArgsConstructor
    private class PatternPair {
        
        private final Pattern search;
        private final String replacement;
        
    }
    
}