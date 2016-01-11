package com.digitaslbi.apigee.view;

import java.awt.event.ActionListener;
import java.nio.file.Path;

import javax.swing.event.DocumentListener;

/**
 * This view interface defines specific methods to import/export/work with Apigee
 * developer apps.
 * 
 * @author VIctor Ortiz
 */
public interface DeveloperAppView extends Runnable, ActionListener, DocumentListener {
    /**
     * Initializes all graphical attributes
     * @param firstTime indicates if this is the first execution of this method
     */
    public void initialize( boolean firstTime );
    
    /**
     * Gets the path of a file for loading from user input
     * @param directoyOnly indicates if only directories can be selected
     * @return the selected file
     */
    public Path getFileOrDirectory( boolean directoyOnly );
    
    /**
     * Gets the path of a file for saving from user input
     * @param directoyOnly indicates if only directories can be selected
     * @return the selected file
     */
    public Path saveFileOrDirectory( boolean directoyOnly );
    
    /**
     * Show a message to the user
     * @param message the message to show
     * @param isError indicates is an error message will be shown
     * @param isYesNo indicates if the message should have a yes/no option
     * @return
     */
    public boolean showMessage( Object message, boolean isError, boolean isYesNo );
    
    /**
     * Requests text input from the user
     * @param message the message to show to the user
     * @return the input from the user
     */
    public String requestNewInput( String message );
}