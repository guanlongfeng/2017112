package com.hy.historydata;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hy.bean.JDBConnection;

public class HistoryDataAcqAction {
	/**
	* @author 关龙锋 
	* @date : 2016年7月20日 下午4:09:49
	*/
	private static final Log LOGGER = LogFactory.getLog(HistoryDataAcqAction.class);
	private static Connection conn = null;
	private static Statement st = null;
	private static JDBConnection jdbc = null;
	private static ResultSet rs = null;
	private static int save_time = 30;//实时库保存时间周期
	private static boolean isrun = true;
	private static int acq_real_time;
	//查询实时入库时间
    private static int acq_history_time;//历史库保存周期

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HisTorystartDataAcq();
		ColseAacqData cthd = new ColseAacqData();
		// /cthd.start();
	}

	public static class ColseAacqData extends Thread {
		public void run() {
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			isrun = false;
			System.out.println("真正所有关闭线程");
		}
	}

	public static void HisTorystartDataAcq() {
			//获取采集周期
			
			//获取线程池数
			//poolnum = getpoolnum();
			startthread();
	}

	public static void getacq_history_time() {
		try {
			JDBConnection jdbc_trd = new JDBConnection();
			Connection conn_trd = jdbc_trd.connection;
			Statement st_trd = conn_trd.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs=null;
			String sql = "select  history  from  windpower_dataacqtime ";
			rs = st_trd.executeQuery(sql);
			while (rs.next()) {
				acq_history_time = rs.getInt("history");
			}
			String sqlone = "select  save_time  from  windpower_dataacqtime ";
			rs = st_trd.executeQuery(sqlone);
			while (rs.next()) {
				save_time = rs.getInt("save_time");
			}
			
			String sqltwo = "select  real_time  from  windpower_dataacqtime ";
			rs = st_trd.executeQuery(sqltwo);
			while (rs.next()) {
				acq_real_time = rs.getInt("real_time");
			}
			rs.close();
			st_trd.close();
			conn_trd.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void startthread() {
		System.out.println("开启历史库采集进程......");
		ExecutorService threadPool = Executors.newScheduledThreadPool(1);
				threadPool.execute(new Runnable() {
					public void run() {
						while (isrun) {
							try {
								getacq_history_time();
								Thread.sleep(acq_real_time*1000);
								delhistorydata();
							} catch (Exception e ) {
								e.printStackTrace();
							}

						}
					}
				});
	
		} 


	/**
	 * 将制定时间之外的数据移到历史库中
	 */
public static void copynowacqdata(int device_id) {
		try {
			JDBConnection jdbc_trd = new JDBConnection();
			Connection conn_trd = jdbc_trd.connection;
			Statement st_trd = conn_trd.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs=null;
			String sql = "SELECT * FROM windpower_dataacq_tab  WHERE  device_id="+device_id+" and create_time <=CURRENT_TIMESTAMP - INTERVAL "+save_time+" MINUTE";
			rs=st_trd.executeQuery(sql);
			while(rs.next()){
				inserthistorydata(rs.getInt("id"),rs.getInt("device_id"),rs.getTimestamp("create_time"),rs.getString("data"));
				
			  }
			st_trd.close();
			conn_trd.close();
		} catch (SQLException e) {
			LOGGER.info(" 查询中采集库记录失败，数据库异常！");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

/**
 * 插入历史库
 * @param id
 * @param devcie_id
 * @param acqtime
 * @param data
 */
public static void inserthistorydata(int id,int devcie_id,Date acqtime,String data) {
	try {
		JDBConnection jdbc_trd = new JDBConnection();
		Connection conn_trd = jdbc_trd.connection;
		Statement st_trd = conn_trd.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		String sql = "insert into  windpower_dataacq_history  (device_id,data,acq_time,data_id) values("+devcie_id+",'"+data+"','"+acqtime+"',"+id+")";
		st_trd.execute(sql);
		deleteacqdata(id);
		st_trd.close();
		conn_trd.close();
	} catch (SQLException e) {
		LOGGER.info(" 插入历史记录失败，数据库异常！");
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
/**
 * 删除插入历史库的实时库数据
 * @param id
 */
public static void deleteacqdata(int id) {
	try {
		//LOGGER.info(" 正在删除实时库超过"+save_time+"分钟的数据.......");
		JDBConnection jdbc_trd = new JDBConnection();
		Connection conn_trd = jdbc_trd.connection;
		Statement st_trd = conn_trd.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		String sql = "delete from  windpower_dataacq_tab WHERE id="+id;
		st_trd.execute(sql);
		st_trd.close();
		conn_trd.close();
	} catch (SQLException e) {
		LOGGER.info(" 删除历史记录失败，数据库异常！");
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

public static void delhistorydata() {
	try {
		//LOGGER.info(" 正在删除历史库超过"+acq_history_time+"天数据.......");
		JDBConnection jdbc_trd = new JDBConnection();
		Connection conn_trd = jdbc_trd.connection;
		Statement st_trd = conn_trd.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		String sql = "delete from  windpower_dataacq_history WHERE create_time <=CURRENT_TIMESTAMP - INTERVAL "+acq_history_time+" Day";;
		st_trd.execute(sql);
		st_trd.close();
		conn_trd.close();
	} catch (SQLException e) {
		LOGGER.info(" 删除历史记录失败，数据库异常！");
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
}
