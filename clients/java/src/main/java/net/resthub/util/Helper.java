package net.resthub.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.resthub.model.Column;
import net.resthub.model.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

public class Helper {
    public static JSONObject getJSONObject(String url) throws IOException, JSONException{
        ClientResource client = new ClientResource(url);
        Representation responseEntity = client.get(MediaType.APPLICATION_JSON);
        String r = responseEntity.getText();
        return new JSONObject(r);
    }
    
    public static Map<String, String> getMetadata(JSONObject ob) throws JSONException {
        Map<String, String> metadata = null;
        JSONObject jsonObject = ob.optJSONObject("metadata");
        if (jsonObject != null){
            metadata = new HashMap<>();
            Iterator i = jsonObject.keys();
            while (i.hasNext()) {
                String metaName = i.next().toString();
                String metaValue = jsonObject.getString(metaName);
                metadata.put(metaName, metaValue);
            }
        }
        return metadata;
    }

    public static List<Column> getColumns(JSONObject ob) throws JSONException {
        List<Column> columns = new ArrayList<>();
        JSONArray jsonArray = ob.getJSONArray("columns");
        for (int j = 0; j < jsonArray.length(); j++) {
            JSONObject jsonObject = jsonArray.getJSONObject(j);
            columns.add(new Column(jsonObject));
        }
        return columns;        
    }

    public static List<Parameter> getParameters(JSONObject ob) throws JSONException{
        List<Parameter> parameters = new ArrayList<>();
        JSONArray jsonArray = ob.optJSONArray("parameters");
        if (jsonArray != null) {
            for (int j = 0; j < jsonArray.length(); j++) {
                JSONObject jsonObject = jsonArray.getJSONObject(j);
                parameters.add(new Parameter(jsonObject));
            }
        }
        return parameters;
    }
}
