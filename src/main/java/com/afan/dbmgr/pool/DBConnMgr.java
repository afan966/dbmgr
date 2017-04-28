package com.afan.dbmgr.pool;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.afan.dbmgr.DBException;
import com.afan.dbmgr.config.DBErrCode;
import com.afan.dbmgr.pool.druid.DruidMgr;
import com.afan.dbmgr.util.StringUtil;

/**
 * 数据库链接管理
 * @author cf
 * 
 */
public abstract class DBConnMgr implements AutoCloseable {
	private static final Logger logger = LoggerFactory.getLogger(DBConnMgr.class);
	
	//日志输出时间标准
	protected static final int MINTIME = 10;
	protected static final int NORMAL = 100;
	protected static final int LARGETIME = 1000;

	public static final int conRetryTime = 10;// 链接失败重试次数

	protected Connection conn;// JDBC连接
	protected PreparedStatement ptmt;// JDBC编译器
	protected ResultSet rs;// JDBC结果集
	protected String sql;// 执行的sql
	protected Object[] params;// sql参数集

	// 数据连接别名
	protected String dbName;
	// 自动提交
	protected boolean autoCommit = true;
	// 自动关闭,实现了AutoCloseable会自动调用close方法(jdk1.7的规范)
	protected boolean autoClose = true;
	// 默认不使用包装
	protected boolean useWrapper = false;
	protected boolean hasError = false;

	public boolean isAutoClose() {
		return autoClose;
	}

	/**
	 * 设置事务级别 四种隔离级别：
	 * con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);//最底级别：只保证不会读到非法数据，上述3个问题有可能发生
	 * con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);//默认级别：可以防止脏读
	 * con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);//可以防止脏读和不可重复读取
	 * con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);//最高级别：防止上述3种情况，事务串行执行，慎用
	 * 
	 * @param level
	 * @throws DBException
	 */
	public void setTransactionLevel(int level) throws DBException {
		if (conn != null) {
			try {
				conn.setTransactionIsolation(level);
			} catch (SQLException e) {
				hasError = true;
				throw new DBException(DBErrCode.ERR_TRANSACTION, "setTransactionLevel", e);
			}
		}
	}

	/**
	 * 设置保存点
	 * 
	 * @param point
	 * @return
	 * @throws DBException
	 */
	public Savepoint setSavepoint(String point) throws DBException {
		if (conn != null) {
			try {
				return conn.setSavepoint(point);
			} catch (SQLException e) {
				hasError = true;
				throw new DBException(DBErrCode.ERR_SAVE_POINT, "setSavepoint", e);
			}
		} else {
			hasError = true;
			throw new DBException(DBErrCode.ERR_CONN_NULL, "conn is null");
		}
	}

	/**
	 * 回滚到保存点
	 * 
	 * @param point
	 * @throws DBException
	 */
	public void releaseSavepoint(Savepoint point) throws DBException {
		if (conn != null) {
			try {
				conn.releaseSavepoint(point);
			} catch (SQLException e) {
				hasError = true;
				throw new DBException(DBErrCode.ERR_RELEASE_SAVE_POINT, "releaseSavepoint", e);
			}
		} else {
			hasError = true;
			throw new DBException(DBErrCode.ERR_CONN_NULL, "conn is null");
		}
	}

	/**
	 * 提交事务
	 * 
	 * @throws DBException
	 */
	public void commit() throws DBException {
		if (conn != null) {
			try {
				conn.commit();
			} catch (SQLException e) {
				hasError = true;
				throw new DBException(DBErrCode.ERR_COMMIT, "commit", e);
			}
		} else {
			hasError = true;
			throw new DBException(DBErrCode.ERR_CONN_NULL, "conn is null");
		}
	}

