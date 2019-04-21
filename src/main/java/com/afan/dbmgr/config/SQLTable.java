package com.afan.dbmgr.config;

import java.util.HashMap;
import java.util.Map;

import com.afan.dbmgr.DBException;

/**
 * 定义数据库表对象
 * 
 * @author cf
 * 
 */
public class SQLTable implements Cloneable {

	private String dbName;
	private String tableName;
	private String javaType;
	private String[] primaryKeys;
	// columnName
	private Map<String, SQLColumn> columnMap = new HashMap<>();

	public SQLColumn getSqlColumnByColumn(String columnName) throws DBException {
		SQLColumn column = columnMap.get(columnName);
		if (column == null)
			throw new DBException(DBErrCode.ERR_COLUMN_NONE, "can not find columnName:" + columnName);
		return column;
	}
	
	public SQLColumn getSqlColumnByField(String fieldName) throws DBException {
		SQLColumn column = null;
		for (String columnName : columnMap.keySet()) {
			SQLColumn c = columnMap.get(columnName);
			if (fieldName.equals(c.getFieldName())){
				column = c;
				break;
			}
		}
		if (column == null)
			throw new DBException(DBErrCode.ERR_COLUMN_NONE, "can not find fieldName:" + fieldName);
		return column;
	}

	public void setSqlColumnValueByColumn(String columnName, Object value) throws DBException {
		getSqlColumnByColumn(columnName).setValue(value);
	}
	
	public void setSqlColumnValueByField(String fieldName, Object value) throws DBException {
		getSqlColumnByField(fieldName).setValue(value);
	}

	public SQLTable clone() {
		SQLTable obj = null;
		try {
			// 深度克隆
			obj = (SQLTable) super.clone();
			obj.primaryKeys = primaryKeys.clone();
			obj.columnMap = new HashMap<>();
			for (String column : columnMap.keySet()) {
				obj.columnMap.put(column, columnMap.get(column).clone());
			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getJavaType() {
		return javaType;
	}

	public void setJavaType(String javaType) {
		this.javaType = javaType;
	}

	public String[] getPrimaryKeys() {
		return primaryKeys;
	}

	public void setPrimaryKeys(String[] primaryKeys) {
		this.primaryKeys = primaryKeys;
	}

	public Map<String, SQLColumn> getColumnMap() {
		return columnMap;
	}

	public void setColumnMap(Map<String, SQLColumn> columnMap) {
		this.columnMap = columnMap;
	}
}
