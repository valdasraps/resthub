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
package lt.emasina.resthub.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.ConnectionFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

/**
 * ConnectionManager class
 * @author valdo
 */
@Log4j
@Singleton
public class ConnectionManager implements AutoCloseable {

    private static final String PROPERTY_URL = "hibernate.connection.url";
    private static final String PROPERTY_USERNAME = "hibernate.connection.username";
    private static final String PROPERTY_PASSWORD = "hibernate.connection.password";
    private static final String PROPERTY_SHOWSQL = "hibernate.show_sql";
    private static final String PROPERTY_FORMATSQL = "hibernate.format_sql";
    
    @Inject
    @Getter
    private ConnectionFactory cf;
    
    private final Map<String, SessionFactory> factories = new ConcurrentHashMap<>();
    
    public Session getSession(String name) {
        if (!factories.containsKey(name)) {
            
            Configuration cfg = new Configuration();
            cfg.configure();
            
            cfg.setProperty(PROPERTY_URL, cf.getUrl(name));
            cfg.setProperty(PROPERTY_USERNAME, cf.getUsername(name));
            cfg.setProperty(PROPERTY_PASSWORD, cf.getPassword(name));
            
            if (log.isDebugEnabled()) {
                cfg.setProperty(PROPERTY_SHOWSQL, "true");
                cfg.setProperty(PROPERTY_FORMATSQL, "true");
            }
            
            if (log.isDebugEnabled()) {
                for (Map.Entry<Object,Object> e: cfg.getProperties().entrySet()) {
                    log.debug(String.format("%s property: %s = %s", name, e.getKey(), e.getValue()));
                }
            }
            
            ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(cfg.getProperties()).buildServiceRegistry(); 
            factories.put(name, cfg.buildSessionFactory(serviceRegistry));
            
        }
        return factories.get(name).openSession();
    }

    @Override
    public void close() throws Exception {
        for (String name: factories.keySet()) {
            factories.remove(name).close();
        }
    }
    
}
