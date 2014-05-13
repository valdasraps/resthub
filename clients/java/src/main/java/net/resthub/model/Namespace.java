package net.resthub.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import net.resthub.util.Helper;
import org.json.JSONException;
import org.json.JSONObject;

    /** 
     * The Namespace class is used to create Table objects.
     */
public class Namespace {

    @Getter
    private final String name;
    private Map<String, String> tableMap;

    /**
     * Creates an object of RestHubServer.
     * 
     * @param name A name of namespace.
     * @param ob JSONObject with it's tables information.
     */
    public Namespace(String name, JSONObject ob) throws JSONException {
        this.tableMap = new HashMap<>();
        this.name = name;
        Iterator i = ob.keys();
        while (i.hasNext()) {
            String tableName = i.next().toString();
            String tableUrl = ob.getString(tableName);
            tableMap.put(tableName, tableUrl);
        }
    }

    private Table getPrivateTable(String name, boolean v) throws JSONException, IOException {
        String url = tableMap.get(name);
        if (v) url += "?v=true";
        
        JSONObject jsonObject = Helper.getJSONObject(url);
        
        Table newTable = new Table(name, jsonObject, v);
        return newTable;
    }
    
    /**
     * Gets a Table object by its name. There is one request to 
     * server.
     * 
     * @param name A table name.
     * @return Created Table object.
     */
    public Table getTable(String name) throws JSONException, IOException {
        return getPrivateTable(name, false);
    }
    
    /**
     * Gets a Table object with additional data by its name. There is one 
     * request to server.
     * 
     * @param name A table name.
     * @return Created Table object.
     */
    public Table getVerboseTable(String name) throws JSONException, IOException {
        return getPrivateTable(name, true);
    }

    /**
     * Gets list of all Table objects. There are more than one (as many as there
     * are tables) request to server.
     * 
     * @return The list of Table objects.
     */
    public List<Table> getAllTables() throws JSONException, IOException {
        List<Table> list = new ArrayList<>();
        for (String tableN : getTableNames()) {
            list.add(getTable(tableN));
        }
        return list;
    }

    /**
     * Gets the list of all table names.
     * 
     * @return The list of table names.
     */
    public ArrayList<String> getTableNames() {
        return new ArrayList<>(tableMap.keySet());
    }
}
