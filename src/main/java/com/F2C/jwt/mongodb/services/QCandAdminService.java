package com.F2C.jwt.mongodb.services;

import java.util.List;
//import java.util.Optional;

import org.springframework.stereotype.Service;

import com.F2C.jwt.mongodb.models.CCAdminResponse;
import com.F2C.jwt.mongodb.models.CCToQCReq;
import com.F2C.jwt.mongodb.models.User;

@Service
public interface QCandAdminService {
   User changeCCAvailable(String ccId,String status);
   List<User> availableEmployees();
   User setEmptyRequestFieldCCQC(String userId);
   User assignCCEmployee(String userId,String requestId);
   List<CCAdminResponse> currentAllEmployeeStatus();
   public List<CCToQCReq> getAllocatedFarmerRequestsForAdmin(String adminUserId);
   
   
   //show all qc 
   public List<User> getAllQualityCheckers();
   
   //set QC available 
   User setEmptyRequestFieldQC(String userId);
   
   //to retrieve Quality Checkers by location and qcAvailable status.
   public List<User> findFreeQCsByAddress(String address);
   
   //to assign QC to farmer and update data in farmers request 
   public CCAdminResponse assignQCToFarmer(String requestId, String qcId);
   
   //to view request on QC portal 
   List<CCAdminResponse> getQCDashboardData(String qcID);
   
   public CCToQCReq viewRequestById(String requestId);
   
   //to approve the farmers request on QC portal 
   boolean approveRequest(String requestId);
   
   
   
}
