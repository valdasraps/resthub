package net.resthub.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "TABLES")
@Getter @Setter
public class MdTables {
    
    @XmlElement(name = "TABLE")
    private List<MdTable> tables = new ArrayList<>();
    
}
