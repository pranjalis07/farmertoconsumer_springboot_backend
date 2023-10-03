package com.F2C.jwt.mongodb.services.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.F2C.jwt.mongodb.config.NotificationHandler;
import com.F2C.jwt.mongodb.models.CCAdminResponse;
import com.F2C.jwt.mongodb.models.CCToQCReq;
import com.F2C.jwt.mongodb.models.ERole;
import com.F2C.jwt.mongodb.models.Role;
import com.F2C.jwt.mongodb.models.User;
import com.F2C.jwt.mongodb.repository.RoleRepository;
import com.F2C.jwt.mongodb.repository.UserRepository;
import com.F2C.jwt.mongodb.services.QCandAdminService;

@Service
public class QCandAdminServiceImpl implements QCandAdminService {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	RoleRepository roleRepository;
	public User changeCCAvailable(String ccId,String status) {
		Optional<User> list = userRepository.findById(ccId);
		User user =list.get();
		
		user.setCcAvailable(status);
		user = userRepository.save(user);
		return user;
	}

	public List<User> availableEmployees(){
		Optional<Role> adminRole = roleRepository.findByName(ERole.ROLE_ADMIN);
		
	    Role role = adminRole.get();
	
		List<User> list2 = new ArrayList<>();
		List<User> list = userRepository.findByRoles(role);
		
      
       for(User user : list) {
    	   String status = user.getCcAvailable();
    	   if (status != null && status.equals("free")) { // Null check for status
               list2.add(user);
           }
	}
       return list2;
	}
	
	
	public User setEmptyRequestFieldCCQC(String userId) {
		Optional<User> list = userRepository.findById(userId);
		User user = list.get();
		//CCToQCReq request = new CCToQCReq();
		//request.setRequestId(UUID.randomUUID().toString());
		//request.setFarmerId(userId);
		//request.setCropId(cropId);
		//request.setCcAvailable("free");  
		user.setCcAvailable("free");
		//List <CCToQCReq> requestList = new ArrayList();
		//requestList.add(request);
		//user.setRequestList(requestList);
		user = userRepository.save(user);
		return user;
		
	}
	
	
	public User assignCCEmployee(String userId,String requestId) {
		Optional<User> list = userRepository.findById(userId);
		User farmer = list.get();
		CCToQCReq request = farmer.getRequestList()
                .stream()
                .filter(req -> req.getRequestId().equals(requestId))
                .findFirst()
                .orElseThrow(() -> new CustomEntityNotFoundException("Request not found"));
		
		 List<User> availableEmployee = availableEmployees();

		//User admin=null;
		
		if (!availableEmployee.isEmpty()) {
            // Implement round-robin logic
            int currentIndex = -1;
            if (request.getAssignedCCId() != null) {
                // Find the index of the currently assigned employee
                for (int i = 0; i < availableEmployee.size(); i++) {
                    if (availableEmployee.get(i).getId().equals(request.getAssignedCCId())) {
                        
                    	//admin = availableEmployee.get(i);
                    			currentIndex = i;
                    			//admin.setCcAvailable("busy");
                               // request.setHandledCC("processing");
                        break;
                    }
                }
            }
            
            int nextIndex = (currentIndex + 1) % availableEmployee.size();
            User nextEmployee = availableEmployee.get(nextIndex);
            System.out.println(nextEmployee.getCcAvailable());
            
        
            
            nextEmployee.setCcAvailable("busy");
            System.out.println(nextEmployee.getCcAvailable());
            userRepository.save(nextEmployee);
           // request.setHandledCC("processing");
            // Assign the request to the next available employee
            request.setAssignedCCId(nextEmployee.getId());
           request.setHandledCC("processing");
           request.setIsHandledByCC(true);
           
           
           
           //I want to add farmer Name crop name and farmer address to be here 
           //this is to display on Call center View farmer request page. 
           request.setFarmerAddress(farmer.getaddress());  
           request.setFarmerName(farmer.getfirstName()+ " "+ farmer.getlastName());
           request.setFarmerContact(farmer.getphoneNo()); //phone number added 
         
           //request.setCropName(requestId););
           
           // Store the request in the Call Center Admin's allocatedRequests list
           nextEmployee.getAllocatedRequests().add(request);
           
           userRepository.save(farmer);  
            
            //
        
           NotificationHandler.sendNotificationToEmployee(nextEmployee.getId(), request);
           //System.out.println(notification);
		}
		return farmer;
		}
	
	
	
