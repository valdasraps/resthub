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
import javax.persistence.UniqueConstraint;
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
@Getter @Setter
@EqualsAndHashCode(callSuper = true, exclude = { "table" })
@Table(name = "HUB_PARAMETER", uniqueConstraints = { 
    @UniqueConstraint(columnNames = {"TABLE_ID", "NAME"})})
public class MdParameter extends MdEntity {   
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "TABLE_ID", referencedColumnName = "ID")
    @XmlTransient
    private MdTable table;

    @Basic
    @Column(name = "NAME", nullable = false, length = 30)
    @XmlElement(name = "NAME")
    private String name;
    
    @Basic
    @Column(name = "TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    @XmlElement(name = "TYPE")
    private MdType type;
    
    @Basic
    @Column(name = "ARRAY_TYPE", nullable = false)
    @XmlElement(name = "ARRAY_TYPE")
    private Boolean array;
    
}
