package com.digitaslbi.apigee.tools;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

/**
 * A simple comparator class to order maps.
 * 
 * @author Victor Ortiz
 */
public class DeveloperAppComparator implements Comparator<String> {
    private static final String PRIMARY_PROPERTY = "DisplayName";
    
    @Override public int compare( String key1, String key2 ) {
        if ( PRIMARY_PROPERTY.equals( key1 ) )
            key1 = StringUtils.EMPTY;
        
        if ( PRIMARY_PROPERTY.equals( key2 ) )
            key2 = StringUtils.EMPTY;
        
        return null != key1 ? key1.compareToIgnoreCase( key2 ) : 1;
    }
}