package com.afan.dbmgr.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.afan.dbmgr.DBException;
import com.afan.dbmgr.config.DBErrCode;
import com.afan.dbmgr.config.SQLColumn;
import com.afan.dbmgr.config.SQLTable;
import com.afan.dbmgr.config.TableSchema;

/**
 * 处理sql工具，自动生成mysql不处理null,0值
 * 
 * @author cf
 * 
 */
public class SQLUtil {

	private static final String SPACE = " ";
	private static final String COMMA = ",";
	private static final String SPOT = ".";
	private static final String SPLIT = "\\|";
	private static final String STAR = "*";
	private static final String QUOT = "'";

	private static final String SELECT = "select";
	private static final String INSERT = "insert";
	private static final String UPDATE = "update";
	private static final String DELETE = "delete";
	private static final String FROM = "from";
	private static final String WHERE = "where";
	private static final String AND = "and";
	private static final String INTO = "into";
	private static final String VALUES = "values";
	private static final String SET = "set";
	private static final String IN = "in";

	private static final String LEFT_BRACKET = "(";
	private static final String RIGHT_BRACKET = ")";
	private static final String EQUAL = "=";
	private static final String QUESTION = "?";
	private static final String ONDUPLICATE = "on duplicate key update";

	private static final String injectStr = "'|and|exec|execute|insert|select|delete|update|count|drop|*|%|chr|mid|master|truncate|"
			+ "char|declare|sitename|net user|xp_cmdshell|;|or|-|+|,|like'|and|exec|execute|insert|create|drop|" + "table|from|grant|use|group_concat|column_name|"
			+ "information_schema.columns|table_schema|union|where|select|delete|update|order|by|count|*|" + "chr|mid|master|truncate|char|declare|or|;|-|--|+|,|like|//|/|%|#";// 过滤掉的sql关键字，可以手动添加
	private static final String[] injects = injectStr.split(SPLIT);

	// 验证注入
	public static String sqlValidate(String s) {
		s = s.toLowerCase();// 统一转为小写
		for (String inject : injects) {
			if (s.contains(inject)) {
				s = s.replace(inject, SPACE);
			}
		}
		return s;
	}

	/**
	 * 自动组装insert sql
	 * 
	 * @param clazz
	 * @param tableName
	 * @return
	 * @throws DBException
	 */
	public static String insert(Class<?> clazz, String tableName) throws DBException {
		SQLTable sqlTable = TableSchema.schema().getSqlTable(clazz);
		if (sqlTable == null) {
			throw new DBException(DBErrCode.ERR_SQL_INSERT, "create insert sql error");
		}

		tableName = tableName != null && tableName.length() > 0 ? tableName : sqlTable.getDbName() + SPOT + sqlTable.getTableName();

		StringBuilder columns = new StringBuilder();
		StringBuilder values = new StringBuilder();
		for (String column : sqlTable.getColumnMap().keySet()) {
			SQLColumn sqlColumn = sqlTable.getColumnMap().get(column);
			if (!sqlColumn.isAutoIncrement()) {//自增列不记录参数
				if (columns.length() > 0) {
					columns.append(COMMA);
					values.append(COMMA);
				}
				columns.append(column);
				values.append(QUESTION);

			}
		}

		return new StringBuilder()
		.append(INSERT).append(SPACE).append(INTO).append(SPACE)
		.append(tableName).append(LEFT_BRACKET).append(columns).append(RIGHT_BRACKET).append(SPACE)
		.append(VALUES).append(LEFT_BRACKET).append(values).append(RIGHT_BRACKET).toString();
	}

	public static String insert(Class<?> clazz) throws DBException {
		return insert(clazz, null);
	}