	/**
	 * 回滚事务
	 * 
	 * @throws DBException
	 */
	public void rollback() throws DBException {
		if (conn != null) {
			try {
				conn.rollback();
			} catch (SQLException e) {
				hasError = true;
				throw new DBException(DBErrCode.ERR_ROLLBACK, "rollback", e);
			}
		} else {
			hasError = true;
			throw new DBException(DBErrCode.ERR_CONN_NULL, "conn is null");
		}
	}

	public void setInt(int i, Integer value) throws DBException {
		try {
			ptmt.setInt(i, value);
			setParams(i - 1, value);
		} catch (SQLException e) {
			hasError = true;
			throw new DBException(DBErrCode.ERR_SET_INT, e.getMessage());
		}
	}

	public void setLong(int i, Long value) throws DBException {
		try {
			ptmt.setLong(i, value);
			setParams(i - 1, value);
		} catch (SQLException e) {
			logger.error("setLong", e);
			throw new DBException(DBErrCode.ERR_SET_LONG, e.getMessage());
		}
	}

	public void setDouble(int i, Double value) throws DBException {
		try {
			ptmt.setDouble(i, value);
			setParams(i - 1, value);
		} catch (SQLException e) {
			logger.error("setDouble", e);
			throw new DBException(DBErrCode.ERR_SET_DOUBLE, e.getMessage());
		}
	}

	public void setString(int i, String value) throws DBException {
		try {
			ptmt.setString(i, value);
			setParams(i - 1, value);
		} catch (SQLException e) {
			hasError = true;
			throw new DBException(DBErrCode.ERR_SET_STRING, e.getMessage());
		}
	}

	public void setObject(int i, Object value) throws DBException {
		try {
			ptmt.setObject(i, value);
			setParams(i - 1, value);
		} catch (SQLException e) {
			hasError = true;
			throw new DBException(DBErrCode.ERR_SET_OBJECT, e.getMessage());
		}
	}

	// 设置参数集，如果不存在就初始化他
	protected void setParams(int no, Object value) {
		if (params == null){
			params = new Object[StringUtil.find(this.sql, StringUtil.QUESTION)];
		}
		params[no] = value;
	}

	/**
	 * 初始化连接信息，从连接池里面获取
	 * 
	 * @param dbName
	 * @param autoCommit
	 * @param autoClose
	 * @throws DBException
	 */
	public void connInit(String dbName, boolean autoCommit, boolean autoClose) throws DBException {
		int retry = 0;
		while (retry < conRetryTime) {
			try {
				this.dbName = dbName;
				this.conn = DruidMgr.getInstance().getConnection(dbName);
				init(autoCommit, autoClose);
			} catch (DBException e) {
				this.conn = null;
				if (DBErrCode.ERR_CONNECT_WAIT == e.getCode()) {
					retry++;
					logger.error("create conn error{}, retry:{}...", e.getMessage(), retry);
					try {
						Thread.sleep(retry * 1000L);
					} catch (InterruptedException ie) {
					}
				} else {
					throw e;
				}
			} catch (Exception e) {
				this.conn = null;
				hasError = true;
				logger.error("init connect error...", e);
				throw new DBException(DBErrCode.ERR_CONNECT_INIT, "init connect error", e);
			}
			if (this.conn != null) {
				break;
			}
		}
	}

	/**
	 * 初始化自动提交和自动关闭
	 * 
	 * @param autoCommit
	 * @param autoClose
	 * @throws DBException
	 */
	public abstract void init(boolean autoCommit, boolean autoClose) throws DBException;
	
	
	
	/**
	 * 预编译sql
	 * 
	 * @param sql
	 * @return
	 * @throws DBException
	 */
	public abstract PreparedStatement prepareStatement(String sql) throws DBException;

	/**
	 * 预编译sql，指定参数集
	 * 
	 * @param sql
	 * @param values
	 * @return
	 * @throws DBException
	 */
	public abstract PreparedStatement prepareStatement(String sql, Object... values) throws DBException;

