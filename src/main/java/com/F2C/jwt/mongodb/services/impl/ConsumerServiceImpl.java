package com.F2C.jwt.mongodb.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.F2C.jwt.mongodb.models.Cart;
import com.F2C.jwt.mongodb.models.CartItem;
import com.F2C.jwt.mongodb.models.CropDetails;
import com.F2C.jwt.mongodb.models.ERole;
import com.F2C.jwt.mongodb.models.ImageResponse;
import com.F2C.jwt.mongodb.models.Images;
import com.F2C.jwt.mongodb.models.Order;
import com.F2C.jwt.mongodb.models.OrderItem;
import com.F2C.jwt.mongodb.models.OrderStatus;
import com.F2C.jwt.mongodb.models.Role;
import com.F2C.jwt.mongodb.models.User;
import com.F2C.jwt.mongodb.repository.ImageRepository;
import com.F2C.jwt.mongodb.repository.RoleRepository;
import com.F2C.jwt.mongodb.repository.UserRepository;
import com.F2C.jwt.mongodb.security.JwtUtils;
import com.F2C.jwt.mongodb.services.ConsumerService;
import com.F2C.jwt.mongodb.services.UserDetailsImpl;

@Service
public class ConsumerServiceImpl implements ConsumerService{
	@Autowired
	private UserRepository userRepository;

	@Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ImageRepository imageRepository;
    
    @Autowired
    private RoleRepository roleRepository;
	
	/*

	    private final CartRepository cartRepository;

	    public ConsumerServiceImpl(UserRepository userRepository, CartRepository cartRepository) {
	        this.userRepository = userRepository;
	        this.cartRepository = cartRepository;
	    }
	*/

	@Override
    public User getUserById(String id) {
		return userRepository.findById(id).orElse(null);
    
	}
		/*            dont use  Optional
        Optional<User> optionalUser = userRepository.findById(id); 
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            return null; // User not found
        }
        */
		 
	@Override
	public List<CropDetails> getAllCropsForConsumer() {
	    List<CropDetails> allCrops = new ArrayList<>();
	    
	    List<User> farmers = userRepository.findAll();
	    for (User farmer : farmers) {
	        for (Role role : farmer.getRoles()) {
	            if (role.getName() == ERole.ROLE_FARMER) {
	                List<CropDetails> cropDetailsList = farmer.getCropDetails();
	                if (cropDetailsList != null) {
	                    allCrops.addAll(cropDetailsList);
	                }
	                break; // No need to check other roles for this user
	            }
	        }
	    }

	    return allCrops;
	}



	public CropDetails getCropById(String consumerId, String cropId) {
	    List<CropDetails> matchingCrops = new ArrayList<>();

	    List<User> farmers = userRepository.findAll();
	    for (User farmer : farmers) {
	        boolean isFarmer = false;
	        for (Role role : farmer.getRoles()) {
	            if (role.getName() == ERole.ROLE_FARMER) {
	                isFarmer = true;
	                break;
	            }
	        }

	        if (isFarmer) {
	            List<CropDetails> cropDetailsList = farmer.getCropDetails();
	            if (cropDetailsList != null) {
	                for (CropDetails cropDetails : cropDetailsList) {
	                    if (cropDetails.getCropId().equals(cropId)) {
	                        return cropDetails; // Return the matching crop immediately
	                    }
	                }
	            }
	        }
	    }

	    return null; // Crop not found
	}

	
    @Override
  public ImageResponse getCropByIdWithImages(String consumerId, String cropId) { 
Optional<Role> role = roleRepository.findByName(ERole.ROLE_FARMER);
Role rol1 = role.get(); 
List<User> farmers = userRepository.findByRoles(rol1);

      for (User farmer : farmers) {

List<CropDetails> cropDetailsList = farmer.getCropDetails();

            if (cropDetailsList != null) {
                for (CropDetails cropDetails : cropDetailsList) {
                  if (cropDetails.getCropId().equals(cropId) && cropDetails.getPublished()) {
                       ImageResponse imgr = new ImageResponse();
                       imgr.setCropId(cropDetails.getCropId());
                      imgr.setCropName(cropDetails.getCropName());
                        imgr.setCropQuantity(cropDetails.getCropQuantity());
                       imgr.setCropRetailPrice(cropDetails.getCropRetailPrice());
                     imgr.setCropSubType(cropDetails.getCropSubType());
                       imgr.setCropWholesalePrice(cropDetails.getCropWholesalePrice());
                      imgr.setDescription(cropDetails.getDescription());
                       imgr.setPublished(cropDetails.getPublished());

 // Initialize a list to store image bytes
                        List<byte[]> imageBytesList = new ArrayList<>();
                      for (String id : cropDetails.getImageIds()) {
                          
						Optional<Images> image = imageRepository.findById(id);
                            if (image.isPresent()) {
                              Images img = image.get();
                              imageBytesList.add(img.getImage());
                   }
 }
                      imgr.setImages(imageBytesList);

                      System.out.println("imgr" + imgr);

                        return imgr; // Return the matching crop with images
                  }
                }
            }
      }
      return null;
    }
	
