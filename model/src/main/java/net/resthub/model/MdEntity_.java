package net.resthub.model;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(MdEntity.class)
public abstract class MdEntity_ {

	public static volatile SingularAttribute<MdEntity, Long> id;
	public static volatile SingularAttribute<MdEntity, Date> createTime;
	public static volatile SingularAttribute<MdEntity, String> createUser;
	public static volatile SingularAttribute<MdEntity, Date> updateTime;
	public static volatile MapAttribute<MdEntity, String, String> metadata;
	public static volatile SingularAttribute<MdEntity, String> updateUser;

}

