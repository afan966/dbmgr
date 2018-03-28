package com.afan.about;


public class About {
	
	/**
	 * 基于proxool数据库连接池JDBC管理器（废弃，不建议使用）
	 * 基于druid数据库连接池JDBC管理器
	 * 基于jsqlparsersql自动处理（废弃，不建议使用）
	 */

	/**
	 * ProxoolMgr proxool链接管理器
	 * DruidMgr druid链接管理器
	 * 
	 * DBConnMgr 抽象的链接管理器
	 * DefaultDBConnMgr 默认的jdbc链接,优化了prepareStatement参数设置方法
	 * WrapperDBConnMgr bean风格的jdbc链接,使用StatementWrapper自动解析bean设置prepareStatement参数
	 * StatementWrapper 封装prepareStatement,根据STable定义设置解析参数
	 * ResultSetWrapper 封装resultSet,根据STable定义自动定义bean返回值
	 */
	
	/**
	 * 初始化
	 * ProxoolMgr.initProxoolMgr()默认加载目录下面的proxool.properties
	 * ProxoolMgr.getInstance().init(proxool.properties)加载指定的配置文件
	 * 
	 * DruidMgr.getInstance().init(druid.properties);
	 * 使用流程：
	 * 1.DBConnMgr conn = new DefaultDBConnMgr()
	 * 2.conn.prepareStatement()
	 * 3.conn.executeUpdate() / new ResultSetWrapper().query()
	 * 
	 * 1.DBConnMgr conn = new WrapperDBConnMgr()
	 * 2.conn.prepareStatement()
	 * 3.conn.executeUpdate() / new ResultSetWrapper().query()
	 * 
	 * 批量操作
	 * conn.prepareStatement()
	 * conn.setParam() / addBatch()
	 * conn.executeBatch()
	 * 
	 * 彩蛋：
	 * conn.insertAndReturnLastId()Mysql的LAST_INSERT_ID()
	 * conn.existQuery() 查询记录是否存在
	 * 
	 * 直接获取对象
	 * ResultSetWrapper().query()
	 * ResultSetWrapper().queryList()
	 * 
	 */
}
