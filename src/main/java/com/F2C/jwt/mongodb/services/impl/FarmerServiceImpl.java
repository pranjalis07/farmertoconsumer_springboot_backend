package com.F2C.jwt.mongodb.services.impl;


import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.F2C.jwt.mongodb.models.CCToQCReq;
import com.F2C.jwt.mongodb.models.CropDetails;
import com.F2C.jwt.mongodb.models.ERole;
import com.F2C.jwt.mongodb.models.Images;
import com.F2C.jwt.mongodb.models.Role;
import com.F2C.jwt.mongodb.models.User;
import com.F2C.jwt.mongodb.repository.ImageRepository;
import com.F2C.jwt.mongodb.repository.PaginationRepository;
import com.F2C.jwt.mongodb.repository.RoleRepository;
import com.F2C.jwt.mongodb.repository.UserRepository;
import com.F2C.jwt.mongodb.services.FarmerService;
import com.F2C.jwt.mongodb.services.QCandAdminService;
import com.F2C.jwt.mongodb.services.TwilioService;
//import com.mongo.example.Exception.CustomEntityNotFoundException;

@Service
public class FarmerServiceImpl implements FarmerService {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ImageRepository imageRepository;
	@Autowired
	private PaginationRepository pageRepository;
	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private TwilioService twilioService;
	
	@Autowired
	private QCandAdminService qcandAdminService;

//	@Autowired
//	private FarmerService userService;

	@Override
	public User getUserById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	private String generateOtp() {
		return new DecimalFormat("000000").format(new Random().nextInt(999999));
		// ...
	}
	
	//userId is farmerID //taken from srushti
	@Override //not working 
	public CCToQCReq viewSingleRequest(String userId,String requestId) {
		 Optional<User> userOptional = userRepository.findById(userId);
		//User  userOptional = userRepository.findById(userId).orElse(null);
		       System.out.println( requestId);
	
		       if (userOptional.isPresent()) {
		    	         User user = userOptional.get();
		    	         System.out.println("user"+user);
		    	         
		    	         for (CCToQCReq request : user.getRequestList()) {
		    	        	 System.out.print(request.getRequestId());
		    	        	         if (requestId.equals(request.getRequestId())) {
		    	        	
		    	        	  System.out.println( requestId);
		
		    	        	  request.setRequestId(requestId);
		    	        	   request.setFarmerId(request.getFarmerId()); // Set the farmerId from the database
		    	        	  request.setFarmerName(user.getfirstName()+" "+user.getlastName());
		    	        	 request.setFarmerAddress(user.getaddress());
		    	        	  request.setFarmerContact(user.getphoneNo());
		    	        	  request.setFarmerEmail(user.getEmail());
		    	        	  
		    	        	                  request.setCropId(request.getCropId()); // Set the cropId from the database
		    	        	                // request.setFarmerAddress(request.getFarmerAddress()); 
		    	        	                 System.out.println("in loop");
		    	        	                 return request;
		    	        	         }
		    	         }
		       }
		       return null;
	}
	
	public  boolean changeHandledCCStatus(String userId,String requestId,String status) {
		Optional<User> list = userRepository.findById(userId);
		User user = list.get();
		CCToQCReq request = new CCToQCReq();
		for (CCToQCReq request1 : user.getRequestList()) {
			if (requestId.equals(request1.getRequestId())) {
					
				//request = request1;	
				if(status.equals("true")) {
			      request1.setIsHandledByCC(true); 
				}else {
					request1.setIsHandledByCC(false);
				}
				//List<CCToQCReq> list1= user.getRequestList();
				//list1.remove(request1);
				//list1.add(request);
				//user.setRequestList(list1);
				userRepository.save(user);
				}
			}
		return request.getIsHandledByCC();
	}
	public List<CCToQCReq> viewRequest(String userId){
		Optional<User> list = userRepository.findById(userId);
		User user = list.get();
		List <CCToQCReq> requestList = user.getRequestList();
		return requestList;
	}
	public boolean changeRequestStatus(String userId,String status) {
		Optional<User> list = userRepository.findById(userId);
		User user = list.get();
		if(status.equals("true")) 
		  user.setRequestCreated(true);
		else
			user.setRequestCreated(false);
		
		
		userRepository.save(user);
		boolean updatedStatus = user.getRequestCreated();
		return updatedStatus ;
	}
	
