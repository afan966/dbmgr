package com.afan.enity;

import java.io.Serializable;
import com.afan.dbmgr.DBTable;
import com.afan.dbmgr.DBColumn;
import java.util.Date;

@DBTable(db="sionou_uc",table="user_wechat_unionid", primaryColumns={"type", "userId"})
public class UserWechatUnionid implements Serializable{
	private static final long serialVersionUID = 1L;

	@DBColumn
	private long userId;
	@DBColumn
	private int type;//主体类型
	@DBColumn
	private String unionId;//unionID
	@DBColumn(handler="date")
	private Date createTime;//创建时间

	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getUnionId() {
		return unionId;
	}
	public void setUnionId(String unionId) {
		this.unionId = unionId;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}