package com.afan.dbmgr.pool.wrap;

import java.io.Closeable;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.afan.dbmgr.DBException;
import com.afan.dbmgr.config.DBErrCode;
import com.afan.dbmgr.config.SQLColumn;
import com.afan.dbmgr.config.SQLTable;
import com.afan.dbmgr.config.TableSchema;
import com.afan.dbmgr.handler.ConvertHandler;
import com.afan.dbmgr.pool.AfanConnect;
import com.afan.dbmgr.pool.DBConnect;
import com.afan.dbmgr.pool.DefaultConnect;
import com.afan.dbmgr.pool.druid.DruidMgr;

/**
 * @author cf
 * @Description: 包装的resultset 根据标准的stable自动组装查询的结果
 */
public class ResultSetWrapper<T> implements Closeable {
	private static final Logger logger = LoggerFactory.getLogger(StatementWrapper.class);
	
	DBConnect conn = null;
	AfanConnect afaconn = null;
	// 结果集
	private ResultSet rs;
	// 表结果对象
	private ResultSetMetaData rsmd;
	// 表信息描述
	private SQLTable sqlTable;
	// 结果类
	private Class<T> clazz;
	// 结果list
	private List<T> results;
	// 结果数
	private AtomicInteger recod = new AtomicInteger();

	public ResultSetWrapper(DBConnect conn, Class<T> clazz) throws DBException {
		if (conn instanceof AfanConnect) {
			this.afaconn = (AfanConnect) conn;
			this.rs = this.afaconn.executeQuery();
			init(this.rs, clazz);
		} else if (conn instanceof DefaultConnect) {
			this.conn = (DefaultConnect) conn;
			this.rs = this.conn.executeQuery();
			init(this.rs, clazz);
		}
	}

	private void init(ResultSet rs, Class<T> clazz) throws DBException {
		try {
			this.rs = rs;
			this.rsmd = rs.getMetaData();
			this.results = new ArrayList<T>();
			this.clazz = clazz;
			this.sqlTable = TableSchema.schema().getSqlTable(this.clazz);
			initJavaType();
		} catch (SQLException e) {
			throw new DBException(DBErrCode.ERR_WRESULT_INIT, "ResultSetWrapper-init", e);
		}
	}

	// 执行解析
	public void exec() throws DBException {
		try {
			while (rs.next()) {
				results.add((T) autoLoad(rs));
				autoRecod();
			}
		} catch (SQLException e) {
			throw new DBException(DBErrCode.ERR_WRESULT_EXEC, "ResultSetWrapper-exec", e);
		} finally {
			// 自动关闭
			close();
		}
	}

	public T query() throws DBException {
		queryList();
		// 没有结果就不返回
		if (results == null || results.size() == 0)
			return null;
		return results.get(0);
	}

	public List<T> queryList() throws DBException {
		exec();
		return results;
	}
	
	// 数据库类型对应的java类型
	private void initJavaType() throws DBException {
		try {
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				this.sqlTable.getSqlColumnByColumn(rsmd.getColumnName(i)).setJdbcJavaType(rsmd.getColumnClassName(i));
			}
		} catch (SQLException e) {
			throw new DBException(DBErrCode.ERR_WRESULT_INIT, "ResultSetWrapper-init", e);
		}
	}

	// 组装单条对象
	private T autoLoad(ResultSet rs) throws DBException {
		T o = null;
		try {
			o = clazz.newInstance();
			for (String colnumName : this.sqlTable.getColumnMap().keySet()) {
				SQLColumn sqlColumn = this.sqlTable.getSqlColumnByColumn(colnumName);
				// 不记录默认值 为空就不复
				Object value = resultSetGet(rs, sqlColumn);
				if (value != null) {
					invokeSet(o, sqlColumn, value);
				}
			}
		} catch (InstantiationException e) {
			throw new DBException(DBErrCode.ERR_WRESULT_AUTOLOAD, "ResultSetWrapper-autoLoad:InstantiationException", e);
		} catch (IllegalAccessException e) {
			throw new DBException(DBErrCode.ERR_WRESULT_AUTOLOAD, "ResultSetWrapper-autoLoad:IllegalAccessException", e);
		}
		return o;
	}

	// 调用执行set方法
	private void invokeSet(Object o, SQLColumn sqlColumn, Object value) throws DBException {
		try {
			Method method = sqlColumn.getSetMethod();
			method.invoke(o, new Object[] { value });
		} catch (Exception e) {
			logger.error("field:" + sqlColumn.getFieldName() + " invoke set method error", e);
			throw new DBException(DBErrCode.ERR_WRESULT_INVOKESET, "field:" + sqlColumn.getFieldName() + " invoke set method error", e);
		}
	}

	// 取出result对应的值
	private Object resultSetGet(ResultSet rs, SQLColumn sqlColumn) throws DBException {
		ConvertHandler handler = DruidMgr.getInstance().getHhandler(sqlColumn.getHandler());
		if (handler == null) {
			logger.error("field:" + sqlColumn.getFieldName() + " can not find handler : [" + sqlColumn.getHandler() + "]");
			throw new DBException(DBErrCode.ERR_WRESULT_SET, "field:" + sqlColumn.getFieldName() + " can not find handler : [" + sqlColumn.getHandler() + "]");
		}
		if(sqlColumn.getJdbcJavaType()!=null){
			return handler.convertResultSet(rs, sqlColumn);
		}
		return null;
	}

	public int getRecod() {
		return recod.get();
	}

	public int autoRecod() {
		return recod.incrementAndGet();
	}
	
	public void close() {
		try {
			if (conn != null && conn.isAutoClose()) {
				conn.close();
			}
			if (afaconn != null && afaconn.isAutoClose()) {
				afaconn.close();
			}
			if (rs != null && !rs.isClosed()) {
				rs.close();
			}
		} catch (SQLException e) {
			logger.error("ResultSetWrapper-close", e);
		}
	}

}