	public  String  setEmptyRequestField(String userId,String cropId) {
		Optional<User> list = userRepository.findById(userId);
		 
		User user = list.get();
		CCToQCReq request = new CCToQCReq();
		request.setRequestId(UUID.randomUUID().toString());
		
		String reqId=request.getRequestId();
		
		request.setFarmerId(userId);
		request.setFarmerName(user.getfirstName()+" "+user.getlastName());
		request.setFarmerAddress(user.getaddress());
		System.out.println("Farmer Address"+request.getFarmerAddress());
		request.setFarmerContact(user.getphoneNo());
		request.setCropId(cropId);
		List<CropDetails> cropdetailslist = user.getCropDetails();
		for(CropDetails crop:cropdetailslist) {
			if(crop.getCropId().equals(cropId)) {
				request.setCropName(crop.getCropName());
			}
		}
		List<CCToQCReq> requestList = user.getRequestList();
		if(requestList.isEmpty()) {
		List <CCToQCReq> newRequestList = new ArrayList();
		newRequestList.add(request);
		user.setRequestList(newRequestList);
		user = userRepository.save(user);
		 }
	else {
		requestList.add(request);
		 user.setRequestList(requestList);
		  user = userRepository.save(user);
		 }
		 User user1=qcandAdminService.assignCCEmployee(userId,reqId);
		 return reqId;
		
	}
	
	
	
	
	@Override
	public String sendOtpForLogin(String phoneNo) {
		Optional<User> userOptional = userRepository.findByPhoneNo(phoneNo);
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			String otp = generateOtp();
			twilioService.sendOtp(phoneNo, otp);
			user.setOtp(otp);
			userRepository.save(user);
			return otp;
		} else {
			throw new RuntimeException("Phone number not found");
		}
	}

	@Override
	public String verifyOtpForLogin(String phoneNo, String otp) {
		Optional<User> userOptional = userRepository.findByPhoneNo(phoneNo);
		if (userOptional.isPresent()) {
			User user = userOptional.get();
		
			if (otp.equals(user.getOtp())) {
				user.setOtp(null);
				userRepository.save(user);
				return "Otp Valid";
			} else {
				throw new RuntimeException("Invalid OTP");
			}
		} else {
			throw new RuntimeException("Phone number not found");
		}
	}

	@Override
	public String sendOtpForForgotPassword(String phoneNo) {
		Optional<User> userOptional = userRepository.findByPhoneNo(phoneNo);
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			String otp = generateOtp();
			twilioService.sendOtp(phoneNo, otp);
			user.setOtp(otp);
			userRepository.save(user);
			return otp;
		} else {
			throw new RuntimeException("Phone number not found");
		}
	}

	@Override
	public String verifyOtpForForgotPassword(String phoneNo, String newPassword) {
		Optional<User> userOptional = userRepository.findByPhoneNo(phoneNo);
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			user.setPassword(newPassword);
			userRepository.save(user);
			return user.getPassword();
		} else {
			throw new RuntimeException("ResetPass Failed");
		}
	}

	@Override
	public String savePhotoAndLinkToFarmer(String id, MultipartFile file) throws IOException {
		Optional<User> userOptional = userRepository.findById(id);
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			Images image = new Images();
			image.setImage(file.getBytes());
			image = imageRepository.save(image);
			user.setImageId(image.getId());
			userRepository.save(user);
			return "/photos/" + user.getImageId();
		} else {
			throw new CustomEntityNotFoundException("Farmer with ID " + id + " not found.");
		}
	}