	/**
	 * 执行call的参数，存储过程等方法
	 * 
	 * @param sql
	 * @return
	 * @throws DBException
	 */
	public CallableStatement prepareCall(String sql) throws DBException {
		try {
			this.sql = sql;
			params = new Object[StringUtil.find(this.sql, "?")];
			return conn.prepareCall(this.sql);
		} catch (SQLException e) {
			hasError = true;
			throw new DBException(DBErrCode.ERR_PREPARECALL, e.getMessage());
		}
	}
	
	/**
	 * 自动设置对象标准参数 default模式：设置第一个参数
	 * wrapper模式：设置对象参数，根据标准对象自动填充参数（需要标注@DBTable，@DBColumn）
	 * 
	 * @param param
	 * @throws DBException
	 */
	public abstract void setStandardParam(Object param) throws DBException;

	/**
	 * 设置参数后，批量加入 调用方法: PreparedStatement ptmt = conn.prepareStatement();
	 * ptmt.setInt(); conn.addBatch();
	 * 
	 * @throws DBException
	 */
	public abstract void addBatch() throws DBException;

	/**
	 * 设置参数后，批量加入 调用方法: conn.addBatch(values);
	 * 
	 * @param values
	 * @throws DBException
	 */
	public abstract void addBatch(Object... values) throws DBException;
	
	

	/**
	 * 执行查询
	 * 
	 * @return
	 * @throws DBException
	 */
	public abstract ResultSet executeQuery() throws DBException;

	/**
	 * 执行查询sql
	 * 
	 * @param sql
	 * @return
	 * @throws DBException
	 */
	public abstract ResultSet executeQuery(String sql) throws DBException;
	
	/**
	 * 执行变更sql
	 * 
	 * @return
	 * @throws DBException
	 */
	public abstract int executeUpdate() throws DBException;

	/**
	 * 执行变更sql
	 * 
	 * @param sql
	 * @return
	 * @throws DBException
	 */
	public abstract int executeUpdate(String sql) throws DBException;
	
	/**
	 * 执行批量sql
	 * 
	 * @return
	 * @throws DBException
	 */
	public abstract int[] executeBatch() throws DBException;
	
	

	/**
	 * wrapper模式执行查询根据标准的主键（@DBTable.primaryClumns）
	 * 
	 * @param value
	 * @throws DBException
	 */
	public abstract void query(Object value) throws DBException;

	/**
	 * wrapper模式执行插入根据标准（@DBTable）
	 * 
	 * @param value
	 * @return
	 * @throws DBException
	 */
	public abstract int insert(Object value) throws DBException;

	/**
	 * wrapper模式执行修改插入根据标准（@DBTable）
	 * 
	 * @param value
	 * @return
	 * @throws DBException
	 */
	public abstract int update(Object value) throws DBException;

	/**
	 * wrapper模式执行删除插入根据标准（@DBTable）
	 * 
	 * @param value
	 * @return
	 * @throws DBException
	 */
	public abstract int delete(Object value) throws DBException;
	
	/**
	 * 查询是否存在
	 * 
	 * @return
	 * @throws DBException
	 */
	public abstract boolean existQuery() throws DBException;

	/**
	 * 返回上次的自增id MYSQL的LAST_INSERT_ID()
	 * 
	 * @return
	 * @throws DBException
	 */
	public abstract long getLastInsertId() throws DBException;

	/**
	 * 插入并返回上次的自增id MYSQL的LAST_INSERT_ID()
	 * 
	 * @return
	 * @throws DBException
	 */
	public abstract long insertReturnAutoId() throws DBException;
	
	/**
	 * wrapper模式执行插入并返回自增ID根据标准（@DBTable）
	 * 
	 * @param value
	 * @return
	 * @throws DBException
	 */
	public abstract long insertReturnAutoId(Object value) throws DBException;

	/**
	 * 执行插入或修改根据标准（@DBTable）
	 * 
	 * @param value
	 * @return
	 * @throws DBException
	 */
	public abstract int insertOrUpdate(Object value) throws DBException;

	/**
	 * 关闭连接
	 */
	public abstract void close();

}
