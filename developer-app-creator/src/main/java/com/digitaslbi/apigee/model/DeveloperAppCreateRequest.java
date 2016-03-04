package com.digitaslbi.apigee.model;

import java.util.List;

import lombok.Data;

/**
 * POJO that defines an Apigee developer app creation request
 * 
 * @author Victor Ortiz
 */
@Data
public class DeveloperAppCreateRequest {
    private String name;
    private Long keyExpiresIn;
    
    private List<String> apiProducts;
    private List<Attribute> attributes;
}