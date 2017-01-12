var slider;
	
    (function($) {
    	"use strict";
    	$(function() {
    		$.extend($.ui.slider.prototype.options, {
    			animate : "fast",
    			stop : function() {
    				var ident = this.id || this.className;
    				/*ga("send", "event", "slider", "interact", ident );*/
    			}
    		});

    			slider = $(".step-table-slider-15").slider({
    			min : 0,
    			max : 100,
    			change: function(event, ui) {
    		    	var operationValue = parseInt(ui.value);
    		    	var currentValue = parseInt($("#slideNum").val());
    		    	if(operationValue == currentValue){
    		    		return;
    		    	}
    		    	if(operationValue > currentValue){
    		    		enlarge(operationValue - currentValue);
    		    	}
    		    	if(operationValue < currentValue){
    		    		reduce(currentValue - operationValue);
    		    	}
		    	}
    		}).slider("pips", {
    			step : 10
    		});
    		
    		$("[class^=step-table-slider-]")
            .slider("float");
    	});
    }(jQuery));
    
	var socketUrl = context_.replace("http", "ws");
	var app = {};
	var chart;
	var webSocket;
	var updateChart;
    debugger;
	var allDatas = new Array(); //数据集合
	var allArr = []; //波形曲线对象数组
	var sumPointerNum = 200000; //数据总点数
	var waveformNum = 8; //波形数量
	//var maxDatalength = sumPointerNum / waveformNum; //最大点数
	var maxdl=$("#samplingFrequency").val();
	if(maxdl=="2"){
		maxdl=20000;
	}else if(maxdl=="2.5"){
		maxdl=25000
	}else{
		maxdl=30000
	}
	var maxDatalength = maxdl; //最大点数
	var mmUnit = maxDatalength / 100; //放大、缩小单位
	var mmsjUnit=10/100;
	//var mmUnit = 10 / 100; //放大、缩小单位
	var dataLength = 1000; // X轴数据点数量
	var defaultRatio = 0.5 / mmsjUnit; //默认显示百分比
	
	var updateInterval = 30; // 刷新速率
	var count = 4000; // 刷新加载点数
	var xVal = 0; //X轴值
	var modelType; //模式
	var moveformNames = "";//波形
	var moveformNamesObject = new Object();//波形
	
	var lastTime = null;
	
	var showMoveform = new Object();  //波形是否展示对象
	var showMoveformIndex = new Object(); //波形是否展示序号对象
	var showMoveformCanvasIndex = new Object(); //波形是否展示图标序号对象
	var maveformIndexs; //图形序号：波形序号
	var maveformCanvasIndexs; //波形序号：图形序号
	
	function initcharset(){
		 maxdl=$("#samplingFrequency").val();
		if(maxdl=="2"){
			maxdl=10;
		}else if(maxdl=="2.5"){
			maxdl=25000
		}else{
			maxdl=30000
		}
		 maxDatalength = maxdl; //最大点数
		 mmUnit = maxDatalength / 100; //放大、缩小单位
		 dataLength = 1000; // X轴数据点数量
		 defaultRatio = 0.5 / mmsjUnit; //默认显示百分比
	}
	
	function setValue(){
		if(1000 % mmUnit > 0){
			maxDatalength = parseInt(sumPointerNum / waveformNum) + 1;
		}else{
			maxDatalength = parseInt(sumPointerNum / waveformNum);
		}
		mmUnit = maxDatalength / 100;
		if(1000 % mmUnit > 0){
			mmUnit = parseInt(1000 / mmUnit) + 1;
		}else{
			mmUnit = parseInt(1000 / mmUnit);
		}
	}
	
	$(function () {
		$("#importSpan").show();
		$("#startSpan").show();
		$("#stopSpan").hide();
		$("#sliderDev").hide();
		
		changeModel();
		
		initWebsocket();
		
		setMaxXDataNumPostion();
		//当浏览器窗口大小改变时，设置显示内容的高度  
        window.onresize=function(){  
        	setMaxXDataNumPostion();
        }
	});
	
	/**
	 * 初始化Websocket
	 */
	function initWebsocket(){
		webSocket = null;
		webSocket = new WebSocket(socketUrl + 'oscilloscopeWebSocket');
		webSocket.onerror = function(event) {};
		webSocket.onopen = function() {};
		webSocket.onclose = function() {};

		webSocket.onmessage = function(event) {
			onMessage(event)
		};
	}
	
	/**
	 * 获取webSocket后台传输过来的DSP数据
	 */
	function onMessage(event) {
		//document.getElementById('messages').innerHTML += '开始接受数据'+allDatas.length;

		var datas = event.data.split("=");
		if(datas.length == 1){
			stopSync();
			alert(datas[0]);
		}
		for(var i = 0; i < datas.length; i++){
			allDatas.push(datas[i]);
		}
		//document.getElementById('messages').innerHTML += '<br/>开始接受数据'+allDatas.length;

		if('A9' == modelType){
			$("#importSpan").show();
			webSocket.close();
		}
//		document.getElementById('messages').innerHTML += '数据传输时间：'+(new Date()).getTime()+'<br/>';
	}
	
	/**
	 * 开始
	 */
	function startSync(){
		debugger;
		$("#importSpan").show();
		$("#startSpan").hide();
		$("#stopSpan").show();
		//$("#importSpan").hide();
		//$("#startSpan").hide();
		
//		/**
//		 * 连接websocket
//		 */
//		if(webSocket != null){
//			webSocket.close();
//		}
//		initWebsocket();
		
		var deviceId = $("#deviceId").val();
		modelType = $("#modelType").val();
		var samplingFrequency = 2.5;
		$("#modelTypeLast").val(modelType);
		// var modelType = $("#modelType").val();
		
		
		var modelname=$("#modelType").find("option:selected").text();
		var params = deviceId;
		params += "#" + modelType;
		params += "#" + samplingFrequency;
		
		/**
		 * 设置默认显示百分比
		 */
		$("#slideNum").val(defaultRatio);
		slider.slider("value", defaultRatio);
		
	    dataLength = parseInt(mmUnit * defaultRatio);
		
		var dataLengthtmp=parseFloat(mmsjUnit * defaultRatio);
		$("#maxXDataNum").html(dataLengthtmp);
		var  tmp="数据录波";
		if("A8" == modelType){
			
			document.getElementById('messages').innerHTML = "";
			
			if(!getOscMaveform()){
				return;
			}
			if(modelname==tmp){
				StartDspDcq();
			}
			$("#stopSpan").show();
			$("#sliderDev").show();
 			getOscShowMoveform();
			var dsp1WaveformCodes = $("#dsp1WaveformCodes").val();
			var dsp2WaveformCodes = $("#dsp2WaveformCodes").val();
			
//			initOscIsShowMoveform(dsp1WaveformCodes, "dsp1IsShow");
//			initOscIsShowMoveform(dsp1WaveformCodes, "dsp2IsShow");
			
			var dsp1WaveformNames = $("#dsp1WaveformNames").val();
			var dsp2WaveformNames = $("#dsp2WaveformNames").val();
			
			moveformNames = dsp1WaveformNames + "," + dsp2WaveformNames;
			
			params += "#" + dsp1WaveformCodes;
			params += "#" + dsp2WaveformCodes;
		}else if("A9" == modelType){
			$("#stopSpan").hide();
			getDefaultShowMoveform();
			var waveformType = $("#waveformType").val();
			var defaultWaveformCode = $("#defaultWaveformCode").val();
			params += "#" + waveformType;
			params += "#" + defaultWaveformCode;
			
			if("1" == waveformType){
				moveformNames = $("#dsp1DefaultWaveformNames").val();
				$("input[name='dspDefault1IsShow']").each(function(i){
			   		$(this).attr("checked",true);
			   		showMoveform[i] = true;
			  	});  
			}else if("2" == waveformType){
				moveformNames = $("#dsp2DefaultWaveformNames").val();
				$("input[name='dspDefault2IsShow']").each(function(i){
			   		$(this).attr("checked",true);
			   		showMoveform[i] = true;
			  	});
			}
		}
		params += "#0#" + (defaultRatio > 5 ? defaultRatio : 0);
		
		initChart(moveformNames);
		webSocket.send(params);
		
		clearInterval(app.timeTicket);
		app.timeTicket = setInterval(function(){updateChart()}, updateInterval);
		lastTime = (new Date()).getTime()
	}
	
	/**
	 * 停止
	 */
	function stopSync(){
		clearInterval(app.timeTicket);
		allDatas = new Array();
		
		webSocket.send("0#0#0#0#0#9#0");
		webSocket.close();
		
		initWebsocket();
		$("#importSpan").show();
		$("#startSpan").show();
		$("#stopSpan").hide();
	}
	
	/**
	 * 初始化图标控件
	 */
	function initChart(moveformNames){
		debugger;
		allDatas = new Array();
		$("#maxXDataNum").show();
		
		var data = initLine(moveformNames);
		var textData = '';
		
		chart = null;
		chart = new CanvasJS.Chart("container",{
			backgroundColor: "#",
			legend: {
	            cursor: "pointer",
	            fontSize: 10,
	            fontColor: "white",
	            itemclick: function (e) {
	                var isShow = true;
	                if (typeof (e.dataSeries.visible) == "undefined" || e.dataSeries.visible) {
	                	isShow = false;
	                }
	                e.dataSeries.visible = isShow
	                
	                var mfIndex = e.dataSeriesIndex;
	                if("A8" == modelType){
	                	mfIndex = maveformIndexs[mfIndex];
	                	if(mfIndex < 8){
		    				showHideMoveform("dsp1IsShow", mfIndex, isShow)
		    			}else{
		    				mfIndex = mfIndex - 8;
		    				showHideMoveform("dsp2IsShow", mfIndex, isShow)
		    			}
	        		}else if("A9" == modelType){
	        			var waveformType = $("#waveformType").val();
		                if("1" == waveformType){
		    				showHideMoveform("dspDefault1IsShow", mfIndex, isShow)
		    			}else if("2" == waveformType){
		    				showHideMoveform("dspDefault2IsShow", mfIndex, isShow);
		    			}
	        		}
	                e.chart.render();
	            }
	        },
			zoomEnabled: true,
			title :{
				text: textData
			},			
			data: data
		});
		chart.render();
	}
	
	/**
	 * 初始化线
	 */
	function initLine(moveformNames){
		allArr = [];
		var mfn = moveformNames.split(",");
		
		var data = [];
		var lineIndex = 0;
		for(var i=0; i<mfn.length; i++){
			
			if(mfn[i] == null || mfn[i] == ''){
				continue;
			}
			
			var tempArr = [];
			allArr.push(tempArr);
			var dt = new Object();
			dt.name = mfn[i];
			dt.showInLegend = true;
			dt.type = "spline";
			dt.dataPoints = allArr[lineIndex];
			if(showMoveformCanvasIndex[i] == null){
				dt.visible = false;
			}
			
			data[lineIndex] = dt;
			lineIndex++;
		}
		return data;
	}
	
	/**
	 * 加载图表数据
	 */
	function updateChart() {
		var ldlength = allDatas.length;
		
		if(ldlength <= 0){
			if(webSocket.readyState == WebSocket.CLOSED){
		        alert("服务器已断开连接，请重新连接！");
		        clearInterval(app.timeTicket);
				allDatas = new Array();
				
				$("#importSpan").show();
				$("#startSpan").show();
				$("#stopSpan").hide();
		    }
			return;
		}
		var lineIndex = 0;
 		var temparr = [];
		for (var j = 0; j < ldlength; j++) {
			/*if(j >= ldlength){
				break;
			}*/
			temparr = allDatas[0].split(",");
			lineIndex = 0;
			for(var i = 0; i< temparr.length; i++){
				if(!showMoveform[i]){
					continue;
				}
				var dps = allArr[lineIndex];
				var yVal = parseFloat(temparr[i]);
				
				dps.push({
					x: xVal,
					y: yVal
				});
				if (dps.length > dataLength){
					dps.shift();
				};
				lineIndex++;
			}
			allDatas.splice(0, 1);
			xVal++;
			
		};
	//	var startTime = (new Date()).getTime();
		//document.getElementById('messages').innerHTML += '；结束获取时间（开始画点）' + startTime;
		chart.render();	
		//var ldlength = allDatas.length;
     	//document.getElementById('messages').innerHTML += "画完后的长度为:"+allDatas.length;
		//document.getElementById('messages').innerHTML += '；结束画点时间' + (new Date()).getTime();
		//document.getElementById('messages').innerHTML += '；时间间隔' + ((new Date()).getTime()-startTime)+ "画完后的长度为:"+allDatas.length;

	};
	/**
	 * 选中不选中波形名称前的checkbox
	 */
	function showHideMoveform(moveformShowNames, mfIndex, isShow){
		var chks = document.getElementsByName(moveformShowNames);
		for(var i = 0; i < chks.length; i++){
			if(i == mfIndex){
				chks[i].checked = isShow;
		   		break;
			}
		}
	}
	
	/**
	 * 根据valuse，选中checkbox
	 * @param moveofrmIsShowName
	 * @param code
	 */
	function checkedOscMoveform(moveofrmIsShowName, code, obj){
		var chks = document.getElementsByName(moveofrmIsShowName);
		for(var i = 0; i < chks.length; i++){
			if(chks[i].value == code){
				chks[i].checked = obj.checked;
				num = i;
		   		break;
			}
		}
	}
	
	/**
	 * 显示/隐藏波形
	 */
	function showHideLine(codesId, code, addDataIndex, obj){
		var waveformCodes = $("#"+codesId).val().split(",");
		var dataIndex = null;
		for(var i = 0; i<waveformCodes.length; i++){
			if(waveformCodes[i] == code){
				dataIndex = i;
				break;
			}
		}
		
		if(dataIndex == null){
			return;
		}
		
		/**
		 * 设置波形显示、不显示
		 */
//		showMoveform[dataIndex+addDataIndex] = obj.checked;
		
		if(chart){
			var result = true;
			if(!obj.checked){
				result = false;
			}
			
			var options = chart.options;
			var data = options.data[maveformCanvasIndexs[dataIndex+addDataIndex]];
			data.visible = result;
			chart.render();
		}
	}
	
	/**
	 * 放大
	 */
	function enlarge(num) {
		if(dataLength >= (mmUnit * 100)){
			return null;
		}
		
		var currentValue = parseInt($("#slideNum").val()) + num;
		$("#slideNum").val(currentValue);
		slider.slider("value", currentValue);
		defaultRatio = currentValue;
		dataLength = parseInt(mmUnit * currentValue);
			var dataLengthtmp=parseFloat(mmsjUnit * defaultRatio);
			$("#maxXDataNum").html(dataLengthtmp);
		webSocket.send("0#0#0#0#0#8#"+(defaultRatio > 5 ? defaultRatio : 0));
	}

	/**
	 *  缩小
	 */
	function reduce(num) {
		debugger;
		if(dataLength <= mmUnit){
			return null;
		}
		for(var i = 0; i < allArr.length;i++){
			var dps = allArr[i];
			dps.splice(0, (mmUnit * num));
		}
		
		var currentValue = parseInt($("#slideNum").val()) - num;
		$("#slideNum").val(currentValue);
		slider.slider("value", currentValue);
		defaultRatio = currentValue;
		  dataLength = parseInt(mmUnit * defaultRatio);
			
			var dataLengthtmp=parseFloat(mmsjUnit * defaultRatio);
			$("#maxXDataNum").html(dataLengthtmp);
		
		webSocket.send("0#0#0#0#0#8#"+(defaultRatio > 5 ? defaultRatio : 0));
	}
	function changeCypl(){
		debugger;
/*		if(document.getElementById("startSpan").style.display="none"&&document.getElementById("stopSpan").style.display=="block"){
			alert("请先停止运行示波器！");
			return ;
		}*/
		stopSync();
		initcharset();
		$("#maxXDataNum").html("");
		$("#maxXDataNum").hide();
		 $("#sliderDev").hide();
		initChart(moveformNames);
	};
	/**
	 * 模式切换，页面控制
	 */
	function changeModel(){
		debugger;
	/*	if(document.getElementById("startSpan").style.display="none"&&document.getElementById("stopSpan").style.display=="block"){
			alert("切换波形时,请先停止运行示波器！");
			return ;
		}*/
		
		stopSync();
		var modelType = $("#modelType").val();
	    $("#sliderDev").hide();
		//$("#importSpan").hide();
		$("#startSpan").show();
		if("A8" == modelType){
			$("#waveformType").hide();
			$("#waveformTypeSpan").hide();
			$("#defaultWaveformCode").hide();
			$("#defaultWaveformCodeSpan").hide();
			$("#defaultDsp1-1").hide();
			$("#defaultDsp1-2").hide();
			$("#defaultDsp2-1").hide();
			$("#defaultDsp2-2").hide();
			$("#oscDsp1-1").show();
			$("#oscDsp1-2").show();
			$("#oscDsp2-1").show();
			$("#oscDsp2-2").show();
		}else if("A9" == modelType){
			$("#waveformType").show();
			$("#waveformTypeSpan").show();
			$("#defaultWaveformCode").show();
			$("#defaultWaveformCodeSpan").show();
			$("#oscDsp1-1").hide();
			$("#oscDsp1-2").hide();
			$("#oscDsp2-1").hide();
			$("#oscDsp2-2").hide();
			$("#defaultDsp1-1").show();
			$("#defaultDsp1-2").show();
			$("#defaultDsp2-1").hide();
			$("#defaultDsp2-2").hide();
		}
		
		dataLength = 1000;
		
		var modelTypeLast = $("#modelTypeLast").val();
		if(modelType != modelTypeLast && modelType == "A8"){
			initWebsocket();
		}
		if(webSocket){
			webSocket.send("0#0#0#0#0#9#0");
		}
	}
	
	/**
	 * 波形该改变事件
	 */
	function changeWaveformType(){
		
		var waveformType = $("#waveformType").val();
		if("1" == waveformType){
			$("#defaultDsp1-1").show();
			$("#defaultDsp1-2").show();
			$("#defaultDsp2-1").hide();
			$("#defaultDsp2-2").hide();
		}else if("2" == waveformType){
			$("#defaultDsp1-1").hide();
			$("#defaultDsp1-2").hide();
			$("#defaultDsp2-1").show();
			$("#defaultDsp2-2").show();
		}
	}
	
	/**
	 * 导出
	 */
	function exportExcel(){
		debugger;
		var modelname=$("#modelType").find("option:selected").text();
		var  tmp="数据录波";
		if(modelname==tmp){
			downFile();
			return;
		}
        
		if(document.getElementById("startSpan").style.display=="none"||document.getElementById("stopSpan").style.display=="block"){
			alert("请先停止示波器在下载！");
			return ;
			
		}
		if(document.getElementById("startSpan").style.display!="none"&&document.getElementById("stopSpan").style.display!="block"&&chart==undefined){
			alert("没有数据可以下载！");
			return ;
			
		}
		var obj = new Object; //提交对象
		var waveformName = new Array(); //波形名称集合
		var waveformDataFlag = "waveformData"; //波形数据基础标识
		var waveformDataFlags = new Array(); //波形数据标识名称集合
		var waveformDataTempFlag = ""; //波形名称
		
		var datas = chart.options.data;
		var data = "";
		var dataPoints = [];
		
		for(var i = 0; i <  datas.length; i++){
			dataPoints = datas[i].dataPoints;
			data = "";
			for(var j = 0; j < dataPoints.length; j++){
				data += dataPoints[j].y + ","; 
			}
			
			waveformDataTempFlag = waveformDataFlag + i;
			waveformDataFlags[i] = waveformDataTempFlag;
			$("#"+waveformDataTempFlag).val(data.substring(0, data.length - 1).split(","));
		}
		$("#waveformDataFlags").val(waveformDataFlags.join(","));
		
		var temMoveformNames = moveformNames.split(",");
		var tmfn = "";
		for(var i=0; i<temMoveformNames.length; i++){
			if(temMoveformNames[i] == null || temMoveformNames[i] == ''){
				continue;
			}
			tmfn += temMoveformNames[i] + ",";
		}
		$("#waveformNames").val(tmfn.substring(0, data.length - 1));
		
		exportForm.submit();
	}
	
	/**
	 * 获取示波器选中波形
	 */
	function getOscMaveform(){
		maveformIndexs = new Object();
		maveformCanvasIndexs = new Object();
		
		var dsp1CodeArr = ''; 
		var dsp2CodeArr = ''; 
		
		var dsp1NameArr = ''; 
		var dsp2NameArr = ''; 
		var num = 0;
		var datact=0;
		var index_i1=0;
		$('input[name="dsp1Code"]:checked').each(function(){
			//if(index_i1<4){
			dsp1CodeArr += $(this).val() + ",";
			dsp1NameArr += $(this).attr("title") + ",";
			maveformCanvasIndexs[num] = num;
			maveformIndexs[num] = num;
			num++;
		//	}
			index_i1++;
			datact++;
		}); 
		var dsp1EmptyNum = 0;
		for(var i = 0; i < (8-num); i++){
			dsp1CodeArr += "00,";
			dsp1NameArr += ",";
			dsp1EmptyNum++;
		}
		var dsp2Index = 0;
		var index_i2=0;
		$('input[name="dsp2Code"]:checked').each(function(){ 
			//if(index_i2<4){
			dsp2CodeArr += $(this).val() + ",";
			dsp2NameArr += $(this).attr("title") + ",";
			maveformCanvasIndexs[dsp2Index+8] = num;
			maveformIndexs[num] = (dsp2Index+8);
			num++;
			dsp2Index++;
		  //}
			index_i2++;
			datact++;
		});
		for(var i = 0; i < (8-dsp1EmptyNum); i++){
			dsp2CodeArr += "00,";
			dsp2NameArr += ",";
		}
		if(num != 8){
			alert("请选择8个波形");
			$("#startSpan").show();
			$("#stopSpan").hide();
			return false;
		}
		
		var dsp1WaveformCodes = dsp1CodeArr.substring(0, dsp1CodeArr.length - 1);
		var dsp2WaveformCodes = dsp2CodeArr.substring(0, dsp2CodeArr.length - 1);
		$("#dsp1WaveformCodes").val(dsp1WaveformCodes);
		$("#dsp2WaveformCodes").val(dsp2WaveformCodes);
		
		var dsp1WaveformNames = dsp1NameArr.substring(0, dsp1NameArr.length - 1);
		var dsp2WaveformNames = dsp2NameArr.substring(0, dsp2NameArr.length - 1);
		$("#dsp1WaveformNames").val(dsp1WaveformNames);
		$("#dsp2WaveformNames").val(dsp2WaveformNames);
		return true;
	}
	
	/**
	 * 获取示波器显示波形编号数据
	 */