	 private boolean isUserFarmer(User user) {
	        for (Role role : user.getRoles()) {
	            if (role.getName() == ERole.ROLE_FARMER) {
	                return true;
	            }
	        }
	        return false;
	    }
	

	    @Override
	    public List<CropDetails> getCropsBySubType(String cropSubType) {
	        List<CropDetails> matchingCrops = new ArrayList<>();

	        for (User farmer : userRepository.findAll()) {
	            if (isUserFarmer(farmer)) {
	                List<CropDetails> cropDetailsList = farmer.getCropDetails();
	               // System.out.println(cropDetailsList);
	                if (cropDetailsList != null) {
	                    for (CropDetails cropDetails : cropDetailsList) {
	                        if (cropDetails.getCropSubType().equals(cropSubType)) {
	                        	
	                            matchingCrops.add(cropDetails);
	                        }
	                    }
	                }
	            }
	        }

	        return matchingCrops;  
	    }

	    /// if sub type/ price range  not match then it returns empty array, it should dislay some msg instead
               
	    @Override
	    public List<CropDetails> getCropsByPriceRange(Double minPrice, Double maxPrice) {
	    	List<CropDetails> cropsInRange = new ArrayList<>();

	    		for (User farmer : userRepository.findAll()) {
	    			if (isUserFarmer(farmer)) {
	    				List<CropDetails> cropDetailsList = farmer.getCropDetails();
	    				if (cropDetailsList != null) {
	    					for (CropDetails cropDetails : cropDetailsList) {
	    						Double cropPrice = cropDetails.getCropRetailPrice();
	    						if (cropPrice >= minPrice && cropPrice <= maxPrice) {
	    							cropsInRange.add(cropDetails);
	    						}
	    					}
	    				}
	    			}
	    		}
	    		return cropsInRange;
	    	}    
	    
	    
	    
	    
	    
	  
// add to cart 

	   

	        @Override
	        public void addToCart(String consumerId, String cropId, Long requiredQuantity) {
	            User consumer = userRepository.findById(consumerId).orElse(null);

	            if (consumer != null) {
	                CropDetails crop = findCropById(cropId);

	                if (crop != null && crop.getCropQuantity() >= requiredQuantity) {
	                    Cart cart = getOrCreateCart(consumer);

	                    CartItem cartItem = new CartItem();
	                    cartItem.setCartItemId(UUID.randomUUID().toString());
	                    
                         ///// 
	                   
	                    List<CropDetails> cartCropDetailsList = new ArrayList<>();
	                    cartCropDetailsList.add(crop);

	                    cartItem.setCropDetailsList(cartCropDetailsList);
	                    cartItem.setCartItemPrice(crop.getCropWholesalePrice() * requiredQuantity);
	                    cartItem.setCartItemQuantity(requiredQuantity);
	                    /*
	                    
	                    ////
	                    cartItem.getCropDetailsList().add(crop);
	                    cartItem.setCartItemPrice(crop.getCropPrice() * requiredQuantity);
	                    cartItem.setCartItemQuantity(requiredQuantity);
                       */
	                    
	                 
	                    cart.getCropItems().add(cartItem);

	                    updateCartTotals(cart);

	                    crop.setCropQuantity(crop.getCropQuantity() - requiredQuantity);

	                    userRepository.save(consumer);
	                }
	            }
	        }

	        
	        
  ///  remove 
	        
	        
	        @Override
	        public void removeFromCart(String consumerId, String cartItemId) {
	            User consumer = userRepository.findById(consumerId).orElse(null);

	            if (consumer != null && consumer.getCart()!= null) {
	                Cart cart = consumer.getCart();
	                List<CartItem> cartItems = cart.getCropItems();
	                if (cartItems != null) {
	                    cartItems.removeIf(item -> item.getCartItemId().equals(cartItemId));
	                    updateCartTotals(cart);

	                    userRepository.save(consumer);
	                }
	            }
	        }

	        
	        
////  cropdeatils fecthing in cart, but deatils list farmerquantity - required q = keep as  deatils list cartquantity         
/// i dont want to reflect that while adding in cart
// out of stock before adding to crop- if all over
	        
