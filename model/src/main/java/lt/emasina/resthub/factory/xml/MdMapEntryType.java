package lt.emasina.resthub.factory.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class MdMapEntryType {
    
    @XmlAttribute(name = "KEY")
    public String key;  
    
    @XmlValue  
    public String value;  
}


