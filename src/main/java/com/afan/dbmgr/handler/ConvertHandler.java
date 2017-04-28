package com.afan.dbmgr.handler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.afan.dbmgr.DBException;
import com.afan.dbmgr.config.SQLColumn;

/**
 * 数据类型转换器
 * 数据库类型和java类型相互转换
 * @author afan
 *
 */
public interface ConvertHandler {

	enum JDBCType {
		/*mysql数据类型对应Java数据类型,jdbcJava数据类型*/
		//数据库类型, java类型, jdbcjava类型
		INT("int", "int", "java.lang.Integer"), //int,tinyint,integer
		BIGINT("bigint", "long", "java.lang.Long"), 
		BIT("bit", "boolean", "java.lang.Boolean"), 
		DOUBLE("double", "double", "java.lang.Double"), 
		FLOAT("float", "float", "java.lang.Float"),
		VARCHAR("varchar", "java.lang.String", "java.lang.String"), //varchar,char,tinytext,text,mediumtext
		DATE("date", "java.sql.Date", "java.sql.Date"), //时间都采用util.Date
		TIME("time", "java.sql.Time", "java.sql.Time"), 
		DATETIME("datetime", "java.sql.Timestamp", "java.sql.Timestamp");

		private String dbType;
		private String javaType;
		private String jdbcJavaType;

		JDBCType(String dbType, String javaType, String jdbcJavaType) {
			this.dbType = dbType;
			this.javaType = javaType;
			this.jdbcJavaType = jdbcJavaType;
		}

		public static JDBCType getEnum(String javaType) throws RuntimeException {
			JDBCType[] pesEnum = JDBCType.values();
			for (int i = 0; i < pesEnum.length; i++) {
				if (pesEnum[i].getJavaType().equals(javaType)) {
					return pesEnum[i];
				}
			}
			return null;
		}
		
		public static JDBCType getEnumJdbc(String jdbcJavaType) throws RuntimeException {
			JDBCType[] pesEnum = JDBCType.values();
			for (int i = 0; i < pesEnum.length; i++) {
				if (pesEnum[i].getJdbcJavaType().equals(jdbcJavaType)) {
					return pesEnum[i];
				}
			}
			return null;
		}

		public String getDbType() {
			return dbType;
		}

		public String getJavaType() {
			return javaType;
		}
		
		public String getJdbcJavaType() {
			return jdbcJavaType;
		}
	}

	/**
	 * 设置PreparedStatement参数，java对象转sql对象
	 * 
	 * @param ptmt
	 * @param i
	 * @param sqlColumn
	 */
	void convertPreparedStatement(PreparedStatement ptmt, int i, SQLColumn sqlColumn) throws DBException;

	/**
	 * 设置ResultSet参数，sql对象转java对象
	 * 
	 * @param rs
	 * @param sqlColumn
	 * @return
	 */
	Object convertResultSet(ResultSet rs, SQLColumn sqlColumn) throws DBException;
}
