package com.F2C.jwt.mongodb.services;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.F2C.jwt.mongodb.models.CCToQCReq;
import com.F2C.jwt.mongodb.models.CropDetails;
import com.F2C.jwt.mongodb.models.Role;
import com.F2C.jwt.mongodb.models.User;
//import com.mongo.example.collection.Farmer;

@Service
public interface FarmerService {
	
	String sendOtpForLogin(String phoneNo);

	String verifyOtpForLogin(String phoneNo, String otp);

	String sendOtpForForgotPassword(String phoneNo);

	String verifyOtpForForgotPassword(String phoneNo, String newPassword);
	
	 User updateFarmerProfile(String userId, String firstName, String lastName, String email,String address, String phoneNo, MultipartFile file) throws IOException;
		

	String savePhotoAndLinkToFarmer(String farmerId, MultipartFile file) throws IOException;
	User addNewProduct(String userId, String cropName, String cropSubType,Double cropRetailPrice,Double cropWholesalePrice, String Description,
			Long cropQuantity, MultipartFile[] files, String perishable, String status) throws IOException;
	User updateProductData(String userId, String cropId, String cropName, String cropSubType, Double cropRetailPrice,Double cropWholesalePrice, String Description,
			Long cropQuantity, MultipartFile file, String perishable, String status)
			throws IOException;
	
	User deleteCropDetails(String userId, String cropId);
	
	CropDetails findCropDetails(String userId, String cropId);
	
	List<CropDetails> findCropDetailsFarmer(String userId);
	
	
	List<CropDetails> getCropDetailsForFarmers();

	String setEmptyRequestField(String userId,String cropId); 
	  
	
	
	Page<User> getUsersWithCropDetailsPaginated(Pageable pageable);
	  
	  
	User getUserById(String id);
	
	

	
	
	
    boolean changeRequestStatus(String userId,String status);
    List<User> getUsersByRole(Role role);
    boolean changeHandledCCStatus(String userId,String requestId,String status);
	
    
    
    //new 
    Page<User> getUsersWithRoleByName(List<User> allFarmers, Pageable pageable);
    List<User> getAllUsersWithCropDetails();
	
	 

     CCToQCReq viewSingleRequest(String userId,String requestId); 
     List<CCToQCReq> viewRequest(String userId); 
    
 	boolean updatePublishStatus(String farmerId,String cropId);
    
}
//
//User updateProduct(String userId, String cropName, String cropSubType, Double cropRetailPrice,Double cropWholesalePrice, String Description,
//		Long cropQuantity, MultipartFile file, String perishable, String status) throws IOException;


