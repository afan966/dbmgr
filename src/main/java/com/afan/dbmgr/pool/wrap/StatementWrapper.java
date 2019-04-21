package com.afan.dbmgr.pool.wrap;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.afan.dbmgr.DBException;
import com.afan.dbmgr.config.DBErrCode;
import com.afan.dbmgr.config.SQLColumn;
import com.afan.dbmgr.config.SQLTable;
import com.afan.dbmgr.handler.ConvertHandler;
import com.afan.dbmgr.pool.druid.DruidMgr;
import com.afan.dbmgr.util.StringUtil;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.util.JdbcConstants;

/**
 * PreparedStatement包装 用于标准参数自动注入，和JDBC实际执行
 * @author afan
 *
 */
public class StatementWrapper {
	private static final Logger logger = LoggerFactory.getLogger(StatementWrapper.class);

	// 用于把sql string解析成JDBC-sql可识别对象，不支持复杂的联表查询
	private PreparedStatement ptmt;
	private String sql;
	private Object[] params;
	private List<SQLStatement> stmtList;

	public StatementWrapper(PreparedStatement ptmt, String sql) throws DBException {
		this.ptmt = ptmt;
		this.sql = sql;
		clearParameters();
		params = new Object[StringUtil.find(this.sql, StringUtil.QUESTION)];
		stmtList = SQLUtils.parseStatements(this.sql, JdbcConstants.MYSQL);
	}

	public void setParameters(Object[] params) {
		this.params = params;
	}

	/**
	 * Statement清理参数
	 * 
	 * @throws DBException
	 */
	public void clearParameters() throws DBException {
		try {
			ptmt.clearParameters();
		} catch (SQLException e) {
			logger.error("clearParameters", e);
			throw new DBException(DBErrCode.ERR_WSTAT_CLEAR_PARAM, e.getMessage(), e);
		}
	}

	/**
	 * 获取自动增长的key
	 * 
	 * @return
	 * @throws DBException
	 */
	public Serializable getGeneratedKeys() throws DBException {
		try {
			ResultSet rs = ptmt.getGeneratedKeys();
			if (rs.next()){
				return (Serializable) rs.getObject(1);
			}
			return null;
		} catch (SQLException e) {
			logger.error("getGeneratedKeys", e);
			throw new DBException(DBErrCode.ERR_WSTAT_GENERATED_KEY, e.getMessage(), e);
		}
	}

	public void addBatch() throws DBException {
		if (ptmt != null) {
			try {
				ptmt.addBatch();
			} catch (SQLException e) {
				throw new DBException(DBErrCode.ERR_WSTAT_BATCH, "addBatch", e);
			}
		}
	}

	/**
	 * 执行查询
	 * 
	 * @return
	 * @throws DBException
	 */
	public ResultSet executeQuery() throws DBException {
		try {
			return ptmt.executeQuery();
		} catch (SQLException e) {
			logger.error("executeQuery", e);
			throw new DBException(DBErrCode.ERR_WSTAT_EXEC_QUERY, e.getMessage(), e);
		}
	}

	/**
	 * 执行修改删除
	 * 
	 * @return
	 * @throws DBException
	 */
	public int executeUpdate() throws DBException {
		try {
			return ptmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("executeUpdate", e);
			throw new DBException(DBErrCode.ERR_WSTAT_EXEC_UPDATE, e.getMessage(), e);
		}
	}

	/**
	 * 执行批量操作
	 * 
	 * @return
	 * @throws DBException
	 */
	public int[] executeBatch() throws DBException {
		try {
			return ptmt.executeBatch();
		} catch (SQLException e) {
			logger.error("executeBatch", e);
			throw new DBException(DBErrCode.ERR_WSTAT_EXEC_BATCH, e.getMessage(), e);
		}
	}

