package com.afan.dbmgr.pool.druid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.afan.dbmgr.DBException;
import com.afan.dbmgr.DBHandler;
import com.afan.dbmgr.config.DBErrCode;
import com.afan.dbmgr.handler.ConvertHandler;
import com.afan.dbmgr.handler.DefaultConvertHandler;
import com.afan.dbmgr.util.DESUtil;
import com.afan.dbmgr.util.StringUtil;
import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.util.StringUtils;

public class DruidMgr {
	private static final Logger logger = LoggerFactory.getLogger(DruidMgr.class);

	private static final Map<String, DruidDataSource> dataSources = new HashMap<String, DruidDataSource>();

	String alias;
	String driverClass;
	String url;
	String username;
	String password;

	int initialSize = 5;// 初始化大小
	int minIdle = 3;// 最小
	int maxActive = 10;// 最大

	// 慢sql统计
	int slowSqlMillis = 10 * 1000;
	boolean logSlowSql = true;

	// 获取连接等待超时的时间
	int maxWait = 60 * 1000;
	// 间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
	int timeBetweenEvictionRunsMillis = 60 * 1000;
	// 一个连接在池中最小生存的时间，单位是毫秒
	int minEvictableIdleTimeMillis = 5 * 60 * 1000;

	// 用来检测连接是否有效的sql如果为null,后面3个参数都不会起作用
	String validationQuery = "SELECT 'x'";
	// 建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
	boolean testWhileIdle = true;
	// 申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。
	boolean testOnBorrow = false;
	// 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
	boolean testOnReturn = false;

	// 是否缓存preparedStatement，也就是PSCache。PSCache对支持游标的数据库性能提升巨大，比如说oracle。在mysql下建议关闭。
	boolean poolPreparedStatements = false;
	int maxPoolPreparedStatementPerConnectionSize = 20;

	String filters = "log4j";

	// 合并多个DruidDataSource的监控数据
	boolean useGlobalDataSourceStat = true;

	String defaultAlia = "jdbc";

	private Map<String, ConvertHandler> handlers = new HashMap<String, ConvertHandler>();

	private DruidMgr() {
	}

	private static DruidMgr instance = null;

	public static DruidMgr getInstance() {
		if (instance == null) {
			synchronized (DruidMgr.class) {
				if (instance == null) {
					instance = new DruidMgr();
				}
			}
		}
		return instance;
	}

