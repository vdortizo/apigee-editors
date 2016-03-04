package com.digitaslbi.apigee.model;

import lombok.Data;

/**
 * POJO that defines the configuration properties for the application
 * 
 * @author Victor Ortiz
 */
@Data
public class ConfigProperties {
	private String credentialsUserLabel = "User";
	private String credentialsPasswordLabel = "Password";
	private String credentialsOrganizationLabel = "Organization";
	private String credentialsDeveloperLabel = "Developer email or id";
	
	private String componentApplicationLabel = "Echidna";
	private String componentDeleteLabel = "Delete";
	private String componentNewAttributeLabel = "New attribute";
    private String componentNewProductLabel = "New product";
    private String componentAttributesLabel = "Attributes";
    private String componentDetailsLabel = "Details";
    private String componentProductsLabel = "Products";
    private String componentDisplayNameLabel = "Display name";
    private String componentNameLabel = "Name";
    private String componentNotesLabel = "Notes";
	
	private String uiMessageFileSelection = "Please select a file/directory";
	private String uiMessageEmptyError = "This field does not accept empty values";
	private String uiMessageGraphicalError = "A graphical interface error has ocurred";
	private String uiMessageOSXError = "Could not initialize fullscreen for OS X";
	
    private String uiTitleError = "Error";
    private String uiTitleWarning = "Warning";
    
    private String uiMenuItemNew = "New";
    private String uiMenuItemOpen = "Open";
    private String uiMenuItemSave = "Save";
    private String uiMenuItemSaveAs = "Save as";
    private String uiMenuItemReload = "Reload";
    private String uiMenuItemTestUpload = "Test upload";
    private String uiMenuItemVerifyProducts = "Verify products";
    private String uiMenuItemGetKey = "Get API key";
    private String uiMenuItemSetCredentials = "Set credentials";
    private String uiMenuItemGetWeb = "Load from web";
    
    private String uiMenuFile = "File";
    private String uiMenuWeb = "Web";
}