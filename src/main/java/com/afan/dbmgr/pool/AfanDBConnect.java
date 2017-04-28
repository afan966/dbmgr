package com.afan.dbmgr.pool;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.afan.dbmgr.DBException;

/**
 * 标准封装JDBC连接接口
 * @author afan
 *
 */
public interface AfanDBConnect extends AutoCloseable {

	public void setInt(int i, Integer value) throws DBException;

	public void setLong(int i, Long value) throws DBException;

	public void setDouble(int i, Double value) throws DBException;

	public void setString(int i, String value) throws DBException;

	public void setObject(int i, Object value) throws DBException;
	
	public boolean isAutoClose();
	
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
	public CallableStatement prepareCall(String sql) throws DBException;
	
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
