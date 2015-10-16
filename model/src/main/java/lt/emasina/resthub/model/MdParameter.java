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
    @XmlElement(name = "NAME", required = true)
    private String name;
    
    @Basic
    @Column(name = "TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    @XmlElement(name = "TYPE", required = true)
    private MdType type;
    
    @Basic
    @Column(name = "ARRAY_TYPE", nullable = false)
    @XmlElement(name = "ARRAY_TYPE")
    private Boolean array = Boolean.FALSE;
    
}
