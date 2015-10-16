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