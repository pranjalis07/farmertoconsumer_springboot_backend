package com.F2C.jwt.mongodb.models;

import lombok.Data;

@Data
public class CCAdminResponse {
	// working fine 
	//future -- you can use single response class 
	// this one is for ui CCToQC is database 
	//request id 
	String reqForQCCC; // this is for request id 
	String farmerName;
	String CCEmployeeName;
	String QCAssignedName;
	
	String handledCC;
	String ccAvailable;
	
	
	String handledQC;
	String qcAvailable;
	
	
}
