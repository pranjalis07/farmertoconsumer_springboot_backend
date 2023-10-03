package com.F2C.jwt.mongodb.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration    
@EnableWebMvc
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
//        registry.addMapping("/**") // Allow CORS for your API routes
//            .allowedOrigins("http://localhost:3000") // Replace with your React frontend URL
//            .allowedMethods("GET", "POST", "PUT", "DELETE") // Allow specific HTTP methods
//            .allowCredentials(true) // Allow cookies, if needed
//            .maxAge(3600); // Max age of the CORS options response, in seconds
    	
    	UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();      
    	CorsConfiguration config = new CorsConfiguration();     
    	config.setAllowCredentials(true);   
    	config.addAllowedOrigin("http://localhost:3000"); // Specify your frontend origin here      
    	config.addAllowedHeader("*");      
    	config.addAllowedMethod("*");     
    	source.registerCorsConfiguration("/api/QCAdmin/**", config);     //**
    	return new CorsFilter(source);
    }
}