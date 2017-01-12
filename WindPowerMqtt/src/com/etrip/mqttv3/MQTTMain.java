package com.etrip.mqttv3;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.Uninterruptibles;
import com.hy.bean.AESUtils;
import com.hy.bean.CRCUtils;
import com.hy.bean.MqttUtil;
import com.hy.bean.SunModbusTcpbyIp;
import com.hy.pojo.CdhData;
import com.hy.pojo.Data_Acq;
import com.hy.pojo.Device;

public class MQTTMain {
	 static MqttUtil mq=new MqttUtil();
	public static void main(String[] args) {
		//������Ϣ�ķ���
		//MQTTSubsribe.doTest();
		//������Ϣ����
		List<Device> devicelist = new ArrayList<Device>();
		devicelist=mq.startDataAcq();
		//ÿһ���ӷ���һ��������ÿ5���ӷ���һ����������
		byte [] bytecode=null;
	//	Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
			for (int num = 0; num < devicelist.size(); num++) {
				Device devone = devicelist.get(num);
				bytecode=sedrunddate(devone,0,null,null,null);
			    MQTTPub.doTest("Wind/BaseData",bytecode);
			};
		//sedrunddate();
	     
		
	}

	private static byte[] sedrunddate(Device dev,int typezt,Socket sck,List<CdhData> cdhlistone,Data_Acq data) { 
		 
		// TODO Auto-generated method stub
		 SunModbusTcpbyIp stp=new SunModbusTcpbyIp();
			//����+�豸����+���ܣ��ϴ��豸���ƣ��豸���кţ��豸����ֵ
			List<CdhData> cdhlist=null;
				//��ȡ�豸������Ϣ
			//cdhlist=mq.getBasedata(dev,data);
			//�豸���24λ����
/*			 String svstr=mq.getSystemServer("sn");
			 String [] svhex=mq.toHexString(svstr);
			 String []svstrmw=new String[24];
			 System.arraycopy(svhex, 0, svstrmw,0, svhex.length);
			 for(int j=svhex.length;j<24;j++){
				 svstrmw[j]="00";
		     }*/
			 //��ȡ�����豸�����ƣ����к�
			 String[] systemdevicename=mq.getcsdcode(50000,null,null);
			 String[] systemdevicensn=mq.getcsdcode(50001,null,null);
			 String[] systemdevicentime=mq.getcsdcode(50002,null,null);
			 String[] devicenname=mq.getcsdcode(50005,null,dev);
			 String[] devicenlx=mq.getcsdcode(50003,null,dev);
			//��ȡ��������״̬�µĲ��Ե�����
			int leng=0;
			List<String[]> listchd=new ArrayList();
	/*		for(int i=0;i<cdhlist.size();i++){
				//��ȡ����������Ϣ
				String[] str=mq.getcddatacode(cdhlist.get(i));
				listchd.add(str);
				leng+=str.length;
			}*/
			String[] rundata=new String[leng];
			int lengtmp=0;
			for(int k=0;k<listchd.size();k++){
				String[] str=listchd.get(k);
				System.arraycopy(str, 0, rundata, lengtmp, str.length);
				lengtmp+=str.length;
			}
			String[] jmrundataall=new String[systemdevicename.length+systemdevicensn.length+systemdevicentime.length+devicenname.length+devicenlx.length+rundata.length];
			
			System.arraycopy(systemdevicename, 0, jmrundataall, 0, systemdevicename.length);
			System.arraycopy(systemdevicensn, 0, jmrundataall, systemdevicename.length, systemdevicensn.length);
			System.arraycopy(systemdevicentime, 0, jmrundataall, systemdevicename.length+systemdevicensn.length, systemdevicentime.length);
			System.arraycopy(devicenname, 0, jmrundataall, systemdevicename.length+systemdevicensn.length+ systemdevicentime.length, devicenname.length);
			System.arraycopy(devicenlx, 0, jmrundataall, systemdevicename.length+systemdevicensn.length+ systemdevicentime.length+devicenname.length, devicenlx.length);
		//	System.arraycopy(rundata, 0, jmrundataall, systemdevicename.length+systemdevicensn.length+systemdevicentime.length+devicenname.length+devicenlx.length, rundata.length);
			String yxztstrtmp="";
			//���Ȳ���16�ı�������FF����
			int zs=jmrundataall.length/16;
			int ys=jmrundataall.length%16;
			if(ys>0){
				zs++;
			}
			String[] allcode=new String[16*zs];
			System.arraycopy(jmrundataall, 0, allcode, 0, jmrundataall.length);

			for(int i=jmrundataall.length;i<allcode.length;i++){
				allcode[i]="FF";
			}
			System.arraycopy(jmrundataall, 0, allcode, 0, jmrundataall.length);
			for (int i = 0; i < allcode.length; i++) {
				if(i==allcode.length-1){
					yxztstrtmp+=allcode[i];
				}else{
				   yxztstrtmp+=allcode[i]+" ";
				}
			}
			try {
				String key=mq.getKey("private_key");
				String jmrundatatmp=new AESUtils().encrypt(yxztstrtmp, key);
				String[] ymjzmstr=jmrundatatmp.split(" ");
				//���� 01 ��ʾAES-128�������� 50 00 ��ʾ��Ч���ݳ���Ϊ80 06 36 ��ʾ���ݵ�CRC16У��ֵ
				String[] basecocde=new String[5];
				String dataleng=Integer.toHexString(jmrundataall.length);
				String name = mq.get4HexString(dataleng);
				//У��
				String crc21 = CRCUtils.checkCRC16(jmrundatatmp);
				basecocde[0]="01";
				basecocde[1]=name.split(",")[1];
				basecocde[2]=name.split(",")[0];
				basecocde[3]=crc21.substring(2, 4);
				basecocde[4]=crc21.substring(0, 2);
				
				String[] tallcode=new String[ymjzmstr.length+basecocde.length];
			 /*  //��������
				System.arraycopy(ztstr, 0, tallcode, 0, ztstr.length);
				//�����豸����
				System.arraycopy(svstrmw, 0, tallcode, ztstr.length, svstrmw.length);*/
				//У����
				System.arraycopy(basecocde, 0, tallcode, 0, basecocde.length);
				//�����豸������Ϣ����������
				System.arraycopy(ymjzmstr, 0, tallcode, basecocde.length, ymjzmstr.length);
				byte[] cdhbyte=new byte[tallcode.length];
				for(int i=0;i<cdhbyte.length;i++){
					cdhbyte[i]=(byte) Integer.parseInt(tallcode[i],16);
				}
				return cdhbyte;
				//String cdhdatastr = new String(cdhbyte);
			   }
			   catch(Exception e){
				  e.printStackTrace();
				 // return "error";
			 }
			return null;
	}
}
