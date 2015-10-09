package net.resthub.server.factory;

import com.google.inject.persist.Transactional;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import net.resthub.TableFactory;
import net.resthub.factory.TableBuilder;
import org.apache.commons.beanutils.BeanUtils;
import net.resthub.server.table.TableId;
import net.resthub.server.table.ServerTable;
import net.resthub.model.MdTable;
import org.json.JSONObject;

@Log4j
@Singleton
public class MetadataFactory implements MetadataFactoryIf {
   
    private final static String ERROR_METADATA_KEY = "Error";
    
    private final Map<TableId, ServerTable> tables = new ConcurrentHashMap<>();
    private final Map<TableId, ServerTable> blacklist = new ConcurrentHashMap<>();

    @Inject
    private ResourceFactory rf;
    
    @Inject
    private QueryFactory qf;
    
    @Inject
    private TableFactory tf;
    
    @Inject
    private TableBuilder tb;
    
    private Date lastUpdate = null;

    @Getter
    private boolean forceRefresh = true;
    
    @Transactional
    @Override
    public synchronized void refresh() throws Exception {
        boolean doRefresh = forceRefresh || tf.isRefresh(lastUpdate);

        if (log.isDebugEnabled()) {
            log.debug(String.format("lastUpdate = %s, forceRefresh = %s, doRefresh = %s", 
                lastUpdate, forceRefresh, doRefresh));
        }

        // Update is needed!
        if (doRefresh) {

            lastUpdate = null;

            Set<TableId> ids = new HashSet<>();

            // Update or add tables
            for(MdTable t: tf.getTables()) {
                
                TableId id = new TableId(t);
                ServerTable st = rf.create(t);
                
                if (!blacklist.containsKey(id)) {
                    
                    st.getTable().getMetadata().remove(ERROR_METADATA_KEY);
                    
                    try {

                        // Check the table
                        tb.collectColumns(t.getConnectionName(), t.getSql(), t.getColumns());
                        tb.collectParameters(t.getSql(), t.getParameters());

                        ids.add(id);

                        if (!hasTable(id)) {

                            if (log.isDebugEnabled()) log.debug(String.format("Adding table: %s", t));

                            tables.put(id, st);

                        } else {
                            MdTable t1 = getTable(id).getTable();
                            if (t.getUpdateTime().after(t1.getUpdateTime())) {

                                if (log.isDebugEnabled()) log.debug(String.format("Updating table %s", t));

                                tables.put(id, st);
                                qf.removeQueries(id);
                            }
                        }

                        if (lastUpdate == null || t.getUpdateTime().after(lastUpdate)) {
                            lastUpdate = t.getUpdateTime();
                        }

                    } catch (Exception ex) {

                        log.warn(String.format("Error while adding table %s.%s (will not be added!): %s", t.getNamespace(), t.getName(), ex.getMessage()));
                        this.blacklist.put(id, st);
                        st.getTable().getMetadata().put(ERROR_METADATA_KEY, ex.getMessage());
                        
                    }
                }
            }

            // Remove tables that does not exist anymore
            Set<TableId> exids = new HashSet<>();
            exids.addAll(tables.keySet());
            for (TableId id: exids) {
                if (!ids.contains(id)) {

                    if (log.isDebugEnabled()) log.debug(String.format("Removing table %s", id));

                    tables.remove(id);
                    qf.removeQueries(id);
                }
            }
        }
        
        forceRefresh = false;
        
    }
    
    @Override
    public Collection<ServerTable> getTables() {
        return Collections.unmodifiableCollection(tables.values());
    }

    public Collection<ServerTable> getBlacklist() {
        return Collections.unmodifiableCollection(blacklist.values());
    }
    
    public ServerTable getBlacklistTable(TableId id) {
        return blacklist.get(id);
    }
    
    public void removeBlacklistTable(TableId id) {
        blacklist.remove(id);
        forceRefresh = true;
    }
    
    public void clearBlacklist() {
        this.blacklist.clear();
        forceRefresh = true;
    }
    
    public void clearBlacklist(String namespace) {
        for (TableId id: blacklist.keySet()) {
            if (id.getNamespace().equals(namespace)) {
                blacklist.remove(id);
            }
        }
        forceRefresh = true;
    }
    
    @Override
    public ServerTable getTable(TableId id) {
        return tables.get(id);
    }

    @Override
    public boolean hasTable(TableId id) {
        return tables.containsKey(id);
    }
    
    public static JSONObject mapToJSONObject(Map<?,?> map) {
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
        Map<?,?> describe = BeanUtils.describe(bean);
        for (Object k: describe.keySet()) {
            if (k instanceof String) {
                String ks = (String) k;
                if (!ks.equals("class")) {
                    o.putOpt(ks, describe.get(k));
                }
            }
        }
        return o;
    }
    
}
