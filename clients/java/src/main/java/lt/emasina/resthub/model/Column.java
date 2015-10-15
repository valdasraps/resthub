package lt.emasina.resthub.model;

import java.util.Map;
import lombok.Getter;
import lt.emasina.resthub.util.Helper;
import org.json.JSONException;
import org.json.JSONObject;

@Getter
public class Column {

    private final String name;
    private final String type;
    private final Map<String, String> metadata;

    public Column(JSONObject jsonObject) throws JSONException {
        this.name = jsonObject.getString("name");
        this.type = jsonObject.getString("type");
        this.metadata = Helper.getMetadata(jsonObject);
    }
}
