package test.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.db.entity.Seller;
import com.afan.dbmgr.DBException;
import com.afan.dbmgr.pool.AfanConnect;
import com.afan.dbmgr.pool.DBConnect;
import com.afan.dbmgr.pool.druid.DruidMgr;
import com.afan.dbmgr.pool.wrap.ResultSetWrapper;
import com.afan.dbmgr.util.LogBackConfigLoader;

public class DatabaseTest {
	private static final Logger logger = LoggerFactory.getLogger(DatabaseTest.class);

	public static void main(String[] args) {
		LogBackConfigLoader.load("E:\\Workspaces\\MyEclipse11\\afan.dbmgr\\src\\main\\resources\\logback.xml");
		DruidMgr.getInstance().init("E:\\Workspaces\\MyEclipse11\\afan.dbmgr\\src\\main\\resources\\druid.properties");
		
		Seller seller = new Seller();
		seller.setSellerId(601134157);
		seller.setSellerNick("学生不懂1");
		seller.setName("haha");
		
		long t1 = System.currentTimeMillis();
		for (int i=0;i<10000;i++) {
			try (DBConnect con = new AfanConnect()) {
				seller.setSellerId(601134188+i);
				con.insert(seller);
			} catch (DBException e) {
				e.printStackTrace();
			}
		}
		System.out.println("total used:"+(System.currentTimeMillis()-t1));
		
		try (DBConnect con = new AfanConnect()) {
			con.query(seller);
			ResultSetWrapper<Seller> rs = new ResultSetWrapper<>(con, Seller.class);
			for (Seller s : rs.queryList()) {
				logger.debug(s.getSellerId()+" - "+s.getSellerNick());
			}
		} catch (DBException e) {
			e.printStackTrace();
		}
		
	}

}
