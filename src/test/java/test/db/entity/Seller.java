package test.db.entity;

import java.io.Serializable;
import com.afan.dbmgr.DBTable;
import com.afan.dbmgr.DBColumn;

@DBTable(db="account",table="seller", primaryClumns={"sellerId"})
public class Seller implements Serializable{
	private static final long serialVersionUID = 1L;

	@DBColumn
	private long sellerId;
	@DBColumn
	private String sellerNick;
	@DBColumn
	private long shopId;
	@DBColumn
	private String shopName;
	@DBColumn
	private String shopType;
	@DBColumn
	private int shopLevel;
	@DBColumn
	private int shopCid;
	@DBColumn
	private String shopBusiness;
	@DBColumn
	private String name;
	@DBColumn
	private String mobile;
	@DBColumn
	private String phone;
	@DBColumn
	private String alipayNo;
	@DBColumn
	private String email;
	@DBColumn
	private String address;
	@DBColumn
	private int src;

	public long getSellerId() {
		return sellerId;
	}
	public void setSellerId(long sellerId) {
		this.sellerId = sellerId;
	}
	public String getSellerNick() {
		return sellerNick;
	}
	public void setSellerNick(String sellerNick) {
		this.sellerNick = sellerNick;
	}
	public long getShopId() {
		return shopId;
	}
	public void setShopId(long shopId) {
		this.shopId = shopId;
	}
	public String getShopName() {
		return shopName;
	}
	public void setShopName(String shopName) {
		this.shopName = shopName;
	}
	public String getShopType() {
		return shopType;
	}
	public void setShopType(String shopType) {
		this.shopType = shopType;
	}
	public int getShopLevel() {
		return shopLevel;
	}
	public void setShopLevel(int shopLevel) {
		this.shopLevel = shopLevel;
	}
	public int getShopCid() {
		return shopCid;
	}
	public void setShopCid(int shopCid) {
		this.shopCid = shopCid;
	}
	public String getShopBusiness() {
		return shopBusiness;
	}
	public void setShopBusiness(String shopBusiness) {
		this.shopBusiness = shopBusiness;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getAlipayNo() {
		return alipayNo;
	}
	public void setAlipayNo(String alipayNo) {
		this.alipayNo = alipayNo;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public int getSrc() {
		return src;
	}
	public void setSrc(int src) {
		this.src = src;
	}
}