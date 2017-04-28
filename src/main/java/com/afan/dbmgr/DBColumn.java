package com.afan.dbmgr;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 注解数据库字段
 * @author cf
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DBColumn {
	
	String column() default "";//数据库列名,默认跟字段名一直
	boolean autoIncrement() default false;//自动增长
	String defaultValue() default "";//默认值
	boolean notField() default false;//非数据库字段
	
	String handler() default "";//解析器
}
