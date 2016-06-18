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
package lt.emasina.resthub.server.factory;

import com.google.inject.persist.Transactional;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.ConnectionFactory;
import lt.emasina.resthub.TableFactory;
import lt.emasina.resthub.factory.TableBuilder;
import org.apache.commons.beanutils.BeanUtils;
import lt.emasina.resthub.server.table.TableId;
import lt.emasina.resthub.server.table.ServerTable;
import lt.emasina.resthub.model.MdTable;
import org.json.JSONObject;

@Log4j
@Singleton
public class MetadataFactory implements MetadataFactoryIf {

    private final static String ERROR_METADATA_KEY = "Error";

    private final TableMaps tables = new TableMaps();

    @Inject
    private ResourceFactory rf;

    @Inject
    private QueryFactory qf;

    @Inject
    private TableFactory tfhead;

    @Inject
    private TableBuilder tb;

    @Getter
    private boolean forceRefresh = true;

    @Transactional
    @Override
    public synchronized void refresh() throws Exception {
        TableFactory tf = tfhead;
        while (tf != null) {

            boolean doRefresh = forceRefresh || tf.isRefresh();

            if (log.isDebugEnabled()) {
                log.debug(String.format("forceRefresh = %s, doRefresh = %s", forceRefresh, doRefresh));
            }

            // Update is needed!
            if (doRefresh) {

                // Tables to be included into map
                TableMaps toload = new TableMaps();
                
                // Update or add tables
                for (MdTable t : tf.getTables()) {

                    TableId id = new TableId(t);
                    ServerTable st = rf.create(t, tf);

                    st.getTable().getMetadata().remove(ERROR_METADATA_KEY);

                    try {

                        // Check the table
                        tb.collectColumns(t.getConnectionName(), t.getSql(), t.getColumns());
                        tb.collectParameters(t.getSql(), t.getParameters());

                        if (!toload.addTable(tf, id, st)) {
                            log.warn(String.format("Duplicate table definition, skipping: %s", t));
                        }

                    } catch (Exception ex) {

                        if (log.isDebugEnabled()) {
                            log.debug(String.format("Error while adding table, skipping: %s", id), ex);
                        } else {
                            log.warn(String.format("Error while adding table, skipping: %s: %s", id, ex.getMessage()));
                        }

                        toload.addBlacklist(tf, id, st);
                        st.getTable().getMetadata().put(ERROR_METADATA_KEY, ex.getMessage());

                    }
                }

                tables.load(tf, toload);
                        
            }

            tf = tf.getNext();

        }

        forceRefresh = false;

    }

    @Override
    public Collection<ServerTable> getTables() {
        return Collections.unmodifiableCollection(tables.whitelist.values());
    }

    public Collection<ServerTable> getBlacklist() {
        return Collections.unmodifiableCollection(tables.blacklist.values());
    }

    public ServerTable getBlacklistTable(TableId id) {
        return tables.blacklist.get(id);
    }

    public void removeBlacklistTable(TableId id) {
        tables.remove(id);
        forceRefresh = true;
    }

    public void clearBlacklist() {
        tables.clearBlacklist();
        forceRefresh = true;
    }

    public void clearBlacklist(String namespace) {
        for (TableId id : tables.blacklist.keySet()) {
            if (id.getNamespace().equals(namespace)) {
                tables.remove(id);
            }
        }
        forceRefresh = true;
    }

    @Override
    public ServerTable getTable(TableId id) {
        return tables.whitelist.get(id);
    }

    @Override
    public boolean hasTable(TableId id) {
        return tables.whitelist.containsKey(id);
    }

    public static JSONObject mapToJSONObject(Map<?, ?> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        return new JSONObject(map);
    }

    public static void injectPrivateField(Object o, Class<?> fieldHolderClass, String fieldName, Object value) throws Exception {
        Field fResourceMd = fieldHolderClass.getDeclaredField(fieldName);
        fResourceMd.setAccessible(true);
        fResourceMd.set(o, value);
    }

    public static JSONObject beanToJSONObject(Object bean) throws Exception {
        if (bean == null) {
            return null;
        }
        JSONObject o = new JSONObject();
        Map<?, ?> describe = BeanUtils.describe(bean);
        for (Object k : describe.keySet()) {
            if (k instanceof String) {
                String ks = (String) k;
                if (!ks.equals("class")) {
                    o.putOpt(ks, describe.get(k));
                }
            }
        }
        return o;
    }
    
