package com.cn.hy.bean;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class StartReadXML {
    //导入知道包下的xml
	public static void main(String[] args) {
		new WindPowerReadModbusXmlAll_hsfd().ReadModbusXmlall("");
	}
}
