package com.afan.dbmgr.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mysql数据库表生成po对象
 * 
 * @author cf
 * 
 */
public class GenertorMysql {

	private static String driver = "com.mysql.jdbc.Driver";
	private static String connUrl = "jdbc:mysql://localhost:3306/du_user";
	private static String user = "root";
	private static String pass = "chenfan";
	private static String targetPackage = "com.entity.base";
	private static String targetProjectSrc = "E:\\Workspaces\\Eclipse\\afan.ducrm.api\\src\\main\\java";
	private static Set<String> includeTable = null;

	public static void main(String[] args) {
//		connUrl = "jdbc:mysql://localhost:3306/best_cust";
//		targetPackage = "com.baishi";
//		targetProjectSrc = "E:\\Workspaces\\MyEclipse11\\tbk\\src\\main\\java";
//		includeTable = new HashSet<String>();
//		includeTable.add("customer");
//		//include.add("jijinxinxi");
//		//include.add("jijinliebiao");
		
//		connUrl = "jdbc:mysql://localhost:3306/tbk";
//		targetPackage = "com.tbk";
//		targetProjectSrc = "E:\\Workspaces\\MyEclipse11\\tbk\\src\\main\\java";
//		includeTable = new HashSet<String>();
//		includeTable.add("wx_wdoya_item_info");
//		create(connUrl, targetPackage, targetProjectSrc, includeTable);
		
//		connUrl = "jdbc:mysql://localhost:3306/account";
//		targetPackage = "test.db.entity";
//		targetProjectSrc = "E:\\Workspaces\\MyEclipse11\\afan.dbmgr\\src\\test\\java";
//		includeTable = new HashSet<String>();
//		includeTable.add("seller");
//		create(connUrl, targetPackage, targetProjectSrc, includeTable);
		
		connUrl = "jdbc:mysql://localhost:3306/province_31";
		targetPackage = "com.buyer.province";
		targetProjectSrc = "E:\\Workspaces\\MyEclipse11\\tbk\\src\\main\\java";
		includeTable = new HashSet<String>();
		includeTable.add("buyer_stat310101");
		create(connUrl, targetPackage, targetProjectSrc, includeTable);
	}
	
	public static void create(String conn, String pakage, String resourceDir, Set<String> include){
		connUrl = conn;
		targetPackage = pakage;
		targetProjectSrc = resourceDir;
		includeTable = include;
		connTables(includeTable);
	}