    private class TableMaps  {
        
        private final Map<TableFactory, Set<TableId>> tfs = new ConcurrentHashMap<>();
        private final Map<TableId, ServerTable> whitelist = new ConcurrentHashMap<>();
        private final Map<TableId, ServerTable> blacklist = new ConcurrentHashMap<>();
        
        /**
         * Add table to good table list
         * @param tf TableFactory
         * @param id Table identifier
         * @param st Server table
         * @return true if added, false if it already exists
         */
        public boolean addTable(TableFactory tf, TableId id, ServerTable st) {
            if (this.whitelist.containsKey(id)) {
                return false;
            }
            if (!this.tfs.containsKey(tf)) {
                this.tfs.put(tf, new HashSet<TableId>());
            }
            this.tfs.get(tf).add(id);
            this.whitelist.put(id, st);
            this.blacklist.remove(id);
            return true;
        }
        
        /**
         * Add table to bad table list
         * @param tf TableFactory
         * @param id Table identifier
         * @param st Server table
         * @return true if added, false if it already exists
         */
        public boolean addBlacklist(TableFactory tf, TableId id, ServerTable st) {
            if (this.blacklist.containsKey(id)) {
                return false;
            }
            if (!this.tfs.containsKey(tf)) {
                this.tfs.put(tf, new HashSet<TableId>());
            }
            this.tfs.get(tf).add(id);
            this.blacklist.put(id, st);
            this.whitelist.remove(id);
            return true;
        }
        
        public void remove(TableId id) {
            ServerTable st = blacklist.remove(id);
            if (st == null) st = whitelist.remove(id);
            if (st != null) {
                TableFactory tf = st.getTf();
                tfs.get(tf).remove(id);
                if (tfs.get(tf).isEmpty()) {
                    tfs.remove(tf);
                }
            }
        }
        
        public void clearBlacklist() {
            Set<TableId> toremove = new HashSet<>(blacklist.keySet());
            for (TableId id: toremove) {
                remove(id);
            }
        }
        
        public void load(TableFactory tf, TableMaps toload) {
            
            Set<TableId> old = tfs.get(tf);
            if (toload.tfs.get(tf) != null) {
                tfs.put(tf, toload.tfs.get(tf));
            } else {
                tfs.remove(tf);
            }
            
            if (tfs.containsKey(tf)) {
                for (TableId id: tfs.get(tf)) {
                    
                    qf.removeQueries(id);
                    
                    if (old != null && old.contains(id)) {

                        if (toload.whitelist.containsKey(id)) {

                            if (this.whitelist.containsKey(id)) {
                                log.info(String.format("Updating table in white list: %s", id));
                            } else {
                                log.info(String.format("Moving table to white list: %s", id));
                                this.blacklist.remove(id);
                            }

                            this.whitelist.put(id, toload.whitelist.get(id));

                        } else {

                            if (this.blacklist.containsKey(id)) {
                                log.info(String.format("Updating table in black list: %s", id));
                            } else {
                                log.warn(String.format("Moving table to black list: %s", id));
                                this.whitelist.remove(id);
                            }

                            this.blacklist.put(id, toload.blacklist.get(id));


                        }

                    } else {

                        if (toload.whitelist.containsKey(id)) {

                            this.whitelist.put(id, toload.whitelist.get(id));
                            log.info(String.format("Adding table to white list: %s", id));

                        } else {

                            this.blacklist.put(id, toload.blacklist.get(id));
                            log.warn(String.format("Adding table to black list: %s", id));

                        }

                    }
                }
            }
            
            if (old != null) {
                for (TableId id: old) {
                    if (!tfs.get(tf).contains(id)) {

                        qf.removeQueries(id);
                        
                        if (this.whitelist.remove(id) != null) {
                            log.warn(String.format("Table removed from white list: %s", id));
                        } else {
                            this.blacklist.remove(id);
                            log.warn(String.format("Table removed from black table list: %s", id));
                        }

                    }
                }
            }
            
        }
        
    }

}
