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

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(MdTable.class)
public abstract class MdTable_ extends lt.emasina.resthub.model.MdEntity_ {

	public static volatile SingularAttribute<MdTable, Integer> hitCount;
	public static volatile ListAttribute<MdTable, MdColumn> columns;
	public static volatile SingularAttribute<MdTable, String> namespace;
	public static volatile SingularAttribute<MdTable, String> name;
	public static volatile SingularAttribute<MdTable, String> connectionName;
	public static volatile SingularAttribute<MdTable, Integer> rowsLimit;
	public static volatile ListAttribute<MdTable, MdParameter> parameters;
	public static volatile SingularAttribute<MdTable, Integer> timeout;
	public static volatile SingularAttribute<MdTable, String> sql;
	public static volatile SingularAttribute<MdTable, Integer> cacheTime;

}