/*	function getOscShowMoveform(){
		debugger;
		showMoveform = new Object();
		showMoveformIndex = new Object();
		showMoveformCanvasIndex = new Object();
		
		var dsp1WaveformCodes = $("#dsp1WaveformCodes").val().split(",");
		var dsp1ShowWaveform = new Object();
		
		var canvasIndex = 0;
		
		$('input[name="dsp1IsShow"]:checked').each(function(){
			dsp1ShowWaveform[$(this).val()] = true;
		});
		
		for(var i = 0; i < dsp1WaveformCodes.length; i++){
			if(dsp1ShowWaveform[dsp1WaveformCodes[i]]){
				if(i<4){
					showMoveform[i] = true;
					showMoveformIndex[canvasIndex] = i;
					showMoveformCanvasIndex[i] = canvasIndex;
					canvasIndex ++;	
				}
				
			}
			if(i<4){
				showMoveform[i] = true;
			} else {
				showMoveform[i] = false;
			}
		}
		
		var dsp2WaveformCodes = $("#dsp2WaveformCodes").val().split(",");
		var dsp2ShowWaveform = new Object();
		$('input[name="dsp2IsShow"]:checked').each(function(){
			dsp2ShowWaveform[$(this).val()] = true;
		});
		
		for(var i = 0; i < dsp2WaveformCodes.length; i++){
			if(dsp2ShowWaveform[dsp2WaveformCodes[i]]){
				if(i<4){
					showMoveform[i+8] = true;
					showMoveformIndex[canvasIndex] = i + 4;
					showMoveformCanvasIndex[i+4] = canvasIndex;
					canvasIndex ++;	
				}
			} 
			if(i<4){
				showMoveform[i+8] = true;
			} else {
				showMoveform[i+8] = false;
			}
		}
	}*/
	
	function getOscShowMoveform(){
		showMoveform = new Object();
		showMoveformIndex = new Object();
		showMoveformCanvasIndex = new Object();
		
		var dsp1WaveformCodes = $("#dsp1WaveformCodes").val().split(",");
		var dsp1ShowWaveform = new Object();
		
		var canvasIndex = 0;
		
		$('input[name="dsp1IsShow"]:checked').each(function(){
			dsp1ShowWaveform[$(this).val()] = true;
		});
		
		for(var i = 0; i < dsp1WaveformCodes.length; i++){
			if(dsp1ShowWaveform[dsp1WaveformCodes[i]]){
				showMoveform[i] = true;
				showMoveformIndex[canvasIndex] = i;
				showMoveformCanvasIndex[i] = canvasIndex;
				canvasIndex ++;
			} else if(dsp1WaveformCodes[i] != "00"){
				showMoveform[i] = true;
			} else {
				showMoveform[i] = false;
			}
		}
		
		var dsp2WaveformCodes = $("#dsp2WaveformCodes").val().split(",");
		var dsp2ShowWaveform = new Object();
		$('input[name="dsp2IsShow"]:checked').each(function(){
			dsp2ShowWaveform[$(this).val()] = true;
		});
		
		for(var i = 0; i < dsp2WaveformCodes.length; i++){
			if(dsp2ShowWaveform[dsp2WaveformCodes[i]]){
				showMoveform[i+8] = true;
				showMoveformIndex[canvasIndex] = i + 8;
				showMoveformCanvasIndex[i+8] = canvasIndex;
				canvasIndex ++;
			} else if(dsp2WaveformCodes[i] != "00"){
				showMoveform[i+8] = true;
			} else {
				showMoveform[i+8] = false;
			}
		}
	}
	
	/**
	 * 获取示波器显示波形编号数据
	 */
	function getDefaultShowMoveform(){
		showMoveform = new Object();
		var waveformType = $("#waveformType").val();
		if("1" == waveformType){
			var dsp1DefaultWaveformCodes = $("#dsp1DefaultWaveformCodes").val().split(",");
			$('input[name="dsp1DefaultIsShow"]:checked').each(function(){
				for(var i = 0; i < dsp1DefaultWaveformCodes.length; i++){
					if($(this).val() == dsp1DefaultWaveformCodes[i]){
						showMoveform[i] = true;
					}
				}
			});
		}else if("2" == waveformType){
			var dsp2DefaultWaveformCodes = $("#dsp2DefaultWaveformCodes").val().split(",");
			$('input[name="dsp2DefaultIsShow"]:checked').each(function(){ 
				for(var i = 0; i < dsp2DefaultWaveformCodes.length; i++){
					if($(this).val() == dsp2DefaultWaveformCodes[i]){
						showMoveform[i+4] = true;
					}
				}
			});
		}
	}
	
	
	/**
	 * 导航栏跳转方法
	 */
	function js_method(type) {
		
		stopSync();
		
		var device_id = $("#deviceId").val();
		var modbusid = $("#modbusid").val();
		
		if (type == 1) {
			url = "Devicetail/chooseMenu.do?device_id=" + device_id
					+ "&modbus_type=" + modbusid + "&type=" + type + "&modbusid=-1";
		} else if (type == 2) {
			url = "Devicetail/chooseMenu.do?device_id=" + device_id
					+ "&modbus_type=" + modbusid + "&type=" + type + "&modbusid=-1";
		} else if (type == 3) {
			url = "Devicetail/chooseMenu.do?device_id=" + device_id
					+ "&modbus_type=" + modbusid + "&type=" + type + "&modbusid=-1";
		} else if (type == 4) {
			if (modbusid == 2) {
				url = "Devicetail/chooseMenu.do?device_id="
						+ device_id + "&modbus_type=" + modbusid + "&type=" + type
						+ "&modbusid=" + modbusid;
	
			} else {
				url = "Devicetail/chooseMenu.do?device_id="
						+ device_id + "&modbus_type=" + modbusid + "&type=" + type
						+ "&modbusid=" + modbusid;
	
			}
		}
		window.location.href = context_ + url;
	}
	
	function setMaxXDataNumPostion(){
		var clientWidth = parseInt(document.body.clientWidth);
		var clientHeight = parseInt(document.body.clientHeight);
		
		document.getElementById("maxXDataNum").style.left = (clientWidth * 71 / 100) + "px";
		document.getElementById("maxXDataNum").style.bottom = (clientHeight * 10 / 100) + "px";
		
//		alert(document.getElementById("maxXDataNum").style.width);
//		alert(document.getElementById("maxXDataNum").style.height);
	}
	
//	
//	function js_method(type){
//		var device_id = $("#deviceId").val();
//		var modbusid = $("#modbusid").val();
//		if(type==1){
//			window.location.href="<%=basePath%>jsp/devicedetailjsp/DeviceSystemParame.jsp?device_id="+ device_id+"&modbus_type="+modbusid;
//		}else if(type==2){
//			window.location.href="<%=basePath%>jsp/devicedetailjsp/parametorlist.jsp?device_id="+ device_id+"&modbus_type="+modbusid;
//		}else if(type==3){
//			window.location.href="<%=basePath%>wfp/osc/toPage.do?device_id="+ device_id+"&modbus_type="+modbusid;
//		}else if(type==4){
//        	 if(modbusid==2){
//      			window.location.href="<%=basePath%>jsp/devicedetailjsp/DeviceSystemTest_hsfd.jsp?device_id="+ device_id+"&modbus_type="+modbusid;
//        	 }else{
//      			window.location.href="<%=basePath%>jsp/devicedetailjsp/DeviceSystemTest.jsp?device_id="+ device_id+"&modbus_type="+modbusid;
//        	 }
//        }
//	}