//	@Override
//	public User updateUser(String userId, String firstName, String lastName, String phoneNo, String email,
//			MultipartFile file) throws IOException {
//		Optional<User> farm = userRepository.findById(userId);
//		User farmer1 = farm.get();
//		Images image = new Images();
//		if (!firstName.isEmpty()) {
//			farmer1.setfirstName(firstName);
//		} else {
//			farmer1.setfirstName(farmer1.getfirstName());
//		}
//
//		if (!lastName.isEmpty()) {
//			farmer1.setlastName(lastName);
//		} else {
//			farmer1.setlastName(farmer1.getlastName());
//		}
////
////		if (!addresses.isEmpty()) {
////			farmer1.setAddresses(addresses);
////		} else {
////			farmer1.setAddresses(farmer1.getAddresses());
////		}
////		if (!aadharNo.isEmpty()) {
////			farmer1.setAadharNo(aadharNo);
////		} else {
////			farmer1.setAadharNo(farmer1.getAadharNo());
////		}
//
//		if (!email.isEmpty()) {
//			farmer1.setEmail(email);
//		} else {
//			farmer1.setEmail(farmer1.getEmail());
//		}
//
//		// farmer1.setPhoneNo(phoneNo);
//
//		image.setImage(file.getBytes());
//		image = imageRepository.save(image);
//
//		farmer1.setImageId(image.getId());
//		userRepository.save(farmer1);
//		return farmer1;
//	}

	// updateProduct is when farmer first time add crop detail



	// when farmer wants to edit crop details

	@Override
	public User updateProductData(String userId, String cropId, String cropName, String cropSubType, Double cropRetailPrice,Double cropWholesalePrice, String Description,
			Long cropQuantity, MultipartFile file, String perishable, String status)
			throws IOException {
		Optional<User> farm = userRepository.findById(userId);
		User farmer1 = farm.get();

		Images img = new Images();
		List<CropDetails> list = farmer1.getCropDetails();
		CropDetails cropDetails = new CropDetails();
		for (CropDetails crops : farmer1.getCropDetails()) {
			if (cropId.equals(crops.getCropId())) {
//    		System.out.println(" Farmer Details ");
//    		System.out.println(crops.getCropId());
//        	System.out.println(crops.getCropName());
//        	System.out.println(crops.getCropPrice());
//        	System.out.println(crops.getCropSubType());
//        	System.out.println(crops.getCropQuantity());
//        	System.out.println("\n");
			}
		}
		for (CropDetails crop : farmer1.getCropDetails()) {
			if (cropId.equals(crop.getCropId())) {
				cropDetails = crop;
				if (!cropName.isEmpty()) {
					cropDetails.setCropName(cropName);
				} else {
					cropDetails.setCropName(crop.getCropName());
				}

				if (cropRetailPrice != null && cropRetailPrice != 0) {
					cropDetails.setCropRetailPrice(cropRetailPrice);
					// Set it to null if it's empty
				} else {
					cropDetails.setCropRetailPrice(crop.getCropRetailPrice());
				}
				if (cropWholesalePrice != null && cropWholesalePrice != 0) {
					cropDetails.setCropRetailPrice(cropWholesalePrice);
					// Set it to null if it's empty
				} else {
					cropDetails.setCropWholesalePrice(crop.getCropWholesalePrice());
				}

				if (cropQuantity != null) {
					cropDetails.setCropQuantity(cropQuantity);
				} else {
					cropDetails.setCropQuantity(crop.getCropQuantity());

				}

				if (!cropSubType.isEmpty()) {
					cropDetails.setCropSubType(cropSubType);
				} else {
					cropDetails.setCropSubType(crop.getCropSubType());
				}
				if(status.equals("true")) {
					 boolean productStatus = true;	
					 cropDetails.setApprovalStatus(productStatus);
					}
					else {
						 boolean productStatus = false;	
						 cropDetails.setApprovalStatus(productStatus);
					}
					    if(perishable.equals("true")) {
						 boolean productPerish = true;	
						 cropDetails.setApprovalStatus(productPerish);
						}
						else {
							 boolean productPerish = false;	
							 cropDetails.setApprovalStatus(productPerish);
						}
				img.setImage(file.getBytes());
				img = imageRepository.save(img);

				List<String> list2 = new ArrayList<String>();
				list2.add(img.getId());
				cropDetails.setImageIds(list2);

				farmer1 = userRepository.save(farmer1);
				return farmer1;
			}
		}

		return farmer1;
	}
	//publish status 
	  public boolean updatePublishStatus(String farmerId,String cropId) {
		   //boolean published=false;
		   Optional<User> farm = userRepository.findById(farmerId);
			User farmer1 = farm.get();
			//List<CropDetails> list = farmer1.getCropDetails();
			CropDetails cropDetails = new CropDetails();
			for (CropDetails crop : farmer1.getCropDetails()) {
				if (cropId.equals(crop.getCropId())) {
					
					cropDetails = crop;
					cropDetails.setPublished(true);
					System.out.println(cropDetails.getPublished());
					userRepository.save(farmer1);
					return true;
				}
			}
			
			
		   return false;
	   }
	// when farmer want to add another crop
	@Override
	public User addNewProduct(String userId, String cropName, String cropSubType, Double cropRetailPrice,Double cropWholesalePrice, String Description,
			Long cropQuantity, MultipartFile[] files, String perishable, String status) throws IOException {
		Optional<User> farm = userRepository.findById(userId);
		User farmer1 = farm.get();
		CropDetails cropDetails = new CropDetails();

		List<CropDetails> list = farmer1.getCropDetails();
		List<String> imageIds = new ArrayList<>();
		if (list == null) {
			// Initialize the list if it's null
			list = new ArrayList<>();
			farmer1.setCropDetails(list);
		}
		if (list.isEmpty()) {
			cropDetails.setCropId(UUID.randomUUID().toString());
			cropDetails.setCropName(cropName);
			cropDetails.setCropQuantity(cropQuantity);
			cropDetails.setDescription(Description);
		
			cropDetails.setCropRetailPrice(cropRetailPrice);
			cropDetails.setCropWholesalePrice(cropWholesalePrice);
			cropDetails.setCropSubType(cropSubType);	
		
			for (MultipartFile file : files) {
				try {
					Images img = new Images();
					img.setImage(file.getBytes());
					img = imageRepository.save(img);
					imageIds.add(img.getId());

				} catch (IOException e) {

					e.printStackTrace();
				}
			}
			cropDetails.setImageIds(imageIds);
			list.add(cropDetails);
			farmer1.setCropDetails(list);

		} else {
			cropDetails.setCropId(UUID.randomUUID().toString());
			cropDetails.setCropName(cropName);
			cropDetails.setCropRetailPrice(cropRetailPrice);
			cropDetails.setCropWholesalePrice(cropWholesalePrice);
			cropDetails.setCropSubType(cropSubType);
			cropDetails.setCropQuantity(cropQuantity);
			cropDetails.setDescription(Description);
			for (MultipartFile file : files) {
				try {
					Images img = new Images();
					img.setImage(file.getBytes());
					img = imageRepository.save(img);
					imageIds.add(img.getId());

				} catch (IOException e) {

					e.printStackTrace();
				}
			}
			
			if(status.equals("true")) {
				 boolean productStatus = true;	
				 cropDetails.setApprovalStatus(productStatus);
				}
				else {
					 boolean productStatus = false;	
					 cropDetails.setApprovalStatus(productStatus);
				}
				    if(perishable.equals("true")) {
					 boolean productPerish = true;	
					 cropDetails.setApprovalStatus(productPerish);
					}
					else {
						 boolean productPerish = false;	
						 cropDetails.setApprovalStatus(productPerish);
					}
			cropDetails.setImageIds(imageIds);
			list.add(cropDetails);
			farmer1.setCropDetails(list);
		}
		farmer1 = userRepository.save(farmer1);
		System.out.println(farmer1.getCropDetails());
		return farmer1;
	}

	
	@Override
	public List<CropDetails> findCropDetailsFarmer(String userId){
		Optional<User> farm = userRepository.findById(userId);
		User farmer1 = farm.get();
		List<CropDetails> list = farmer1.getCropDetails();
		return list;
	}
	
	
	// find crop detail by id
	@Override
	public CropDetails findCropDetails(String userId, String cropId) {
		Optional<User> farm = userRepository.findById(userId);
		User farmer1 = farm.get();

		CropDetails cropDetails = new CropDetails();
		List<CropDetails> list = farmer1.getCropDetails();
		for (CropDetails crops : farmer1.getCropDetails()) {
			if (cropId.equals(crops.getCropId())) {
//    		System.out.println(" Farmer Details ");
//    		System.out.println(crops.getCropId());
//        	System.out.println(crops.getCropName());
//        	System.out.println(crops.getCropPrice());
    	System.out.println(crops.getCropSubType());
//        	System.out.println(crops.getCropQuantity());
				// cropDetails.setCropId(cropId);
				cropDetails.setCropName(crops.getCropName());
				cropDetails.setCropSubType(crops.getCropSubType());
				cropDetails.setCropRetailPrice(crops.getCropRetailPrice());
				cropDetails.setCropWholesalePrice(crops.getCropWholesalePrice());
				cropDetails.setCropQuantity(crops.getCropQuantity());
				
				cropDetails.setImageIds(crops.getImageIds());
				System.out.println("\n");
				return cropDetails;
			}
		}

		return cropDetails;
	}

	// delete the specific crop
	@Override
	public User deleteCropDetails(String userId, String cropId) {
		Optional<User> farm = userRepository.findById(userId);
		User farmer1 = farm.get();
		farmer1.getCropDetails().removeIf(cropDetails -> cropDetails.getCropId().equals(cropId));
		farmer1 = userRepository.save(farmer1);

		return farmer1;
	}

	@Override
	public User updateFarmerProfile(String userId, String firstName, String lastName, String email,String address, String phoneNo,
			MultipartFile file) throws IOException {
		Optional<User> optionalFarmer = userRepository.findById(userId);
		if (!optionalFarmer.isPresent()) {
			throw new CustomEntityNotFoundException("Farmer with ID " + userId + " not found.");
		}

		User farmer = optionalFarmer.get();
        System.out.println(farmer.getfirstName()+" "+farmer.getlastName()+" "+farmer.getEmail()+" "+farmer.getaddress()+" "+farmer.getImageId()+" "+farmer.getphoneNo()+" ");
		if (firstName != null && !firstName.isEmpty()) {
			farmer.setfirstName(firstName);
		}

		if (lastName != null && !lastName.isEmpty()) {
			farmer.setlastName(lastName);
		}

		if (email != null && !email.isEmpty()) {
			farmer.setEmail(email);
		}

		if (address != null && !address.isEmpty()) {
			farmer.setaddress(address);
		}
		if (phoneNo != null && !phoneNo.isEmpty()) {
			farmer.setphoneNo(phoneNo);
		}

		//if (file != null) {
//			Images image = new Images();
//			image.setImage(file.getBytes());
//			image = imageRepository.save(image);
//			if (farmer.getImageId() != null) {
//	            imageRepository.deleteById(farmer.getImageId());
//	        }
//			farmer.setImageId(image.getId());
		//}
		if (file != null && !file.isEmpty()) {
	        Images image = new Images();
	        image.setImage(file.getBytes());
	        image = imageRepository.save(image);
	        System.out.println(image.getId());
	        String imageId = image.getId();
	        farmer.setImageId(imageId);
	    }
		return userRepository.save(farmer);
	}

	@Override
	public List<User> getUsersByRole(Role role) {
		return userRepository.findByRoles(role);
	}

	@Override
	public List<CropDetails> getCropDetailsForFarmers() {
		List<CropDetails> allCropDetails = new ArrayList<>();

		Optional<Role> farmerRole = roleRepository.findByName(ERole.ROLE_FARMER);
		if (farmerRole.isPresent()) {
			List<User> farmers = getUsersByRole(farmerRole.get());
			for (User farmer : farmers) {
				List<CropDetails> cropDetails = farmer.getCropDetails();
				if(cropDetails !=null) {
//				System.out.println(cropDetails);
//				System.out.println(" ");
				allCropDetails.addAll(cropDetails);
				}
			}
		}

		return allCropDetails;
	}
	
	
	 public Page<User> getUsersWithCropDetailsPaginated(Pageable pageable) {
	        return pageRepository.findAll(pageable);
	    }
	 
	 public Page<User> getUsersWithRoleByName(List<User> allFarmers, Pageable pageable) {
	        int pageSize = pageable.getPageSize();
	        int currentPage = pageable.getPageNumber();
	        int startItem = currentPage * pageSize;

	        List<User> pageList;

	        if (allFarmers.size() < startItem) {
	            pageList = Collections.emptyList();
	        } else {
	            int toIndex = Math.min(startItem + pageSize, allFarmers.size());
	            pageList = allFarmers.subList(startItem, toIndex);
	        }

	        return new PageImpl<>(pageList, pageable, allFarmers.size());
	    }
	 
	 
	  public List<User> getAllUsersWithCropDetails() {
	        return userRepository.findAllWithCropDetails();
	    }
}



