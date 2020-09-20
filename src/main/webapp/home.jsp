<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Spring Boot Demo App</title>
</head>
<body>

	<H1>Home Page</H1>
	<H2>View Profile</H2>
	<form action="viewProfile">
	<input type="Submit" ><br>
	<H3>User Details Below:</H3> 
	User Name : ${user.userName}<br>
	User Role :  ${user.userRole}<br>
	User Last Name :  ${user.lastName}<br>
	User First Name :  ${user.firstName}
	</form>
	
	<br><br>
	
	<H2>Retrieve Auth Token </H2>
	<form action="requestUserAuthToken">
	<input type="Submit" ><br>
	<H3>User Details Form : </H3> 
	Result Auth Token : <label value="${USERTOKEN}"></label><br>
	</form>
	
	<br><br>
	
	<H2>Reset Password</H2>
	<form action="updPassword">
	<input type="Submit" ><br>
	<H3>Password Reset Form Below : </H3> 
	Enter Current Password : <input type="text" name="currPwd" ><br>
	Enter New Password : <input type="text" name="newPwd" ><br>
	</form>
	
	<br><br>
	<div>
	<sec:authorize access="${user.userRole == 'SUPERUSER'}">
	<H2>Create User</H2>
	<form action="createUser">
	<input type="Submit" ><br>
	<H3>User Details Form : </H3> 
	Enter User Name : <input type="text" name="userName"><br>
	Enter User Password : <input type="text" name="userPassword" ><br>
	Enter User Last Name : <input type="text" name="lastName" ><br>
	Enter User First Name : <input type="text" name="firstName" ><br>
	Select User Role : <input type="text" name="userRole" ><br>
	User Details Status : ${result}<br>
	</form>
	
	<br><br>
	</sec:authorize>
	</div>
	<H2>Logout</H2>
	<form action="logout">
	<input type="Submit"><br>
	</form>
	<br><br>
	
	</body>
</html>