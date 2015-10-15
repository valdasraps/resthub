package lt.emasina.resthub.factory.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public final class MapAdapter extends XmlAdapter<MdMapType, Map<String, String>> {

    @Override
    public MdMapType marshal(Map<String, String> arg0) throws Exception {
        MdMapType myMapType = new MdMapType();
        for (Entry<String, String> entry : arg0.entrySet()) {
            MdMapEntryType myMapEntryType = new MdMapEntryType();
            myMapEntryType.key = entry.getKey();
            myMapEntryType.value = entry.getValue();
            myMapType.entry.add(myMapEntryType);
        }
        return myMapType;
    }

    @Override
    public Map<String, String> unmarshal(MdMapType arg0) throws Exception {
        HashMap<String, String> hashMap = new HashMap<>();
        for (MdMapEntryType myEntryType : arg0.entry) {
            hashMap.put(myEntryType.key, myEntryType.value);
        }
        return hashMap;
    }
}