package com.hy.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.hy.bean.JDBConnection;
import com.hy.pojo.Device;

public class ServiceManagerStart {
	private static Connection conn = null;
	private static Statement st = null;
	private static JDBConnection jdbc = null;
	private static ResultSet rs = null;
	private static List<Device> devicelist = new ArrayList<Device>();
	private static boolean isrun = true;
	// 查询实时入库时间
	public static String rw_role_req  ="";
	public static String rw_role_res  = "";
		 public static void main(String[] args) {
			     startDataAcq();
			    if(devicelist.size()==0){
			    	System.out.print("没有设备可以采集......");
			    	return;
			    }
		        List<Service> services = Lists.newArrayList();
		        for (int num = 0; num < devicelist.size(); num++) {
		        	SungrowService serviceImp = new SungrowService(devicelist.get(num));
		            services.add(serviceImp);
		        }
		        System.out.println("*******构造多线程服务管理器*******");
		 
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
		 
		     /*   Runtime.getRuntime().addShutdownHook(new Thread() {
		            public void run() {
		                try {
		                    System.out.println("********终止所有任务********");
		                    serviceManager.stopAsync();
		                } catch (Exception timeout) {
		                	
		                    System.out.println("timeout");
		                }
		            }
		        });
		 */
		        System.out.println("********启动多线程所有任务********");
		        serviceManager.startAsync().awaitHealthy();;
		    }
/**
 * 查询所有可以采集的arm设备	
 */
		 public static void startDataAcq() {
				int device_id = 0;
				String ip = "";
				int port = 0;
				int startaddr = 0;
				int modbuslength = 0;
				int d_type = 0;
				int length = 0;
				try {
					jdbc = new JDBConnection();
					conn = jdbc.connection;
					st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
					String sql = "select  dinfo.device_id,dinfo.ip,dinfo.port,md.id d_type,md.startaddr,md.readlength,md.datalength  from   windpower_deviceinfo dinfo,windpower_device dev ,windpower_devicetype   devtype ,modbustcp_type md"
							+ " where  dinfo.device_id=dev.id and dev.device_type_id=devtype.id and devtype.modbus_type=md.id  and dev.run_state=0   and dinfo.d_type='arm' ";
					rs = st.executeQuery(sql);

					while (rs.next()) {
						Device dev = new Device();
						device_id = rs.getInt("device_id");
						ip = rs.getString("ip");
						port = rs.getInt("port");
						startaddr = rs.getInt("startaddr");
						modbuslength = rs.getInt("datalength");
						d_type = rs.getInt("d_type");
						length = rs.getInt("readlength");
						dev.setDevice_id(device_id);
						dev.setIp(ip);
						dev.setLength(length);
						dev.setModbuslength(modbuslength);
						dev.setPort(port);
						dev.setStartaddr(startaddr);
						dev.setD_type(d_type);
						devicelist.add(dev);
					}
					rs.close();
					st.close();
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			

}
