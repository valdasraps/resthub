package lt.emasina.resthub.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Table
 * @author valdo
 */
@Entity
@Table(name = "HUB_COLUMN")
@Getter @Setter
@EqualsAndHashCode(callSuper = true, exclude = { "table" })
public class MdColumn extends MdEntity {   
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "TABLE_ID", referencedColumnName = "ID")
    @XmlTransient
    private MdTable table;
    
    @Basic
    @Column(name = "NUM", nullable = false)
    @XmlAttribute(name = "NUM")
    private Integer number;

    @Basic
    @Column(name = "NAME", nullable = false, length = 30)
    @XmlElement(name = "NAME")
    private String name;
    
    @Transient
    @XmlTransient
    private String cName;

    @Basic
    @Column(name = "TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    @XmlElement(name = "TYPE")
    private MdType type;
    
}
