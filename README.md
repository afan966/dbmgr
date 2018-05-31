# dbmgr
dbmgr
---------------------
JDBC封装，简化JDBC代码，不用编写简单的sql和大量繁琐的get/set操作
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
