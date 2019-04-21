package com.afan.enity;

import java.io.Serializable;
import com.afan.dbmgr.DBTable;
import com.afan.dbmgr.DBColumn;
import java.util.Date;

@DBTable(db="sionou_uc",table="user", primaryColumns={"userId"})
public class User implements Serializable{
	private static final long serialVersionUID = 1L;

	@DBColumn(autoIncrement=true)
	private long userId;//自增用户ID
	@DBColumn
	private String userCode;//用户码
	@DBColumn
	private String nick;//昵称
	@DBColumn
	private String pazzwd;//自有密码
	@DBColumn
	private String cellphone;//手机号码
	@DBColumn
	private int status;//状态
	@DBColumn(handler="date")
	private Date createTime;//创建时间

	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public String getUserCode() {
		return userCode;
	}
	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}
	public String getNick() {
		return nick;
	}
	public void setNick(String nick) {
		this.nick = nick;
	}
	public String getPazzwd() {
		return pazzwd;
	}
	public void setPazzwd(String pazzwd) {
		this.pazzwd = pazzwd;
	}
	public String getCellphone() {
		return cellphone;
	}
	public void setCellphone(String cellphone) {
		this.cellphone = cellphone;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}