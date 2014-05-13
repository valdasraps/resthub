package net.resthub.model;

import java.util.Map;
import lombok.Getter;
import net.resthub.util.Helper;
import org.json.JSONException;
import org.json.JSONObject;

@Getter
public class Parameter {

    private final String name;
    private final String type;
    private final boolean array;
    private final Map<String, String> metadata;
    
    public Parameter(JSONObject jsonObject) throws JSONException {
        this.name = jsonObject.getString("name");
        this.type = jsonObject.getString("type");
        this.array = jsonObject.getBoolean("array");
        this.metadata = Helper.getMetadata(jsonObject);
    }
}
