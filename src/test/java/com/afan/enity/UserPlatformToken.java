package com.afan.enity;

import java.io.Serializable;
import com.afan.dbmgr.DBTable;
import com.afan.dbmgr.DBColumn;
import java.util.Date;

@DBTable(db="sionou_uc",table="user_platform_token", primaryColumns={"userId", "platform"})
public class UserPlatformToken implements Serializable{
	private static final long serialVersionUID = 1L;

	@DBColumn
	private long userId;
	@DBColumn
	private int platform;//平台类型
	@DBColumn
	private String platformToken;//平台token
	@DBColumn
	private String refreshToken;//刷新token
	@DBColumn(handler="date")
	private Date expireTime;//过期时间
	@DBColumn(handler="date")
	private Date createTime;//创建时间

	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getPlatform() {
		return platform;
	}
	public void setPlatform(int platform) {
		this.platform = platform;
	}
	public String getPlatformToken() {
		return platformToken;
	}
	public void setPlatformToken(String platformToken) {
		this.platformToken = platformToken;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	public Date getExpireTime() {
		return expireTime;
	}
	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}