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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import lt.emasina.resthub.TableFactory;
import lt.emasina.resthub.model.MdTable;
import lt.emasina.resthub.model.MdTable_;

public class DbTableFactory implements TableFactory {
    
    private final EntityManagerFactory emf;
    private final EntityManager em;
    
    public DbTableFactory(String persistenceUnitName) {
        this.emf = Persistence.createEntityManagerFactory(persistenceUnitName);
        this.em = emf.createEntityManager();
    }
    
    public DbTableFactory(String persistenceUnitName, String dbUrl, String dbUser, String dbPassword) {
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.url", dbUrl);
        properties.put("javax.persistence.jdbc.user", dbUser);
        properties.put("javax.persistence.jdbc.password", dbPassword);
        this.emf = Persistence.createEntityManagerFactory(persistenceUnitName, properties);
        this.em = emf.createEntityManager();
    }
    
    @Override
    public boolean isRefresh(Date lastUpdate) {    
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // If lastUpdate exists - check if it has changed
        CriteriaQuery<Date> cq = cb.createQuery(Date.class);
        Root<MdTable> root = cq.from(MdTable.class);

        cq.select(cb.greatest(root.get(MdTable_.updateTime)));
        Date newLastUpdate = em.createQuery(cq).getSingleResult();
        boolean doRefresh = (newLastUpdate == null && lastUpdate != null) || 
                            (newLastUpdate != null && lastUpdate == null) || 
                            (newLastUpdate != null && lastUpdate != null && newLastUpdate.after(lastUpdate));
        return doRefresh;
    }


    @Override
    public List<MdTable> getTables() {
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MdTable> cq = cb.createQuery(MdTable.class);
        cq.from(MdTable.class);
        
        List<MdTable> tables = new ArrayList<>();

        // Update or add tables
        for(MdTable t: em.createQuery(cq).getResultList()) {
            em.detach(t);
            tables.add(t);
        }
        
        return tables;

    }

    @Override
    public void close() throws Exception {
        this.em.close();
        this.emf.close();
    }

    @Override
    public boolean isRefreshable() {
        return true;
    }
    
}