	public static void connTables(Set<String> include) {
		try {
			Class.forName(driver);
			Connection conn = DriverManager.getConnection(connUrl, user, pass);
			DatabaseMetaData dbMetaData = conn.getMetaData();
			// 返回一个String类对象，代表数据库的URL
			System.out.println("URL:" + dbMetaData.getURL() + ";");
			// 返回连接当前数据库管理系统的用户名。
			System.out.println("UserName:" + dbMetaData.getUserName() + ";");
			// 返回一个boolean值，指示数据库是否只允许读操作。
			System.out.println("isReadOnly:" + dbMetaData.isReadOnly() + ";");
			// 返回数据库的产品名称。
			System.out.println("DatabaseProductName:" + dbMetaData.getDatabaseProductName() + ";");
			// 返回数据库的版本号。
			System.out.println("DatabaseProductVersion:" + dbMetaData.getDatabaseProductVersion() + ";");
			// 返回驱动驱动程序的名称。
			System.out.println("DriverName:" + dbMetaData.getDriverName() + ";");
			// 返回驱动程序的版本号。
			System.out.println("DriverVersion:" + dbMetaData.getDriverVersion());

			ResultSet rs = dbMetaData.getTables(null, null, null, null);
			Map<String, List<String[]>> tableColumnsMap = new HashMap<String, List<String[]>>();
			Map<String, Set<String>> tablePksMap = new HashMap<String, Set<String>>();
			int i = 1;
			while (rs.next()) {
				String db = rs.getString(1);
				if (i == 1) {
					System.out.println("|库名：" + rs.getString(1));
					System.out.println("+====================+");
				}
				String table = rs.getString("TABLE_NAME");
				System.out.println("|表" + (i++) + ":" + table);
				// 主键
				ResultSet pk = dbMetaData.getPrimaryKeys("", "", table);
				Set<String> pkset = new HashSet<String>();
				while (pk.next()) {
					pkset.add(pk.getObject(4).toString());
					// System.out.println("PKTABLE_CAT:"+pk.getObject(1));
					// System.out.println("PKTABLE_SCHEM:"+pk.getObject(2));
					// System.out.println("PKTABLE_NAME:"+pk.getObject(3));
					// System.out.println("COLUMN_NAME:"+pk.getObject(4));
					// System.out.println("KEY_SEQ:"+pk.getObject(5));
					// System.out.println("PK_NAME:"+pk.getObject(6));
					System.out.print(pk.getObject(4) + "\t");
				}
				String dt = db + "." + table;
				System.out.println();
				tablePksMap.put(dt, pkset);
				System.out.println("+------------------+");
				if (include != null && !include.contains(table)) {
					System.out.println("|表" + (i++) + ":" + dt + " 忽略.");
					continue;
				}

				ResultSet rsc = dbMetaData.getColumns(null, null, table, null);
				List<String[]> columns = new ArrayList<String[]>();
				while (rsc.next()) {
					String[] o = { rsc.getString("COLUMN_NAME"), rsc.getString("DATA_TYPE"), rsc.getString("REMARKS"), rsc.getString("IS_AUTOINCREMENT") };
					columns.add(o);

					// System.out.println(rsc.getString("COLUMN_NAME")+"\t"+
					// rsc.getString("DATA_TYPE")+"\t"+
					// rsc.getString("TYPE_NAME")+"\t"+
					// rsc.getString("SQL_DATA_TYPE")+"\t"+
					// rsc.getString("IS_NULLABLE")+"\t"+
					// rsc.getString("REMARKS"));
				}

				tableColumnsMap.put(dt, columns);
			}
			// 关闭连接
			conn.close();

			createClass(tableColumnsMap, tablePksMap);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void createClass(Map<String, List<String[]>> tables, Map<String, Set<String>> tablePksMap) {
		for (String dt : tables.keySet()) {
			try {
				String[] dts = dt.split("\\.");
				String db = dts[0];
				String table = dts[1];
				// 创建文件
				String d = genDir();
				String c = genClassName(table);
				File file = new File(d + "\\" + c + ".java");
				file.createNewFile();

				StringBuffer con = new StringBuffer();
				StringBuffer clazz = new StringBuffer();
				StringBuffer body = new StringBuffer();
				StringBuffer getset = new StringBuffer();
				// 加心内容
				con.append("package " + targetPackage + ";\r\n");
				con.append("\r\n");
				con.append("import java.io.Serializable;\r\n");
				con.append("import com.afan.dbmgr.DBTable;\r\n");
				con.append("import com.afan.dbmgr.DBColumn;\r\n");
				String pks = "";
				if (tablePksMap.get(dt) != null && tablePksMap.get(dt).size() > 0) {
					pks = ", primaryClumns={";
					String pk = null;
					for (String p : tablePksMap.get(dt)) {
						if (pk == null)
							pk = "\"" + p + "\"";
						else
							pk = pk + ", \"" + p + "\"";
					}
					pks = pks + pk + "}";
				}
				clazz.append("@DBTable(db=\"" + db + "\",table=\"" + table + "\"" + pks + ")\r\n");
				clazz.append("public class " + c + " implements Serializable{\r\n");
				clazz.append("\t" + "private static final long serialVersionUID = 1L;\r\n\r\n");

				boolean useDate = false;

				for (String[] s : tables.get(dt)) {
					// -5 long 12 String 4 int 8 double -6 int
					String type = "long";
					int typeCode = Integer.parseInt(s[1]);
					if (Types.BIGINT == typeCode) {
						type = "long";
					} else if (Types.VARCHAR == typeCode 
							|| Types.CHAR == typeCode
							|| Types.LONGVARCHAR == typeCode) {
						type = "String";
					} else if (Types.INTEGER == typeCode 
							|| Types.TINYINT == typeCode) {
						type = "int";
					} else if (Types.DOUBLE == typeCode) {
						type = "double";
					} else if (Types.BIT == typeCode) {
						type = "boolean";
					} else if (Types.DATE == typeCode 
							|| Types.TIME == typeCode
							|| Types.TIMESTAMP == typeCode) {
						type = "Date";
						useDate = true;
					} else {
						System.out.println("error find sql column:" + s[0] + "  type:" + s[1]);
					}
					String colnum = s[0];
					String col = genFieldName(colnum);
					String remark = "";
					if (s[2] != null && s[2].length() > 0)
						remark = "//" + s[2];
					boolean autoIncr = "YES".equals(s[3]);
					
					if (autoIncr) {
						if (col.equals(colnum)) {
							body.append("\t" + "@DBColumn(autoIncrement=true)\r\n");
						} else {
							body.append("\t" + "@DBColumn(column=\"" + colnum + "\",autoIncrement=true)\r\n");
						}
					} else {
						String handler = "";
						if("Date".equals(type)){
							handler = "handler=\"date\"";
						}
						if (col.equals(colnum)) {
							if(handler.length()>0){
								body.append("\t" + "@DBColumn("+handler+")\r\n");
							}else{
								body.append("\t" + "@DBColumn\r\n");
							}
						} else {
							if(handler.length()>0){
								body.append("\t" + "@DBColumn(column=\"" + colnum + "\" "+handler+")\r\n");
							}else{
								body.append("\t" + "@DBColumn(column=\"" + colnum + "\")\r\n");
							}
						}
					}
					body.append("\t" + "private " + type + " " + col + ";" + remark + "\r\n");

					getset.append("\t" + "public " + type + " get" + genClassName(colnum) + "() {\r\n");
					getset.append("\t\t" + "return " + col + ";\r\n");
					getset.append("\t}\r\n");
					getset.append("\t" + "public void set" + genClassName(colnum) + "(" + type + " " + col + ") {\r\n");
					getset.append("\t\t" + "this." + col + " = " + col + ";\r\n");
					getset.append("\t}\r\n");
				}
				if (useDate) {
					con.append("import java.util.Date;\r\n");
				}
				con.append("\r\n");
				con.append(clazz);
				con.append(body);
				con.append("\r\n");
				con.append(getset);

				con.append("}");
				// System.out.println(con.toString());

				FileWriter fw = new FileWriter(file);
				fw.write(con.toString());
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private static String genDir() {
		String pack = targetPackage.replace(".", "\\");
		String dir = targetProjectSrc + "\\" + pack;
		File file = new File(dir);
		if (!file.isDirectory()) {
			file.mkdirs();
		}
		return dir;
	}

	private static String genClassName(String table) {
		return genCamelName(table, true);
	}

	private static String genFieldName(String field) {
		return genCamelName(field, false);
	}

	private static String genCamelName(String name, boolean isClazz) {
		char[] c = name.toCharArray();
		StringBuffer sb = new StringBuffer();
		if (isClazz) {
			sb.append(Character.toUpperCase(c[0]));
		} else {
			sb.append(Character.toLowerCase(c[0]));
		}
		boolean useUpper = false;
		for (int i = 1; i < c.length; i++) {
			if ('_' == c[i]) {
				useUpper = true;
				continue;
			}
			if (useUpper) {
				sb.append(Character.toUpperCase(c[i]));
				useUpper = false;
			} else {
				sb.append(c[i]);
				useUpper = false;
			}
		}
		return sb.toString();
	}

}