	/**
	 * 自动组装updatesql
	 * 
	 * @param clazz
	 * @param tableName
	 * @param columnNames 指定需要修改的列
	 * @return
	 * @throws DBException
	 */
	public static String updateByPrimaryKeys(Class<?> clazz, String tableName, String columnNames) throws DBException {
		SQLTable sqlTable = TableSchema.schema().getSqlTable(clazz);
		if (sqlTable == null) {
			throw new DBException(DBErrCode.ERR_SQL_UPDATE, "create update sql error");
		}
		if (sqlTable.getPrimaryKeys() == null || sqlTable.getPrimaryKeys().length == 0) {
			throw new DBException(DBErrCode.ERR_SQL_NOPRIMARY, "create update sql without primary key");
		}

		List<String> columnList = null;
		if (columnNames != null) {
			columnList = Arrays.asList(columnNames.split(COMMA));
		}

		tableName = tableName != null && tableName.length() > 0 ? tableName : sqlTable.getDbName() + SPOT + sqlTable.getTableName();

		StringBuilder where = new StringBuilder();
		for (String pk : sqlTable.getPrimaryKeys()) {
			if (where.length() > 0) {
				where.append(SPACE).append(AND).append(SPACE);
			}
			where.append(pk).append(EQUAL).append(QUESTION);
		}

		StringBuilder update = new StringBuilder();
		for (String column : sqlTable.getColumnMap().keySet()) {
			SQLColumn sqlColumn = sqlTable.getColumnMap().get(column);
			if (columnList != null && columnList.size() > 0) {
				if (!columnList.contains(column)) {
					continue;
				}
			}
			if (!sqlColumn.isPrimaryKey()) {
				if (update.length() > 0) update.append(COMMA);
				update.append(column).append(EQUAL).append(QUESTION);
			}
		}

		return new StringBuilder()
		.append(UPDATE).append(SPACE).append(tableName).append(SPACE)
		.append(SET).append(SPACE).append(update).append(SPACE)
		.append(WHERE).append(SPACE).append(where).toString();
	}

	public static String updateByPrimaryKeys(Class<?> clazz) throws DBException {
		return updateByPrimaryKeys(clazz, null, null);
	}

	/**
	 * 根据主键删除
	 * 
	 * @param clazz
	 * @param tableName
	 * @return
	 * @throws DBException
	 */
	public static String deleteByPrimaryKeys(Class<?> clazz, String tableName) throws DBException {
		SQLTable sqlTable = TableSchema.schema().getSqlTable(clazz);
		if (sqlTable == null) {
			throw new DBException(DBErrCode.ERR_SQL_DELETE, "create update sql error");
		}
		if (sqlTable.getPrimaryKeys() == null || sqlTable.getPrimaryKeys().length == 0) {
			throw new DBException(DBErrCode.ERR_SQL_NOPRIMARY, "create update sql without primary key");
		}

		tableName = tableName != null && tableName.length() > 0 ? tableName : sqlTable.getDbName() + SPOT + sqlTable.getTableName();

		StringBuilder where = new StringBuilder();
		for (String pk : sqlTable.getPrimaryKeys()) {
			if (where.length() > 0) {
				where.append(SPACE).append(AND).append(SPACE);
			}
			where.append(pk).append(EQUAL).append(QUESTION);
		}

		return new StringBuilder()
		.append(DELETE).append(SPACE).append(FROM).append(SPACE)
		.append(tableName).append(SPACE)
		.append(WHERE).append(SPACE).append(where).toString();
	}

	public static String deleteByPrimaryKeys(Class<?> clazz) throws DBException {
		return deleteByPrimaryKeys(clazz, null);
	}

	/**
	 * 自动组装insertorupdatesql,mysql语法
	 * 
	 * @param value
	 * @param tableName
	 * @param columnNames
	 * @return sql,参数集合
	 * @throws DBException
	 */
	public static SqlObject insertOrUpdate(Object value, String tableName, String columnNames) throws DBException {
		SQLTable sqlTable = TableSchema.schema().getSqlTableParam(value);
		if (sqlTable == null) {
			throw new DBException(DBErrCode.ERR_SQL_UPDATE, "create update sql error");
		}
		if (sqlTable.getPrimaryKeys() == null || sqlTable.getPrimaryKeys().length == 0) {
			throw new DBException(DBErrCode.ERR_SQL_NOPRIMARY, "create update sql without primary key");
		}

		List<String> columnList = null;
		if (columnNames != null) {
			columnList = Arrays.asList(columnNames.split(COMMA));
		}

		tableName = tableName != null && tableName.length() > 0 ? tableName : sqlTable.getDbName() + SPOT + sqlTable.getTableName();

		List<Object> params = new ArrayList<>();
		StringBuilder columns = new StringBuilder();
		StringBuilder values = new StringBuilder();
		for (String column : sqlTable.getColumnMap().keySet()) {
			SQLColumn sqlColumn = sqlTable.getColumnMap().get(column);
			if (!sqlColumn.isAutoIncrement()) {//自增列不记录参数
				if (columns.length() > 0) {
					columns.append(COMMA).append(column);
					values.append(COMMA).append(QUESTION);
				} else {
					columns.append(column);
					values.append(QUESTION);
				}
				params.add(sqlColumn.getValue());
			}
		}

		StringBuilder update = new StringBuilder();
		for (String column : sqlTable.getColumnMap().keySet()) {
			SQLColumn sqlColumn = sqlTable.getColumnMap().get(column);
			if (columnList != null && columnList.size() > 0) {
				if (!columnList.contains(column)) {
					continue;
				}
			}
			if (!sqlColumn.isPrimaryKey()) {
				if (update.length() > 0) {
					update.append(COMMA);
				}
				update.append(column).append(EQUAL).append(QUESTION);
				params.add(sqlColumn.getValue());
			}
		}

		StringBuilder sql = new StringBuilder();
		sql.append(INSERT).append(SPACE).append(INTO).append(SPACE);
		sql.append(tableName).append(LEFT_BRACKET).append(columns).append(RIGHT_BRACKET).append(SPACE);
		sql.append(VALUES).append(SPACE).append(LEFT_BRACKET).append(values).append(RIGHT_BRACKET);

		if (update.length() > 0) {
			sql.append(SPACE).append(ONDUPLICATE).append(SPACE).append(update);
		}
		return new SqlObject(sql.toString(), params);
	}

