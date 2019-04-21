package com.afan.enity;

import java.io.Serializable;
import com.afan.dbmgr.DBTable;
import com.afan.dbmgr.DBColumn;
import java.util.Date;

@DBTable(db="sionou_uc",table="user_info", primaryColumns={"userId"})
public class UserInfo implements Serializable{
	private static final long serialVersionUID = 1L;

	@DBColumn
	private long userId;
	@DBColumn
	private String name;//姓名
	@DBColumn
	private int platform;//平台
	@DBColumn
	private String province;//省
	@DBColumn
	private String city;//市
	@DBColumn
	private String address;//详细地址
	@DBColumn
	private String avatar;//头像
	@DBColumn
	private String extCode;//推广码
	@DBColumn
	private String token;//token
	@DBColumn(handler="date")
	private Date expireTime;//授权过期时间
	@DBColumn(handler="date")
	private Date createTime;//创建时间

	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPlatform() {
		return platform;
	}
	public void setPlatform(int platform) {
		this.platform = platform;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	public String getExtCode() {
		return extCode;
	}
	public void setExtCode(String extCode) {
		this.extCode = extCode;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
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