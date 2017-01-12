package com.etrip.mqttv3;

import com.ibm.micro.client.mqttv3.MqttClient;
import com.ibm.micro.client.mqttv3.MqttConnectOptions;

public class MQTTSubsribe {
	public static String doTest() { 
		try { 
			//����MqttClient
			MqttClient client = new MqttClient("tcp://192.168.69.68:1883", "A140506988"); 
			//�ص�������
			CallBack callback = new CallBack(); 
			client.setCallback(callback); 
			//�������ӿ�ѡ����Ϣ
			MqttConnectOptions conOptions = new MqttConnectOptions(); 
			//
			conOptions.setCleanSession(false); 
			//����broker
			client.connect(conOptions); 
			//������صĶ���
			client.subscribe("PC/Password", 1); 
			client.subscribe("/Time", 1); 
			client.disconnect(); 
		} catch (Exception e) { 
			e.printStackTrace(); 
			return "failed"; 
		} 
		return "success"; 
	} 
} 
