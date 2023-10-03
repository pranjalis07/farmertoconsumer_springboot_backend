package com.F2C.jwt.mongodb.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.F2C.jwt.mongodb.models.CCAdminResponse;
import com.F2C.jwt.mongodb.models.CCToQCReq;
import com.F2C.jwt.mongodb.models.User;
import com.F2C.jwt.mongodb.repository.UserRepository;
//import com.F2C.jwt.mongodb.models.CropDetails;
//import com.F2C.jwt.mongodb.models.User;
//import com.F2C.jwt.mongodb.repository.PaginationRepository;
import com.F2C.jwt.mongodb.services.FarmerService;
import com.F2C.jwt.mongodb.services.QCandAdminService;
//import com.F2C.jwt.mongodb.services.TwilioService;
//
//import io.jsonwebtoken.io.IOException;
//import io.swagger.v3.oas.annotations.parameters.RequestBody;

@CrossOrigin(origins = "http://localhost:3000/*", maxAge = 3600)
@RestController
@RequestMapping("/api/QCAdmin")
public class QCandAdminController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FarmerService farmerService;

	@Autowired
	private QCandAdminService qcAndAdminService;

	// 1
	// cc available free set userId as farmer id //useful and working
	@PostMapping("/setEmptyRequestFieldCCQC/{userId}")
	public ResponseEntity<User> setEmptyRequestFieldCCQC(@PathVariable("userId") String userId) {
		User user = qcAndAdminService.setEmptyRequestFieldCCQC(userId);
		return ResponseEntity.ok(user);
	}

	// 2 View all requests ---> allocated farmer requests list
	//@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/allocated-farmer-requests/{adminUserId}")
	public ResponseEntity<List<CCToQCReq>> viewAllocatedFarmerRequestsForAdmin(@PathVariable String adminUserId) {
		List<CCToQCReq> allocatedRequests = qcAndAdminService.getAllocatedFarmerRequestsForAdmin(adminUserId);
		System.out.print(allocatedRequests);
		
		return ResponseEntity.ok(allocatedRequests);
	}

	// 3 --single farmer view //userId is farmerId 
	@GetMapping("/viewSingleRequest/{userId}/{requestId}")
	public ResponseEntity<CCToQCReq> viewSingleRequest(@PathVariable("userId") String userId,
			@PathVariable("requestId") String requestId) {
		// List<CropDetails> allCropDetails = farmerService.getCropDetailsForFarmers();
		CCToQCReq request = farmerService.viewSingleRequest(userId, requestId);
		//System.out.print("Hi");
		//System.out.print(request);
		return ResponseEntity.ok(request);
	}

	

	// 4
	// to view all quality checkers
	//@PreAuthorize("admin")
	  @CrossOrigin(origins = "http://localhost:3000")
	@GetMapping("/quality-checkers")
	public ResponseEntity<List<User>> getAllQualityCheckers() {
		List<User> qualityCheckers = qcAndAdminService.getAllQualityCheckers();
		
		return ResponseEntity.ok(qualityCheckers);
	}

	// 5
	// to set quality checker as free
	// In response CC available is null
	// @PreAuthorize("QUALITYCHECK")
	@PostMapping("/set-qc-available/{userId}")
	public ResponseEntity<User> setQcAvailable(@PathVariable String userId) {
		User updatedUser = qcAndAdminService.setEmptyRequestFieldQC(userId);
		return ResponseEntity.ok(updatedUser);
	}

	// 6
	// @PreAuthorize("admin")
	@GetMapping("/free-qcs")
	public ResponseEntity<List<User>> findFreeQCsByLocationAndStatus(@RequestParam(name = "location") String location) {
		List<User> freeQCs = qcAndAdminService.findFreeQCsByAddress(location);
		return ResponseEntity.ok(freeQCs);
	}

	// 7
	// asignment of farmer to quality checker
	// @PreAuthorize("admin")
	@PostMapping("/assign-qc-to-farmer/{requestId}/{qcId}")
	public ResponseEntity<CCAdminResponse> assignQCToFarmer(@PathVariable String requestId, @PathVariable String qcId) {
		CCAdminResponse ccAdminResponse = qcAndAdminService.assignQCToFarmer(requestId, qcId);

		if (ccAdminResponse != null) {
			return ResponseEntity.status(HttpStatus.OK).body(ccAdminResponse);
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
	}

	// to view all requests on qc portal
	// 8
	@GetMapping("/view-all-requests/{qcId}")
	public List<CCAdminResponse> viewAllRequests(@PathVariable("qcId") String qcId) {
		// Call the service method to get all requests for the QC
		return qcAndAdminService.getQCDashboardData(qcId);
	}

	//9 View single request on QC  
	@GetMapping("/{requestId}")
	public ResponseEntity<CCToQCReq> viewRequestById(@PathVariable String requestId) {
		CCToQCReq request = qcAndAdminService.viewRequestById(requestId);

		if (request != null) {
			return ResponseEntity.ok(request);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	// 10
	@PostMapping("/approve/{requestId}")
	public ResponseEntity<String> approveRequest(@PathVariable String requestId) {
		boolean approved = qcAndAdminService.approveRequest(requestId);

		if (approved) {
			return ResponseEntity.ok("Request approved successfully.");
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	// 11
	@PostMapping("/changeHandledCCStatus/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public boolean changeHandledCCStatus(@PathVariable String userId, @RequestParam String requestId,
			@RequestParam String status) {
//		boolean status1 = farmerService.changeRequestStatus(userId, status);
		boolean status1 = farmerService.changeHandledCCStatus(userId, requestId, status);
		return status1;
	}

	// 12
	@GetMapping("/currentAllEmployeeStatus")
	public ResponseEntity<List<CCAdminResponse>> currentAllEmployeeStatus() {
		List<CCAdminResponse> responseList = qcAndAdminService.currentAllEmployeeStatus();
		return ResponseEntity.ok(responseList);
	}

	// 13
	@PostMapping("/changeCCAvailable/{ccId}")
	public ResponseEntity<User> changeCCAvailable(@PathVariable("ccId") String ccId, @RequestParam String status) {
		User user = qcAndAdminService.changeCCAvailable(ccId, status);
		return ResponseEntity.ok(user);
	}
	
	//14
	@GetMapping("/availableEmployees")
	// @PreAuthorize("hasRole('FARMER')")
	public ResponseEntity<List<User>> availableEmployees() {
		List<User> user = qcAndAdminService.availableEmployees();
		return ResponseEntity.ok(user);
	}

	// 15
	@PostMapping("/changeRequestCreatedStatus/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public boolean changeRequestStatus(@PathVariable String userId, @RequestParam String status) {
		boolean status1 = farmerService.changeRequestStatus(userId, status);
		return status1;
	}

}

//	

//@Autowired
//private FarmerService farmerService;
//
//@Autowired
//private TwilioService twilioService;
//
//@Autowired
//private PaginationRepository pageRepository;
//
//@PutMapping("/{farmerId}/photo")
//@PreAuthorize("hasRole('FARMER')")
//public ResponseEntity<String> updateFarmerPhoto(@PathVariable("farmerId") String farmerId,
//		@RequestParam MultipartFile file) throws java.io.IOException {
//	try {
//		String imageLink = farmerService.savePhotoAndLinkToFarmer(farmerId, file);
//		return ResponseEntity.ok("Image uploaded and linked to the farmer successfully. Image Link: " + imageLink);
//	} catch (IOException e) {
//		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image.");
//	}
//}
//
//@PostMapping("/otplogin")
//@PreAuthorize("hasRole('FARMER') or hasRole('CONSUMER') or hasRole('ADMIN')")
//public void sendOtpForLogin(@RequestParam String phoneNo) {
//	farmerService.sendOtpForLogin(phoneNo);
//}
//
//@PostMapping("/verify-otp")
//@PreAuthorize("hasRole('FARMER') or hasRole('CONSUMER') or hasRole('ADMIN')")
//public String verifyOtpForLogin(@RequestParam String phoneNo, @RequestParam String otp) {
//	return farmerService.verifyOtpForLogin(phoneNo, otp);
//
//}
//
//@PostMapping("/forgot-password")
////@PreAuthorize("hasRole('FARMER') or hasRole('CONSUMER') or hasRole('ADMIN')")
//public void sendOtpForForgotPassword(@RequestParam String phoneNo) {
//	farmerService.sendOtpForForgotPassword(phoneNo);
//}
//
//@PostMapping("/verify-forgot-password-otp")
////@PreAuthorize("hasRole('FARMER') or hasRole('CONSUMER') or hasRole('ADMIN')")
//public void verifyOtpForForgotPassword(@RequestParam String phoneNo, @RequestParam String newPassword) {
//	farmerService.verifyOtpForForgotPassword(phoneNo, newPassword);
//}
//
//// update farmer by id
//@PutMapping("/updateFarmer/{userId}")
//@PreAuthorize("hasRole('FARMER')")
//public ResponseEntity<User> updateFarmerProfile(@PathVariable("userId") String userId,
//		@RequestParam(required = false) String firstName, @RequestParam(required = false) String lastName,
//		@RequestParam(required = false) String email,@RequestParam(required = false) String address, @RequestParam(required = false) String phoneNo,
//		@RequestParam(required = false) MultipartFile file) throws IOException, java.io.IOException {
//
//	User updatedUser = farmerService.updateFarmerProfile(userId, firstName, lastName, email,address, phoneNo, file);
//	return ResponseEntity.ok(updatedUser);
//}
//
////@PutMapping("/updateFarmer/{farmerId}")
////@PreAuthorize("hasRole('FARMER')")
////public ResponseEntity<User> updateFarmer(@PathVariable String farmerId, @RequestParam String firstName,
////		@RequestParam String lastName,@RequestParam String email, @RequestParam String phoneNo,@RequestParam MultipartFile file) throws java.io.IOException {
////	//User existingFarmer = farmerService.getUserById(farmerId);
////
//////	if (existingFarmer == null) {
//////		return new ResponseEntity<>("Farmer not found", HttpStatus.NOT_FOUND);
//////	}
////
////	User updatedUser = farmerService.updateUser(farmerId, firstName, lastName, phoneNo, email, file);
////	return ResponseEntity.ok(updatedUser);
////}
////// update farmer profile
////@PutMapping("/{farmerId}/photo")
////public ResponseEntity<String> updateFarmerProfile(@PathVariable String userId, @RequestParam MultipartFile file)
////		throws java.io.IOException {
////	try {
////		String imageLink = farmerService.savePhotoAndLinkToFarmer(userId, file);
////		return ResponseEntity.ok("Image uploaded and linked to the farmer successfully. Image Link: " + imageLink);
////	} catch (IOException e) {
////		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image.");
////	}
//// }
//// first time product entry No Need Boy
////@PreAuthorize("hasRole('FARMER')")
////@PutMapping("/updateProduct/{userId}")
////public ResponseEntity<User> updateFarmProduct(@PathVariable String userId, @RequestParam String cropName,
////		@RequestParam String cropSubType, @RequestParam Double cropRetailPrice, @RequestParam Double cropWholesalePrice, @RequestParam Long cropQuantity,
////		@RequestParam String Description, @RequestParam MultipartFile file,@RequestParam String perishable, @RequestParam String status) throws IOException, java.io.IOException {
//////        user.setId(id);
////	User updatedProduct = farmerService.updateProduct(userId, cropName, cropSubType, cropRetailPrice,cropWholesalePrice, Description,
////			cropQuantity, file, perishable, status);
////	return ResponseEntity.ok(updatedProduct);
////}
//
//// next product entry
//
//@PostMapping("/addNewFarmProduct/{farmerId}")
//@PreAuthorize("hasRole('FARMER')")
//public ResponseEntity<User> addNewFarmProduct(@PathVariable("farmerId") String farmerId,
//		@RequestParam String cropName, @RequestParam String cropSubType, @RequestParam Double cropRetailPrice, @RequestParam Double cropWholesalePrice,
//		@RequestParam Long cropQuantity, @RequestParam String Description, @RequestParam MultipartFile[] files,
//		@RequestParam String perishable, @RequestParam String status) throws IOException, java.io.IOException {
//	User farmer = farmerService.addNewProduct(farmerId, cropName, cropSubType, cropRetailPrice,cropWholesalePrice, Description, cropQuantity,
//			files, perishable, status);
//	return ResponseEntity.ok(farmer);
//}
//
//// update product by id
//@PreAuthorize("hasRole('FARMER')")
//@PutMapping("/updateProductById/{farmerId}/{cropId}")
//public ResponseEntity<User> updateFarmProductById(@PathVariable String farmerId, @PathVariable String cropId,
//		@RequestParam String cropName, @RequestParam String cropSubType, @RequestParam String Description,
//		@RequestParam Double cropRetailPrice, @RequestParam Double cropWholesalePrice, @RequestParam long cropQuantity, @RequestParam MultipartFile file,
//		@RequestParam String perishable, @RequestParam String status) throws IOException, java.io.IOException {
////        user.setId(id);
//	User updatedUser = farmerService. updateProductData( farmerId,  cropId,  cropName,  cropSubType,  cropRetailPrice, cropWholesalePrice,  Description,
//			 cropQuantity,  file,  perishable,  status);
//	return ResponseEntity.ok(updatedUser);
//}
//
//// deleter specific cropdetails
//
//@DeleteMapping("/deleteCropDetails/{farmerId}/{cropId}")
//@PreAuthorize("hasRole('FARMER')")
//public ResponseEntity<User> deleteCropDetails(@PathVariable String farmerId, @PathVariable String cropId) {
//	User farmer = farmerService.deleteCropDetails(farmerId, cropId);
//	return ResponseEntity.ok(farmer);
//}
//
//// get specific crop detail for farmer
//@PreAuthorize("hasRole('FARMER')")
//@GetMapping("/getCropDetails/{farmerId}/{cropId}")
//public ResponseEntity<CropDetails> findCropDetails(@PathVariable String farmerId, @PathVariable String cropId) {
//	CropDetails cropDetails = farmerService.findCropDetails(farmerId, cropId);
//	return ResponseEntity.ok(cropDetails);
//}
//
//@PreAuthorize("hasRole('FARMER')")
//@GetMapping("/getCropDetailsFarmer/{farmerId}")
//public ResponseEntity<List<CropDetails>> findCropDetailsFarmer(@PathVariable String farmerId) {
//	List<CropDetails> cropDetails = farmerService.findCropDetailsFarmer(farmerId);
//	return ResponseEntity.ok(cropDetails);
//}
//
//@PreAuthorize("hasRole('FARMER')")
//@GetMapping("/getAllCropDetailsForFarmers")
//public ResponseEntity<List<CropDetails>> getAllCropDetailsForFarmers() {
//	List<CropDetails> allCropDetails = farmerService.getCropDetailsForFarmers();
//	return ResponseEntity.ok(allCropDetails);
//}
////@GetMapping("/crops")
////@PreAuthorize("hasRole('FARMER') or hasRole('CONSUMER') or hasRole('ADMIN')")
////public ResponseEntity<Map<String, Object>> getAllCropPage(
////    @RequestParam(required = false) String cropName,
////    @RequestParam(defaultValue = "0") int page,
////    @RequestParam(defaultValue = "3") int size) {
////
////    try {
//////        List<CropDetails> crops = farmerService.getCropDetailsForFarmers();
//////        Map<String, Object> response = new HashMap<>();
//////      
//////        Pageable paging = PageRequest.of(page, size);
//////
//////        Page<CropDetails> pageCrops;
//////        if (cropName == null)
//////            pageCrops = pageRepository.findAll(paging);
//////        else
//////            pageCrops = pageRepository.findByCropNameContainingIgnoreCase(cropName, paging);
//////
//////        crops = pageCrops.getContent();
//////
//////       
//////        response.put("crops", crops);
//////        response.put("currentPage", pageCrops.getNumber());
//////        response.put("totalItems", pageCrops.getTotalElements());
//////        response.put("totalPages", pageCrops.getTotalPages());
////    
////        return new ResponseEntity<>(response, HttpStatus.OK);
////    } catch (Exception e) {
////        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
////    }}
//
//@GetMapping("/crops")
//@PreAuthorize("hasRole('FARMER') or hasRole('CONSUMER') or hasRole('ADMIN')")
//public ResponseEntity<Map<String, Object>> getCropDetails(
//        @RequestParam(defaultValue = "0") int page,
//        @RequestParam(defaultValue = "4") int size) {
//    Pageable pageable = PageRequest.of(page, size);
//    Map<String, Object> response = new HashMap<>();
//    
//    Page<User> users = farmerService.getUsersWithCropDetailsPaginated(pageable);
//    List<CropDetails> cropDetailsList = users.getContent().stream()
//            .flatMap(user -> user.getCropDetails().stream())
//            .collect(Collectors.toList());
//    
//    response.put("crops", cropDetailsList);
//    response.put("currentPage", users.getNumber());
//    response.put("totalItems", users.getTotalElements());
//    response.put("totalPages", users.getTotalPages());
//    
//    return ResponseEntity.ok(response);
//}
//
////for admin
//@GetMapping("/userinfowithcrop")
//@PreAuthorize("hasRole('FARMER') or hasRole('CONSUMER') or hasRole('ADMIN')")
//public ResponseEntity<Page<User>> getUsersForAdmin(@RequestParam(defaultValue = "0") int page,
//		@RequestParam(defaultValue = "4") int size) {
//	Pageable pageable = PageRequest.of(page, size);
//	//Map<String, Object> response = new HashMap<>();
//	//List<CropDetails> crops = farmerService.getCropDetailsForFarmers();
//	Page<User> users = farmerService.getUsersWithCropDetailsPaginated(pageable);
//	 
//       // return ResponseEntity.ok(users);
//	return ResponseEntity.ok(users);
////	 return new ResponseEntity<>(response, HttpStatus.OK);
////	List<CropDetails> cropDetailsList = users.getContent().stream()
////            .flatMap(user -> user.getCropDetails().stream())
////            .collect(Collectors.toList());
////	 response.put("crops", cropDetailsList);
////        response.put("currentPage", users.getNumber());
////        response.put("totalItems", users.getTotalElements());
////        response.put("totalPages", users.getTotalPages());
////        return ResponseEntity.ok(cropDetailsList);
//       // return new ResponseEntity<>(response, HttpStatus.OK);
//
//}
//
//
//
//
//
//
//
// @GetMapping("/filter/cropname")
// @PreAuthorize("hasRole('FARMER') or hasRole('CONSUMER') or hasRole('ADMIN')")
//  public ResponseEntity<Map<String, Object>> getCropDetails(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "4") int size,
//            @RequestParam(required = false) String cropNameFilter,
//            @RequestParam(required = false) String cropSubTypeFilter,
//            @RequestParam(required = false, defaultValue = "false") boolean combinedFilter,
//            @RequestParam(required = false) Integer minQuantity,
//            @RequestParam(required = false) Integer maxQuantity) {
//        Pageable pageable = PageRequest.of(page, size);
//        Map<String, Object> response = new HashMap<>();
//        
//        Page<User> users = farmerService.getUsersWithCropDetailsPaginated(pageable);
//        
//        List<CropDetails> cropDetailsList = users.getContent().stream()
//                .flatMap(user -> user.getCropDetails().stream())
//                .filter(crop -> (cropNameFilter == null || crop.getCropName().equalsIgnoreCase(cropNameFilter)) &&
//                                (cropSubTypeFilter == null || crop.getCropSubType().equalsIgnoreCase(cropSubTypeFilter)) &&
//                                (!combinedFilter || 
//                                 (cropNameFilter != null && cropSubTypeFilter != null && 
//                                  crop.getCropName().equalsIgnoreCase(cropNameFilter) && 
//                                  crop.getCropSubType().equalsIgnoreCase(cropSubTypeFilter))) &&
//                                (minQuantity == null || crop.getCropQuantity() >= minQuantity) &&
//                                (maxQuantity == null || crop.getCropQuantity() <= maxQuantity))
//                .collect(Collectors.toList());
//        
//        response.put("crops", cropDetailsList);
//        response.put("currentPage", users.getNumber());
//        response.put("totalItems", users.getTotalElements());
//        response.put("totalPages", users.getTotalPages());
//        
//        return ResponseEntity.ok(response);
//    }
// 
// @GetMapping("/search")
// @PreAuthorize("hasRole('FARMER') or hasRole('CONSUMER') or hasRole('ADMIN')")
// public ResponseEntity<Map<String, Object>> searchCrops(
//         @RequestParam(defaultValue = "0") int page,
//         @RequestParam(defaultValue = "4") int size,
//         @RequestParam(required = false) String query) {
//     Pageable pageable = PageRequest.of(page, size);
//     Map<String, Object> response = new HashMap<>();
//     
//     Page<User> users = farmerService.getUsersWithCropDetailsPaginated(pageable);
//     
//     List<CropDetails> cropDetailsList = users.getContent().stream()
//             .flatMap(user -> user.getCropDetails().stream())
//             .filter(crop -> isCropMatchingQuery(crop, query))
//             .collect(Collectors.toList());
//     
//     System.out.println("CropDetails count: " + cropDetailsList.size());
//     
//     response.put("crops", cropDetailsList);
//     response.put("currentPage", users.getNumber());
//     response.put("totalItems", users.getTotalElements());
//     response.put("totalPages", users.getTotalPages());
//     
//     return ResponseEntity.ok(response);
// }
//
// private boolean isCropMatchingQuery(CropDetails crop, String query) {
//	    if (query == null) {
//	        return true; // Include all crops if no query is provided
//	    }
//
//	    String lowercaseQuery = query.trim().toLowerCase();
//
//	    return crop.getCropName().toLowerCase().contains(lowercaseQuery) ||
//	           crop.getCropSubType().toLowerCase().contains(lowercaseQuery);
//	}
