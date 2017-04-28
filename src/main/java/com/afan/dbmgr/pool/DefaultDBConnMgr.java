package com.afan.dbmgr.pool;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.afan.dbmgr.DBException;
import com.afan.dbmgr.config.DBErrCode;
import com.afan.dbmgr.util.SQLUtil;

/**
 * 默认的数据库链接管理实现(JDBC)
 * @author cf
 * @Description: 使用默认的 connect,preporetment,result
 */
public class DefaultDBConnMgr extends DBConnMgr {
	private static final Logger logger = LoggerFactory.getLogger(DefaultDBConnMgr.class);

	public DefaultDBConnMgr() {
		this.useWrapper = false;
	}

	public DefaultDBConnMgr(String dbName) throws DBException {
		this(dbName, true, true);
	}

	public DefaultDBConnMgr(String dbName, boolean autoCommit, boolean autoClose) throws DBException {
		long t1 = System.currentTimeMillis();
		connInit(dbName, autoCommit, autoClose);
		long used = (System.currentTimeMillis() - t1);
		if (used > MINTIME) {
			logger.debug("init {} conn used:{}ms", dbName, used);
		}
		this.useWrapper = false;
	}

	public void init(boolean autoCommit, boolean autoClose) throws DBException {
		try {
			this.autoCommit = autoCommit;
			this.autoClose = autoClose;
			this.conn.setAutoCommit(autoCommit);
		} catch (Exception e) {
			hasError = true;
			throw new DBException(DBErrCode.ERR_MGR_INIT, "init DefaultDBConnMgr error", e);
		}
	}

	public PreparedStatement prepareStatement(String sql) throws DBException {
		this.sql = sql;
		this.params = null;
		if (this.conn == null)
			connInit(null, true, true);
		try {
			this.ptmt = this.conn.prepareStatement(sql);
		} catch (SQLException e) {
			hasError = true;
			throw new DBException(DBErrCode.ERR_MGR_PREPARE, "prepareStatement", sql, e);
		}
		return this.ptmt;
	}

	public PreparedStatement prepareStatement(String sql, Object... values) throws DBException {
		this.sql = sql;
		if (this.conn == null)
			connInit(null, true, true);
		try {
			ptmt = this.conn.prepareStatement(this.sql);
			params = values;
			for (int i = 0; i < values.length; i++) {
				ptmt.setObject(i + 1, params[i]);
			}
		} catch (SQLException e) {
			hasError = true;
			throw new DBException(DBErrCode.ERR_MGR_PREPARE_SQL_VALUE, "prepareStatement-values", sql, e);
		}
		return this.ptmt;
	}

	public void addBatch() throws DBException {
		if (ptmt != null) {
			try {
				ptmt.addBatch();
			} catch (SQLException e) {
				hasError = true;
				throw new DBException(DBErrCode.ERR_MGR_BATCH, "addBatch", e);
			}
		}
	}

	public void addBatch(Object... values) throws DBException {
		try {
			params = values;
			for (int i = 0; i < values.length; i++) {
				ptmt.setObject(i + 1, params[i]);
			}
			ptmt.addBatch();
		} catch (SQLException e) {
			hasError = true;
			throw new DBException(DBErrCode.ERR_MGR_BATCH_VALUE, "addBatch-values", e);
		}
	}

	public ResultSet executeQuery() throws DBException {
		try {
			this.rs = ptmt.executeQuery();
		} catch (SQLException e) {
			hasError = true;
			throw new DBException(DBErrCode.ERR_MGR_EXEC_QUERY, "executeQuery", sql, e);
		}
		return this.rs;
	}

	public ResultSet executeQuery(String sql) throws DBException {
		this.sql = sql;
		if (this.conn == null)
			connInit(null, true, true);
		try {
			this.ptmt = this.conn.prepareStatement(this.sql);
		} catch (SQLException e) {
			hasError = true;
			throw new DBException(DBErrCode.ERR_MGR_PREPARE_SQL, "prepareStatement-ptmt-sql", sql, e);
		}

		try {
			this.rs = this.ptmt.executeQuery(sql);
		} catch (SQLException e) {
			hasError = true;
			throw new DBException(DBErrCode.ERR_MGR_EXEC_QUERY_SQL, "executeQuery-sql", sql, e);
		}
		return this.rs;
	}

	public int executeUpdate() throws DBException {
		try {
			return ptmt.executeUpdate();
		} catch (SQLException e) {
			hasError = true;
			throw new DBException(DBErrCode.ERR_MGR_EXEC_UPDATE, "executeUpdate", e);
		}
	}

