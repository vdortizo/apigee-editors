package com.digitaslbi.apigee.model;

import java.util.List;

import lombok.Data;

/**
 * POJO that defines an Apigee product
 * 
 * @author Victor Ortiz
 */
@Data
public class Product {
	private String apiproduct;
	private String name;
	private String displayName;
	private String description;
	private String approvalType;
	private List<String> apiResources;
	private List<String> environments;
	
	private String createdBy;
	private String lastModifiedBy;
	private Long createdAt;
	private Long lastModifiedAt;
	
	private List<String> proxies;
	private List<String> scopes;
	private List<Attribute> attributes;
}