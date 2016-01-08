package com.digitaslbi.apigee.model;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NonNull;

/**
 * POJO that defines an Apigee product
 * 
 * @author Victor Ortiz
 */
@Data
public class Product {
    @NonNull private String name;
    @NonNull private String displayName;
    @NonNull private List<String> proxies;
    
    private Map<String, String> attributes;
}