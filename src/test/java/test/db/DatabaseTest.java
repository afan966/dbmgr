package test.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.afan.dbmgr.DBException;
import com.afan.dbmgr.pool.DBConnect;
import com.afan.dbmgr.pool.DefaultDBConnMgr;
import com.afan.dbmgr.pool.druid.DruidMgr;
import com.afan.dbmgr.util.LogBackConfigLoader;

public class DatabaseTest {
	private static final Logger logger = LoggerFactory.getLogger(DatabaseTest.class);

	public static void main(String[] args) {
		LogBackConfigLoader.load("E:\\Workspaces\\MyEclipse11\\dbmgr\\src\\main\\resources\\logback.xml");
		
		DruidMgr.getInstance().init("E:\\Workspaces\\MyEclipse11\\dbmgr\\src\\main\\resources\\druid.properties");
//		try (DBConnMgr conn = new DefaultDBConnMgr()) {
//			conn.prepareStatement("select * from user_account_consume where userId=?", userId);
//			return new ResultSetWrapper<UserAccountConsume>(conn, UserAccountConsume.class).queryList();
//		}
		int i=0;
		DBConnect conn = new DefaultDBConnMgr();
		try {
			PreparedStatement ptmt = conn.prepareStatement("select count(1) from seller where shopType = 'B' and shopLevel = ?", 8);
			
			//ResultSetWrapper<T> rs = new ResultSetWrapper<>(conn, clazz);
			ResultSet rs = ptmt.executeQuery();
			while(rs.next()){
				System.out.println(rs.getString(1));
				logger.debug("debug >>> {}", rs.getInt(1));
			}
			//conn.executeQuery("select * from seller where sellerId=60113414");
			try{
			System.out.println(10/i);
			}catch(Exception e){
				logger.error("hahahah - ", e);
			}	
		} catch (DBException | SQLException e) {
			e.printStackTrace();
		}
	}

}
