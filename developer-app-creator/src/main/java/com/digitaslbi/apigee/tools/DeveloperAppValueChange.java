package com.digitaslbi.apigee.tools;

import lombok.Data;

/**
 * A standard key value pair class.
 * 
 * @author Victor Ortiz
 */
@Data
public class DeveloperAppValueChange {
    private String oldValue;
    private String newValue;
}