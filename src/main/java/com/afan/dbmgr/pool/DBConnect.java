package com.afan.dbmgr.pool;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import com.afan.dbmgr.DBException;
import com.afan.dbmgr.util.SqlObject;

/**
 * 简单JDBC连接接口
 * @author afan
 *
 */
public interface DBConnect extends AutoCloseable {

	/**
	 * 基础参数赋值
	 * @param i 编号
	 * @param value 值
	 * @throws DBException 自定异常
	 */
	void setInt(int i, Integer value) throws DBException;

	void setLong(int i, Long value) throws DBException;

	void setDouble(int i, Double value) throws DBException;

	void setString(int i, String value) throws DBException;

	void setObject(int i, Object value) throws DBException;
	
	boolean isAutoClose();
	
	/**
	 * 预编译sql
	 * 
	 * @param sql
	 * @return
	 * @throws DBException
	 */
	 PreparedStatement prepareStatement(String sql) throws DBException;
	
	 PreparedStatement prepareStatement(SqlObject sqlObject) throws DBException;

	/**
	 * 预编译sql，指定参数集
	 * 
	 * @param sql
	 * @param values
	 * @return
	 * @throws DBException
	 */
	 PreparedStatement prepareStatement(String sql, Object... values) throws DBException;
	
	/**
	 * 预编译sql，指定参数对象
	 * @param sql
	 * @param param
	 * @return
	 * @throws DBException
	 */
	 PreparedStatement prepareStatement(String sql, Object param) throws DBException;

	/**
	 * 执行call的参数，存储过程等方法
	 * 
	 * @param sql
	 * @return
	 * @throws DBException
	 */
	CallableStatement prepareCall(String sql) throws DBException;
	
	/**
	 * 设置参数后，批量加入 调用方法: PreparedStatement ptmt = conn.prepareStatement();
	 * ptmt.setInt(); conn.addBatch();
	 * 
	 * @throws DBException
	 */
	 void addBatch() throws DBException;

	/**
	 * 设置参数后，批量加入 调用方法: conn.addBatch(values);
	 * 
	 * @param values
	 * @throws DBException
	 */
	 void addBatch(Object... values) throws DBException;
	
	

	/**
	 * 执行查询
	 * 
	 * @return
	 * @throws DBException
	 */
	 ResultSet executeQuery() throws DBException;

	/**
	 * 执行查询sql
	 * 
	 * @param sql
	 * @return
	 * @throws DBException
	 */
	 ResultSet executeQuery(String sql) throws DBException;
	
	/**
	 * 执行变更sql
	 * 
	 * @return
	 * @throws DBException
	 */
	 int executeUpdate() throws DBException;

	/**
	 * 执行变更sql
	 * 
	 * @param sql
	 * @return
	 * @throws DBException
	 */
	 int executeUpdate(String sql) throws DBException;
	
	/**
	 * 执行批量sql
	 * 
	 * @return
	 * @throws DBException
	 */
	 int[] executeBatch() throws DBException;
	
	/**
	 * 执行批量sql
	 * 
	 * @return
	 * @throws DBException
	 */
	 int[] executeBatch(Object param) throws DBException;
	
	/**
	 * 查询是否存在
	 * 
	 * @return
	 * @throws DBException
	 */
	 boolean existQuery() throws DBException;

	/**
	 * 返回上次的自增id MYSQL的LAST_INSERT_ID()
	 * 
	 * @return
	 * @throws DBException
	 */
	 long getLastInsertId() throws DBException;

	/**
	 * 插入并返回上次的自增id MYSQL的LAST_INSERT_ID()
	 * 
	 * @return
	 * @throws DBException
	 */
	 long insertReturnAutoId() throws DBException;
	
	/**
	 * 执行插入或修改根据标准（@DBTable）
	 * 
	 * @param value
	 * @return
	 * @throws DBException
	 */
	 int insertOrUpdate(Object value) throws DBException;
	
	/**
	 * 插入并返回上次的自增id MYSQL的LAST_INSERT_ID() 根据标准（@DBTable）
	 * @param value
	 * @return
	 * @throws DBException
	 */
	 long insertReturnAutoId(Object value) throws DBException;

	/**
	 * 插入 根据标准（@DBTable）
	 * @param value
	 * @return
	 * @throws DBException
	 */
	 int insert(Object value) throws DBException;

	/**
	 * 修改 根据标准（@DBTable）
	 * @param value
	 * @return
	 * @throws DBException
	 */
	 int update(Object value) throws DBException;
	
	/**
	 * 删除 根据标准（@DBTable）
	 * @param value
	 * @return
	 * @throws DBException
	 */
	 int delete(Object value) throws DBException;
	
	/**
	 * 查询 根据标准（@DBTable）
	 * @param value
	 * @return
	 * @throws DBException
	 */
	 void query(Object value) throws DBException;
	
	/**
	 * 批量插入 根据标准（@DBTable）
	 * @param values
	 * @return
	 * @throws DBException
	 */
	 int[] insertBatch(List<?> values) throws DBException;

	/**
	 * 批量修改 根据标准（@DBTable）
	 * @param values
	 * @return
	 * @throws DBException
	 */
	 int[] updateBatch(List<?> values) throws DBException;
	
	/**
	 * 批量删除 根据标准（@DBTable）
	 * @param values
	 * @return
	 * @throws DBException
	 */
	 int[] deleteBatch(List<?> values) throws DBException;
	
	/**
	 * 关闭连接
	 */
	 void close();
}
