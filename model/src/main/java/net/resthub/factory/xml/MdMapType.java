package net.resthub.factory.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
 
public class MdMapType {
 
   @XmlElement(name = "ENTRY")
   public List<MdMapEntryType> entry = new ArrayList<>();
 
}