	public List<CCAdminResponse> currentAllEmployeeStatus() {
		 List<CCAdminResponse> responseList = new ArrayList<>();
		    Optional<Role> farmerRole = roleRepository.findByName(ERole.ROLE_FARMER);
		    Role role = farmerRole.get();
		    List<User> list = userRepository.findByRoles(role);

		    for (User user : list) {
		        if (user.getRequestCreated()) {
		            List<CCToQCReq> requestList = user.getRequestList();

		            for (CCToQCReq req : requestList) {
		                if (req.getIsHandledByCC()) {
		                    CCAdminResponse response = new CCAdminResponse(); 
		                    
		                    Optional<User> farmerList = userRepository.findById(req.getFarmerId());  
		                    User farmer = farmerList.get();
		                    String name = farmer.getfirstName() + " " + farmer.getlastName();
		                    response.setFarmerName(name);

		                    Optional<User> ccList1 = userRepository.findById(req.getAssignedCCId());  
		                    User ccAdmin1 = ccList1.get();
		                    response.setCcAvailable(ccAdmin1.getCcAvailable());
		                    String ccname = ccAdmin1.getfirstName() + " " + ccAdmin1.getlastName();
		                    response.setCCEmployeeName(ccname);

		                    response.setHandledCC(req.getHandledCC());
		                    
		                    responseList.add(response);
		                }
		            }
		        }
		    }

		    return responseList;
	}
	
	@Override
	public List<CCToQCReq> getAllocatedFarmerRequestsForAdmin(String adminUserId) {
	    // Retrieve the Call Center Admin user by ID
	    User adminUser = userRepository.findById(adminUserId)
	            .orElseThrow(() -> new CustomEntityNotFoundException("Admin user not found"));

	    // Check if the user has the ROLE_ADMIN role
	    if (adminUser.getRoles().stream().noneMatch(role -> role.getName() == ERole.ROLE_ADMIN)) {
	        throw new CustomEntityNotFoundException("User is not a Call Center Admin");
	    }

	    // Return the allocated requests for the Call Center Admin
	    return adminUser.getAllocatedRequests();
	}
	
	
	// to view all quality checkers on call center admin portal 
	//done by pranjali 
	
	public List<User> getAllQualityCheckers() {
	    // Fetch the ROLE_QUALITYCHECK role from the repository
	    Optional<Role> qualityCheckRole = roleRepository.findByName(ERole.ROLE_QUALITYCHECK);

	    // Check if the role was found
	    if (qualityCheckRole.isPresent()) {
	        // Assuming you have a UserRepository injected
	        return userRepository.findByRoles(qualityCheckRole.get());
	    } else {
	        // Handle the case where the ROLE_QUALITYCHECK role is not found (throw an exception or return an empty list)
	        return Collections.emptyList(); // Return an empty list for now
	    }
	}
	
	
	//to set QC as free  
	@Override
	public User setEmptyRequestFieldQC(String userId) {
	    Optional<User> userOptional = userRepository.findById(userId);
	    if (userOptional.isPresent()) {
	        User user = userOptional.get();
	        user.setQcAvailable("free");
	        return userRepository.save(user);
	    } else {
	        throw new CustomEntityNotFoundException("User not found with ID: " + userId);
	    }
	}
	
	
	//to display qc who are free and match location 
	 @Override
	    public List<User> findFreeQCsByAddress(String address) {
	        // Retrieve all users from the repository
	        List<User> allUsers = userRepository.findAll();

	        // Filter the users to find QCs with matching address and "free" status
	        return allUsers.stream()
	            .filter(user -> user.getaddress().equals(address) && "free".equals(user.getQcAvailable()))
	            .toList(); // You can use .collect(Collectors.toList()) in Java 8
	    }
	 
