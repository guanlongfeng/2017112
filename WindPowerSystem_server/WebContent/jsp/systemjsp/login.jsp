<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
 	String path = request.getContextPath();
	String basePath = request.getScheme() + "://"
			+ request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>欢迎登录后台管理系统</title>
<link href="<%=basePath%>/css/login.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="<%=basePath%>/js/jquery-1.8.3.min.js"></script>
<script type="text/javascript">
   function mainView(){
	   var ajaxURL = "<%=basePath%>User/login.do";
	   $.ajax({
		   	type:"post",
		   	url:ajaxURL,
		   	data:$('#login-form').serialize(),
		   	success:function(data){
		   	var result = JSON.parse(data);	
		   	var msg=result.message;
		    if(msg == "获取成功"){
		    	window.location.href="<%=basePath%>jsp/index.jsp"
		   	}else{
		   		alert(msg);
		   	}
	   } });
}
</script>
</head>
<body>
<div class="main-login">
	<!-- <div class="login-logo">

	</div> -->
	<div class="login-content">
	<input type="hidden" id="path" value="<%=basePath %>" />
    <form action="" method="post" id="login-form" name="login-form">

<div class="login-info">
<img src="<%=basePath%>images/logo.png" >

	</div>
    <div class="login-info"><span class="span_01">用户名：</span>
<!-- 	<span class="user">&nbsp;</span>
 -->	<input name="employeeId" id="employeeId" type="text" onblur="checkemployeeId()" value="" class="login-input"/>
	</div>
    <div class="login-info"><span class="span_01">密&nbsp&nbsp码：</span>
<!-- 	<span class="pwd">&nbsp;</span>
 -->	<input name="passwd" id="passwd" type="password" onblur="checkPasswd()" value="" class="login-input"/>
	</div>

	<!-- <div class="login-info"><span  class="span_01">验证码：</span>
	<span class="user">&nbsp;</span>
	<input type="text" name="vcode" type="text" class="login-vcode"  />
	<img style="vertical-align: middle; cursor:pointer;" id="imgVCode" title="看不清验证码？请点击刷新！" alt="vcode" 
		src="hy_vcode.jsp" onclick="javascript:this.src='hy_vcode.jsp?'+(new Date().getTime())">	
	</div> -->
    <!-- <div class="login-oper">
	<input name="" type="checkbox" value="" checked="checked" />记住密码
	</div> -->
    <div class="login-oper">
	<input type="button" value="登&nbsp录" onclick="mainView()" class="login-btn"/>
<!-- 	<input name="" type="submit" value="重 置" class="login-reset"/>
 -->	</div>
    </form>
    </div>

</div>
</body>
</html>