package com.afan.dbmgr.config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.afan.dbmgr.DBColumn;
import com.afan.dbmgr.DBException;
import com.afan.dbmgr.DBTable;
import com.afan.dbmgr.util.StringUtil;

/**
 * 表结构对象信息(schema)，缓存
 * 
 * @author cf
 * 
 */
public class TableSchema {
	private static final Logger logger = LoggerFactory.getLogger(TableSchema.class);

	private static final Map<String, Map<String, SQLTable>> databaseSchemas = new HashMap<>();

	private static TableSchema schema = null;

	private TableSchema() {
	}

	public static synchronized TableSchema schema() {
		if (schema == null) {
			schema = new TableSchema();
		}
		return schema;
	}

	public SQLTable getSqlTableParam(Object value) throws DBException {
		SQLTable sqlTable = getSqlTable(value.getClass());
		if (sqlTable != null) {
			return setSqlTable(sqlTable, value);
		}
		return null;
	}

	/**
	 * 获取空的表结构对象信息
	 *
	 * @param clazz 对象类
	 * @return 标准SQL Table
	 */
	public SQLTable getSqlTable(Class<?> clazz) {
		DBTable t = clazz.getAnnotation(DBTable.class);
		SQLTable sqlTable = getSqlTable(t.db(), t.table());
		if (sqlTable != null) {
			return sqlTable;
		} else {
			return createSqlTable(t, clazz);
		}
	}

	/**
	 * 表对象字段赋值
	 * 
	 * @param sqlTable 标准SQL Table
	 * @param value 对象
	 * @return 组装值对象
	 */
	public SQLTable setSqlTable(SQLTable sqlTable, Object value) throws DBException {
		SQLTable tableParam = sqlTable.clone();
		Class<?> clazz = value.getClass();
		Field[] fs = clazz.getDeclaredFields();
		for (Field field : fs) {
			DBColumn c = field.getAnnotation(DBColumn.class);
			if (c != null) {
				String dbColumn = c.column();
				if (StringUtil.isEmpty(dbColumn)) {
					dbColumn = field.getName();
				}
				field.setAccessible(true);
                try {
                    tableParam.setSqlColumnValueByColumn(dbColumn, field.get(value));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
		}
		return tableParam;
	}

	private SQLTable createSqlTable(DBTable st, Class<?> clazz) {
		SQLTable sqlTable = new SQLTable();
		sqlTable.setDbName(st.db());
		sqlTable.setTableName(st.table());
		sqlTable.setJavaType(clazz.getName());
		sqlTable.setPrimaryKeys(st.primaryColumns());

		Map<String, SQLColumn> columnMap = new HashMap<>();
		Field[] fs = clazz.getDeclaredFields();
		for (Field f : fs) {
			DBColumn c = f.getAnnotation(DBColumn.class);
			if (c != null) {
				String dbColumn = c.column();
				if (StringUtil.isEmpty(dbColumn)) {//如果空说明字段名是一样的
					dbColumn = f.getName();
				}
				SQLColumn sqlColumn = new SQLColumn();
				sqlColumn.setColumnName(dbColumn);
				sqlColumn.setFieldName(f.getName());
				sqlColumn.setJavaType(f.getType().getName());
				sqlColumn.setDefaultValue(c.defaultValue());
				sqlColumn.setAutoIncrement(c.autoIncrement());
				sqlColumn.setHandler(c.handler());
				//sqlColumn.setSetMethod(setMethod(f.getName(), clazz));
				//sqlColumn.setGetMethod(getMethod(f.getName(), clazz));
				if (sqlTable.getPrimaryKeys() != null && sqlTable.getPrimaryKeys().length > 0) {
					for (String pk : sqlTable.getPrimaryKeys()) {
						if (pk.equals(dbColumn)) {
							sqlColumn.setPrimaryKey(true);
							break;
						}
					}
				}
				columnMap.put(dbColumn, sqlColumn);
			}
		}
		sqlTable.setColumnMap(columnMap);
		return putSqlTable(st.db(), st.table(), sqlTable);
	}

	private SQLTable getSqlTable(String db, String table) {
		Map<String, SQLTable> tables = databaseSchemas.get(db);
		if (tables != null && tables.size() > 0) {
			return tables.get(table);
		} else {
			databaseSchemas.put(db, new HashMap<String, SQLTable>());
		}
		return null;
	}

	private SQLTable putSqlTable(String db, String table, SQLTable sqlTable) {
		Map<String, SQLTable> tables = databaseSchemas.get(db);
		if (tables == null) {
			databaseSchemas.put(db, new HashMap<String, SQLTable>());
		}
		databaseSchemas.get(db).put(table, sqlTable);
		return sqlTable;
	}

}
