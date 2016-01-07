package com.digitaslbi.apigee;

import java.awt.EventQueue;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.digitaslbi.apigee.controller.DeveloperAppControllerImpl;
import com.digitaslbi.apigee.view.DeveloperAppViewImpl;

/**
 * Launcher class for the editor.
 * 
 * @author Victor Ortiz
 */
@SpringBootApplication
public class DeveloperAppEditor {
    /**
     * Main entry method
     * @param args the application parameters
     */
    public static void main( String[] args ) {
        EventQueue.invokeLater( new DeveloperAppViewImpl( new DeveloperAppControllerImpl() ) );
    }
}