/*
@Override
public User updateProduct(String userId, String cropName, String cropSubType, Double cropRetailPrice,Double cropWholesalePrice, String Description,
		Long cropQuantity, MultipartFile file, String perishable, String status) throws IOException {
	Optional<User> farm = userRepository.findById(userId);
	User farmer1 = farm.get();
	CropDetails cropDetails = new CropDetails();
	Images img = new Images();
	List<CropDetails> list = new ArrayList<CropDetails>();
	cropDetails.setCropId(UUID.randomUUID().toString());
	
	cropDetails.setCropName(cropName);
	cropDetails.setCropWholesalePrice(cropRetailPrice);
	cropDetails.setCropRetailPrice(cropWholesalePrice);
	cropDetails.setCropSubType(cropSubType);
	cropDetails.setCropQuantity(cropQuantity);
	if(status.equals("true")) {
	 boolean productStatus = true;	
	 cropDetails.setApprovalStatus(productStatus);
	}
	else {
		 boolean productStatus = false;	
		 cropDetails.setApprovalStatus(productStatus);
	}
	    if(perishable.equals("true")) {
		 boolean productPerish = true;	
		 cropDetails.setApprovalStatus(productPerish);
		}
		else {
			 boolean productPerish = false;	
			 cropDetails.setApprovalStatus(productPerish);
		}
	//cropDetails.setPerishable(perishable);
	cropDetails.setDescription(Description);

	img.setImage(file.getBytes());
	img = imageRepository.save(img);
	List<String> list1 = new ArrayList<String>();
	list1.add(img.getId());
	cropDetails.setImageIds(list1);

	list.add(cropDetails);
	farmer1.setCropDetails(list);
	farmer1 = userRepository.save(farmer1);

	return farmer1;
}*/
