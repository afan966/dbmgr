package com.afan.dbmgr.pool;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.afan.dbmgr.DBException;
import com.afan.dbmgr.DBTable;
import com.afan.dbmgr.config.DBErrCode;
import com.afan.dbmgr.config.DBMgrCache;
import com.afan.dbmgr.config.SQLTable;
import com.afan.dbmgr.config.TableSchema;
import com.afan.dbmgr.pool.wrap.StatementWrapper;

/**
 * 数据库链接管理封装实先
 * @author afan
 * @Description: 使用包装的 connect,preporetment,result 
 * 1.使用标准的SqlTable对象自动添加sql参数
 * 2.使用标准的SqlTable对象自动组装、返回对象，对象list
 */
public class AfanConnect extends DefaultConnect {
	private static final Logger logger = LoggerFactory.getLogger(AfanConnect.class);
	private StatementWrapper ptmtw;//包装PreparedStatement
	
	public AfanConnect() {
		this(null, true, true);
	}
	
	public AfanConnect(String dbName) {
		this(dbName, true, true);
	}

	public AfanConnect(String dbName, boolean autoCommit, boolean autoClose) {
		super(dbName, autoCommit, autoClose);
	}

	public PreparedStatement prepareStatement(String sql) throws DBException {
		this.ptmtw = new StatementWrapper(super.prepareStatement(sql), sql);
		return this.ptmt;
	}
	
	public PreparedStatement prepareStatement(String sql, Object... values) throws DBException {
		hasError = true;
		throw new DBException(DBErrCode.ERR_MGR_UN_SUPPORT, "AfanConnect unsupport. need use DefaultConnect");
	}

	public PreparedStatement prepareStatement(String sql, Object param) throws DBException {
		prepareStatement(sql);
		setStandardParam(param);
		return this.ptmt;
	}
	
	public void close(){
		super.close();
		System.out.println("close conn..");
	}
	
	/**
	 * 设置@DBTable的标准参数
	 */
	private void setStandardParam(Object param) throws DBException {
		if(param==null){
			throw new DBException(DBErrCode.ERR_WMGR_PARAM, "param is null");
		}
		if(param.getClass().getAnnotation(DBTable.class)==null){
			super.setObject(1, param);
		}else{
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
	
	public int[] executeBatch(Object param) throws DBException {
		addBatch(param);
		return this.ptmtw.executeBatch();
	}
	
	@Override
	public int insertOrUpdate(Object value) throws DBException {
		return super.insertOrUpdate(value);
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
	public void query(Object value) throws DBException {
		String sql = DBMgrCache.getStandardSql(value, DBMgrCache.SELECT);
		this.prepareStatement(sql, value);
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
