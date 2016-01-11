package com.digitaslbi.apigee.tools;

import java.util.Comparator;

import com.digitaslbi.apigee.model.Product;

/**
 * A simple comparator class to order maps.
 * 
 * @author Victor Ortiz
 */
public class ProductComparator implements Comparator<Product> {
    @Override public int compare( Product key1, Product key2 ) {
        String key1Name = null;
        String key2Name = null;

        if ( null != key1 )
            key1Name = key1.getDisplayName();
        
        if ( null != key2 )
            key2Name = key2.getDisplayName();
        
        return null != key1Name ? key1Name.compareToIgnoreCase( key2Name ) : 1;
    }
}