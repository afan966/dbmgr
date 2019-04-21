package test.db;

import com.afan.dbmgr.pool.DefaultConnect;
import com.afan.enity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.afan.dbmgr.DBException;
import com.afan.dbmgr.pool.AfanConnect;
import com.afan.dbmgr.pool.DBConnect;
import com.afan.dbmgr.pool.druid.DruidMgr;
import com.afan.dbmgr.pool.wrap.ResultSetWrapper;
import com.afan.dbmgr.util.LogBackConfigLoader;

import java.util.Date;

public class DatabaseTest {
	private static final Logger logger = LoggerFactory.getLogger(DatabaseTest.class);

	public static void main(String[] args) {
		LogBackConfigLoader.load("D:\\Workspaces\\Idea2018\\dbmgr\\src\\main\\resources\\logback.xml");
		DruidMgr.getInstance().init("D:\\Workspaces\\Idea2018\\dbmgr\\src\\main\\resources\\druid.properties");

		User user = new User();
		user.setUserId(1001);
		user.setNick("陈烦");
		user.setCellphone("15968185312");
		user.setPazzwd("123456");
		user.setCreateTime(new Date());

		long t1 = System.currentTimeMillis();
		//for (int i=0;i<10000;i++) {
		/*	try (DBConnect con = new AfanConnect()) {
				con.insert(user);
			} catch (DBException e) {
				e.printStackTrace();
			}*/
		//}

		try (DBConnect con = new DefaultConnect()) {
			con.prepareStatement("update user set nick = ? where userId = ?", "哈哈哈", 100001);
			con.executeUpdate();
		} catch (DBException e) {
			e.printStackTrace();
		}
		System.out.println("total used:"+(System.currentTimeMillis()-t1));
	}

}
