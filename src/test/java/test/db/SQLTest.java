package test.db;

import java.util.List;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.util.JdbcConstants;

public class SQLTest {
	
	public static void main3(String[] args) {
		String sql = "update package set consigneeName=?,consigneeMobile=?,consigneePhone=?,consigneeProvince=?,consigneeCity=? where packageId = ? and createTime>? and createTime<? and createTime = ? and consigneeCity=? and type in (1,2)";
		List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, JdbcConstants.MYSQL);
		Incr i = new Incr(1);
		SQLStatement stmt = stmtList.get(0);
        if (stmt instanceof MySqlUpdateStatement) {
        	MySqlUpdateStatement update = (MySqlUpdateStatement) stmt;
        	SQLBinaryOpExpr whereExpr = (SQLBinaryOpExpr)update.getWhere();
        	setWhereParam(whereExpr, i);
        }
	}
	
	public static void setWhereParam(SQLBinaryOpExpr expr, Incr i) {
		SQLExpr left = expr.getLeft();
		SQLExpr right = expr.getRight();
		
		if (left instanceof SQLBinaryOpExpr) {
			SQLBinaryOpExpr opExpr = (SQLBinaryOpExpr) left;
			setWhereParam(opExpr, i);
		}else if(left instanceof SQLIdentifierExpr) {
			SQLIdentifierExpr leftExpr = (SQLIdentifierExpr) left;
			SQLVariantRefExpr rightExpr = (SQLVariantRefExpr) right;
			System.out.println(i.incr()+">>>"+leftExpr.getName()+"----"+rightExpr.getName());
			
		}
		
		if (right instanceof SQLBinaryOpExpr) {
			SQLBinaryOpExpr rightExpr = (SQLBinaryOpExpr) right;
			SQLIdentifierExpr leftEx = (SQLIdentifierExpr) rightExpr.getLeft();
			SQLVariantRefExpr rightEx = (SQLVariantRefExpr) rightExpr.getRight();
			System.out.println(i.incr()+">>>"+leftEx.getName()+"----"+rightEx.getName());
		}
	}
	

	
	public static void main(String[] args) {
//		String sql1 = "select * from package where packageId = ? and userId = ?";
//		String sql2 = "delete from package where packageId = ?";
//		String sql3 = "update package set consigneeName=?,consigneeMobile=?,consigneePhone=?,consigneeProvince=?,consigneeCity=? where packageId = ? and createTime>? and createTime<? and createTime = 122 and consigneeCity=? and type in (1,2)";
//		String sql4 = "insert into package(packageId,userId,buyerUserId,createUserId,status,createTime," +
//				"consigneeName,consigneeMobile,consigneePhone,consigneeProvince,consigneeCity,consigneeArea,consigneeTown,consigneeAddress,consigneeZip," +
//				"shippingName,shippingMobile,shippingPhone,shippingProvince,shippingCity,shippingArea,shippingTown,shippingAddress,shippingZip," +
//				"packageFlag,packageCategory,packageWeight,packageValume,packageNote,packagePostfee,packageAmts," +
//				"cpCode,netpointName,wlbType,wlbStatus,wlbCode,wlbCodeTime,wlbCodeErrorInfo,logisticsStatus,logisticsTime,expressType,printType,printTime,printCode,batchPackageId,batchNote,editCount,note) " +
//				"values(?,?,?,?,0,?," + "?,?,?,?,?,?,?,?,?," + "?,?,?,?,?,?,?,?,?," + "?,?,?,?,?,?,?," + "'','',0,0,'',0,'ada',0,0,0,0,0,?,?,?,0.0,?) " +
//				"on duplicate key update consigneeName=?,consigneeMobile=?,consigneePhone=?,consigneeProvince=?,consigneeCity=?,consigneeArea=?,consigneeTown=?,consigneeAddress=?,consigneeZip=?," +
//				"shippingName=?,shippingMobile=?,shippingPhone=?,shippingProvince=?,shippingCity=?,shippingArea=?,shippingTown=?,shippingAddress=?,shippingZip=?," +
//				"status=?,packageFlag=?,packageCategory=?,packageWeight=?,packageValume=?,packageNote=?,packagePostfee=?,packageAmts=?," +
//				"cpCode=?,netpointName=?,wlbType=?,wlbStatus=?,wlbCode=?,wlbCodeTime=?,wlbCodeErrorInfo=?,logisticsStatus=?,logisticsTime=?,expressType=?,printType=?,printTime=?,printCode=?,batchPackageId=?,batchNote=?,editCount=?,note=?";
//		 List<SQLStatement> stmtList = SQLUtils.parseStatements(sql1, JdbcConstants.MYSQL);
//		
//		//解析出的独立语句的个数
//	        System.out.println("size is:" + stmtList.size());
//	        for (int i = 0; i < stmtList.size(); i++) {
//	 
//	            SQLStatement stmt = stmtList.get(i);
//	            
//	            if (stmt instanceof SQLSelectStatement) {
//	            	SQLSelectStatement new_name = (SQLSelectStatement) stmt;
//	            	((MySqlSelectQueryBlock)new_name.getSelect().getQuery()).getWhere();
//				}
//	            
//	            if (stmt instanceof MySqlUpdateStatement) {
//	            	MySqlUpdateStatement new_name = (MySqlUpdateStatement) stmt;
//	            	new_name.getItems();
//	            	new_name.getWhere();
//	            	new_name.getLimit();
//	            }
//	            
//	            if (stmt instanceof MySqlInsertStatement) {
//					MySqlInsertStatement new_name = (MySqlInsertStatement) stmt;
//					System.out.println(new_name);
//					((SQLIdentifierExpr)new_name.getColumns().get(0)).getName();
//					((SQLBinaryOpExpr)new_name.getDuplicateKeyUpdate().get(0)).getLeft();
//					new_name.getTableName();
//					((SQLNumberExpr)(new_name.getValues().getValues().get(46))).getNumber().doubleValue();
//					new_name.getValuesList().get(0).getValues().get(4).toString();
//				}
//	            
//	            PGSchemaStatVisitor visitor = new PGSchemaStatVisitor();
//	            stmt.accept(visitor);
//	            Map<String, String> aliasmap = visitor.getAliasMap();
//	            for (Iterator iterator = aliasmap.keySet().iterator(); iterator.hasNext();) {
//	                String key = iterator.next().toString();
//	                System.out.println("[ALIAS]" + key + " - " + aliasmap.get(key));
//	            }
//	            Set<Column> groupby_col = visitor.getGroupByColumns();
//	            //
//	            for (Iterator iterator = groupby_col.iterator(); iterator.hasNext();) {
//	                Column column = (Column) iterator.next();
//	                System.out.println("[GROUP]" + column.toString());
//	            }
//	            //获取表名称
//	            System.out.println("table names:");
//	            Map<Name, TableStat> tabmap = visitor.getTables();
//	            for (Iterator iterator = tabmap.keySet().iterator(); iterator.hasNext();) {
//	                Name name = (Name) iterator.next();
//	                System.out.println(name.toString() + " - " + tabmap.get(name).toString());
//	            }
//	            //System.out.println("Tables : " + visitor.getCurrentTable());
//	            //获取操作方法名称,依赖于表名称
//	            System.out.println("Manipulation : " + visitor.getTables());
//	            //获取字段名称
//	            System.out.println("fields : " + visitor.getColumns());
//	        }
		
	}
	
}
class Incr{
	int value;
	public Incr(int i) {
		value = i;
	}
	public int incr() {
		return value++;
	}
}