	 @Override
	 public CCAdminResponse assignQCToFarmer(String requestId, String qcId) {
	     // Retrieve the user (farmer) by requestId
	     Optional<User> userOptional = userRepository.findUserByRequestListRequestId(requestId);

	     if (userOptional.isPresent()) {
	         User farmer = userOptional.get();

	         // Find the request by requestId
	         CCToQCReq requestToUpdate = farmer.getRequestList().stream()
	                 .filter(req -> req.getRequestId().equals(requestId))
	                 .findFirst()
	                 .orElse(null);
	         System.out.print("Hiii");

	         System.out.print(requestToUpdate);

	         if (requestToUpdate != null) {
	             // Update the assigned QC ID, handledQC, and isHandledByQC for the specific request
	             requestToUpdate.setAssignedQCId(qcId);
	             requestToUpdate.setHandledQC("processing");
	             requestToUpdate.setIsHandledByQC(true);

	             // Check if the farmer's assigned CC ID is not null
	             if (requestToUpdate.getAssignedCCId() != null) {
	                 // Retrieve the CC admin by ID
	                 Optional<User> ccAdminOptional = userRepository.findById(requestToUpdate.getAssignedCCId());

	                 if (ccAdminOptional.isPresent()) {
	                     User ccAdmin = ccAdminOptional.get();

	                     // Create a CCAdminResponse object and populate it with data
	                     CCAdminResponse ccAdminResponse = new CCAdminResponse();
	                     
	                     // Set reqForQCCC to the requestId
	                     ccAdminResponse.setReqForQCCC(requestId);
	                     ccAdminResponse.setFarmerName(farmer.getfirstName() + " " + farmer.getlastName());
	                     ccAdminResponse.setCCEmployeeName(ccAdmin.getfirstName() + " " + ccAdmin.getlastName());

	                     // Fetch QC name based on qcId (You need to implement this logic)
	                     String qcName = fetchQCName(qcId); // Replace with actual logic

	                     ccAdminResponse.setQCAssignedName(qcName);
	                     ccAdminResponse.setHandledCC("processing");
	                     ccAdminResponse.setCcAvailable("busy");
	                     ccAdminResponse.setHandledQC("processing");
	                     ccAdminResponse.setQcAvailable("busy");

	                     // Add the CCAdminResponse to the CC admin's list
	                     ccAdmin.getCcAdminResponses().add(ccAdminResponse);
	                     
	                     ///extra 
	                  // Rebuild the requestList for the farmer with the updated request
	                     List<CCToQCReq> updatedRequestList = farmer.getRequestList().stream()
	                             .map(req -> req.getRequestId().equals(requestId) ? requestToUpdate : req)
	                             .collect(Collectors.toList());

	                     // Update the farmer's requestList
	                     farmer.setRequestList(updatedRequestList);

	                     // Save the updated CC admin user object and farmer user object
	                     userRepository.save(ccAdmin);
	                     userRepository.save(farmer);
	                     
	                 
	                     
	                     // Update QC available status in QC collection
	                     Optional<User> qcOptional = userRepository.findById(qcId);
	                     if (qcOptional.isPresent()) {
	                         User qcUser = qcOptional.get();
	                         qcUser.setQcAvailable("busy");
	                         // Add the CCAdminResponse to the QC user's list
	                         qcUser.getCcAdminResponses().add(ccAdminResponse);
	                         userRepository.save(qcUser);
	                     }

	                     return ccAdminResponse; // Return the CCAdminResponse
	                 }
	             }

	             // Save the updated user (farmer) with the modified request
	           //  userRepository.save(farmer);
	         }
	     }

	     return null; // Return null if the operation was not successful
	 }

	
	 
	 
	 //for fetch name 
	 private String fetchQCName(String qcId) {
		    // Implement logic to fetch QC name based on qcId
		    Optional<User> qcUserOptional = userRepository.findById(qcId);

		    if (qcUserOptional.isPresent()) {
		        User qcUser = qcUserOptional.get();
		        String qcName = qcUser.getfirstName() + " " + qcUser.getlastName(); // Assuming firstName and lastName are QC's name fields
		        return qcName;
		    } else {
		        // Handle the case where QC with qcId is not found
		        return "QC Name Not Found";
		    }
		}
	 
	 
	    public List<CCAdminResponse> getQCDashboardData(String qcID) {
	        // Fetch the QC user from the repository (adjust this based on your criteria)
	        User qcUser = userRepository.findById( qcID).orElse(null);

	        if (qcUser != null) {
	            // Extract CCAdminResponse list from the QC user
	            List<CCAdminResponse> ccAdminResponses = qcUser.getCcAdminResponses();

	            return ccAdminResponses;
	        } else {
	            return null;
	        }
	    }
	    
	    

