package com.digitaslbi.apigee.model;

import java.util.List;

import lombok.Data;

/**
 * POJO that defines an Apigee attributes list
 * 
 * @author Victor Ortiz
 */
@Data
public class AttributesList {
    private List<Attribute> attributes;
}