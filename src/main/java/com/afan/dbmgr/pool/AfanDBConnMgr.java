package com.afan.dbmgr.pool;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.afan.dbmgr.DBException;
import com.afan.dbmgr.config.DBErrCode;
import com.afan.dbmgr.config.DBMgrCache;
import com.afan.dbmgr.config.SQLTable;
import com.afan.dbmgr.config.TableSchema;
import com.afan.dbmgr.pool.wrap.StatementWrapper;
import com.afan.dbmgr.util.SQLUtil;

/**
 * 数据库链接管理封装实现
 * @author cf
 * @Description: 使用包装的 connect,preporetment,result 
 * 1.使用标准的SqlTable对象自动添加sql参数
 * 2.使用标准的SqlTable对象自动组装、返回对象，对象list
 */
public class AfanDBConnMgr extends DefaultDBConnMgr implements AfanDBConnect {
	private static final Logger logger = LoggerFactory.getLogger(AfanDBConnMgr.class);

	private StatementWrapper ptmtw;//包装PreparedStatement

	public AfanDBConnMgr() {
		this(null, true, true);
	}
	
	public AfanDBConnMgr(String dbName) {
		this(dbName, true, true);
	}

	public AfanDBConnMgr(String dbName, boolean autoCommit, boolean autoClose) {
		super(dbName, autoCommit, autoClose);
	}

	public PreparedStatement prepareStatement(String sql) throws DBException {
		this.sql = sql;
		super.prepareStatement(sql);
		this.ptmtw = new StatementWrapper(this.ptmt, sql);
		return this.ptmt;
	}

	public PreparedStatement prepareStatement(String sql, Object param) throws DBException {
		prepareStatement(sql);
		setStandardParam(param);
		return this.ptmt;
	}

	public ResultSet executeQuery() throws DBException {
		if (this.params != null && this.params.length > 0) {
			ptmtw.setParameters(this.params);
		}
		this.rs = ptmtw.executeQuery();
		return this.rs;
	}

	public int executeUpdate() throws DBException {
		if (this.params != null && this.params.length > 0) {
			ptmtw.setParameters(this.params);
		}
		return ptmtw.executeUpdate();
	}

	public boolean existQuery() throws DBException {
		executeQuery();
		try {
			if (this.rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			hasError = true;
			throw new DBException(DBErrCode.ERR_WMGR_EXISTQUERY, "existQuery", e);
		}
		return false;
	}

	public void addBatch() throws DBException {
		this.ptmtw.addBatch();
	}
	
	public void addBatch(Object... values) throws DBException {
		for (Object param : values) {
			if (param instanceof ArrayList) {
				List<?> ps = (ArrayList<?>)param;
				if(ps == null || ps.size() ==0){
					throw new DBException(DBErrCode.ERR_BATCH_PARAM_NULL, "batch param is null");
				}
				for (Object p : (ArrayList<?>)param) {
					setStandardParam(p);
					this.ptmtw.addBatch();
				}
			} else {
				setStandardParam(param);
				this.ptmtw.addBatch();
			}
		}
	}

	public int[] executeBatch() throws DBException {
		return this.ptmtw.executeBatch();
	}

	@Override
	public long insertReturnAutoId(Object value) throws DBException {
		String sql = DBMgrCache.getStandardSql(value, DBMgrCache.INSERT);
		this.prepareStatement(sql, value);
		return this.insertReturnAutoId();
	}

	@Override
	public int insert(Object value) throws DBException {
		String sql = DBMgrCache.getStandardSql(value, DBMgrCache.INSERT);
		this.prepareStatement(sql, value);
		return this.executeUpdate();
	}

	@Override
	public int update(Object value) throws DBException {
		String sql = DBMgrCache.getStandardSql(value, DBMgrCache.UPDATE);
		this.prepareStatement(sql, value);
		return this.executeUpdate();
	}
	
	@Override
	public int delete(Object value) throws DBException {
		String sql = DBMgrCache.getStandardSql(value, DBMgrCache.DELETE);
		this.prepareStatement(sql, value);
		return this.executeUpdate();
	}
	
	@Override
	public int insertOrUpdate(Object value) throws DBException {
		Object[] sqlParams = SQLUtil.insertOrUpdate(value, null, null);
		if (sqlParams != null && sqlParams.length == 2) {
			String sql = sqlParams[0].toString();
			Object[] values = ((List<?>) sqlParams[1]).toArray();
			this.prepareStatement(sql, values);
			return super.executeUpdate();
		}
		return 0;
	}
	
	@Override
	public void query(Object value) throws DBException {
		String sql = DBMgrCache.getStandardSql(value, DBMgrCache.SELECT);
		this.prepareStatement(sql, value);
	}

	/**
	 * 设置SQLtable的参数
	 */
	public void setStandardParam(Object param) throws DBException {
		try {
			SQLTable sqlTable = TableSchema.schema().getSqlTableParam(param);
			if (sqlTable != null) {
				ptmtw.setStandardParam(sqlTable);
			} else {
				logger.error("STable:{} setParam schema is null", param.getClass().getName());
				hasError = true;
			}
		} catch (DBException e) {
			throw e;
		} catch (Exception e) {
			hasError = true;
			logger.error("STable setParam error:"+param.getClass().getName(), e);
			throw new DBException(DBErrCode.ERR_WMGR_PARAM, e.getMessage(), e);
		}
	}

	@Override
	public int[] insertBatch(List<?> values) throws DBException {
		String sql = DBMgrCache.getStandardSql(values.get(0), DBMgrCache.INSERT);
		this.prepareStatement(sql);
		this.addBatch(values);
		return this.executeBatch();
	}

	@Override
	public int[] updateBatch(List<?> values) throws DBException {
		String sql = DBMgrCache.getStandardSql(values.get(0), DBMgrCache.UPDATE);
		this.prepareStatement(sql);
		this.addBatch(values);
		return this.executeBatch();
	}

	@Override
	public int[] deleteBatch(List<?> values) throws DBException {
		String sql = DBMgrCache.getStandardSql(values.get(0), DBMgrCache.DELETE);
		this.prepareStatement(sql);
		this.addBatch(values);
		return this.executeBatch();
	}

}
