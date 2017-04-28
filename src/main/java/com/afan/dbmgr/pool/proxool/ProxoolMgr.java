/*package com.afan.dbmgr.pool.proxool;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.configuration.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.afan.dbmgr.DBException;
import com.afan.dbmgr.util.AutoSearchConfig;

*//**
 * proxool管理 1.ProxoolMgr.getInstance().init();初始化 2.DBConnMgr获取可用连接执行操作
 * 
 * @author cf
 * 
 *//*
public class ProxoolMgr {
	private static final Logger logger = LoggerFactory.getLogger(ProxoolMgr.class);

	private static ProxoolMgr instance = null;
	private boolean showsql = false;
	private static final String ProxoolProperties = "proxool.properties";// 配置文件
	private String defaultDbName = null;

	public static void main(String[] args) throws Exception {
		ProxoolMgr.getInstance().init("D:\\MyEclipse2015\\Workspaces\\MyEclipse2015\\afan.com\\src\\test\\resources\\proxool.properties");
		Connection conn = DriverManager.getConnection("proxool.tb_seller");
		try {
			ResultSet rs = conn.prepareStatement("select * from xyy_seller_base limit 1").executeQuery();
			if (rs.next())
				System.out.println(rs.getInt(1));
		} catch (SQLException e) {
			throw new DBException(0, "executeQuery", e);
		}
	}

	*//**
	 * 获取一个指定的可用连接
	 * 
	 * @param dbName
	 * @return
	 * @throws SQLException
	 *//*
	Connection getConnection(String dbName) throws SQLException {
		if (dbName != null)
			return DriverManager.getConnection("proxool." + dbName);
		else
			return DriverManager.getConnection("proxool." + defaultDbName);
	}

	public static ProxoolMgr getInstance() throws ProxoolException {
		if (instance == null) {
			synchronized (ProxoolMgr.class) {
				if (instance == null) {
					instance = new ProxoolMgr();
				}
			}
		}
		return instance;
	}

	public static void destory() {
		ProxoolFacade.shutdown();
	}

	// 受保护的初始化
	private ProxoolMgr() throws ProxoolException {
	}
	
	public static void initProxoolMgr() throws ProxoolException {
		ProxoolMgr.getInstance().init();
	}
	
	//读取默认的配置
	public void init() throws ProxoolException {
		initProxoolConfig();
	}

	//读取指定的配置
	public void init(String config) throws ProxoolException {
		initProxoolConfig(config);
	}

	// 加载默认路径下的配置文件
	private void initProxoolConfig() throws ProxoolException {
		Properties proxool = null;
		try {
			proxool = loadConfigFile();
			initProxoolConfig(proxool);
		} catch (Exception e) {
			logger.error("can not find proxool.properties", e);
		}
	}

	// 指定文件初始化配置
	private void initProxoolConfig(String config) throws ProxoolException {
		Properties proxool = null;
		try {
			proxool = new Properties();
			proxool.load(new FileInputStream(config));
			initProxoolConfig(proxool);
		} catch (Exception e) {
			logger.error("can not find proxool.properties", e);
		}
	}

	
	 * 初始化proxool.properties配置
	 * 
	 * 参数示意： alias链接别名（程序中需要使用的名称） driver-url驱动地址 driver-class驱动类 user用户名
	 * password密码
	 * house-keeping-sleep-time=60000//自动侦察各个连接状态的时间间隔(毫秒),侦察到空闲的连接就马上回收,超时的销毁
	 * 默认30秒) house-keeping-test-sql=select CURRENT_DATE
	 * maximum-connection-count最大连接数量，如果超过最大连接数量则会抛出异常。连接数设置过多，服务器CPU和内存性能消耗很大。
	 * minimum-connection-count最小连接数量，建议设置0以上，保证第一次连接时间
	 * maximum-connection-lifetime
	 * =18000000//指一个连接最大的存活时间（毫秒为单位），超过这个时间，连接会被杀死。默认值是4小时。
	 * prototype-count=2//连接池中可用的连接数量.如果现在设置为4个，但是现在已经有2个可以获得的连接，那么将会试图再创建2个连接。
	 * 但不能超过最大连接数。 simultaneous-build-throttle=10//可以（同时）建立的最大连接数默认值是
	 * maximum-active-time=60000//如果一个线程活动时间超过这个数值，线程会被杀死。默认是5分钟。
	 * recently-started-threshold=60000 overload-without-refusal-lifetime=50000
	 * verbose=true//执行日志记录 jdbc-0.proxool.trace=true fatal-sql-exception=Fatal
	 * error
	 
	private void initProxoolConfig(Properties proxool) throws ProxoolException {
		String[] alialist = proxool.getProperty("proxool.alialist").split(",");
		this.defaultDbName = proxool.getProperty("proxool.default");
		this.showsql = "true".equals(proxool.getProperty("proxool.showsql"));
		// this.usedb =
		// "true".equals(proxool.getProperty("proxool.usedb","false"));
		String defaultDriver = proxool.getProperty("jdbc.global.driver");
		String defaultName = proxool.getProperty("jdbc.global.username");
		String defaultPassword = proxool.getProperty("jdbc.global.password");
		String defaultMaxconn = proxool.getProperty("jdbc.global.maxconn");
		String defaultMinconn = proxool.getProperty("jdbc.global.minconn");
		String defaultActivetime = proxool.getProperty("jdbc.global.activetime");
		for (int i = 0; i < alialist.length; i++) {
			Properties pro = new Properties();
			String alia = alialist[i];
			pro.put("jdbc-" + i + ".proxool.alias", proxool.getProperty(alia + ".alias", alia));
			pro.put("jdbc-" + i + ".proxool.driver-url", proxool.getProperty(alia + ".driver.url"));
			pro.put("jdbc-" + i + ".proxool.driver-class", proxool.getProperty(alia + ".driver", defaultDriver));
			pro.put("jdbc-" + i + ".user", proxool.getProperty(alia + ".username", defaultName));
			pro.put("jdbc-" + i + ".password", proxool.getProperty(alia + ".password", defaultPassword));
			pro.put("jdbc-" + i + ".proxool.maximum-connection-count", proxool.getProperty(alia + ".maxconn", defaultMaxconn));
			pro.put("jdbc-" + i + ".proxool.minimum-connection-count", proxool.getProperty(alia + ".minconn", defaultMinconn));
			// pro.put("jdbc-" + i +".proxool.house-keeping-sleep-time", "30000");
			// pro.put("jdbc-" + i + ".proxool.house-keeping-test-sql", "select CURRENT_DATE");
			pro.put("jdbc-" + i + ".proxool.maximum-active-time", proxool.getProperty(alia + ".activetime", defaultActivetime));
			PropertyConfigurator.configure(pro);
		}
	}

	// 试图找到proxool.properties
	private Properties loadConfigFile() {
		Properties proxool = null;
		try {
			List<File> result = AutoSearchConfig.search(ProxoolProperties, true);
			File file = result.get(0);
			logger.debug("find proxool :" + file.getPath());
			proxool = new Properties();
			proxool.load(new FileInputStream(file));
			return proxool;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean isShowsql() {
		return showsql;
	}
}*/