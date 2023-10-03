package com.F2C.jwt.mongodb.services;


import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
//import com.razorpay.Utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RazorpayService {

    @Value("${razorpay.api.key}")
    private String apiKey;

    @Value("${razorpay.api.secret}")
    private String apiSecret;

    public String createOrder(int amount) throws RazorpayException {
        RazorpayClient client = new RazorpayClient(apiKey, apiSecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount*100);
        orderRequest.put("currency", "INR");
        System.out.println(client.payments.fetchAll());
        Order order = client.orders.create(orderRequest);
        System.out.println(order);
//        return order.get("id");
        return order.toString();
    }

    public void verifyPayment(String orderId, String paymentId, String signature) throws RazorpayException {
        RazorpayClient client = new RazorpayClient(apiKey, apiSecret);
        
        
        String concatenatedString = orderId + "|" + paymentId;

        String expectedSignature = generateSHA256(concatenatedString + apiSecret);

        if (signature.equals(expectedSignature)) {
 
            System.out.println("Payment verified successfully");
        } else {
            
            System.out.println("Payment verification failed");
        }
    }
    
    private String generateSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
}