	        private void updateCartTotals(Cart cart) {
	            cart.setFinalPrice(0.0);
	            cart.setFinalQuantity(0L);

	            for (CartItem item : cart.getCropItems()) {
	                cart.setFinalPrice(cart.getFinalPrice() + item.getCartItemPrice());
	                cart.setFinalQuantity(cart.getFinalQuantity() + item.getCartItemQuantity());
	            }
	        }

	        
	        
	        
	        private CropDetails findCropById(String cropId) {
	            for (User farmer : userRepository.findAll()) {
	                List<CropDetails> cropDetailsList = farmer.getCropDetails();
	                if (cropDetailsList != null) {
	                    for (CropDetails cropDetails : cropDetailsList) {
	                        if (cropDetails.getCropId().equals(cropId)) {
	                            return cropDetails;
	                        }
	                    }
	                }
	            }
	            return null;
	        }

	        private Cart getOrCreateCart(User consumer) {
	            if (consumer.getCart() == null) {
	                consumer.setCart(new Cart());
	            }
	            return consumer.getCart();
	        }
	    
         



	       //////////  view cart

	        @Override
	        public Cart viewCart(String consumerId) {
	            User consumer = userRepository.findById(consumerId).orElse(null);
	            
	            if (consumer != null) {
	                // Check if the user has the role CONSUMER (you can also use a dedicated method for role checking)
	                boolean isConsumer = consumer.getRoles().stream().anyMatch(role -> role.getName() == ERole.ROLE_CONSUMER);
	                
	                if (isConsumer) {
	                    // Assuming that the Cart is a field in the User class
	                    Cart cart = consumer.getCart();
	                    return cart;
	                }
	            }
	            
	            return null; // Cart not found or user is not a consumer
	        }

	            @Override
	            public CartItem viewCartItem(String consumerId, String cartItemId) {
	                User consumer = userRepository.findById(consumerId).orElse(null);
	                if (consumer != null && consumer.getCart() != null) {
	                    Cart cart = consumer.getCart();
	                    for (CartItem cartItem : cart.getCropItems()) {
	                        if (cartItem.getCartItemId().equals(cartItemId)) {
	                            return cartItem;
	                        }
	                    }
	                }
	                return null; // Cart item not found or does not belong to the specified consumer's cart
	            }

	            
	        
	            @Override
	            public User updateConsumerProfile(String consumerId, User updatedUser) {
	                User existingUser = userRepository.findById(consumerId).orElse(null);
	                if (existingUser != null) {
	                    // Update the profile fields you want to allow
	                    existingUser.setfirstName(updatedUser.getfirstName());
	                    existingUser.setlastName(updatedUser.getlastName());
	                    existingUser.setEmail(updatedUser.getEmail());
	                    existingUser.setaddress(updatedUser.getaddress());
	                    existingUser.setphoneNo(updatedUser.getphoneNo()); // Update phone number
	                    
	                   // "phoneNo": "987654321", 
	                   // "password": "sima123@",	
	                    
	                    /*
	                 // If a new password is provided, update the password
	                    if (!updatedUser.getPassword().isEmpty()) {
	                        existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
	                    }
	                    */
	                    		
	                    return userRepository.save(existingUser);
	                }
	                return null; // User not found
	            }

