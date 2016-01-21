package com.digitaslbi.apigee;

import java.awt.EventQueue;

import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

import com.digitaslbi.apigee.view.DeveloperAppView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.extern.slf4j.Slf4j;

/**
 * Launcher class for the editor.
 * 
 * @author Victor Ortiz
 */
@Slf4j
@SpringBootApplication
public class DeveloperAppEditor {
    /**
     * Main entry method
     * @param args the application parameters
     */
    public static void main( String[] args ) {
    	AnnotationConfigApplicationContext context = null;
    	
    	try {
    		context = new AnnotationConfigApplicationContext( DeveloperAppEditor.class );
    		EventQueue.invokeLater( context.getBean( DeveloperAppView.class ) );
    	} catch ( Exception e ) {
    		log.error( e.getLocalizedMessage(), e );
    	} finally {
    		if ( null != context )
    			context.close();
    	}
    }
    
    @Bean public HttpClientBuilder clientBuilder() {
    	return HttpClientBuilder.create();
    }
    
    @Bean public Gson gson() {
        return new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    }
}