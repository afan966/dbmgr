package com.afan.enity;

import java.io.Serializable;
import com.afan.dbmgr.DBTable;
import com.afan.dbmgr.DBColumn;
import java.util.Date;

@DBTable(db="sionou_uc",table="user_relevance", primaryColumns={"userId"})
public class UserRelevance implements Serializable{
	private static final long serialVersionUID = 1L;

	@DBColumn
	private long userId;
	@DBColumn
	private long relevanceUserId;
	@DBColumn
	private int status;
	@DBColumn(handler="date")
	private Date createTime;

	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public long getRelevanceUserId() {
		return relevanceUserId;
	}
	public void setRelevanceUserId(long relevanceUserId) {
		this.relevanceUserId = relevanceUserId;
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