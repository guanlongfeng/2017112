package com.hy.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.CharBuffer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.hy.bean.ExcelExport;
import com.hy.bean.JDBConnection;
import com.hy.historydata.CreateExcel;
import com.hy.historydata.HistoryDataAcqAction;
import com.hy.pojo.AcqData;
import com.hy.pojo.DateTypeAction;
import com.hy.pojo.Device;
import com.hy.pojo.FormColnum;
import com.hy.pojo.FormRunData;

public class TextThd {
	private static Map<Integer,Device> devicelist = new Hashtable<Integer,Device>();
	private static Map<Integer,Device> newdevicelist = new Hashtable<Integer,Device>();
	private static Map<Integer,Device> adddevicelist = new Hashtable<Integer,Device>();
	static int num=1;
	public static void main(String[] args) {
		startacq();
	}
	public static  void startacq(){
		//关闭所有设备上次的运行状态时间。
		DataAcqAction.closeRundata();
		//关闭所有设备的读写状态权限，只开放采集权限
		DataAcqAction.updatere_roleall();
		startDataAcq();
		devicelist=newdevicelist;
		//第一次初始化采集设备
		statrSerivceManager(devicelist);
		//开启历史库数据采集
		HistoryDataAcqAction.HisTorystartDataAcq();
		new THD_ONE().start();
		//启动自动导出excel线程
		new RunDataTHD().start();
	}
	/**
	 * 定时刷新需要采集的设备
	 * @author Administrator
	 *
	 */
	public static class THD_ONE extends Thread {
		public void run() {
			while(true){
				try {
					THD_ONE.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				startDataAcq();
				System.out.println("更新采集设备......采集总数为："+newdevicelist.size());
				int addid=0;
				for (int i=0;i<newdevicelist.size();i++) {
					Device dev =newdevicelist.get(i);
					boolean iscz = false;
					for (int j=0;j<devicelist.size();j++){
						Device dev_old =devicelist.get(j);
						if (dev_old.getDevice_id() == dev.getDevice_id() && dev_old.getIp().equals(dev.getIp())) {
							iscz = true;
							break;
						}
					}
					
					if (iscz == false) {
						adddevicelist.put(addid, dev);
						addid++;
					}
				}
				if (adddevicelist.size() > 0) {
					devicelist=new Hashtable<Integer,Device>();
					devicelist = newdevicelist;
					// 开始创建新的管理线程
					System.out.println("发现新的采集设备......");
					statrSerivceManager(adddevicelist);
					adddevicelist = new Hashtable<Integer,Device>();

				}
			}
		 }
		}
	public static void statrSerivceManager(Map<Integer,Device> devicelisttmp){
		    if(devicelisttmp.size()==0||devicelisttmp==null){
		    	System.out.print("没有设备可以采集......");
		    	return;
		    }
	        List<Service> services = Lists.newArrayList();
	        for (int num = 0; num < devicelisttmp.size(); num++) {
	        	SungrowService serviceImp = new SungrowService(devicelisttmp.get(num));
	            services.add(serviceImp);
	        }
	        System.out.println("*******构造第"+num+"个多线程服务管理器*******");
	 
	        final ServiceManager serviceManager = new ServiceManager(services);
	        serviceManager. addListener(new ServiceManager.Listener() {
	            @Override
	            public void healthy() {
	                System.out.println("多线程服务运行健康！");
	            }
	            @Override
	            public void stopped() {
	                System.out.println("多线程服务运行结束！");
	            }
	            @Override
	            public void failure(Service service) {
	                System.out.println("多线程服务运行失败！");
	            }
	        }, MoreExecutors.sameThreadExecutor());
	 
	        System.out.println("********启动多线程所有任务********");
	        serviceManager.startAsync().awaitHealthy();
	        num++;
	    }
	/**
	 * 获取最新采集设备
	 */
    public static void startDataAcq() {
    	    newdevicelist=new Hashtable<Integer,Device>();
			int device_id = 0;
			String ip = "";
			int port = 0;
			int startaddr = 0;
			int modbuslength = 0;
			int d_type = 0;
			int length = 0;
			try {
				 JDBConnection jdbc_trd = new JDBConnection();
					Connection conn_trd = jdbc_trd.connection;
					ResultSet rs_trd=null;
					Statement st_trd  = conn_trd.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				String sql = "select  dinfo.device_id,dinfo.ip,dinfo.port,md.id d_type,md.startaddr,md.readlength,md.datalength  from   windpower_deviceinfo dinfo,windpower_device dev ,windpower_devicetype   devtype ,modbustcp_type md"
						+ " where  dinfo.device_id=dev.id and dev.device_type_id=devtype.id and devtype.modbus_type=md.id  and dev.run_state=0   and dinfo.d_type='arm'";
				rs_trd = st_trd.executeQuery(sql);
				int id=0;
				while (rs_trd.next()) {
					Device dev = new Device();
					device_id = rs_trd.getInt("device_id");
					ip = rs_trd.getString("ip");
					port = rs_trd.getInt("port");
					startaddr = rs_trd.getInt("startaddr");
					modbuslength = rs_trd.getInt("datalength");
					d_type = rs_trd.getInt("d_type");
					length = rs_trd.getInt("readlength");
					dev.setDevice_id(device_id);
					dev.setIp(ip);
					dev.setLength(length);
					dev.setModbuslength(modbuslength);
					dev.setPort(port);
					dev.setStartaddr(startaddr);
					dev.setD_type(d_type);
					newdevicelist.put(id, dev);
					id++;
				}
				rs_trd.close();
				st_trd.close();
				conn_trd.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

}