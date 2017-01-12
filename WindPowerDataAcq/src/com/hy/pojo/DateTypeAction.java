package com.hy.pojo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.hy.bean.JDBConnection;
import com.hy.data.DataAcqAction;
import com.hy.pojo.AcqData;

public class DateTypeAction {
	/**
	 * @author 关龙锋
	 * @date : 2016年7月20日 下午4:09:22
	 */
	private static final Log LOGGER = LogFactory.getLog(DataAcqAction.class);
	private static Connection conn = null;
	private static Statement st = null;
	private static JDBConnection jdbc = null;
	private static ResultSet rs = null;
	private static int poolnum = 0;
	private static List<AcqData> devicelist = new ArrayList();
	private static boolean isrun = true;
	public List<AcqData> selectAllDate(int device_id){
		/*
		 * 根据设备ID查询数据
		 */
		List<AcqData> devicelist = new ArrayList();
		JDBConnection jdbc_trd = new JDBConnection();
		Connection conn_trd = jdbc_trd.connection;
		try { 
			Statement st_trd = conn_trd.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = null;
			String sql = "select *from windpower_dataacq_tab where device_id="+ device_id +" order by  create_time desc limit 101";
			rs = st_trd.executeQuery(sql);
			while(rs.next()){
				AcqData dt=new AcqData();
			 dt.setId(rs.getInt("id"));
			 dt.setCreate_time(rs.getTimestamp("create_time"));
			 dt.setDevice_id(rs.getInt("device_id"));
			 dt.setData(rs.getString("data"));
			 devicelist.add(dt);
			}
			
			st_trd.close();
			conn_trd.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return devicelist;
	}
	public AcqData SelectDeviceName(int device_id){
		/*
		 * 根据设备ID查询设备名
		 */
		JDBConnection jdbc_trd = new JDBConnection();
		Connection conn_trd = jdbc_trd.connection;
		AcqData dt=new AcqData();
		Statement st_trd;
		try {
			st_trd = conn_trd.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = null;
			String sql = "select *from windpower_device where id="+ device_id +"";
			rs = st_trd.executeQuery(sql);
			while(rs.next()){
				    dt.setName(rs.getString("name"));
				}
			st_trd.close();
			conn_trd.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dt ;
	}
	public String  selectAddr(int addr,int modbus_type){
		JDBConnection jdbc_trd = new JDBConnection();
		Connection conn_trd = jdbc_trd.connection;
		AcqData dt=new AcqData();
		String name="";
		try {
			Statement st_trd = conn_trd.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = null;
		//	String sql = "select *from modbustcp_sk_app where addr="+addr+"  and modbus_type="+modbus_type +" and array_type="+3;
			String sql = "select *from modbustcp_sk_app where addr="+addr+"  and modbus_type="+modbus_type ;
			rs = st_trd.executeQuery(sql);
			while(rs.next()){
				name= rs.getString("name");
			}
			st_trd.close();
			conn_trd.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return name;
	}
}
