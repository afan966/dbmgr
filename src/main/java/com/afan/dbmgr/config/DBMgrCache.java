package com.afan.dbmgr.config;

import java.util.HashMap;
import java.util.Map;
import com.afan.dbmgr.DBException;
import com.afan.dbmgr.util.SQLUtil;
import com.afan.dbmgr.util.StringUtil;

/**
 * 缓存sql
 * 
 * @author cf
 * 
 */
public class DBMgrCache {

	private static final String sp = ".";

	public static final int SELECT = 1;
	public static final int INSERT = 2;
	public static final int UPDATE = 3;
	public static final int DELETE = 4;
	public static final int INSERTORUPDATE = 5;

	private static final Map<String, String> standardSqlCaches = new HashMap<String, String>();

	// 标准sql
	public static String getStandardSql(Object value, int type) throws DBException {
		String key = value.getClass().getName() + sp + type;
		String sql = standardSqlCaches.get(key);
		if (StringUtil.isEmpty(sql)) {
			if (SELECT == type) {
				sql = SQLUtil.selectByPrimaryKeys(value.getClass());
			} else if (INSERT == type) {
				sql = SQLUtil.insert(value.getClass());
			} else if (UPDATE == type) {
				sql = SQLUtil.updateByPrimaryKeys(value.getClass());
			} else if (DELETE == type) {
				sql = SQLUtil.deleteByPrimaryKeys(value.getClass());
			}
			standardSqlCaches.put(key, sql);
		}
		return sql;
	}

}
