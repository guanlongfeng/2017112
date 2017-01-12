package com.hy.historydata;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.hy.pojo.AcqData;
import com.hy.pojo.DateTypeAction;

public class CreateExcel {

	public static void main(String[] args) {
		//创建详细数据的表格
		// TODO Auto-generated method stub
		//createexc();
		Timestamp time;
		String date;
		List<AcqData> datalist=new ArrayList();
		DateTypeAction data=new DateTypeAction();
		int device_id=7;
		
				//获取对应ID的用户名
		AcqData namelist=data.SelectDeviceName(device_id);
		//获取对应设备的数据
		List<AcqData> typelist=data.selectAllDate(device_id);
		String name=namelist.getName();
		//把设备名和设备进行封装
		 for(int i=0;i<typelist.size();i++){
			 AcqData dt=new AcqData();
			time=typelist.get(i).getCreate_time();
			date=typelist.get(i).getData();
			dt.setData(date);
			dt.setName(name);
			dt.setCreate_time(time);
			datalist.add(dt);	
		 }
	  // createexc(datalist,0);
	}
	/**
	 * 创建EXCEL
	 * @param datalist
	 * @param modbus_type
	 * @return
	 */
	public String createexc(List<AcqData> datalist,int modbus_type) {
		String filename="";
		try {
		DateTypeAction da=new DateTypeAction();
		String rootPath=getClass().getResource("../../../../../").getFile().toString();  
		System.out.println("创建XML1:"+rootPath);
		File directory = new File(rootPath);//参数为空

		
			String courseFile = directory.getAbsolutePath() ;//获取绝对路径
			String xmlpath = courseFile + "/ExcelLog/";
			SimpleDateFormat dfe = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			filename=xmlpath+datalist.get(0).getName()+"_"+ dfe.format(new Date())+".xls";
			System.out.println("创建Excel"+filename);
			
			SXSSFWorkbook  workbook = new SXSSFWorkbook();
			Sheet sheet = workbook.createSheet("事件历史记录文件");
			sheet.setColumnWidth(0, 5120);  
			Row row=null;  
            Cell cell=null;
            
            for(int i=0;i<datalist.size()+1;i++){
            //	System.out.println("第"+datalist.get(i).getCreate_time()+"采集值"+datalist.get(i).getData().split(",")[515]);
            	row=sheet.createRow(i);
            	if(i==0){
            		cell = row.createCell(0);
    				cell.setCellType(SXSSFCell.CELL_TYPE_STRING);
    				cell.setCellValue("采集时间");
    				String data=datalist.get(i).getData();
    				String datas[]=data.split(",");
    				for(int j=0;j<datas.length;j++){
    					String name = "";
    					if(modbus_type == 3){
    						name=da.selectAddr(j+12788, modbus_type);
    					}else{
    						name=da.selectAddr(j, modbus_type);
    					}
						cell = row.createCell(j+1);
	    				cell.setCellType(SXSSFCell.CELL_TYPE_STRING);
	    				cell.setCellValue(name);
    				}
    				
            	}else{
            		Timestamp acqtime=datalist.get(i-1).getCreate_time();
            		cell = row.createCell(0);
    				cell.setCellType(SXSSFCell.CELL_TYPE_STRING);
    				cell.setCellValue(TimestampToStr(acqtime));
    				String data=datalist.get(i-1).getData();
    				String datas[]=data.split(",");
    				for(int j=0;j<datas.length;j++){
						cell = row.createCell(j+1);
						cell.setCellType(SXSSFCell.CELL_TYPE_STRING);
						cell.setCellValue(datas[j]);
    				}
            	}
            }
			
            FileOutputStream fout = new FileOutputStream(filename);  
            workbook.write(fout);
            fout.flush();  
            fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return filename;
	}
	
	public static String TimestampToStr(Timestamp ts){
		String tsStr = ""; 
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   
		try {
			tsStr = sdf.format(ts);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tsStr;
	}

}
