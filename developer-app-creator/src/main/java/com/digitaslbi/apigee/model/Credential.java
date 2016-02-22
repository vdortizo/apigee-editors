package com.digitaslbi.apigee.model;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO that defines an Apigee credential
 * 
 * @author Victor Ortiz
 */
@Data
@NoArgsConstructor
public class Credential {
	private String consumerKey;
	private String consumerSecret;
	private Long issuedAt;
	private Long expiresAt;
	private String status;
	
	private List<String> scopes;
	private List<Product> apiProducts;
	private List<Attribute> attributes;
}