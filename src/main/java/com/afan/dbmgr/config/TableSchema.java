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

	private static final Map<String, Map<String, SQLTable>> databaseSchemas = new HashMap<String, Map<String, SQLTable>>();

	private static TableSchema schema = null;

	private static final String GET = "get";
	private static final String SET = "set";

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
	 * @param value
	 * @return
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
	 * @param sqlTable
	 * @param value
	 * @return
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
				tableParam.setSqlColumnValueByColumn(dbColumn, invokeGet(field.getName(), value));
			}
		}
		return tableParam;
	}

	private SQLTable createSqlTable(DBTable st, Class<?> clazz) {
		SQLTable sqlTable = new SQLTable();
		sqlTable.setDbName(st.db());
		sqlTable.setTableName(st.table());
		sqlTable.setJavaType(clazz.getName());
		sqlTable.setParimaryKeys(st.primaryClumns());

		Map<String, SQLColumn> columnMap = new HashMap<String, SQLColumn>();
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
				sqlColumn.setSetMethod(setMethod(f.getName(), clazz));
				sqlColumn.setGetMethod(getMethod(f.getName(), clazz));
				if (sqlTable.getParimaryKeys() != null && sqlTable.getParimaryKeys().length > 0) {
					for (String pk : sqlTable.getParimaryKeys()) {
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
	
	private Method getMethod(String fieldName, Class<?> clazz) {
		try {
			StringBuilder method = new StringBuilder();
			method.append(GET);
			method.append(fieldName.substring(0, 1).toUpperCase());
			method.append(fieldName.substring(1));
			return clazz.getMethod(method.toString());
		} catch (Exception e) {
			logger.error("getGetMethod error field:" + fieldName, e);
		}
		return null;
	}
	
	private Method setMethod(String fieldName, Class<?> clazz) {
		try {
				Class<?>[] parameterTypes = new Class<?>[1];
				Field field = clazz.getDeclaredField(fieldName);
				parameterTypes[0] = field.getType();
				StringBuilder method = new StringBuilder();
				method.append(SET);
				method.append(fieldName.substring(0, 1).toUpperCase());
				method.append(fieldName.substring(1));
				return clazz.getMethod(method.toString(), parameterTypes);
		} catch (Exception e) {
			logger.error("getSetMethod error field:" + fieldName, e);
		}
		return null;
	}

	private Object invokeGet(String fieldName, Object value) {
		try {
			Method method = getMethod(fieldName, value.getClass());
			return method.invoke(value);
		} catch (Exception e) {
			logger.error("invokeGet error field:" + fieldName, e);
		}
		return null;
	}

}