	/**
	 * 包装的statement自动注入参数（基于Druid.SQLUtils）
	 * 
	 * @param sqlTable
	 * @throws DBException
	 */
	public void setStandardParam(SQLTable sqlTable) throws DBException {
		Incr i = new Incr(1);
		SQLStatement stmt = stmtList.get(0);
		if (stmt instanceof MySqlInsertStatement) {
			//insert
			MySqlInsertStatement insert = (MySqlInsertStatement) stmt;
			List<SQLExpr> insertColumns = insert.getColumns();
			List<SQLExpr> insertValues = insert.getValues().getValues();
			List<SQLExpr> duplicateKeyColumns = insert.getDuplicateKeyUpdate();
			if(insertColumns.size()!=insertValues.size()){
				throw new DBException(DBErrCode.ERR_SYNTAX_ERROR, "SQL["+this.sql+"] Syntax Error");
			}
			for (int j = 0; j < insertColumns.size(); j++) {
				SQLExpr colExpr = insertColumns.get(j);
				SQLExpr valExpr = insertValues.get(j);
				setVarParam(i, colExpr, valExpr, sqlTable);
			}
            for (SQLExpr duplicateKeyColumn : duplicateKeyColumns) {
                SQLBinaryOpExpr opExpr = (SQLBinaryOpExpr) duplicateKeyColumn;
                SQLExpr colExpr = opExpr.getLeft();
                SQLExpr valExpr = opExpr.getRight();
                setVarParam(i, colExpr, valExpr, sqlTable);
            }
		} else if (stmt instanceof MySqlUpdateStatement) {
			MySqlUpdateStatement update = (MySqlUpdateStatement) stmt;
			SQLBinaryOpExpr whereExpr = (SQLBinaryOpExpr)update.getWhere();
            List<SQLUpdateSetItem> itemList = update.getItems();
            for (SQLUpdateSetItem item : itemList) {
                SQLExpr colExpr = item.getColumn();
                SQLExpr valExpr = item.getValue();
                setVarParam(i, colExpr, valExpr, sqlTable);
            }
			setWhereParam(i, whereExpr, sqlTable);
		} else if (stmt instanceof MySqlDeleteStatement) {
			MySqlDeleteStatement delete = (MySqlDeleteStatement) stmt;
			SQLBinaryOpExpr whereExpr = (SQLBinaryOpExpr)delete.getWhere();
			setWhereParam(i, whereExpr, sqlTable);
		} else if (stmt instanceof SQLSelectStatement) {
			SQLSelectStatement select = (SQLSelectStatement) stmt;
			MySqlSelectQueryBlock queryBlock = (MySqlSelectQueryBlock)select.getSelect().getQuery();
			SQLBinaryOpExpr whereExpr = (SQLBinaryOpExpr)queryBlock.getWhere();
			setWhereParam(i, whereExpr, sqlTable);
		}
	}
	
	//设置where条件参数
	public void setWhereParam(Incr i, SQLBinaryOpExpr expr, SQLTable sqlTable) throws DBException {
		SQLExpr left = expr.getLeft();
		SQLExpr right = expr.getRight();
		
		if (left instanceof SQLBinaryOpExpr) {
			SQLBinaryOpExpr opExpr = (SQLBinaryOpExpr) left;
			setWhereParam(i, opExpr, sqlTable);
		}else if(left instanceof SQLIdentifierExpr) {
			setVarParam(i, left, right, sqlTable);
		}
		
		if (right instanceof SQLBinaryOpExpr) {
			SQLBinaryOpExpr rightExpr = (SQLBinaryOpExpr) right;
			setVarParam(i, rightExpr.getLeft(), rightExpr.getRight(), sqlTable);
		}
	}
	
	//设置SQL中?占位符的数据结构值
	private void setVarParam(Incr i, SQLExpr colExpr, SQLExpr valExpr, SQLTable sqlTable) throws DBException {
		String columnName = ((SQLIdentifierExpr)(colExpr)).getName();
		SQLColumn sqlColumn = sqlTable.getSqlColumnByColumn(columnName);
		if(sqlColumn == null){
			throw new DBException(DBErrCode.ERR_COLUMN_NON_EXIST, " column:["+columnName+"] not exist in SQL["+this.sql+"]");
		}
		if (valExpr instanceof SQLVariantRefExpr) {
			setParam(i.incr(), sqlColumn);
		}
	}

	private void setParam(int i, SQLColumn sqlColumn) throws DBException {
		try {
			ConvertHandler handler = DruidMgr.getInstance().getHandler(sqlColumn.getHandler());
			if (handler == null) {
				logger.error("field:" + sqlColumn.getFieldName() + " can not find handler : [" + sqlColumn.getHandler() + "]");
				throw new DBException(DBErrCode.ERR_WRESULT_SET, "field:" + sqlColumn.getFieldName() + " can not find handler : [" + sqlColumn.getHandler() + "]");
			}
			handler.convertPreparedStatement(this.ptmt, i, sqlColumn);
			params[i - 1] = sqlColumn.getValue();
		} catch (Exception e) {
			e.printStackTrace();
			throw new DBException(DBErrCode.ERR_SET_PARAM, "setParam error:" + sqlColumn.toString(), sql, e);
		}
	}
	
	public void close() {
		try {
			ptmt.close();
		} catch (SQLException e) {
			logger.error("close", e);
			e.printStackTrace();
		}
	}
	
	class Incr{
		int value;
		public Incr(int i) {
			value = i;
		}
		public int incr() {
			return value++;
		}
	}

}
