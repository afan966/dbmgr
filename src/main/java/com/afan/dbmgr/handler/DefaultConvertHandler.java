package com.afan.dbmgr.handler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.afan.dbmgr.DBException;
import com.afan.dbmgr.DBHandler;
import com.afan.dbmgr.config.DBErrCode;
import com.afan.dbmgr.config.SQLColumn;

@DBHandler
public class DefaultConvertHandler implements ConvertHandler {

	@Override
	public void convertPreparedStatement(PreparedStatement ptmt, int i, SQLColumn sqlColumn) throws DBException {
		try {
			JDBCType type = JDBCType.getEnum(sqlColumn.getJavaType());
			switch (type) {
			case INT:
				ptmt.setInt(i, sqlColumn.getInt(0));
				break;
			case BIGINT:
				ptmt.setLong(i, sqlColumn.getLong(0));
				break;
			case BIT:
				ptmt.setBoolean(1, sqlColumn.getBoolean(false));
				break;
			case DOUBLE:
				ptmt.setDouble(i, sqlColumn.getDouble(0));
				break;
			case FLOAT:
				ptmt.setFloat(i, sqlColumn.getFloat(0));
				break;
			case VARCHAR:
				ptmt.setString(i, sqlColumn.getStringValue());
				break;
			case DATE:
				ptmt.setDate(i, (java.sql.Date) sqlColumn.getValue());
				break;
			case TIME:
				ptmt.setTime(i, (java.sql.Time) sqlColumn.getValue());
				break;
			case DATETIME:
				ptmt.setTimestamp(i, (java.sql.Timestamp) sqlColumn.getValue());
				break;
			default:
				break;
			}
		} catch (SQLException e) {
			throw new DBException(DBErrCode.ERR_WSTAT_SET_PARAM, e.getMessage(), e);
		}
	}

	@Override
	public Object convertResultSet(ResultSet rs, SQLColumn sqlColumn) throws DBException {
		try {
			JDBCType type = JDBCType.getEnumJdbc(sqlColumn.getJdbcJavaType());
			switch (type) {
			case INT:
				return rs.getInt(sqlColumn.getColumnName());
			case BIGINT:
				return rs.getLong(sqlColumn.getColumnName());
			case BIT:
				return rs.getBoolean(sqlColumn.getColumnName());
			case DOUBLE:
				return rs.getDouble(sqlColumn.getColumnName());
			case FLOAT:
				return rs.getFloat(sqlColumn.getColumnName());
			case VARCHAR:
				return rs.getString(sqlColumn.getColumnName());
			case DATE:
				return rs.getDate(sqlColumn.getColumnName());
			case TIME:
				return rs.getTime(sqlColumn.getColumnName());
			case DATETIME:
				return rs.getTimestamp(sqlColumn.getColumnName());
			default:
				break;
			}
		} catch (SQLException e) {
			throw new DBException(DBErrCode.ERR_WRESULT_SET, e.getMessage(), e);
		}
		return null;
	}

}
