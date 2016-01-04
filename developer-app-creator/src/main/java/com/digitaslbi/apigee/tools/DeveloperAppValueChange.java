package com.digitaslbi.apigee.tools;

import lombok.Getter;
import lombok.Setter;

/**
 * A standard key value pair class.
 * 
 * @author Victor Ortiz
 */
public class DeveloperAppValueChange {
    @Getter @Setter private String oldValue;
    @Getter @Setter private String newValue;
}