package com.digitaslbi.apigee.model;

import java.util.List;

import lombok.Data;

/**
 * POJO that defines an Apigee developer app
 * 
 * @author Victor Ortiz
 */
@Data
public class DeveloperApp {
	private String appFamily;
	private String developerId;
	private String appId;
	private String name;
	private String status;
	
	private String createdBy;
	private String lastModifiedBy;
	private Long createdAt;
	private Long lastModifiedAt;
	
	private String callbackUrl;
	private List<String> scopes;
	private List<Attribute> attributes;
	private List<Credential> credentials;
}