package lt.emasina.resthub.model;

import lt.emasina.resthub.factory.xml.MapAdapter;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;

/**
 * PortletEntity
 * @author valdo
 */
@MappedSuperclass
@Getter @Setter
@EqualsAndHashCode(of = "id")
public abstract class MdEntity implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "HUB_ID_SEQ", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "HUB_ID_SEQ", sequenceName = "HUB_ID_SEQ")
    @XmlTransient
    private Long id;
    
    @Column(name = "CREATE_TIME", insertable = true, updatable = false, nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @XmlTransient
    private Date createTime;
    
    @Column(name = "UPDATE_TIME", insertable = true, updatable = true, nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @XmlTransient
    private Date updateTime;
    
    @Column(name = "CREATE_USER", insertable = true, updatable = false, nullable = false)
    @XmlTransient
    private String createUser;
    
    @Column(name = "UPDATE_USER", insertable = true, updatable = true, nullable = false)
    @XmlTransient
    private String updateUser;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name="HUB_METADATA", joinColumns=@JoinColumn(name="ID"))
    @MapKeyColumn (name="MAP_NAME")
    @ForeignKey(name = "none") 
    @Column(name="MAP_VALUE")
    @Cascade(CascadeType.ALL)
    @XmlJavaTypeAdapter(MapAdapter.class)
    @XmlElement(name = "METADATA")
    private Map<String, String> metadata = new HashMap<>();
    
    private String getUsername() {
        return System.getProperty("user.name", "undefined");
    }
    
    public void beforeSave() {
        if (getId() == null) {
            setCreateUser(getUsername());
            setCreateTime(new Date());
        }
        setUpdateUser(getUsername());
        setUpdateTime(new Date());
    }
    
}
