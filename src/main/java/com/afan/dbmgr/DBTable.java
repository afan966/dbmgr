package com.afan.dbmgr;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解数据库表
 * @author cf
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DBTable {
	
	String db();//数据库表名

	String table();//数据库表名
	
	String[] primaryClumns() default {};
	
}
