package com.digitaslbi.apigee.controller;

import java.io.IOException;
import java.nio.file.Path;

import com.digitaslbi.apigee.model.DeveloperApp;
import com.digitaslbi.apigee.view.DeveloperAppView;

/**
 * This controller interface defines specific methods to import/export/work with
 * Apigee developer apps.
 * 
 * @author Victor Ortiz
 */
public interface DeveloperAppController {
	public static final String CMD_NEW = "NEW";
	public static final String CMD_OPEN = "OPEN";
    public static final String CMD_SAVE = "SAVE";
    public static final String CMD_SAVEAS = "SAVEAS";
    public static final String CMD_RELOAD = "RELOAD";
    public static final String CMD_ADDAT = "ADDAT";
    public static final String CMD_ADDPR = "ADDPR";
    public static final String CMD_DELAT = "DELAT";
    public static final String CMD_DELPR = "DELPR";
    public static final String CMD_EDIAT = "EDIAT";
    public static final String CMD_EDIPR = "EDIPR";
    public static final String CMD_TSTUP = "TSTUP";
    
    public static final String KEY = "key";
    public static final String VALUE = "value";
	
    /**
     * Sets the view of this controller
     * @param view the view to set
     */
    public void setView( DeveloperAppView view );
    
    /**
     * Imports a developer app from the file system
     * @param directory the path to import from
     * @throws IOException any exception when reading/writing to a file will be thrown
     */
	public void importDeveloperApp( Path directory ) throws IOException;
	
	/**
	 * Exports a developer app to the file system
	 * @param directory the path to export to
     * @throws IOException any exception when reading/writing to a file will be thrown
	 */
	public void exportDeveloperApp( Path directory ) throws IOException;
	
	/**
	 * Returns the current model
	 * @return the model
	 */
	public DeveloperApp getModel();
	
	/**
	 * Creates a unique name for a command and the index of an element that uses it
	 * @param command the name of the command
	 * @param index the index of the element
	 * @return the name of the command and the element as established in the implementation
	 */
	public String getCommandForIndex( String command, Object index );
	
	/**
	 * Executes a command
	 * @param command the command name
	 * @param source the source object/parameters
	 */
	public void commandExecuted( String command, Object source );
}