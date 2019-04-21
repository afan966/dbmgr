package com.afan.enity;

import java.io.Serializable;
import com.afan.dbmgr.DBTable;
import com.afan.dbmgr.DBColumn;

@DBTable(db="sionou_uc",table="user_shop_info", primaryColumns={"shopId"})
public class UserShopInfo implements Serializable{
	private static final long serialVersionUID = 1L;

	@DBColumn(autoIncrement=true)
	private long shopId;
	@DBColumn
	private long userId;
	@DBColumn
	private String shopName;//店铺名
	@DBColumn
	private int shopType;//店铺类型
	@DBColumn
	private String shopCat;//店铺分类
	@DBColumn
	private String province;//省
	@DBColumn
	private String city;//市
	@DBColumn
	private String address;//地址
	@DBColumn
	private String location;//位置

	public long getShopId() {
		return shopId;
	}
	public void setShopId(long shopId) {
		this.shopId = shopId;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public String getShopName() {
		return shopName;
	}
	public void setShopName(String shopName) {
		this.shopName = shopName;
	}
	public int getShopType() {
		return shopType;
	}
	public void setShopType(int shopType) {
		this.shopType = shopType;
	}
	public String getShopCat() {
		return shopCat;
	}
	public void setShopCat(String shopCat) {
		this.shopCat = shopCat;
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
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
}