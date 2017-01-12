package com.hy.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;


public class XlsImport extends HttpServlet {


public XlsImport() {
super();
}


public void destroy() {
super.destroy();
}


public void doGet(HttpServletRequest request, HttpServletResponse response)
throws ServletException, IOException {
       doPost(request, response);
}


public void doPost(HttpServletRequest request, HttpServletResponse response)
throws ServletException, IOException {


response.setContentType("text/html;charset=utf-8");
PrintWriter out = response.getWriter();
try{
Iterator<FileItem> it =  getUPFiles(request) ;
while (it.hasNext()) {
FileItem item = it.next();
if(item!=null && !item.isFormField() && item.getSize()>0){
InputStream f = item.getInputStream();
byte [] bb=new byte[1024];
int ii=f.read(bb);
out.println(importExcle(f));
}
}




out.flush();
out.close();
}catch (Exception e) {
e.printStackTrace();
}
}




public void init() throws ServletException {
// Put your code here
}
public String importExcle(InputStream in) {
StringBuffer buf = new StringBuffer();
try {
int beginRowIndex = 0;// 从excel 中开始读取的起始行数
int totalRows = 0;// 从excel 表的总行数
// 根据文件的输入流，创建对Excel 工作薄文件的引用
HSSFWorkbook workbook = new HSSFWorkbook(in);
// 默认exce的书页是“sheet1”
HSSFSheet sheet = workbook.getSheetAt(0);
// 得到该excel 表的总行数
totalRows = sheet.getLastRowNum();
System.out.println("xls总行数"+sheet.getLastRowNum());
// 循环读取excel表格的每行记录，并逐行进行保存
for (int i = beginRowIndex; i <= totalRows; i++) {
HSSFRow row = sheet.getRow(i);
// 获取一行每列的数据
HSSFCell num0 = row.getCell((short) 0);
HSSFCell num1 = row.getCell((short) 1);
HSSFCell num2 = row.getCell((short) 2);
HSSFCell num3 = row.getCell((short) 3);
// 将数据赋给相关的变量
String str0 = num0.getRichStringCellValue().getString().trim();
String str1 = num1.getRichStringCellValue().getString().trim();
String str2 = num2.getRichStringCellValue().getString().trim();
String str3 = num3.getRichStringCellValue().getString().trim();
System.out.println(str0+"-->"+str1+"-->"+str2+"-->"+str3);
buf.append(str0+"-->"+str1+"-->"+str2+"-->"+str3);
}
return buf.toString();
} catch (Exception e) {
e.printStackTrace();
return e.fillInStackTrace().toString();
} finally {
try {
in.close();
} catch (Exception e) {
e.printStackTrace();
}
}
}
/**
* 获取上传的文件集合
* @param request
* @return
* @throws FileUploadException
*/
public static  Iterator<FileItem> getUPFiles(HttpServletRequest request) throws FileUploadException{
DiskFileItemFactory fac = new DiskFileItemFactory();
ServletFileUpload upload = new ServletFileUpload(fac);
return upload.parseRequest(request).iterator(); 
}


}
