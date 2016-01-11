package com.digitaslbi.apigee.tools;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import com.digitaslbi.apigee.model.Attribute;

/**
 * A simple comparator class to order maps.
 * 
 * @author Victor Ortiz
 */
public class AttributeComparator implements Comparator<Attribute> {
    private static final String PRIMARY_PROPERTY = "DisplayName";
    
    @Override public int compare( Attribute key1, Attribute key2 ) {
        String key1Name = null;
        String key2Name = null;

        if ( null != key1 )
            key1Name = key1.getName();
        
        if ( null != key2 )
            key2Name = key2.getName();
        
        if ( PRIMARY_PROPERTY.equals( key1.getName() ) )
            key1Name = StringUtils.EMPTY;
        
        if ( PRIMARY_PROPERTY.equals( key2.getName() ) )
            key2Name = StringUtils.EMPTY;
        
        return null != key1Name ? key1Name.compareToIgnoreCase( key2Name ) : 1;
    }
}