	            @Override
	            public String placeOrder(String consumerId, List<String> cartItemIds) {
	            	String orderId=""; //extra 
	                UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	                if (userDetails == null || !userDetails.getId().equals(consumerId)) {
	                    throw new IllegalArgumentException("Unauthorized or invalid user.");
	                }

	                User consumer = getUserById(consumerId);
	                if (consumer != null) {
	                    Cart cart = consumer.getCart();

	                    // Find the cart items with the given IDs and mark them as ordered
	                    for (String cartItemId : cartItemIds) {
	                        CartItem cartItem = cart.getCropItems().stream()
	                                .filter(item -> item.getCartItemId().equals(cartItemId))
	                                .findFirst()
	                                .orElse(null);

	                        if (cartItem != null) {
	                            CropDetails crop = cartItem.getCropDetailsList().get(0);

	                            Order order = new Order();
	                            order.setOrderId(UUID.randomUUID().toString());
	                             orderId = order.getOrderId(); //extra 
	                            System.out.print(orderId);
	                            // Set order date and time
	                            LocalDateTime currentDateTime = LocalDateTime.now();
	                            order.setOrderDateTime(currentDateTime);

	                            // Calculate and set probable delivery date (e.g., 3 days from now)
	                            LocalDateTime probableDeliveryDateTime = currentDateTime.plusDays(3); // Adjust the logic as needed
	                            order.setProbableDeliveryDateTime(probableDeliveryDateTime);

	                            // Calculate total order quantity and amount
	                            Long totalQuantity = cartItem.getCartItemQuantity();
	                            Double totalAmount = cartItem.getCartItemPrice() ; //*totalQuantity;

	                            OrderItem orderItem = new OrderItem();
	                            orderItem.setOrderItemId(UUID.randomUUID().toString());
	                            orderItem.setCropId(crop.getCropId());
	                            orderItem.setQuantity(totalQuantity);
	                            orderItem.setItemPrice(cartItem.getCartItemPrice());
	                            order.setItems(Collections.singletonList(orderItem));
	                            order.setOrderStatus(OrderStatus.PLACED);
	                            order.setTotalAmount(totalAmount);
	                            order.setTotalQuantity(totalQuantity);

	                            // Set order address (you can modify this based on your structure)
	                            order.setAddress(consumer.getaddress());

	                            if (consumer.getOrders() == null) {
	                                consumer.setOrders(new ArrayList<>()); // Initialize the list
	                            }
	                            consumer.getOrders().add(order);

	                            cart.getCropItems().remove(cartItem);
	                           
	                            
	                        }
	                        updateCartTotals(cart);
	                         
	                    }

	                   // updateCartTotals(cart);
	                    userRepository.save(consumer);
	                }
	              

	                // Return the order ID
	                return orderId;
	               
	            }
	            
	            
	            //srushtis method of direct order 
	            
	            //  public void placeDirectOrder(String consumerId, String cropId) {
	            @Override	         
	            public String placeDirectOrder(String consumerId, String cropId) {
	
	            	String orderIdd = " ";
	            	
	                UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	                if (userDetails == null || !userDetails.getId().equals(consumerId)) {
	                    throw new IllegalArgumentException("Unauthorized or invalid user.");
	                }

	                User consumer = getUserById1(consumerId);
	                if (consumer == null) {
	                    throw new IllegalArgumentException("Consumer not found.");
	                }

	                CropDetails crop = getCropDetailsById(cropId);
	                if (crop == null) {
	                    throw new IllegalArgumentException("Crop not found.");
	                }

	                Order order = createOrder(consumer, crop);

	                if (consumer.getOrders() == null) {
	                    consumer.setOrders(new ArrayList<>()); // Initialize the list if it doesn't exist
	                }
	                consumer.getOrders().add(order);
	                
	                orderIdd = order.getOrderId();// from below method 

	                userRepository.save(consumer);
					return orderIdd;
	            }
	            
	            

	            // Helper method to fetch the user by userId
	            private User getUserById1(String userId) {
	                Optional<User> userOptional = userRepository.findById(userId);
	                return userOptional.orElse(null);
	            }

	            // Helper method to fetch crop details by cropId
	            private CropDetails getCropDetailsById(String cropId) {
	            	
	            	    // Iterate through farmers to find the crop by cropId
	            	    Optional<Role> role = roleRepository.findByName(ERole.ROLE_FARMER);
	            	    Role rol1 = role.get();
	            	    
	            	    List<User> farmers = userRepository.findByRoles(rol1);

	            	    for (User farmer : farmers) {
	            	        List<CropDetails> cropDetailsList = farmer.getCropDetails();

	            	        if (cropDetailsList != null) {
	            	            for (CropDetails cropDetails : cropDetailsList) {
	            	                if (cropDetails.getCropId().equals(cropId)) {
	            	                    return cropDetails; // Return the matching crop
	            	                }
	            	            }
	            	        }
	            	    }

	            	    return null; // Crop not found
	            	   
	            	}


