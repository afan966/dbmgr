package com.afan.enity;

import java.io.Serializable;
import com.afan.dbmgr.DBTable;
import com.afan.dbmgr.DBColumn;
import java.util.Date;

@DBTable(db="sionou_uc",table="user_service", primaryColumns={"userId"})
public class UserService implements Serializable{
	private static final long serialVersionUID = 1L;

	@DBColumn
	private long userId;
	@DBColumn
	private int vipLevel;//VIP等级
	@DBColumn
	private String serviceCode;
	@DBColumn
	private String serviceName;//服务套餐名
	@DBColumn(handler="date")
	private Date renewTime;//续费时间
	@DBColumn(handler="date")
	private Date endTime;//过期时间
	@DBColumn(handler="date")
	private Date createTime;//创建时间

	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getVipLevel() {
		return vipLevel;
	}
	public void setVipLevel(int vipLevel) {
		this.vipLevel = vipLevel;
	}
	public String getServiceCode() {
		return serviceCode;
	}
	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public Date getRenewTime() {
		return renewTime;
	}
	public void setRenewTime(Date renewTime) {
		this.renewTime = renewTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}