package com.hy.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;

import com.hy.bean.ExcelExport;
import com.hy.bean.JDBConnection;
import com.hy.data.TextThd.THD_ONE;
import com.hy.pojo.Device;
import com.hy.pojo.FormColnum;
import com.hy.pojo.FormRunData;

public class RunDataTHD extends Thread {
	private boolean daystate=false;
	private boolean weekstate=false;
	private boolean monthstate=false;
	private boolean offmonthstate=false;
	private boolean quarterstate=false;
	private boolean offyearstate=false;
	private boolean yearstate=false;
	public void run() {
		System.out.println("开启自动导出统计报表excel!");
		while (true) {
			try {
				THD_ONE.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Date d = new Date();
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTime(d);
			if ((gc.get(GregorianCalendar.HOUR_OF_DAY) == 0)&&daystate==false) {
			//if(1==1){
				FormColnum formColnum = new FormColnum();
				formColnum.setForm_type(2);
				formColnum.setShowin(1);
				List<FormColnum> colnumList = listFormColnum(formColnum);
				List<FormRunData> runList = listFormRunData("日");
				new ExcelExport().createexcl(colnumList, runList,"日");
				daystate=true;
			}else{
				daystate=false;
			}
			//if(1==1){
			if ((gc.get(GregorianCalendar.DAY_OF_WEEK) == 0)&&weekstate==false) {
				FormColnum formColnum = new FormColnum();
				formColnum.setForm_type(2);
				formColnum.setShowin(1);
				List<FormColnum> colnumList = listFormColnum(formColnum);
				List<FormRunData> runList = listFormRunData("周");
				new ExcelExport().createexcl(colnumList, runList,"周");
				weekstate=true;
			}else{
				weekstate=false;
			}
			//if(1==1){
			if ((gc.get(GregorianCalendar.DAY_OF_MONTH) == 0)&&monthstate==false) {
				FormColnum formColnum = new FormColnum();
				formColnum.setForm_type(2);
				formColnum.setShowin(1);
				List<FormColnum> colnumList = listFormColnum(formColnum);
				List<FormRunData> runList = listFormRunData("月");
				new ExcelExport().createexcl(colnumList, runList,"月");
				monthstate=true;
			}else{
				monthstate=false;
			}
			if ((gc.get(GregorianCalendar.DAY_OF_YEAR) == 0)&&yearstate==false) {
				FormColnum formColnum = new FormColnum();
				formColnum.setForm_type(2);
				formColnum.setShowin(1);
				List<FormColnum> colnumList = listFormColnum(formColnum);
				List<FormRunData> runList = listFormRunData("年");
				new ExcelExport().createexcl(colnumList, runList,"年");
				yearstate=true;
			}else{
				yearstate=false;
			}

			//半个月的日期
			int num = gc.getActualMaximum(Calendar.DAY_OF_MONTH);
			if (num == 30 || num == 31) {
				if ((gc.get(GregorianCalendar.DAY_OF_MONTH) == 14)&&offmonthstate==false) {
					FormColnum formColnum = new FormColnum();
					formColnum.setForm_type(2);
					formColnum.setShowin(1);
					List<FormColnum> colnumList = listFormColnum(formColnum);
					List<FormRunData> runList = listFormRunData("半月");
					new ExcelExport().createexcl(colnumList, runList,"半月");
					offmonthstate=true;
				}else{
					offmonthstate=false;
				}
			} else if (num == 28 || num == 29) {
				if ((gc.get(GregorianCalendar.DAY_OF_MONTH) == 13)&&offmonthstate==false) {
					FormColnum formColnum = new FormColnum();
					formColnum.setForm_type(2);
					formColnum.setShowin(1);
					List<FormColnum> colnumList = listFormColnum(formColnum);
					List<FormRunData> runList = listFormRunData("半月");
					new ExcelExport().createexcl(colnumList, runList,"半月");
					offmonthstate=true;
				}else{
					offmonthstate=false;
				}
			}

			// 季度
			int mouthtmp=gc.get(Calendar.MONTH)+1;
			if(mouthtmp==4||mouthtmp==7||mouthtmp==10||mouthtmp==1){
				int maxday = gc.getActualMaximum(Calendar.DAY_OF_MONTH);
				int daytmp=gc.get(GregorianCalendar.DAY_OF_MONTH);
				if(maxday==daytmp&&quarterstate==false){
					FormColnum formColnum = new FormColnum();
					formColnum.setForm_type(2);
					formColnum.setShowin(1);
					List<FormColnum> colnumList = listFormColnum(formColnum);
					List<FormRunData> runList = listFormRunData("季度");
					new ExcelExport().createexcl(colnumList, runList,"季度");	
				}
				quarterstate=true;
			}else{
				quarterstate=false;
			}
			// 半年
			int motmp = gc.get(GregorianCalendar.MONTH + 1);
			int daytmp = gc.get(GregorianCalendar.DAY_OF_MONTH);
				if (motmp == 6 && daytmp == 30&&offyearstate==false) {
				FormColnum formColnum = new FormColnum();
				formColnum.setForm_type(2);
				formColnum.setShowin(1);
				List<FormColnum> colnumList = listFormColnum(formColnum);
				List<FormRunData> runList = listFormRunData("半年");
				new ExcelExport().createexcl(colnumList, runList,"半年");
				offyearstate=true;
			}else{
				offyearstate=false;
			}
         
		}
	}

	public List<FormColnum> listFormColnum(FormColnum formColnum) {
		List<FormColnum> formcolnumlist = new ArrayList<FormColnum>();
		int form_type = formColnum.getForm_type();
		int showin = formColnum.getShowin();
		try {
			JDBConnection jdbc_trd = new JDBConnection();
			Connection conn_trd = jdbc_trd.connection;
			ResultSet rs_trd = null;
			Statement st_trd = conn_trd.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			String sql = "select id,errorcode,codesx,form_type,showin from windpower_formcolnumset where form_type = "
					+ form_type + "	and showin=" + showin;
			rs_trd = st_trd.executeQuery(sql);
			int id = 0;
			while (rs_trd.next()) {
				FormColnum fromone = new FormColnum();
				fromone.setId(rs_trd.getInt("id"));
				fromone.setErrorcode(rs_trd.getString("errorcode"));
				fromone.setCodesx(rs_trd.getString("codesx"));
				fromone.setShowin(rs_trd.getInt("showin"));
				formcolnumlist.add(fromone);
			}
			rs_trd.close();
			st_trd.close();
			conn_trd.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return formcolnumlist;
	}

	public List<FormRunData> listFormRunData(String timename) {
		String time_sql = getDateAreaSql(timename);
		return updaterundata(listFormRunDataDao(time_sql), time_sql);
	}

	public List<FormRunData> listFormRunDataDao(String time_sql) {
		List<FormRunData> FormRunDatalist = new ArrayList<FormRunData>();
		try {
			JDBConnection jdbc_trd = new JDBConnection();
			Connection conn_trd = jdbc_trd.connection;
			ResultSet rs_trd = null;
			Statement st_trd = conn_trd.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			String sql = "	select t1.name device_name,t.device_id,t.id,dwdy,dl,pl,bwyggl,bwwggl,"
					+ " max(fdl)-min(fdl) fdl,sum(djsj) djsj,sum(bwsj) bwsj,sum(gzsj) gzsj,sum(pjlyxss) pjlyxss,sum(blqwgzlyl) blqwgzlyl,max(createtime) createtime,max(createtime) maxtime,min(createtime) mintime,count(1) ct"
					+ " from windpower_formrundata t" + ",windpower_device t1 " + "WHERE t.device_id=t1.id  and "
					+ time_sql + "   group  by device_id ";
			rs_trd = st_trd.executeQuery(sql);
			int id = 0;
			while (rs_trd.next()) {
				FormRunData fromone = new FormRunData();
				fromone.setDevice_name(rs_trd.getString("device_name"));
				fromone.setDevice_id(rs_trd.getInt("device_id"));
				fromone.setId(rs_trd.getInt("id"));
				fromone.setDwdy(rs_trd.getInt("dwdy"));
				fromone.setDl(rs_trd.getInt("dl"));
				fromone.setPl(rs_trd.getDouble("pl"));
				fromone.setBwyggl(rs_trd.getInt("bwyggl"));
				fromone.setBwwggl(rs_trd.getInt("bwwggl"));
				fromone.setFdl(rs_trd.getInt("fdl"));
				fromone.setDjsj(rs_trd.getString("djsj"));
				fromone.setBwsj(rs_trd.getString("bwsj"));
				fromone.setGzsj(rs_trd.getString("gzsj"));
				fromone.setPjlyxss(rs_trd.getFloat("pjlyxss"));
				fromone.setBlqwgzlyl(rs_trd.getString("blqwgzlyl"));
				fromone.setMaxtime(rs_trd.getString("maxtime"));
				fromone.setMintime(rs_trd.getString("mintime"));
				FormRunDatalist.add(fromone);
			}
			rs_trd.close();
			st_trd.close();
			conn_trd.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return FormRunDatalist;
	}

	public FormRunData getdevicerundata(int device_id, String time_sql) {
		FormRunData fromone = new FormRunData();
		try {
			JDBConnection jdbc_trd = new JDBConnection();
			Connection conn_trd = jdbc_trd.connection;
			ResultSet rs_trd = null;
			Statement st_trd = conn_trd.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			String sql = "select *,1 ct from windpower_formrundata	where " + time_sql + " and device_id =" + device_id
					+ "	order by createtime desc limit 0,1 ";
			rs_trd = st_trd.executeQuery(sql);
			int id = 0;
			while (rs_trd.next()) {
				fromone.setDevice_name(rs_trd.getString("device_name"));
				fromone.setDevice_id(rs_trd.getInt("device_id"));
				fromone.setId(rs_trd.getInt("id"));
				fromone.setDwdy(rs_trd.getInt("dwdy"));
				fromone.setDl(rs_trd.getInt("dl"));
				fromone.setPl(rs_trd.getDouble("pl"));
				fromone.setBwyggl(rs_trd.getInt("bwyggl"));
				fromone.setBwwggl(rs_trd.getInt("bwwggl"));
				fromone.setCreatetime(rs_trd.getString("createtime"));
			}
			rs_trd.close();
			st_trd.close();
			conn_trd.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fromone;
	}

	/**
	 * 获取SQL语句
	 * 
	 * @param timename
	 * @return
	 */
	public String getDateAreaSql(String timename) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String date_area_sql = "";

		DecimalFormat df00 = new DecimalFormat("###0.00");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d = new Date();
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(d);
		// 每天0点兑时
		int YEAR = gc.get(GregorianCalendar.YEAR);
		int MONTH = gc.get(GregorianCalendar.MONTH) + 1;
		int DATE = gc.get(GregorianCalendar.DATE);
		String mintimestr = YEAR + "-" + MONTH + "-" + DATE + " 00";
		String minstr = " 00";
		if ("日".equals(timename)) {// 天
			date_area_sql = "to_days(createtime) = to_days(now()) and createtime NOT LIKE CONCAT('%" + mintimestr
					+ "%')";
		} else if ("周".equals(timename)) {// 周
			Calendar cal_1 = Calendar.getInstance();
			cal_1.add(Calendar.WEDNESDAY, 0);
			cal_1.set(Calendar.DAY_OF_WEEK, 1);// 设置为1号,当前日期既为本月第一天
			String firstday = format.format(cal_1.getTime());
			mintimestr = firstday + minstr;
			date_area_sql = "YEARWEEK(date_format(createtime,'%Y-%m-%d')) = YEARWEEK(now()) and createtime NOT LIKE CONCAT('%"
					+ mintimestr + "%')";
		} else if ("半月".equals(timename)) {// 半月
			try {
				Calendar cal_1 = Calendar.getInstance();
				cal_1.add(Calendar.MONTH, 0);
				cal_1.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
				String firstday = format.format(cal_1.getTime());
				int num = cal_1.getActualMaximum(Calendar.DAY_OF_MONTH);
				// 半个月的日期
				String midday = "";
				Date midDate = null;
				if (num == 30 || num == 31) {
					cal_1.add(Calendar.DAY_OF_YEAR, 14);
					midday = format.format(cal_1.getTime());
					// midDate = cal_1.getTime();
					midDate = format.parse(midday);
				} else if (num == 28 || num == 29) {
					cal_1.add(Calendar.DAY_OF_YEAR, 13);
					midday = format.format(cal_1.getTime());
					// midDate = cal_1.getTime();
					midDate = format.parse(midday);
				}
				// 本月最后一天
				cal_1.set(Calendar.DAY_OF_MONTH, cal_1.getActualMaximum(Calendar.DAY_OF_MONTH));
				String lastday = format.format(cal_1.getTime());
				Date nowDate = new Date();
				if (nowDate.before(midDate)) {
					mintimestr = firstday + minstr;
					date_area_sql = "TO_DAYS(createtime) BETWEEN TO_DAYS('" + firstday + "') and TO_DAYS('" + midday
							+ "') and createtime NOT LIKE CONCAT('%" + mintimestr + "%')";
				} else if (nowDate.after(midDate)) {
					mintimestr = midday + minstr;
					date_area_sql = "TO_DAYS(createtime) BETWEEN TO_DAYS('" + midday + "') and TO_DAYS('" + lastday
							+ "') and createtime NOT LIKE CONCAT('%" + mintimestr + "%')";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("月".equals(timename)) {// 月
			Calendar cal_1 = Calendar.getInstance();
			cal_1.add(Calendar.MONTH, 0);
			cal_1.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
			String firstday = format.format(cal_1.getTime());
			mintimestr = firstday + minstr;
			date_area_sql = "DATE_FORMAT(createtime,'%Y%m') = DATE_FORMAT(CURDATE( ) ,'%Y%m') and createtime NOT LIKE CONCAT('%"
					+ mintimestr + "%')";
		} else if ("季度".equals(timename)) {// 季度
			Calendar cal_1 = Calendar.getInstance();
			cal_1.setTime(new Date());
			int month = getQuarterInMonth(cal_1.get(Calendar.MONTH), true);
			cal_1.set(Calendar.MONTH, month);
			cal_1.set(Calendar.DAY_OF_MONTH, 1);
			String firstday = format.format(cal_1.getTime());
			mintimestr = firstday + minstr;
			date_area_sql = "QUARTER(createtime)=QUARTER(now()) and createtime NOT LIKE CONCAT('%" + mintimestr + "%')";
		} else if ("半年".equals(timename)) {// 半年
			try {
				Calendar now = Calendar.getInstance();
				int year = now.get(Calendar.YEAR);
				String midyeardayStr = year + "-06-30";
				Date midyearday = format.parse(midyeardayStr);
				String nowdate = format.format(new Date());
				Date nowyearday = format.parse(nowdate);
				String firstdayStr = year + "-01-01";
				String enddayStr = year + "-12-31";

				if (nowyearday.before(midyearday)) {
					mintimestr = firstdayStr + minstr;
					date_area_sql = "TO_DAYS(createtime) BETWEEN TO_DAYS('" + firstdayStr + "') and TO_DAYS('"
							+ midyeardayStr + "') and createtime NOT LIKE CONCAT('%" + mintimestr + "%')";
				} else if (nowyearday.after(midyearday)) {
					mintimestr = midyeardayStr + minstr;
					date_area_sql = "TO_DAYS(createtime) BETWEEN TO_DAYS('" + midyeardayStr + "') and TO_DAYS('"
							+ enddayStr + "') and createtime NOT LIKE CONCAT('%" + mintimestr + "%')";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("年".equals(timename)) {// 年
			Calendar cal_1 = Calendar.getInstance();
			cal_1.set(Calendar.DAY_OF_YEAR, 1);
			String firstday = format.format(cal_1.getTime());
			mintimestr = firstday + minstr;
			date_area_sql = "YEAR(createtime)=YEAR(NOW()) and createtime NOT LIKE CONCAT('%" + mintimestr + "%')";
		}
		return date_area_sql;
	}

	// 返回第几个月份，不是几月
	// 季度一年四季， 第一季度：2月-4月， 第二季度：5月-7月， 第三季度：8月-10月， 第四季度：11月-1月
	private static int getQuarterInMonth(int month, boolean isQuarterStart) {
		int months[] = { 1, 4, 7, 10 };
		if (!isQuarterStart) {
			months = new int[] { 3, 6, 9, 12 };
		}
		if (month >= 2 && month <= 4)
			return months[0];
		else if (month >= 5 && month <= 7)
			return months[1];
		else if (month >= 8 && month <= 10)
			return months[2];
		else
			return months[3];
	}

	private List<FormRunData> updaterundata(List<FormRunData> listFormRunData, String time_sql) {
		List<FormRunData> runlist = new ArrayList<FormRunData>();
		DecimalFormat df00 = new DecimalFormat("###0.00");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d = new Date();
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(d);
		// 每天0点兑时
		int YEAR = gc.get(GregorianCalendar.YEAR);
		int MONTH = gc.get(GregorianCalendar.MONTH) + 1;
		int DATE = gc.get(GregorianCalendar.DATE);
		int MINUTE = gc.get(GregorianCalendar.MINUTE);
		int SECOND = gc.get(GregorianCalendar.SECOND);
		int HOUR_OF_DAY = gc.get(GregorianCalendar.HOUR_OF_DAY);

		int hour = HOUR_OF_DAY;
		for (int i = 0; i < listFormRunData.size(); i++) {
			int device_id = 0;
			FormRunData formrundataone = listFormRunData.get(i);
			String starttimestr = formrundataone.getMintime();
			String endtimestr = formrundataone.getMaxtime();
			try {
				long starttime = df.parse(starttimestr).getTime();
				long endtime = df.parse(endtimestr).getTime();
				long sjtime = endtime - starttime;
				int length = 3600000;
				hour = (int) Math.ceil(sjtime / length) + 1;
				int numy = (int) Math.ceil(sjtime % length);
				if (numy > 1800000) {
					hour++;
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			device_id = formrundataone.getDevice_id();
			FormRunData rundataone = getdevicerundata(device_id, time_sql);
			// 指定时间内采集了多少数据
			formrundataone.setDl(rundataone.getDl());
			formrundataone.setDwdy(rundataone.getDwdy());
			formrundataone.setPl(rundataone.getPl());
			formrundataone.setBwyggl(rundataone.getBwyggl());
			formrundataone.setBwwggl(rundataone.getBwwggl());
			formrundataone.setCreatetime(rundataone.getCreatetime());
			// int hour=formrundataone.getCt();
			// int hour=HOUR_OF_DAY;
			double bwsjtmp = Double.parseDouble(formrundataone.getBwsj()) / 3600;
			String bwsjstr = df00.format(bwsjtmp);
			formrundataone.setBwsj(bwsjstr);

			double djsjtmp = Double.parseDouble(formrundataone.getDjsj()) / 3600;
			String djsjstr = df00.format(djsjtmp);
			formrundataone.setDjsj(djsjstr);

			double gzsjtmp = Double.parseDouble(formrundataone.getGzsj()) / 3600;
			String gzsjstr = df00.format(gzsjtmp);
			formrundataone.setGzsj(gzsjstr);

			// 变流器无故障利用率:(统计时间-故障时间)/统计时间,百分数标量.
			double blqwgzlyltmp = Double.parseDouble(formrundataone.getBlqwgzlyl()) / hour;
			String blqwgzlylstr = df00.format(blqwgzlyltmp * 100);
			formrundataone.setBlqwgzlyl(blqwgzlylstr + "%");
			// 平均发电功率:单位为kw,发电量/统计时间(h)
			double pjfdgltmp = (double) formrundataone.getFdl() / hour;
			String pjfdglstr = df00.format(pjfdgltmp);
			formrundataone.setPjfdgl(Float.parseFloat(pjfdglstr));

			int hit = hour - HOUR_OF_DAY;
			int day = hit / 24 + 1;
			int dayys = hit % 24;
			if (dayys > 0) {
				day++;
			}
			// 平均利用小时数:(统计时间-故障时间)/统计天数
			double pjlyxsstmp = (double) formrundataone.getPjlyxss() / 3600;// 转换成小时对象
			String pjlyxssstr = df00.format(pjlyxsstmp / day);// 这周的第几天
			formrundataone.setPjlyxss(Float.parseFloat(pjlyxssstr));
			runlist.add(i, formrundataone);
			;
		}

		return runlist;
	}
}