	            // Helper method to create an order
	            private Order createOrder(User consumer, CropDetails crop) {
	                Order order = new Order();
	                order.setOrderId(UUID.randomUUID().toString());
	                order.setAddress(consumer.getaddress());
	                
	                //String orderId = order.getOrderId();

	                // Set other order details here, such as total amount, quantity, dates, etc.

	                OrderItem orderItem = new OrderItem();
	                orderItem.setOrderItemId(UUID.randomUUID().toString());
	            
	                orderItem.setCropId(crop.getCropId());
	                orderItem.setQuantity(1L); // Adjust as needed
	                orderItem.setItemPrice(crop.getCropRetailPrice()); // Assuming retail price

	                order.setItems(Collections.singletonList(orderItem));
	                order.setOrderStatus(OrderStatus.PLACED);

	                // Calculate total amount and set it
	                Double totalAmount = orderItem.getItemPrice() * orderItem.getQuantity();
	                order.setTotalAmount(totalAmount);

	                // Set total quantity
	                order.setTotalQuantity(orderItem.getQuantity());

	                // Set order date and probable delivery date (adjust as needed)
	                LocalDateTime currentDateTime = LocalDateTime.now();
	                order.setOrderDateTime(currentDateTime);
	                LocalDateTime probableDeliveryDateTime = currentDateTime.plusDays(3); // Adjust the logic
	                order.setProbableDeliveryDateTime(probableDeliveryDateTime);

	                return order;
	               
	            }

	            
       
	            
	            ////////////////////////////////////////////////////////

	            @Override
	            public List<Order> getOrdersByConsumerId(String consumerId) {
	                User consumer = getUserById(consumerId);
	                if (consumer != null) {
	                    return consumer.getOrders();
	                }
	                return Collections.emptyList();
	            }
	            
	            @Override
	            public Order getOrderById(String consumerId, String orderId) {
	                User consumer = getUserById(consumerId);
	                if (consumer != null) {
	                    for (Order order : consumer.getOrders()) {
	                        if (order.getOrderId().equals(orderId)) {
	                            return order;
	                        }
	                    }
	                }
	                return null; // Order not found or doesn't belong to the consumer
	            }
	            
	            @Override
	            public OrderItem getOrderItemById(String consumerId, String orderId, String orderItemId) {
	                User consumer = getUserById(consumerId);
	                if (consumer != null) {
	                    for (Order order : consumer.getOrders()) {
	                        if (order.getOrderId().equals(orderId)) {
	                            for (OrderItem orderItem : order.getItems()) {
	                                if (orderItem.getOrderItemId().equals(orderItemId)) {
	                                    return orderItem;
	                                }
	                            }
	                        }
	                    }
	                }
	                return null; // Order item not found or does not belong to the specified order
	            }
	            
	            @Override
	            public void cancelOrder(String consumerId, String orderId) {
	                User consumer = getUserById(consumerId);
	                if (consumer != null) {
	                    consumer.getOrders().removeIf(order -> order.getOrderId().equals(orderId));
	                    userRepository.save(consumer);
	                }
	            }

}

/* Order Placed -- back up 
@Override
public void placeOrder(String consumerId, String cartItemId) {
    UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (userDetails == null || !userDetails.getId().equals(consumerId)) {
        throw new IllegalArgumentException("Unauthorized or invalid user.");
    }

    User consumer = getUserById(consumerId);
    if (consumer != null) {
        Cart cart = consumer.getCart();
        CartItem cartItem = cart.getCropItems().stream()
                .filter(item -> item.getCartItemId().equals(cartItemId))
                .findFirst()
                .orElse(null);
        if (cartItem != null) {
            CropDetails crop = cartItem.getCropDetailsList().get(0);

            Order order = new Order();
            order.setOrderId(UUID.randomUUID().toString());

            // Set order date and time
            LocalDateTime currentDateTime = LocalDateTime.now();
            order.setOrderDateTime(currentDateTime);

            // Calculate and set probable delivery date (e.g., 3 days from now)
            LocalDateTime probableDeliveryDateTime = currentDateTime.plusDays(3); // Adjust the logic as needed
            order.setProbableDeliveryDateTime(probableDeliveryDateTime);

            // Calculate total order quantity and amount
            Long totalQuantity = cartItem.getCartItemQuantity();
            Double totalAmount = cartItem.getCartItemPrice() * totalQuantity;

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderItemId(UUID.randomUUID().toString());
            orderItem.setCropId(crop.getCropId());
            orderItem.setQuantity(totalQuantity);
            orderItem.setItemPrice(cartItem.getCartItemPrice());
            order.setItems(Collections.singletonList(orderItem));
            order.setOrderStatus(OrderStatus.PLACED);
            order.setTotalAmount(totalAmount);
            order.setTotalQuantity(totalQuantity);

            // Set order address (you can modify this based on your structure)
            order.setAddress(consumer.getaddress());

            if (consumer.getOrders() == null) {
                consumer.setOrders(new ArrayList<>()); // Initialize the list
            }
            consumer.getOrders().add(order);

            cart.getCropItems().remove(cartItem);
            updateCartTotals(cart);
            userRepository.save(consumer);
        }
    }
}*/