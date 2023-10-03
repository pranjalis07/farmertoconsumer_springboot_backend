package com.F2C.jwt.mongodb.models;

import lombok.Data;

@Data
public class OrderItem {
	//@Id
	private String orderItemId;
	private String cropId;
    private Long quantity;
    private Double itemPrice;
}