	public int executeUpdate(String sql) throws DBException {
		this.sql = sql;
		if (this.conn == null)
			connInit(null, true, true);
		try {
			this.ptmt = this.conn.prepareStatement(this.sql);
		} catch (SQLException e) {
			hasError = true;
			throw new DBException(DBErrCode.ERR_MGR_PREPARE_SQL, "executeUpdate-ptmt-sql", sql, e);
		}

		try {
			return ptmt.executeUpdate();
		} catch (SQLException e) {
			throw new DBException(DBErrCode.ERR_MGR_EXEC_UPDATE_SQL, "executeUpdate-sql", sql, e);
		}
	}

	public int[] executeBatch() throws DBException {
		try {
			return ptmt.executeBatch();
		} catch (SQLException e) {
			hasError = true;
			throw new DBException(DBErrCode.ERR_MGR_EXEC_BATCH, "executeBatch", e);
		}
	}

	public long getLastInsertId() throws DBException {
		Statement s = null;
		ResultSet r = null;
		try {
			s = this.conn.createStatement();
			r = s.executeQuery("select LAST_INSERT_ID()");
			if (r.next()) {
				return r.getLong(1);
			}
		} catch (SQLException e) {
			hasError = true;
			throw new DBException(DBErrCode.ERR_MGR_LAST_INSERT_ID, "LAST_INSERT_ID", e);
		} finally {
			try {
				s.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				r.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0l;
	}

	public long insertReturnAutoId() throws DBException {
		ResultSet rrs = null;
		try {
			ptmt.executeUpdate();
			rrs = ptmt.executeQuery("select LAST_INSERT_ID()");
			if (rrs.next()) {
				return rrs.getLong(1);
			}
		} catch (SQLException e) {
			hasError = true;
			throw new DBException(DBErrCode.ERR_MGR_AND_LAST_INSERT_ID, "executeUpdate", e);
		}
		return 0;
	}

	public boolean existQuery() throws DBException {
		executeQuery();
		try {
			if (this.rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			hasError = true;
			throw new DBException(DBErrCode.ERR_MGR_EXISTQUERY, "existQuery", e);
		}
		return false;
	}
	
	/**
	 * 设置第一个参数 wrapper模式实现标准对象注入参数
	 */
	public void setStandardParam(Object param) throws DBException {
		throw new DBException(DBErrCode.ERR_MGR_UN_SUPPORT, "DefaultDBConnMgr Method not supported : standard param");
	}

	public long insertReturnAutoId(Object value) throws DBException {
		throw new DBException(DBErrCode.ERR_MGR_UN_SUPPORT, "DefaultDBConnMgr Method not supported : insertReturnAutoId");
	}

	public int insert(Object value) throws DBException {
		throw new DBException(DBErrCode.ERR_MGR_UN_SUPPORT, "DefaultDBConnMgr Method not supported : insert");
	}

	public int update(Object value) throws DBException {
		throw new DBException(DBErrCode.ERR_MGR_UN_SUPPORT, "DefaultDBConnMgr Method not supported : update");
	}

	public int delete(Object value) throws DBException {
		throw new DBException(DBErrCode.ERR_MGR_UN_SUPPORT, "DefaultDBConnMgr Method not supported : delete");
	}

	public void query(Object value) throws DBException {
		throw new DBException(DBErrCode.ERR_MGR_UN_SUPPORT, "DefaultDBConnMgr Method not supported : query");
	}

	public int insertOrUpdate(Object value) throws DBException {
		Object[] sqlParams = SQLUtil.insertOrUpdate(value, null, null);
		if (sqlParams != null && sqlParams.length == 2) {
			String sql = sqlParams[0].toString();
			Object[] values = ((List<?>) sqlParams[1]).toArray();
			this.prepareStatement(sql, values);
			return this.executeUpdate();
		}
		return 0;
	}

	public void close() {
		try {
			if (conn != null && !conn.isClosed()) {
				if (autoCommit == false) {
					if (hasError) {
						rollback();
					} else {
						commit();
					}
				}
				conn.close();
			}
		} catch (Exception e) {
			logger.error("close connection error", e);
		}
		try {
			if (ptmt != null && !ptmt.isClosed()) {
				ptmt.close();
				ptmt = null;
			}
		} catch (Exception e) {
			logger.error("close prepareStatement error", e);
		}
		try {
			if (rs != null && !rs.isClosed()) {
				rs.close();
				rs = null;
			}
		} catch (Exception e) {
			logger.error("close result error", e);
		}
	}

}