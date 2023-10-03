package com.F2C.jwt.mongodb.config;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.F2C.jwt.mongodb.models.CCToQCReq;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationHandler extends TextWebSocketHandler {

    private static Map<String, WebSocketSession> employeeSessions = new ConcurrentHashMap<>();

//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) {
//        // Extract the employeeId from session attributes
//        String employeeId = (String) session.getAttributes().get("employeeId");
//
//        if (employeeId != null) {
//            employeeSessions.put(employeeId, session);
//        }
//    }

    public static void sendNotificationToEmployee(String employeeId, CCToQCReq request) {
        WebSocketSession session = employeeSessions.get(employeeId);
        System.out.println(request.getAssignedCCId());
        System.out.println(request.getFarmerId());
        String jsonreq="";
        if (session != null && session.isOpen()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
            	
                String jsonRequest = objectMapper.writeValueAsString(request);
                session.sendMessage(new TextMessage(jsonRequest));
                jsonreq = jsonRequest;
            } catch (IOException e) {
                // Handle the exception
            }
        }
        
        
        
    }
}
