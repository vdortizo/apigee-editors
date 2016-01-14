package com.digitaslbi.apigee.model;

import java.util.List;

import lombok.Data;

/**
 * POJO that defines an Apigee product list
 * 
 * @author Victor Ortiz
 */
@Data
public class ProductsList {
    private List<Product> apiProduct;
}