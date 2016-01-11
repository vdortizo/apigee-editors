package com.digitaslbi.apigee.model;

import lombok.Data;

/**
 * POJO that defines an Apigee login credential
 * 
 * @author Victor Ortiz
 */
@Data
public class LoginCredentials {
	private String user;
	private String password;
	private String organization;
}