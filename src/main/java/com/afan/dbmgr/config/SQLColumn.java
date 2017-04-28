package com.afan.dbmgr.config;

import java.lang.reflect.Method;

import com.afan.dbmgr.util.StringUtil;

/**
 * 定义数据库列对象
 * 
 * @author cf
 * 
 */
public class SQLColumn implements Cloneable {

	private String javaType;// java实体类型
	private String fieldName;// java字段名称
	private String jdbcJavaType;// jdbc对应的java字段类型，可能跟自定义的实体字段
	private String columnName;// 数据库字段名称
	private Object value;// 值
	private Object defaultValue;// 默认值
	private boolean autoIncrement;// 是否自动增长列
	private boolean primaryKey;// 是否主键

	private Method getMethod;// get方法
	private Method setMethod;// set方法
	private String handler;

	public SQLColumn clone() {
		SQLColumn obj = null;
		try {
			// 深度克隆
			obj = (SQLColumn) super.clone();
			obj.value = null;
			// obj.defaultValue = defaultValue;//defaultvalue不可变
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return obj;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getStringValue() {
		if (getValue() == null) {
			if (getDefaultValue() != null) {
				return getDefaultValue().toString();
			}
			return null;
		} else {
			return getValue().toString();
		}
	}
	
	public int getInt(int def) {
		if (StringUtil.isEmpty(getStringValue())) {
			return def;
		}
		try {
			return Integer.parseInt(getStringValue());
		} catch (Exception e) {
			return def;
		}
	}
	
	public long getLong(long def) {
		if (StringUtil.isEmpty(getStringValue())) {
			return def;
		}
		try {
			return Long.parseLong(getStringValue());
		} catch (Exception e) {
			return def;
		}
	}
	
	public double getDouble(double def) {
		if (StringUtil.isEmpty(getStringValue())) {
			return def;
		}
		try {
			return Double.parseDouble(getStringValue());
		} catch (Exception e) {
			return def;
		}
	}
	
	public float getFloat(float def) {
		if (StringUtil.isEmpty(getStringValue())) {
			return def;
		}
		try {
			return Float.parseFloat(getStringValue());
		} catch (Exception e) {
			return def;
		}
	}
	
	public boolean getBoolean(boolean def) {
		if (StringUtil.isEmpty(getStringValue())) {
			return def;
		}
		try {
			return Boolean.parseBoolean(getStringValue());
		} catch (Exception e) {
			return def;
		}
	}

	public String toString() {
		return "columnName:" + this.columnName + " value:" + this.value;
	}

	public String getJavaType() {
		return javaType;
	}

	public void setJavaType(String javaType) {
		this.javaType = javaType;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public boolean isAutoIncrement() {
		return autoIncrement;
	}

	public void setAutoIncrement(boolean autoIncrement) {
		this.autoIncrement = autoIncrement;
	}

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	public String getJdbcJavaType() {
		return jdbcJavaType;
	}

	public void setJdbcJavaType(String jdbcJavaType) {
		this.jdbcJavaType = jdbcJavaType;
	}

	public Method getGetMethod() {
		return getMethod;
	}

	public void setGetMethod(Method getMethod) {
		this.getMethod = getMethod;
	}

	public Method getSetMethod() {
		return setMethod;
	}

	public void setSetMethod(Method setMethod) {
		this.setMethod = setMethod;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

}
