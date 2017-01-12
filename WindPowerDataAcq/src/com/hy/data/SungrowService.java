package com.hy.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Uninterruptibles;
import com.hy.bean.JDBConnection;
import com.hy.pojo.Device;
import static com.google.common.util.concurrent.Service.State.FAILED;
import static com.google.common.util.concurrent.Service.State.NEW;
import static com.google.common.util.concurrent.Service.State.RUNNING;
import static com.google.common.util.concurrent.Service.State.STARTING;
import static com.google.common.util.concurrent.Service.State.STOPPING;
import static com.google.common.util.concurrent.Service.State.TERMINATED;

public class SungrowService extends AbstractService implements Service {
	private State state = NEW;
	private Device dev;
	private Listener listener;
	// private static Map<Integer,Device> devicelist = new
	// Hashtable<Integer,Device>();

	SungrowService(Device device) {
		this.dev = device;
	}

	public State stopAndWait() {
		return state;
	}

	@Override
	protected void doStart() {
		new Thread() {
			@Override
			public void run() {
				notifyStarted();
				boolean isrun = true;
				DataAcqAction acq = new DataAcqAction();
				while (isrun) {
					Map<Integer, Device> devicelist = startDataAcq();
					// 获取采集频率

					int acq_real_time = acq.getacq_real_time();
					Uninterruptibles.sleepUninterruptibly(acq_real_time * 1000, TimeUnit.MILLISECONDS);
					boolean iscz = false;
					for (int num = 0; num < devicelist.size(); num++) {
						Device devone = devicelist.get(num);
						if (devone.getIp().equals(dev.getIp()) && devone.getDevice_id() == (dev.getDevice_id())) {
							iscz = true;
							break;
						}
					}
					;
					if (iscz == true) {
						acq.startthreadother(dev);
					} else {
						System.out.println(dev.getDevice_id() + "-" + dev.getIp() + ":已经被删除，不采集");
						acq.updatedevicestate(dev.getDevice_id(), "设备删除");
						isrun = false;
					}

				}
			}
		}.start();
	}

	@Override
	protected void doStop() {
		notifyStopped();
		new Thread() {
			@Override
			public void run() {
				Uninterruptibles.sleepUninterruptibly(1000, TimeUnit.MILLISECONDS);
				notifyStopped();
			}
		}.start();

		System.out.println("已结停止：" + dev.getDevice_id());
	}

	/**
	 * 获取最新采集设备
	 */
	public static Map<Integer, Device> startDataAcq() {
		Map<Integer, Device> devicelist = new Hashtable<Integer, Device>();
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
			ResultSet rs_trd = null;
			Statement st_trd = conn_trd.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			String sql = "select  dinfo.device_id,dinfo.ip,dinfo.port,md.id d_type,md.startaddr,md.readlength,md.datalength  from   windpower_deviceinfo dinfo,windpower_device dev ,windpower_devicetype   devtype ,modbustcp_type md"
					+ " where  dinfo.device_id=dev.id and dev.device_type_id=devtype.id and devtype.modbus_type=md.id  and dev.run_state=0   and dinfo.d_type='arm' ";
			rs_trd = st_trd.executeQuery(sql);
			int i = 0;
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
				devicelist.put(i, dev);
				i++;
			}
			rs_trd.close();
			st_trd.close();
			conn_trd.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return devicelist;
	}

}