package com.afan.dbmgr.pool.wrap;

import java.io.Serializable;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.afan.dbmgr.DBException;
import com.afan.dbmgr.config.DBErrCode;
import com.afan.dbmgr.config.SQLColumn;
import com.afan.dbmgr.config.SQLTable;
import com.afan.dbmgr.handler.ConvertHandler;
import com.afan.dbmgr.pool.druid.DruidMgr;
import com.afan.dbmgr.util.StringUtil;

/**
 * 
 * @author cf
 * @Description: PreparedStatement包装 用于标准参数自动注入，和JDBC实际执行
 */
public class StatementWrapper {
	private static final Logger logger = LoggerFactory.getLogger(StatementWrapper.class);

	// 用于把sql string解析成JDBC-sql可识别对象，不支持复杂的联表查询
	private CCJSqlParserManager sqlParser = new CCJSqlParserManager();
	private Statement parser;
	private PreparedStatement ptmt;
	private String sql;
	private Object[] params;

	public StatementWrapper(PreparedStatement ptmt, String sql) throws DBException {
		this.ptmt = ptmt;
		this.sql = sql;
		clearParameters();
		params = new Object[StringUtil.find(this.sql, StringUtil.QUESTION)];
		try {
			parser = sqlParser.parse(new StringReader(sql));
		} catch (JSQLParserException e) {
			logger.error("StatementWrapper-ptmt unknow sql:" + sql, e);
			throw new DBException(DBErrCode.ERR_WSTAT_PARSE_SQL, e.getMessage(), sql, e);
		}
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
			if (rs.next())
				return (Serializable) rs.getObject(1);
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
	 * 包装的statement自动注入参数
	 * 
	 * @param sqlTable
	 * @throws DBException
	 */
	@SuppressWarnings({ "rawtypes" })
	public void setStandardParam(SQLTable sqlTable) throws DBException {
		ParamNo pno = new ParamNo(1);
		if (parser instanceof Insert) {
			Insert insert = (Insert) parser;
			List expressionList = ((ExpressionList) insert.getItemsList()).getExpressions();
			for (int i = 0; i < insert.getColumns().size(); i++) {
				String columnName = ((Column) insert.getColumns().get(i)).getColumnName();
				SQLColumn column = sqlTable.getSqlColumnByColumn(columnName);
				// 把sql参数替换成对象属性值
				if (expressionList.get(i) instanceof JdbcParameter) {
					setParam(pno.grow(), column);
				}
			}
		} else if (parser instanceof Update) {
			Update update = (Update) parser;
			List expressionList = update.getExpressions();
			for (int i = 0; i < update.getColumns().size(); i++) {
				String columnName = ((Column) update.getColumns().get(i)).getColumnName();
				SQLColumn column = sqlTable.getSqlColumnByColumn(columnName);
				if (expressionList.get(i) instanceof JdbcParameter) {
					setParam(pno.grow(), column);
				}
			}
			// where
			Expression expre = update.getWhere();
			setWhereParam(expre, sqlTable, pno);
		} else if (parser instanceof Delete) {
			Delete delete = (Delete) parser;
			Expression expre = delete.getWhere();
			setWhereParam(expre, sqlTable, pno);
		} else if (parser instanceof Select) {
			PlainSelect select = (PlainSelect) ((Select) parser).getSelectBody();
			Expression expre = select.getWhere();
			setWhereParam(expre, sqlTable, pno);
		}
	}

	private void setWhereParam(Expression expre, SQLTable sqlTable, ParamNo pno) throws DBException {
		while (andor(expre) || condition(expre)) {
			if (andor(expre)) {
				if (expre instanceof AndExpression) {
					AndExpression andExpression = (AndExpression) expre;
					setWhereParam(andExpression.getLeftExpression(), sqlTable, pno);
					setWhereParam(andExpression.getRightExpression(), sqlTable, pno);
				} else if (expre instanceof OrExpression) {
					OrExpression orExpression = (OrExpression) expre;
					setWhereParam(orExpression.getLeftExpression(), sqlTable, pno);
					setWhereParam(orExpression.getRightExpression(), sqlTable, pno);
				} else if (expre instanceof LikeExpression) {
					LikeExpression likeExpression = (LikeExpression) expre;
					setWhereParam(likeExpression.getLeftExpression(), sqlTable, pno);
					setWhereParam(likeExpression.getRightExpression(), sqlTable, pno);
				}
				break;// 这里要跳出来，不然内层递归跳不出来
			} else if (condition(expre)) {
				if (expre instanceof EqualsTo) {
					EqualsTo expression = (EqualsTo) expre;
					if (!isColumnExpression(expression.getLeftExpression(), expression.getRightExpression(), sqlTable, pno)) {
						setWhereParam(expression.getLeftExpression(), sqlTable, pno);
						setWhereParam(expression.getRightExpression(), sqlTable, pno);
					} else {
						break;
					}
				} else if (expre instanceof GreaterThan) {
					GreaterThan expression = (GreaterThan) expre;
					if (!isColumnExpression(expression.getLeftExpression(), expression.getRightExpression(), sqlTable, pno)) {
						setWhereParam(expression.getLeftExpression(), sqlTable, pno);
						setWhereParam(expression.getRightExpression(), sqlTable, pno);
					} else {
						break;
					}
				} else if (expre instanceof GreaterThanEquals) {
					GreaterThanEquals expression = (GreaterThanEquals) expre;
					if (!isColumnExpression(expression.getLeftExpression(), expression.getRightExpression(), sqlTable, pno)) {
						setWhereParam(expression.getLeftExpression(), sqlTable, pno);
						setWhereParam(expression.getRightExpression(), sqlTable, pno);
					} else {
						break;
					}
				} else if (expre instanceof MinorThan) {
					MinorThan expression = (MinorThan) expre;
					if (!isColumnExpression(expression.getLeftExpression(), expression.getRightExpression(), sqlTable, pno)) {
						setWhereParam(expression.getLeftExpression(), sqlTable, pno);
						setWhereParam(expression.getRightExpression(), sqlTable, pno);
					} else {
						break;
					}
				} else if (expre instanceof MinorThanEquals) {
					MinorThanEquals expression = (MinorThanEquals) expre;
					if (!isColumnExpression(expression.getLeftExpression(), expression.getRightExpression(), sqlTable, pno)) {
						setWhereParam(expression.getLeftExpression(), sqlTable, pno);
						setWhereParam(expression.getRightExpression(), sqlTable, pno);
					} else {
						break;
					}
				} else if (expre instanceof NotEqualsTo) {
					NotEqualsTo expression = (NotEqualsTo) expre;
					if (!isColumnExpression(expression.getLeftExpression(), expression.getRightExpression(), sqlTable, pno)) {
						setWhereParam(expression.getLeftExpression(), sqlTable, pno);
						setWhereParam(expression.getRightExpression(), sqlTable, pno);
					} else {
						break;
					}
				}
			} else {
				break;
			}
		}
	}

	private boolean andor(Expression expre) {
		return (expre instanceof AndExpression) || (expre instanceof OrExpression) || (expre instanceof LikeExpression);
	}

	private boolean condition(Expression expre) {
		return (expre instanceof EqualsTo) || (expre instanceof GreaterThan) || (expre instanceof GreaterThanEquals) || (expre instanceof MinorThan) || (expre instanceof MinorThanEquals)
				|| (expre instanceof NotEqualsTo);
	}

	private boolean isColumnExpression(Expression left, Expression right, SQLTable sqlTable, ParamNo pno) throws DBException {
		if (left instanceof Column) {
			if (jsJdbcParam(right)) {
				Column c = (Column) left;
				SQLColumn column = sqlTable.getSqlColumnByColumn(c.getColumnName());
				if (column == null) {
					throw new DBException(DBErrCode.ERR_WSTAT_COLUMN_EXPRESSION, "SqlColumn is null-" + c.getColumnName(), getSqlString());
				}
				setParam(pno.grow(), column);
				return true;
			}
			return false;
		} else
			return false;
	}

	private boolean jsJdbcParam(Expression expre) {
		return (expre instanceof JdbcParameter);
	}

	public void setParam(int i, SQLColumn sqlColumn) throws DBException {
		try {
			ConvertHandler handler = DruidMgr.getInstance().getHhandler(sqlColumn.getHandler());
			if (handler == null) {
				logger.error("field:" + sqlColumn.getFieldName() + " can not find handler : [" + sqlColumn.getHandler() + "]");
				throw new DBException(DBErrCode.ERR_WRESULT_SET, "field:" + sqlColumn.getFieldName() + " can not find handler : [" + sqlColumn.getHandler() + "]");
			}
			handler.convertPreparedStatement(this.ptmt, i, sqlColumn);
			params[i - 1] = sqlColumn.getValue();
		} catch (Exception e) {
			throw new DBException(DBErrCode.ERR_SET_PARAM, "setParam error:" + sqlColumn.toString(), getSqlString(), e);
		}
	}

	public String getSqlString() {
		return parser.toString();
	}

	public void close() {
		try {
			ptmt.close();
			parser = null;
		} catch (SQLException e) {
			logger.error("close", e);
			e.printStackTrace();
		}
	}

	// 自动增长的param参数下标
	class ParamNo {
		int paramNo;

		public ParamNo(int pno) {
			this.paramNo = pno;
		}

		public int grow() {
			return paramNo++;
		}
	}

}
