package com.hy.bean;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hy.bean.JDBConnection;
import com.hy.pojo.AcqData;


public class CreateTable {
	private static final Log LOGGER = LogFactory.getLog(CreateTable.class);
	@SuppressWarnings("unused")
	public static Connection conn = null;
	public static Statement st = null;
	private static JDBConnection jdbc = null;

	public static void main(String[] args) {
		//createtab();
	}

	public static void createtab() {
		String tablename = "windpower_dataacq_tab";
		try {
			jdbc = new JDBConnection();
			conn = jdbc.connection;
			st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			LOGGER.info("开始创建表" + tablename + "......");
			String createtablesql = "CREATE TABLE `" + tablename + "` (" + " `id` int(11) NOT NULL AUTO_INCREMENT,"
					+ " `name` varchar(10) DEFAULT NULL," + " `ip` varchar(20) DEFAULT NULL,"
					+ " `code` varchar(50) DEFAULT NULL," + " `modbustype` int(1) DEFAULT 0,"
					+ " `modbusstr` text DEFAULT NULL," + " `createtime` datetime DEFAULT   now(),"
					+ "  PRIMARY KEY (`id`)" + ") ENGINE=InnoDB  DEFAULT CHARSET=utf8";
			st.execute(createtablesql);
			LOGGER.info(tablename + "表格创建完成......");
			st.close();
			conn.close();
		} catch (Exception sqlexception) {
			sqlexception.printStackTrace();
			LOGGER.info("数据库连接发生异常！");
		}

	}
/**
 * 创建采集数据表
 * @param celllength
 * @throws SQLException
 */
	public static void createtable(int celllength) throws SQLException {
		// 500个字段为一个表，获取要创建的表个数
		double ii = celllength / 500;
		LOGGER.info(ii);
		LOGGER.info(Math.ceil(celllength / 500));
		int num = (int) (Math.ceil(celllength / 500) + 0.5);
		String tablename = "windpower_dataacq_table";
		try {
			jdbc = new JDBConnection();
			conn = jdbc.connection;
			st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			for (int i = 0; i < num; i++) {
				tablename += "0" + i;
				LOGGER.info("开始创建表" + tablename + "......");
				String createtablesql = "CREATE TABLE `" + tablename + "` (" + " `id` int(11) NOT NULL AUTO_INCREMENT,"
						+ " `name` varchar(10) DEFAULT NULL," + " `ip` varchar(20) DEFAULT NULL,"
						+ " `code` varchar(50) DEFAULT NULL," + " `createtime` datetime DEFAULT   now(),"
						+ "  PRIMARY KEY (`id`)" + ") ENGINE=InnoDB  DEFAULT CHARSET=utf8";
				st.execute(createtablesql);
				for (int j = 1; j <= 500; j++) {
					String columsql = " alter table windpower_dataacq_int add p" + 500 * i + j + " int(10);";
					st.execute(columsql);
				}
				LOGGER.info(tablename + "表格创建完成......");
			}
		} catch (Exception sqlexception) {
			sqlexception.printStackTrace();
			LOGGER.info("数据库连接发生异常！");
		}finally{
			st.close();
			conn.close();
		}

	}

}