	    @Override
	    public CCToQCReq viewRequestById(String requestId) {
	        Optional<User> userOptional = userRepository.findUserByRequestListRequestId(requestId);

	        if (userOptional.isPresent()) {
	            User farmer = userOptional.get();

	            // Find the request by requestId
	            return farmer.getRequestList().stream()
	                .filter(req -> req.getRequestId().equals(requestId))
	                .findFirst()
	                .orElse(null);
	        }

	        return null; // Return null if the request is not found
	    }
	  
	    //this is working fine for approval but changes are only reflecting it in CCtoQCreq that is in farmers collection. I want to 
	    //make changes in CCADmin Response dashboard 
	    //approve farmers request BY QC Id 
	    @Override
	    public boolean approveRequest(String requestId) {
	    	
	        // Retrieve the user (farmer) by requestId
	        Optional<User> userOptional = userRepository.findUserByRequestListRequestId(requestId);
	        
	        

	        if (userOptional.isPresent()) {
	            User farmer = userOptional.get();

	            // Find the request by requestId //this will make changes in Farmer side 
	            
	            Optional<CCToQCReq> requestOptional = farmer.getRequestList().stream()
	                .filter(req -> req.getRequestId().equals(requestId))
	                .findFirst();

	            if (requestOptional.isPresent()) {
	                CCToQCReq requestToUpdate = requestOptional.get();
	                System.out.print(requestToUpdate);
System.out.print(requestToUpdate.getIsHandledByQC());
	                // Update the status or any other fields as needed
	                requestToUpdate.setHandledQC("approved");
	                requestToUpdate.setIsHandledByQC(true);

	                // Save the updated farmer object
	                userRepository.save(farmer);
	                
	              

	                return true; // Request approved successfully
	            }
	        }

	        return false; // Request not found or approval failed
	    }
		
		
	    
