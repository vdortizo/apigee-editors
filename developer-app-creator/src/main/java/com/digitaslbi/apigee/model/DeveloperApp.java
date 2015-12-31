package com.digitaslbi.apigee.model;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NonNull;

/**
 * POJO that defines an Apigee developer app
 * 
 * @author Victor Ortiz
 */
@Data
public class DeveloperApp {
	@NonNull private String name; 
	@NonNull private List<String> products;

	private Map<String, String> attributes;
}