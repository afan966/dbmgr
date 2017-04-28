package com.afan.dbmgr;

public class DBException extends Exception {

	private static final long serialVersionUID = 1L;

	private int code;
	private String message;

	private String sql;

	public DBException(int code) {
		super("DB(" + code + ")");
		this.code = code;
	}

	public DBException(int code, String message) {
		super("DB(" + code + ") " + message);
		this.code = code;
		this.message = message;
	}
	
	public DBException(int code, String message, String sql) {
		super("DB(" + code + ") " + message);
		this.code = code;
		this.message = message;
		this.sql = sql;
	}

	public DBException(int code, String message, Throwable t) {
		super("DB(" + code + ") " + message, t);
		this.code = code;
		this.message = message;
	}

	public DBException(int code, String message, String sql, Throwable t) {
		super("DB(" + code + ") " + message, t);
		this.code = code;
		this.message = message;
		this.sql = sql;
	}
	
	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public String getSql() {
		return this.sql;
	}
}