	 /*   //approve farmers request BY QC  // for reflecting changes in both farmer and qc side // updated api by pranjali  
	 // Updated approveRequest method
	    @Override
	    public boolean approveRequest(String requestId) {
	        // Retrieve the user (farmer) by requestId
	        Optional<User> userOptional = userRepository.findUserByRequestListRequestId(requestId);

	        // Retrieve the QC user by reqForQCCC
	        Optional<User> qcUserOptional = userRepository.findUserByCcAdminResponsesReqForQCCC(requestId);

	        if (userOptional.isPresent() && qcUserOptional.isPresent()) {
	            User farmer = userOptional.get();
	            User qcUser = qcUserOptional.get();

	            // Find the request(s) by requestId in the farmer's list
	            List<CCToQCReq> requestsToUpdate = farmer.getRequestList().stream()
	                .filter(req -> req.getRequestId().equals(requestId))
	                .collect(Collectors.toList());

	            // Update the status or any other fields as needed for all matching requests
	            requestsToUpdate.forEach(request -> {
	                request.setHandledQC("approved");
	                request.setIsHandledByQC(true);
	            });

	            // Save the updated farmer object
	            userRepository.save(farmer);

	            // Find the matching CCAdminResponses in the QC user's list
	            List<CCAdminResponse> ccAdminResponsesToUpdate = qcUser.getCcAdminResponses().stream()
	                .filter(ccAdminResponse -> ccAdminResponse.getReqForQCCC().equals(requestId))
	                .collect(Collectors.toList());

	            // Update the CCAdminResponses for the QC user
	            ccAdminResponsesToUpdate.forEach(ccAdminResponse -> {
	                ccAdminResponse.setHandledQC("approved");
	            });

	            // Save the updated QC user object
	            userRepository.save(qcUser);

	            return true;
	        } else {
	            // Handle the case where either the farmer or QC user is not found
	            // You can log an error or perform other error handling actions here
	            // For example, you can log an error message:
	          //logger("Failed to approve request with ID: {}. Either farmer or QC user not found.", requestId);
	        	System.out.print("error");

	            // You can also throw a custom exception, return an error code, or perform other actions as needed
	            return false; // Request not found or approval failed
	        }
	    }
	    */
	/* working fine with farmer id  
	 @Override
	 public CCAdminResponse assignQCToFarmer(String farmerId, String qcId) {
	     // Retrieve the user (farmer) by ID
	     Optional<User> userOptional = userRepository.findById(farmerId);

	     if (userOptional.isPresent()) {
	         User farmer = userOptional.get();

	         // Update the CCToQCReq object for the farmer (you might need additional logic here)
	         List<CCToQCReq> requestList = farmer.getRequestList();

	         for (CCToQCReq req : requestList) {
	             if (req.getFarmerId().equals(farmerId)) {
	                 // Update the assigned QC ID, handledQC, and isHandledByQC
	                 req.setAssignedQCId(qcId);
	                 req.setHandledQC("processing");
	                 req.setIsHandledByQC(true);

	                 // Add the request to the allocatedRequests list
	                 farmer.getAllocatedRequests().add(req);

	                 // Check if the farmer's assigned CC ID is not null
	                 if (req.getAssignedCCId() != null) {
	                     // Retrieve the CC admin by ID
	                     Optional<User> ccAdminOptional = userRepository.findById(req.getAssignedCCId());

	                     if (ccAdminOptional.isPresent()) {
	                         User ccAdmin = ccAdminOptional.get();

	                         // Create a CCAdminResponse object and populate it with data
	                         CCAdminResponse ccAdminResponse = new CCAdminResponse();
	                         ccAdminResponse.setFarmerName(farmer.getfirstName() + " " + farmer.getlastName());
	                         ccAdminResponse.setCCEmployeeName(ccAdmin.getfirstName() + " " + ccAdmin.getlastName());
	                         ccAdminResponse.setQCAssignedName("QC Name Here"); // Replace with actual QC data
	                         ccAdminResponse.setHandledCC("processing");
	                         ccAdminResponse.setCcAvailable("busy");

	                         // Add the CCAdminResponse to the CC admin's list
	                         ccAdmin.getCcAdminResponses().add(ccAdminResponse);

	                         // Save the updated CC admin user object
	                         userRepository.save(ccAdmin);

	                         return ccAdminResponse; // Return the CCAdminResponse
	                     }
	                 }
	             }
	         }

	         // Save the updated user (farmer)
	         userRepository.save(farmer);
	     }

	     return null; // Return null if the operation was not successful
	 } */
}
