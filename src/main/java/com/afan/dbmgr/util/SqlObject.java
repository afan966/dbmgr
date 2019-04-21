package com.afan.dbmgr.util;

import java.util.List;

public class SqlObject {
    String sql;
    Object[] paramValues;

    public SqlObject(String sql, List<Object> params) {
        this.sql = sql;
        this.paramValues = params.toArray();
    }

    public int size() {
        if (this.paramValues != null) {
            return this.paramValues.length;
        }
        return 0;
    }

    public String getSql() {
        return this.sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Object[] getParamValues() {
        return this.paramValues;
    }

    public void setParamValues(Object[] paramValues) {
        this.paramValues = paramValues;
    }
}