	public static SqlObject insertOrUpdate(Object value) throws DBException {
		return insertOrUpdate(value, null, null);
	}

	/**
	 * 根据主键查询
	 * 
	 * @param clazz
	 * @param tableName
	 * @param columnNames
	 * @return
	 * @throws DBException
	 */
	public static String selectByPrimaryKeys(Class<?> clazz, String tableName, String columnNames) throws DBException {
		SQLTable sqlTable = TableSchema.schema().getSqlTable(clazz);
		if (sqlTable == null) {
			throw new DBException(DBErrCode.ERR_SQL_SELECT, "create select sql error");
		}
		if (sqlTable.getPrimaryKeys() == null || sqlTable.getPrimaryKeys().length == 0) {
			throw new DBException(DBErrCode.ERR_SQL_NOPRIMARY, "create select sql without primary key");
		}

		tableName = tableName != null && tableName.length() > 0 ? tableName : sqlTable.getDbName() + SPOT + sqlTable.getTableName();

		StringBuilder select = new StringBuilder();
		if (columnNames != null) {
			select.append(SPACE).append(columnNames);
		} else {
			select.append(SPACE).append(STAR);
		}

		StringBuilder where = new StringBuilder();
		for (String pk : sqlTable.getPrimaryKeys()) {
			if (where.length() > 0) {
				where.append(SPACE).append(AND).append(SPACE);
			}
			where.append(pk).append(EQUAL).append(QUESTION);
		}

		return new StringBuilder()
		.append(SELECT).append(select).append(SPACE)
		.append(FROM).append(SPACE).append(tableName).append(SPACE)
		.append(WHERE).append(SPACE).append(where).toString();
	}

	public static String selectByPrimaryKeys(Class<?> clazz) throws DBException {
		return selectByPrimaryKeys(clazz, null, null);
	}
	
	/**
	 * SQL拼接List in(xxx)
	 * @param colnum
	 * @param items
	 * @param isStr
	 * @return
	 */
	public static String appendListIn(String colnum, List<?> items, boolean isStr){
		if (StringUtil.isBlank(colnum) || items == null || items.size() == 0) {
			return null;
		}
		if (isStr == items.get(0) instanceof String) {
			return null;
		}
		StringBuilder sql = new StringBuilder();
		sql.append(colnum);
		sql.append(SPACE).append(IN).append(SPACE).append(LEFT_BRACKET);
		for (int i = 0; i < items.size(); i++) {
			if(i>0){
				sql.append(COMMA);
			}
			if(isStr){
				sql.append(QUOT);
				sql.append(sqlValidate((String)items.get(i)));
				sql.append(QUOT);
			}else{
				sql.append(items.get(i));
			}
		}
		sql.append(RIGHT_BRACKET);
		return sql.toString();
	}
	
	public static String appendArrayIn(String colnum, Object[] items, boolean isStr){
		if (items == null) {
			return null;
		}
		return appendListIn(colnum, Arrays.asList(items), isStr);
	}
}