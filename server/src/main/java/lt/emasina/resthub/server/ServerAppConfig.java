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
package lt.emasina.resthub.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.restlet.data.Reference;

/**
 * ServerAppConfig
 * @author valdo
 */
@Log4j
@Getter
public class ServerAppConfig {

    private static final int UPDATE_INTERVAL_SEC = 120;
    private static final String RESTHUB_PROPERTIES = "/resthub.properties";
    private static final String VERSION_UNDEFINED = "undefined";
    
    @Setter
    private int updateInterval = UPDATE_INTERVAL_SEC;
    
    @Setter
    private String serviceVersion = VERSION_UNDEFINED;
    
    private final String resthubVersion;
    
    public ServerAppConfig() {
        //ServerAppConfig.class
        Properties props = new Properties();
        try (InputStream is = ServerAppConfig.class.getResourceAsStream(RESTHUB_PROPERTIES)) {
            props.load(is);
        } catch (IOException ex) {
            log.warn("Error while loading properties file " + RESTHUB_PROPERTIES, ex);
        }
        this.resthubVersion = props.getProperty("resthub_version", VERSION_UNDEFINED);
    }
    
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