package net.resthub.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(MdTable.class)
public abstract class MdTable_ extends net.resthub.model.MdEntity_ {

	public static volatile SingularAttribute<MdTable, String> sql;
	public static volatile SingularAttribute<MdTable, Integer> hitCount;
	public static volatile SingularAttribute<MdTable, String> name;
	public static volatile ListAttribute<MdTable, MdColumn> columns;
	public static volatile SingularAttribute<MdTable, Integer> cacheTime;
	public static volatile SingularAttribute<MdTable, Integer> rowsLimit;
	public static volatile ListAttribute<MdTable, MdParameter> parameters;
	public static volatile SingularAttribute<MdTable, String> connectionName;
	public static volatile SingularAttribute<MdTable, Integer> timeout;
	public static volatile SingularAttribute<MdTable, String> namespace;

}

