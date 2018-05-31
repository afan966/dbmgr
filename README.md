# dbmgr
---------------------
JDBC封装，简化JDBC代码，不用编写大量繁琐的get/set操作，自动填充标准的@DBTable对象
简单的sql自动生成
基于druid链接池
---------------------
基础组件：
@DBColumn 解数DB列
@DBTable 解DB表
@DBHandler 解类型转换器

SQLColumn DB列
SQLTable DB表
TableSchema DB表结构
DBMgrCache 缓存DB数据接口

DateConvertHandler 日期类型转换器（sql.Data 和 util.Data 互相转换）
DefaultConvertHandler 默认数据类型转换器
---------------------

核心组件：
DruidMgr 链接池管理器，初始化druid.properties文件
DBConnMgr 数据库链接管理基类
DBConnect JDBC接口
DefaultConnect 未封装的JDBC实现，简化set参数流程
AfanConnect 封装JDBC实现,对象无需写SQL，直接使用@DBTable对象自动转化成可执行的SQL语句

StatementWrapper 封装PreparedStatement解析器
ResultSetWrapper 封装ResultSet查询直接返回@DBTable对象

GenertorMysql根据Mysql自动生成标准的@DBTable对象
---------------------

如何使用：
初始化
DruidMgr.getInstance().init("druid.properties");

DefaultConnect用法
	DBConnect con = new DefaultConnect(dbName);
	con.prepareStatement("insert into xxx(c1,c2,c3) values(?,?,?)", new Object[]{'1','2','3'});
	con.prepareStatement("update xxx set name=? where id=?", "haha", 1);
	con.executeUpdate();
	//批量用法
	con.addBatch(new Object[]{'1','2','3'});
	con.addBatch(new Object[]{'1','2','3'});
	...
	con.executeBatch();
	
	con.prepareStatement("select * from xxx where id = ? and name = ?", 1, "haha");
	ResultSet rs = con.executeQuery();
	配合ResultSetWrapper用法
	ResultSetWrapper<Xxx> rs = new ResultSetWrapper<Xxx>(con, Xxx.class);
	rs.query();//返回单个@DBTable对象
	rs.queryList();//返回List@DBTable对象
	rs.getRecod();//返回记录数

AfanConnect用法
	DBConnect con = new AfanConnect(dbName);
	con.prepareStatement("insert into xxx(c1,c2,c3) values(?,?,?)", Xxx);
	con.prepareStatement("update xxx set name=? where id=?", Xxx);
	con.executeUpdate();
	//进阶用法,快捷执行
	con.insert(Xxx);
	con.insertOrUpdate(Xxx);//mysql on duplicate key update
	con.insertReturnAutoId(Xxx);//mysql select LAST_INSERT_ID()
	//主键操作
	con.update(Xxx);
	con.delete(Xxx);
	con.query(Xxx);
	//批量操作
	insertBatch(List<Xxx>);
	updateBatch(List<Xxx>);
	deleteBatch(List<Xxx>);
