package com.afan.dbmgr.pool.wrap;

import com.afan.dbmgr.DBException;
import com.afan.dbmgr.config.DBErrCode;
import com.afan.dbmgr.config.SQLColumn;
import com.afan.dbmgr.config.SQLTable;
import com.afan.dbmgr.config.TableSchema;
import com.afan.dbmgr.handler.ConvertHandler;
import com.afan.dbmgr.pool.DBConnect;
import com.afan.dbmgr.pool.druid.DruidMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 包装的resultSet 根据标准的stable自动组装查询的结果
 * @author afan
 *
 */
public class ResultSetWrapper<T> implements Closeable {
	private static final Logger logger = LoggerFactory.getLogger(StatementWrapper.class);
	
	DBConnect conn = null;
	// 结果集
	private ResultSet rs;
	// 表结果对象
	private ResultSetMetaData metaData;
	// 表信息描述
	private SQLTable sqlTable;
	// 结果类
	private Class<T> clazz;
	// 结果list
	private List<T> results;
	// 结果数
	private AtomicInteger recode = new AtomicInteger();

	public ResultSetWrapper(DBConnect conn, Class<T> clazz) throws DBException {
		this.rs = this.conn.executeQuery();
		init(this.rs, clazz);
	}

	private void init(ResultSet rs, Class<T> clazz) throws DBException {
		try {
			this.rs = rs;
			this.metaData = rs.getMetaData();
			this.results = new ArrayList<>();
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
				results.add(autoLoad(rs));
				autoRecode();
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
			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				this.sqlTable.getSqlColumnByColumn(metaData.getColumnName(i)).setJdbcJavaType(metaData.getColumnClassName(i));
			}
		} catch (SQLException e) {
			throw new DBException(DBErrCode.ERR_WRESULT_INIT, "ResultSetWrapper-init", e);
		}
	}

	// 组装单条对象
	private T autoLoad(ResultSet rs) throws DBException {
		T o;
		try {
			o = clazz.newInstance();
			for (String columnName : this.sqlTable.getColumnMap().keySet()) {
				SQLColumn sqlColumn = this.sqlTable.getSqlColumnByColumn(columnName);
				// 不记录默认值 为空就不复
                try {
                    Object value = resultSetGet(rs, sqlColumn);
                    Field field = clazz.getField(sqlColumn.getFieldName());
                    field.setAccessible(true);
                    field.set(o, value);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
			}
		} catch (InstantiationException e) {
			throw new DBException(DBErrCode.ERR_WRESULT_AUTOLOAD, "ResultSetWrapper-autoLoad:InstantiationException", e);
		} catch (IllegalAccessException e) {
			throw new DBException(DBErrCode.ERR_WRESULT_AUTOLOAD, "ResultSetWrapper-autoLoad:IllegalAccessException", e);
		}
		return o;
	}
	
	// 取出result对应的值
	private Object resultSetGet(ResultSet rs, SQLColumn sqlColumn) throws DBException {
		ConvertHandler handler = DruidMgr.getInstance().getHandler(sqlColumn.getHandler());
		if (handler == null) {
			logger.error("field:" + sqlColumn.getFieldName() + " can not find handler : [" + sqlColumn.getHandler() + "]");
			throw new DBException(DBErrCode.ERR_WRESULT_SET, "field:" + sqlColumn.getFieldName() + " can not find handler : [" + sqlColumn.getHandler() + "]");
		}
		if(sqlColumn.getJdbcJavaType()!=null){
			return handler.convertResultSet(rs, sqlColumn);
		}
		return null;
	}

	public int getRecode() {
		return recode.get();
	}

	public void autoRecode() {
		recode.incrementAndGet();
	}
	
	public void close() {
		try {
			if (conn != null && conn.isAutoClose()) {
				conn.close();
			}
			if (rs != null && !rs.isClosed()) {
				rs.close();
			}
		} catch (SQLException e) {
			logger.error("ResultSetWrapper-close", e);
		}
	}

}
