package com.afan.dbmgr.handler;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import com.afan.dbmgr.DBException;
import com.afan.dbmgr.DBHandler;
import com.afan.dbmgr.config.DBErrCode;
import com.afan.dbmgr.config.SQLColumn;
import com.afan.dbmgr.handler.ConvertHandler;

/**
 * 日期类型转换器（sql.Data 和 util.Data 互相转换）
 * @author afan
 *
 */
@DBHandler(alia = "date")
public class DateConvertHandler implements ConvertHandler {

	@Override
	public void convertPreparedStatement(PreparedStatement ptmt, int i, SQLColumn sqlColumn) throws DBException {
		try {
			if (sqlColumn.getValue() instanceof java.util.Date) {
				java.util.Date date = (java.util.Date) sqlColumn.getValue();
				ptmt.setTimestamp(i, new Timestamp(date.getTime()));
			} else {
				throw new DBException(DBErrCode.ERR_WSTAT_SET_PARAM, "field:" + sqlColumn.getFieldName() + " can not convert to java.util.Date");
			}

		} catch (SQLException e) {
			throw new DBException(DBErrCode.ERR_WSTAT_SET_PARAM, e.getMessage(), e);
		}
	}

	@Override
	public Object convertResultSet(ResultSet rs, SQLColumn sqlColumn) throws DBException {
		try {
			Timestamp timestamp = rs.getTimestamp(sqlColumn.getColumnName());
			if (timestamp != null) {
				return new Date(timestamp.getTime());
			}
		} catch (SQLException e) {
			throw new DBException(DBErrCode.ERR_WRESULT_SET, e.getMessage(), e);
		}
		return null;
	}

}