	@SuppressWarnings("unchecked")
	public void init(String config) {
		Properties prop = new Properties();
		try {
			if (!new File(config).exists()) {
				logger.error("can not find druid config : {}", config);
				return;
			}
			prop.load(new FileInputStream(config));
			init(prop);
			// 初始化handler
			handlers.put(defaultAlia, new DefaultConvertHandler());
			String jdbcTypeHandlers = prop.getProperty("jdbc.jdbcTypeHandlers");
			if (StringUtils.isEmpty(jdbcTypeHandlers)) {
				jdbcTypeHandlers = "com.afan.dbmgr.handler.DefaultConvertHandler,com.afan.dbmgr.handler.DateConvertHandler";
			}
			for (String handlerName : jdbcTypeHandlers.split(",")) {
				try {
					Class<ConvertHandler> handlerClazz = ((Class<ConvertHandler>) Class.forName(handlerName));
					DBHandler handler = handlerClazz.getAnnotation(DBHandler.class);
					if (handler == null) {
						logger.warn("jdbc type handler:[" + handlerName + "] need annotation:@DBHandler");
						continue;
					}
					handlers.put(handler.alia(), handlerClazz.newInstance());
					logger.debug("init jdbc type handler: [{},{}] success", handler.alia(), handlerName);
				} catch (Exception e) {
					logger.error("init jdbc type handler: [" + handlerName + "] error", e);
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void init(Properties prop) {
		try {
			if (!StringUtils.isEmpty(prop.getProperty("jdbc.slowSqlMillis"))) {
				slowSqlMillis = Integer.parseInt(prop.getProperty("jdbc.slowSqlMillis"));
			}
			if (!StringUtils.isEmpty(prop.getProperty("jdbc.logSlowSql"))) {
				logSlowSql = Boolean.parseBoolean(prop.getProperty("jdbc.logSlowSql"));
			}
			if (!StringUtils.isEmpty(prop.getProperty("jdbc.useGlobalDataSourceStat"))) {
				useGlobalDataSourceStat = Boolean.parseBoolean(prop.getProperty("jdbc.useGlobalDataSourceStat"));
			}
			alias = prop.getProperty("jdbc.alias");
			if (StringUtils.isEmpty(alias)) {
				logger.warn("jdbc.alias is empty");
			}

			String publicKey = prop.getProperty("jdbc.publicKey");

			List<Filter> proxyFilters = new ArrayList<Filter>();
			StatFilter stat = new StatFilter();
			stat.setSlowSqlMillis(slowSqlMillis);
			stat.setLogSlowSql(logSlowSql);
			if (useGlobalDataSourceStat) {
				stat.setMergeSql(true);
			}
			proxyFilters.add(stat);

			Slf4jLogFilter slf4j = new Slf4jLogFilter();
			slf4j.setStatementExecutableSqlLogEnable(true);
			proxyFilters.add(slf4j);

			if (StringUtils.isEmpty(alias)) {
				createDataSource(defaultAlia, proxyFilters, prop, publicKey);
			} else {
				String[] aliaList = alias.split(",");
				for (String alia : aliaList) {
					createDataSource(alia, proxyFilters, prop, publicKey);
				}
			}
		} catch (Exception e) {
			logger.error("init data source error", e);
		}
	}

	private void createDataSource(String alia, List<Filter> proxyFilters, Properties env, String publicKey) {
		try {
			String password = env.getProperty(alia + ".password");
			if (!StringUtil.isEmpty(publicKey)) {
				password = DESUtil.decrypt(password, publicKey);
			}

			Properties properties = new Properties();
			properties.setProperty("driverClassName", env.getProperty(alia + ".driver"));
			properties.setProperty("url", env.getProperty(alia + ".url"));
			properties.setProperty("username", env.getProperty(alia + ".username"));
			properties.setProperty("password", password);
			properties.setProperty("initialSize", env.getProperty(alia + ".initialSize", "1"));
			properties.setProperty("minIdle", env.getProperty(alia + ".minIdle", "1"));
			properties.setProperty("maxActive", env.getProperty(alia + ".maxActive", "10"));
			properties.setProperty("maxWait", env.getProperty(alia + ".maxWait", "10000"));
			properties.setProperty("timeBetweenEvictionRunsMillis", env.getProperty(alia + ".timeBetweenEvictionRunsMillis", timeBetweenEvictionRunsMillis + ""));
			properties.setProperty("minEvictableIdleTimeMillis", env.getProperty(alia + ".minEvictableIdleTimeMillis", minEvictableIdleTimeMillis + ""));
			properties.setProperty("validationQuery", env.getProperty(alia + ".validationQuery", validationQuery));
			properties.setProperty("testWhileIdle", env.getProperty(alia + ".testWhileIdle", testWhileIdle + ""));
			properties.setProperty("testOnBorrow", env.getProperty(alia + ".testOnBorrow", testOnBorrow + ""));
			properties.setProperty("testOnReturn", env.getProperty(alia + ".testOnReturn", testOnReturn + ""));
			properties.setProperty("poolPreparedStatements", env.getProperty(alia + ".poolPreparedStatements", poolPreparedStatements + ""));
			properties.setProperty("maxPoolPreparedStatementPerConnectionSize", env.getProperty(alia + ".maxPoolPreparedStatementPerConnectionSize", maxPoolPreparedStatementPerConnectionSize + ""));
			DruidDataSource dataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(properties);
			// filter和proxyFileter是重复的只要一个就好了
			// dataSource.setFilters(filters);
			dataSource.setProxyFilters(proxyFilters);
			dataSources.put(alia, dataSource);
			logger.debug("init [" + alia + "] data source success");
		} catch (Exception e) {
			logger.error("init [" + alia + "] data source error", e);
		}
	}

	public Connection getConnection(String alia) throws DBException {
		if (StringUtils.isEmpty(alia)) {
			alia = defaultAlia;
		}
		Connection connection = null;
		try {
			connection = dataSources.get(alia).getConnection();
			if (connection == null) {
				throw new DBException(DBErrCode.ERR_CONNECT_ALIA, "can not find connection alia:" + alia);
			}
		} catch (SQLException e) {
			logger.error("can not find connection alia:" + alia, e);
			throw new DBException(DBErrCode.ERR_CONNECT_WAIT, "can not find connection alia:" + alia, e);
		}
		return connection;
	}

	public ConvertHandler getHhandler(String name) throws DBException {
		if (StringUtils.isEmpty(name)) {
			name = defaultAlia;
		}
		ConvertHandler handler = handlers.get(name);
		if (handler == null) {
			throw new DBException(DBErrCode.ERR_MGR_NO_HANDLER, "can not find handler:" + name);
		}
		return handler;
	}

}
