package com.digitaslbi.apigee.model;

import java.util.List;

import lombok.Data;

/**
 * POJO that defines an Apigee developer app update request
 * 
 * @author Victor Ortiz
 */
@Data
public class DeveloperAppUpdateRequest {
    private List<String> apiProducts;
    private List<Attribute> attribute;
}