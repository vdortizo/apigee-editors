package com.digitaslbi.apigee.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Document;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.digitaslbi.apigee.controller.DeveloperAppController;
import com.digitaslbi.apigee.model.Attribute;
import com.digitaslbi.apigee.model.ConfigProperties;
import com.digitaslbi.apigee.model.DeveloperApp;
import com.digitaslbi.apigee.model.LoginCredentials;
import com.digitaslbi.apigee.model.Product;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of DeveloperAppView
 * 
 * @author Victor Ortiz
 */
@Slf4j
@Component
public class DeveloperAppViewImpl extends InputVerifier implements DeveloperAppView {
	private static final String PROPERTY_INDEX = "INDEX";
    private static final String PROPERTY_TYPE = "TYPE";
    private static final String PROPERTY_TYPE_ATT = "ATTRIBU";
    private static final String PROPERTY_TYPE_PRO = "PRODUCT";
    private static final String PROPERTY_KV = "KEYVALUE";
    
    private static final String LF_APPLE_MENU = "apple.laf.useScreenMenuBar";
    private static final String LF_APPLE_FULLSCREEN = "setWindowCanFullScreen";
    private static final String LF_APPLE_UTIL = "com.apple.eawt.FullScreenUtilities";
    private static final String LF_JAVA_NIMBUS = "Nimbus";
    
    private JFrame frame;
    
    private JLabel nameLabel;
    private JLabel displayNameLabel;
    private JLabel notesLabel;
    
    private JTextField nameContentField;
    private JTextField displayNameContentField;
    private JTextArea notesContentArea;
    
    private JScrollPane notesContentScrollPane;
    private JScrollPane panelScrollPane;
    
    private JButton productNewButton;
    private JButton attributeNewButton;
    
    private JPanel namePanel;
    private JPanel productsPanel;
    private JPanel attributesPanel;
    private JPanel panel;
    
    private boolean loading;
    
    private String lastChange;
    private String lastType;
    private String lastIndex;
    private String lastKv;
    
    private LoginCredentials lastCredentials;
    private Path lastPath;

    private DeveloperAppController controller;
    private ConfigProperties configProperties;
    
    @Autowired private Gson gson;
    
    @Autowired 
    public DeveloperAppViewImpl( DeveloperAppController controller, ConfigProperties configProperties ) {
    	this.controller = controller;
    	this.configProperties = configProperties;
        
    	lastChange = StringUtils.EMPTY;
        lastType = StringUtils.EMPTY;
        lastIndex = StringUtils.EMPTY;
        lastKv = StringUtils.EMPTY;
        
        lastCredentials = new LoginCredentials();
        lastCredentials.setUser( StringUtils.EMPTY );
        lastCredentials.setPassword( StringUtils.EMPTY );
        lastCredentials.setOrganization( StringUtils.EMPTY );
        lastCredentials.setDeveloper( StringUtils.EMPTY );
        
        System.setProperty( LF_APPLE_MENU, Boolean.TRUE.toString() );
        boolean nimbusSet = false;
        
        for ( LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
        	if ( LF_JAVA_NIMBUS.equals( info.getName() ) ) {
        		try {
        			UIManager.setLookAndFeel( info.getClassName() );
        			nimbusSet = true;
        		} catch ( Exception e ) {
        			log.error( configProperties.getUiMessageGraphicalError(), e );
        		}
        		
        		break;
        	}
        }
        
        if ( !nimbusSet )
        	try {
            	UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
			} catch ( Exception ex ) {
				log.error( configProperties.getUiMessageGraphicalError(), ex );
			}
    }
    
    @Override public void initialize( boolean firstTime ) {
        DeveloperApp model = controller.getModel();
        loading = true;
        
        if ( firstTime )
        	initializeStatic();
        
        if ( null != model ) {
        	List<Attribute> attributes = model.getAttributes();

        	for ( Attribute attribute : attributes )
				if ( DeveloperAppController.DISPLAY_NAME.equals( attribute.getName() ) ) {
					displayNameContentField.setText( attribute.getValue() );
					
					Document displayNameDocument = displayNameContentField.getDocument();
		            displayNameDocument.putProperty( PROPERTY_INDEX, attributes.indexOf( attribute ) );
					break;
				}
        	
        	for ( Attribute attribute : attributes )
				if ( DeveloperAppController.NOTES.equals( attribute.getName() ) ) {
					notesContentArea.setText( attribute.getValue() );
					
					Document notesContentDocument = notesContentArea.getDocument();
					notesContentDocument.putProperty( PROPERTY_INDEX, attributes.indexOf( attribute ) );
					break;
				}
        	
        	verify( displayNameContentField );
        } else {
        	displayNameContentField.setText( StringUtils.EMPTY );
            notesContentArea.setText( StringUtils.EMPTY );
        }
        
        nameContentField.setText( null != model ? model.getName() : StringUtils.EMPTY );
        displayNameContentField.setEditable( null != model );
        notesContentArea.setEditable( null != model );
        notesContentArea.setBackground( null != model ? null : nameContentField.getBackground() );
        productsPanel.removeAll();
        productsPanel.validate();
        
        GroupLayout productsPanelLayout = new GroupLayout( productsPanel );
        productsPanel.setLayout( productsPanelLayout );
        productsPanelLayout.setAutoCreateContainerGaps( true );
        productsPanelLayout.setAutoCreateGaps( true );
        
        Group productsPanelLayoutVerticalGroup = productsPanelLayout.createSequentialGroup();
        Group productsPanelLayoutHorizontalGroup = productsPanelLayout.createSequentialGroup();
        Group productsPanelLayoutHorizontalProductGroup = productsPanelLayout.createParallelGroup();
        Group productsPanelLayoutHorizontalDeleteGroup = productsPanelLayout.createParallelGroup( Alignment.TRAILING );
        
        if ( null != model ) {
            List<Product> products = new ArrayList<>();
            Group productsPanelNewProductGroup = productsPanelLayout.createParallelGroup();
            productsPanelNewProductGroup.addComponent( productNewButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
            
            productsPanelLayoutVerticalGroup.addGroup( productsPanelNewProductGroup );
            productsPanelLayoutHorizontalDeleteGroup.addComponent( productNewButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
            
            if ( null != model.getCredentials() && 1 == model.getCredentials().size() )
            	products = model.getCredentials().get( 0 ).getApiProducts();

    		for ( Product product : products ) {
                JTextField productName = new JTextField( product.getDisplayName(), 1 );
                productName.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
                productName.setInputVerifier( this );
                
                Document productDocument = productName.getDocument();
                productDocument.addDocumentListener( this );
                productDocument.putProperty( PROPERTY_INDEX, products.indexOf( product ) );
                productDocument.putProperty( PROPERTY_TYPE, PROPERTY_TYPE_PRO );
                productDocument.putProperty( PROPERTY_KV, DeveloperAppController.KEY );
                verify( productName );
                
                JButton productDeleteButton = new JButton();
                productDeleteButton.setText( configProperties.getComponentDeleteLabel() );
                productDeleteButton.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_DELPR, products.indexOf( product ) ) );
                productDeleteButton.addActionListener( this );
                
                Group productsPanelProductsGroup = productsPanelLayout.createParallelGroup();
                productsPanelProductsGroup.addComponent( productName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE );
                productsPanelProductsGroup.addComponent( productDeleteButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
                
                productsPanelLayoutVerticalGroup.addGroup( productsPanelProductsGroup );
                productsPanelLayoutHorizontalProductGroup.addComponent( productName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE );
                productsPanelLayoutHorizontalDeleteGroup.addComponent( productDeleteButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
            }
        }
        
        productsPanelLayout.setVerticalGroup( productsPanelLayoutVerticalGroup );
        productsPanelLayout.setHorizontalGroup( productsPanelLayoutHorizontalGroup.addGroup( productsPanelLayoutHorizontalProductGroup ).addGroup( productsPanelLayoutHorizontalDeleteGroup ) );
        
        attributesPanel.removeAll();
        attributesPanel.validate();
        
        GroupLayout attributesPanelLayout = new GroupLayout( attributesPanel );
        attributesPanel.setLayout( attributesPanelLayout );
        attributesPanelLayout.setAutoCreateContainerGaps( true );
        attributesPanelLayout.setAutoCreateGaps( true );
        
        Group attributesPanelLayoutVerticalGroup = attributesPanelLayout.createSequentialGroup();
        Group attributesPanelLayoutHorizontalGroup = attributesPanelLayout.createSequentialGroup();
        Group attributesPanelLayoutHorizontalNameGroup = attributesPanelLayout.createParallelGroup();
        Group attributesPanelLayoutHorizontalContentGroup = attributesPanelLayout.createParallelGroup();
        Group attributesPanelLayoutHorizontalDeleteGroup = attributesPanelLayout.createParallelGroup( Alignment.TRAILING );
        
        if ( null != model && null != model.getAttributes() ) {
            List<Attribute> attributes = model.getAttributes();
            JLabel emptyAttributeName = new JLabel();
            JLabel emptyAttributeContent = new JLabel();
            
            Group attributesPanelEnptyAttributeGroup = attributesPanelLayout.createParallelGroup();
            attributesPanelEnptyAttributeGroup.addComponent( emptyAttributeName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE );
            attributesPanelEnptyAttributeGroup.addComponent( emptyAttributeContent, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE );
            attributesPanelEnptyAttributeGroup.addComponent( attributeNewButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
            
            attributesPanelLayoutVerticalGroup.addGroup( attributesPanelEnptyAttributeGroup );
            attributesPanelLayoutHorizontalNameGroup.addComponent( emptyAttributeName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE );
            attributesPanelLayoutHorizontalContentGroup.addComponent( emptyAttributeContent, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE );
            attributesPanelLayoutHorizontalDeleteGroup.addComponent( attributeNewButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
            
            for ( Attribute attribute : attributes ) {
                if ( !DeveloperAppController.DISPLAY_NAME.equals( attribute.getName() ) && !DeveloperAppController.NOTES.equals( attribute.getName() ) ) {
                    JTextField attributeName = new JTextField( attribute.getName(), 1 );
                    attributeName.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
                    attributeName.setInputVerifier( this );
                    
                    Document attributeNameDocument = attributeName.getDocument();
                    attributeNameDocument.addDocumentListener( this );
                    attributeNameDocument.putProperty( PROPERTY_INDEX, attributes.indexOf( attribute ) );
                    attributeNameDocument.putProperty( PROPERTY_TYPE, PROPERTY_TYPE_ATT );
                    attributeNameDocument.putProperty( PROPERTY_KV, DeveloperAppController.KEY );
                    
                    JTextField attributeContent = new JTextField( attribute.getValue(), 1 );
                    attributeContent.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
                    attributeContent.setInputVerifier( this );
                    
                    Document attributeContentDocument = attributeContent.getDocument();
                    attributeContentDocument.addDocumentListener( this );
                    attributeContentDocument.putProperty( PROPERTY_INDEX, attributes.indexOf( attribute ) );
                    attributeContentDocument.putProperty( PROPERTY_TYPE, PROPERTY_TYPE_ATT );
                    attributeContentDocument.putProperty( PROPERTY_KV, DeveloperAppController.VALUE );
                    verify( attributeName );
                    verify( attributeContent );
                    
                    JButton attributeDeleteButton = new JButton();
                    attributeDeleteButton.setText( configProperties.getComponentDeleteLabel() );
                    attributeDeleteButton.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_DELAT, attributes.indexOf( attribute ) ) );
                    attributeDeleteButton.addActionListener( this );
                    
                    Group attributesPanelNewAttributeContentGroup = attributesPanelLayout.createParallelGroup();
                    attributesPanelNewAttributeContentGroup.addComponent( attributeName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE );
                    attributesPanelNewAttributeContentGroup.addComponent( attributeContent, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE );
                    attributesPanelNewAttributeContentGroup.addComponent( attributeDeleteButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
                    
                    attributesPanelLayoutVerticalGroup.addGroup( attributesPanelNewAttributeContentGroup );
                    attributesPanelLayoutHorizontalNameGroup.addComponent( attributeName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE );
                    attributesPanelLayoutHorizontalContentGroup.addComponent( attributeContent, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE );
                    attributesPanelLayoutHorizontalDeleteGroup.addComponent( attributeDeleteButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
                }
            }
        }
        
        attributesPanelLayoutHorizontalGroup.addGroup( attributesPanelLayoutHorizontalNameGroup );
        attributesPanelLayoutHorizontalGroup.addGroup( attributesPanelLayoutHorizontalContentGroup );
        attributesPanelLayoutHorizontalGroup.addGroup( attributesPanelLayoutHorizontalDeleteGroup );
        attributesPanelLayout.setVerticalGroup( attributesPanelLayoutVerticalGroup );
        attributesPanelLayout.setHorizontalGroup( attributesPanelLayoutHorizontalGroup );
        loading = false;
    }
    
    private void initializeStatic() {
    	nameLabel = new JLabel( configProperties.getComponentNameLabel() );
        displayNameLabel = new JLabel( configProperties.getComponentDisplayNameLabel() );
        notesLabel = new JLabel( configProperties.getComponentNotesLabel() );
        nameContentField = new JTextField( 1 );
        nameContentField.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
        nameContentField.setEditable( false );
        displayNameContentField = new JTextField( 1 );
        displayNameContentField.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
        displayNameContentField.setInputVerifier( this );
        displayNameContentField.getDocument().addDocumentListener( this );
        displayNameContentField.getDocument().putProperty( PROPERTY_TYPE, PROPERTY_TYPE_ATT );
        displayNameContentField.getDocument().putProperty( PROPERTY_KV, DeveloperAppController.VALUE );
        notesContentArea = new JTextArea( 3, 1 );
        notesContentArea.setFont( nameContentField.getFont() );
        notesContentArea.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
        notesContentArea.getDocument().addDocumentListener( this );
        notesContentArea.getDocument().putProperty( PROPERTY_TYPE, PROPERTY_TYPE_ATT );
        notesContentArea.getDocument().putProperty( PROPERTY_KV, DeveloperAppController.VALUE );
        notesContentScrollPane = new JScrollPane( notesContentArea );
        notesContentScrollPane.setBorder( null );
        attributeNewButton = new JButton();
        attributeNewButton.setText( configProperties.getComponentNewAttributeLabel() );
        attributeNewButton.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_ADDAT, null ) );
        attributeNewButton.addActionListener( this );
        productNewButton = new JButton();
        productNewButton.setText( configProperties.getComponentNewProductLabel() );
        productNewButton.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_ADDPR, null ) );
        productNewButton.addActionListener( this );
        namePanel = new JPanel( true );
        namePanel.setBorder( new TitledBorder( configProperties.getComponentDetailsLabel() ) );

        GroupLayout namePanelLayout = new GroupLayout( namePanel );
        namePanel.setLayout( namePanelLayout );
        namePanelLayout.setAutoCreateContainerGaps( true );
        namePanelLayout.setAutoCreateGaps( true );
        namePanelLayout.setVerticalGroup( namePanelLayout.createSequentialGroup().addGroup( namePanelLayout.createParallelGroup().addComponent( nameLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ).addComponent( nameContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ) ).addGroup( namePanelLayout.createParallelGroup().addComponent( displayNameLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ).addComponent( displayNameContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ) ).addGroup( namePanelLayout.createParallelGroup().addComponent( notesLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ).addComponent( notesContentScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ) ) );
        namePanelLayout.setHorizontalGroup( namePanelLayout.createSequentialGroup().addGroup( namePanelLayout.createParallelGroup().addComponent( nameLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ).addComponent( displayNameLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ).addComponent( notesLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ) ).addGroup( namePanelLayout.createParallelGroup().addComponent( nameContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ).addComponent( displayNameContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ).addComponent( notesContentScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) ) );
        
        productsPanel = new JPanel( true );
        productsPanel.setBorder( new TitledBorder( configProperties.getComponentProductsLabel() ) );
        attributesPanel = new JPanel( true );
        attributesPanel.setBorder( new TitledBorder( configProperties.getComponentAttributesLabel() ) );
        panel = new JPanel();
        
        GroupLayout panelLayout = new GroupLayout( panel );
        panel.setLayout( panelLayout );
        panelLayout.setAutoCreateContainerGaps( true );
        panelLayout.setAutoCreateGaps( true );
        panelLayout.setVerticalGroup( panelLayout.createSequentialGroup().addComponent( namePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ).addComponent( productsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ).addComponent( attributesPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ) );
        panelLayout.setHorizontalGroup( panelLayout.createParallelGroup().addComponent( namePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ).addComponent( productsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ).addComponent( attributesPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) );
        
        panelScrollPane = new JScrollPane( panel );
        panelScrollPane.setPreferredSize( new Dimension( 1, 1 ) );
        panelScrollPane.setBorder( null );
        
        JMenuItem newApp = new JMenuItem( configProperties.getUiMenuItemNew(), KeyEvent.VK_N );
        newApp.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_NEW, null ) );
        newApp.addActionListener( this );
        
        JMenuItem open = new JMenuItem( configProperties.getUiMenuItemOpen(), KeyEvent.VK_O );
        open.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_OPEN, null ) );
        open.addActionListener( this );
        
        JMenuItem save = new JMenuItem( configProperties.getUiMenuItemSave(), KeyEvent.VK_S );
        save.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_SAVE, null ) );
        save.addActionListener( this );
        
        JMenuItem saveAs = new JMenuItem( configProperties.getUiMenuItemSaveAs(), KeyEvent.VK_V );
        saveAs.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_SAVEAS, null ) );
        saveAs.addActionListener( this );
        
        JMenuItem reload = new JMenuItem( configProperties.getUiMenuItemReload(), KeyEvent.VK_R );
        reload.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_RELOAD, null ) );
        reload.addActionListener( this );
        
        JMenuItem verifyProducts = new JMenuItem( configProperties.getUiMenuItemVerifyProducts(), KeyEvent.VK_V );
        verifyProducts.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_VRPRO, null ) );
        verifyProducts.addActionListener( this );
        
        JMenuItem testUpload = new JMenuItem( configProperties.getUiMenuItemTestUpload(), KeyEvent.VK_T );
        testUpload.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_TSTUP, null ) );
        testUpload.addActionListener( this );
        
        JMenuItem setCredentials = new JMenuItem( configProperties.getUiMenuItemSetCredentials(), KeyEvent.VK_S );
        setCredentials.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_SETCR, null ) );
        setCredentials.addActionListener( this );
        
        JMenuItem getApiKey = new JMenuItem( configProperties.getUiMenuItemGetKey(), KeyEvent.VK_G );
        getApiKey.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_GETAP, null ) );
        getApiKey.addActionListener( this );
        
        JMenuItem loadWeb = new JMenuItem( configProperties.getUiMenuItemGetWeb(), KeyEvent.VK_L );
        loadWeb.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_GETWB, null ) );
        loadWeb.addActionListener( this );
        
        JMenu fileMenu = new JMenu( configProperties.getUiMenuFile() );
        fileMenu.setMnemonic( KeyEvent.VK_F );
        fileMenu.add( newApp );
        fileMenu.add( open );
        fileMenu.add( save );
        fileMenu.add( saveAs );
        fileMenu.add( reload );
                    
        JMenu webMenu = new JMenu( configProperties.getUiMenuWeb() );
        webMenu.setMnemonic( KeyEvent.VK_W );
        webMenu.add( setCredentials );
        webMenu.add( loadWeb );
        webMenu.add( getApiKey );
        webMenu.add( verifyProducts );
        webMenu.add( testUpload );
        
        JMenuBar menubar = new JMenuBar();
        menubar.add( fileMenu );
        menubar.add( webMenu );
        
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension window = new Dimension( screen.width / 3, screen.height / 3 );
        
        frame = new JFrame();
        frame.setBounds( ( screen.width / 2 ) - ( window.width / 2 ), ( screen.height / 2 ) - ( window.height / 2 ), window.width, window.height );
        frame.setPreferredSize( window );
        frame.setMinimumSize( window );
        frame.setMaximumSize( screen );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setJMenuBar( menubar );
        frame.setTitle( configProperties.getComponentApplicationLabel() );
        
        GroupLayout frameLayout = new GroupLayout( frame.getContentPane() );
        frame.getContentPane().invalidate();
        frame.getContentPane().setLayout( frameLayout );
        frameLayout.setAutoCreateContainerGaps( true );
        frameLayout.setAutoCreateGaps( true );
        frameLayout.setVerticalGroup( frameLayout.createSequentialGroup().addComponent( panelScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) );
        frameLayout.setHorizontalGroup( frameLayout.createSequentialGroup().addComponent( panelScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) );
    }
    
    @Override public boolean showMessage( Object message, boolean isError, boolean isYesNo ) {
        int response = -1;
        String title = isError ? configProperties.getUiTitleError() : configProperties.getUiTitleWarning();
        
        if ( isYesNo )
        	response = JOptionPane.showConfirmDialog( frame, message, title, JOptionPane.YES_NO_OPTION );
        else 
        	JOptionPane.showMessageDialog( frame, message, title, isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.WARNING_MESSAGE );

        if ( JOptionPane.YES_OPTION == response )
        	return true;
        else
        	return false;
    }
    
    @Override public Path getFileOrDirectory( boolean directoyOnly ) {
        JFileChooser chooser = getFileChooser( directoyOnly );
        
        if ( null != lastPath )
        	chooser.setCurrentDirectory( lastPath.toFile() );
        
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog( frame ) )
            return getSelectedFilePath( chooser );
    	else
            return null;
    }
    
    @Override public Path saveFileOrDirectory( boolean directoyOnly ) {
        JFileChooser chooser = getFileChooser( directoyOnly );
        
        if ( JFileChooser.APPROVE_OPTION == chooser.showSaveDialog( frame ) )
            return getSelectedFilePath( chooser );
        else
            return null;
    }
    
    private JFileChooser getFileChooser( boolean directoyOnly ) {
    	JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode( directoyOnly ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY );
        chooser.setDialogTitle( configProperties.getUiMessageFileSelection() );
        chooser.setMultiSelectionEnabled( false );
        
        return chooser;
    }
    
    private Path getSelectedFilePath( JFileChooser chooser )  {
    	File currentDirectory = chooser.getCurrentDirectory();
    	File selectedFile = chooser.getSelectedFile();
    	
    	lastPath = currentDirectory.toPath();
    	return selectedFile.toPath();
    }
    
    @Override public void actionPerformed( ActionEvent e ) {
        EventQueue.invokeLater( () -> {
            if ( !loading ) {
            	String actionCommand = e.getActionCommand();
            	Object source = e.getSource();
            	
                controller.commandExecuted( actionCommand, source );
                
                if ( !actionCommand.contains( DeveloperAppController.CMD_EDIPR ) &&
                	 !actionCommand.contains( DeveloperAppController.CMD_EDIAT ) &&
                	 !actionCommand.contains( DeveloperAppController.CMD_TSTUP ) &&
                	 !actionCommand.contains( DeveloperAppController.CMD_VRPRO ) &&
                	 !actionCommand.contains( DeveloperAppController.CMD_SETCR ) &&
                	 !actionCommand.contains( DeveloperAppController.CMD_GETAP ) )
                    initialize( false );
            }
        } );
    }
    
    @Override public boolean verify( JComponent input ) {
        if ( input instanceof JTextField ) {
            JTextField component = ( JTextField ) input;
            
            if ( StringUtils.isEmpty( component.getText() ) ) {
                component.setBackground( Color.PINK );
                component.setToolTipText( configProperties.getUiMessageEmptyError() );
                EventQueue.invokeLater( () -> component.requestFocus() );
                return false;
            } else {
                component.setBackground( null );
                component.setToolTipText( null );
                return true;
            }
        }
        
        return false;
    }
    
    @Override public void run() {
    	initialize( true );
        enableOSX( frame );
        frame.setVisible( true );
    }
    
    private void enableOSX( JFrame frame ) {
        try {
            Class<?> utility = Class.forName( LF_APPLE_UTIL );
            Class<?> params[] = new Class[] { Window.class, Boolean.TYPE };
            Method method = utility.getMethod( LF_APPLE_FULLSCREEN, params );
            method.invoke( utility, frame, true );
        } catch ( Exception e ) {
            log.warn( configProperties.getUiMessageOSXError() );
        }
    }
    
    @Override public void insertUpdate( DocumentEvent e ) {
        EventQueue.invokeLater( () -> documentUpdate( e.getDocument() ) );
    }

    @Override public void removeUpdate( DocumentEvent e ) {
        EventQueue.invokeLater( () -> documentUpdate( e.getDocument() ) );
    }

    @Override public void changedUpdate( DocumentEvent e ) {
        EventQueue.invokeLater( () -> documentUpdate( e.getDocument() ) );
    }
    
    private void documentUpdate( Document document ) {
        if ( !loading ) {
            try {
                String change = document.getText( 0, document.getLength() );
                String type = String.valueOf( document.getProperty( PROPERTY_TYPE ) );
                String index = String.valueOf( document.getProperty( PROPERTY_INDEX ) );
                String kv = String.valueOf( document.getProperty( PROPERTY_KV ) );
                
                if ( lastChange.equals( change ) && lastType.equals( type ) && lastIndex.equals( index ) && lastKv.equals( kv ) )
                    return;
                else {
                    if ( PROPERTY_TYPE_ATT.equals( type ) ) {
                        Attribute changes = new Attribute();
                        
                        if ( DeveloperAppController.KEY.equals( kv ) )
                        	changes.setName( change );
                        else if ( DeveloperAppController.VALUE.equals( kv ) )
                        	changes.setValue( change );
    
                        ActionEvent action = new ActionEvent( changes, ActionEvent.ACTION_PERFORMED, controller.getCommandForIndex( DeveloperAppController.CMD_EDIAT, index ) );
                        actionPerformed( action );
                    } else if ( PROPERTY_TYPE_PRO.equals( type ) ) {
                        ActionEvent action = new ActionEvent( change, ActionEvent.ACTION_PERFORMED, controller.getCommandForIndex( DeveloperAppController.CMD_EDIPR, index ) );
                        actionPerformed( action );
                    }
                    
                    lastChange = change;
                    lastType = type;
                    lastIndex = index;
                    lastKv = kv;
                }
            } catch ( Exception ex ) {
                log.error( configProperties.getUiMessageGraphicalError(), ex );
            }
        }
    }
    
    @Override public String requestNewInput( String command, String message ) {
    	int response = -1;
    	InputPanel inputPanel = null;
    	
    	switch ( command ) {
            case DeveloperAppController.CMD_SETCR:
                inputPanel = getLoginPanel( message ); break;
            default:
                inputPanel = getMessagePanel( message ); break;
        }
    	
    	response = JOptionPane.showConfirmDialog( frame, inputPanel, StringUtils.EMPTY, JOptionPane.OK_CANCEL_OPTION );
        
        if ( JOptionPane.OK_OPTION == response )
            return inputPanel.getInput();
        else
            return null;
    }
    
    private InputPanel getLoginPanel( String message ) {
        JLabel messageLabel = new JLabel( message );
    	JLabel userLabel = new JLabel( configProperties.getCredentialsUserLabel() );
    	JLabel passwordLabel = new JLabel( configProperties.getCredentialsPasswordLabel() );
    	JLabel organizationLabel = new JLabel( configProperties.getCredentialsOrganizationLabel() );
    	JLabel developerLabel = new JLabel( configProperties.getCredentialsDeveloperLabel() );

    	JTextField userContentField = new JTextField( lastCredentials.getUser(), 1 );
    	JPasswordField passwordContentField = new JPasswordField( lastCredentials.getPassword(), 1 );
    	JTextField organizationContentField = new JTextField( lastCredentials.getOrganization(), 1 );
    	JTextField developerContentField = new JTextField( lastCredentials.getDeveloper(), 1 );
    	
    	userContentField.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
    	passwordContentField.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
    	organizationContentField.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
    	developerContentField.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
    	
    	InputPanel loginPanel = new InputPanel( true );
    	GroupLayout loginPanelLayout = new GroupLayout( loginPanel );
    	
    	Group loginPanelVerticalMessageGroup = loginPanelLayout.createSequentialGroup();
    	loginPanelVerticalMessageGroup.addComponent( messageLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
    	
    	Group loginPanelUserGroup = loginPanelLayout.createParallelGroup();
    	loginPanelUserGroup.addComponent( userLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
    	loginPanelUserGroup.addComponent( userContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
    	
    	Group loginPanelPasswordGroup = loginPanelLayout.createParallelGroup();
    	loginPanelPasswordGroup.addComponent( passwordLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
    	loginPanelPasswordGroup.addComponent( passwordContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
    	
    	Group loginPanelOrganizationGroup = loginPanelLayout.createParallelGroup();
    	loginPanelOrganizationGroup.addComponent( organizationLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
    	loginPanelOrganizationGroup.addComponent( organizationContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
    	
    	Group loginPanelDeveloperGroup = loginPanelLayout.createParallelGroup();
    	loginPanelDeveloperGroup.addComponent( developerLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
    	loginPanelDeveloperGroup.addComponent( developerContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
    	
    	Group loginPanelVerticalCredentialGroup = loginPanelLayout.createSequentialGroup();
		loginPanelVerticalCredentialGroup.addGroup( loginPanelUserGroup );
		loginPanelVerticalCredentialGroup.addGroup( loginPanelPasswordGroup );
		loginPanelVerticalCredentialGroup.addGroup( loginPanelOrganizationGroup );
		loginPanelVerticalCredentialGroup.addGroup( loginPanelDeveloperGroup );
    	
    	Group loginPanelVerticalGroup = loginPanelLayout.createSequentialGroup();
    	loginPanelVerticalGroup.addGroup( loginPanelVerticalMessageGroup );
    	loginPanelVerticalGroup.addGroup( loginPanelVerticalCredentialGroup );
    	
    	Group loginPanelHorizontalMessageGroup = loginPanelLayout.createSequentialGroup();
    	loginPanelHorizontalMessageGroup.addComponent( messageLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
    	
    	Group loginPanelLabelGroup = loginPanelLayout.createParallelGroup();
    	loginPanelLabelGroup.addComponent( userLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
    	loginPanelLabelGroup.addComponent( passwordLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
    	loginPanelLabelGroup.addComponent( organizationLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
    	loginPanelLabelGroup.addComponent( developerLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
		
		Group loginPanelContentGroup = loginPanelLayout.createParallelGroup();
		loginPanelContentGroup.addComponent( userContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE );
		loginPanelContentGroup.addComponent( passwordContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE );
		loginPanelContentGroup.addComponent( organizationContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE );
		loginPanelContentGroup.addComponent( developerContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE );
    	
    	Group loginPanelHorizontalCredentialGroup = loginPanelLayout.createSequentialGroup();
    	loginPanelHorizontalCredentialGroup.addGroup( loginPanelLabelGroup );
    	loginPanelHorizontalCredentialGroup.addGroup( loginPanelContentGroup );
    	
    	Group loginPanelHorizontalGroup = loginPanelLayout.createParallelGroup();
    	loginPanelHorizontalGroup.addGroup( loginPanelHorizontalMessageGroup );
    	loginPanelHorizontalGroup.addGroup( loginPanelHorizontalCredentialGroup );
    	
    	loginPanel.setLayout( loginPanelLayout );
    	loginPanelLayout.setAutoCreateContainerGaps( true );
    	loginPanelLayout.setAutoCreateGaps( true );
    	loginPanelLayout.setVerticalGroup( loginPanelVerticalGroup );
    	loginPanelLayout.setHorizontalGroup( loginPanelHorizontalGroup );
    	
    	Action action = new Action() {
    		@Override public void actionPerformed( ActionEvent e ) {}
    		@Override public void setEnabled( boolean b ) {}
    		@Override public void removePropertyChangeListener( PropertyChangeListener listener ) {}
    		@Override public void putValue( String key, Object value ) {}
    		@Override public void addPropertyChangeListener( PropertyChangeListener listener ) {}
    		@Override public boolean isEnabled() { return true; }
    		
    		@Override public Object getValue( String key ) {
    			String user = userContentField.getText();
    			String password = new String( passwordContentField.getPassword() );
    			String organization = organizationContentField.getText();
    			String developer = developerContentField.getText();
    			
    			if ( !StringUtils.isEmpty( user ) && !StringUtils.isEmpty( password ) && !StringUtils.isEmpty( organization ) ) {
    				LoginCredentials loginCredentials = new LoginCredentials();
    				loginCredentials.setUser( user );
    				loginCredentials.setPassword( password );
    				loginCredentials.setOrganization( organization );
    				loginCredentials.setDeveloper( developer );
    				
    				lastCredentials.setUser( user );
    				lastCredentials.setPassword( password );
    				lastCredentials.setOrganization( organization );
    				lastCredentials.setDeveloper( developer );
    				
    				return gson.toJson( loginCredentials, LoginCredentials.class );
    			} else
    				return null;
    		}
    	};
    	
    	loginPanel.setPreferredSize( new Dimension( 384, loginPanel.getPreferredSize().height ) );
    	loginPanel.setAction( action );
    	return loginPanel;
    }
    
    private InputPanel getMessagePanel( String message ) {
    	JLabel messageLabel = new JLabel( message );
    	JTextField messageContentField = new JTextField( 1 );
    	messageContentField.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
    	
    	InputPanel messagePanel = new InputPanel( true );
    	GroupLayout messagePanelLayout = new GroupLayout( messagePanel );
    	messagePanel.setLayout( messagePanelLayout );
    	
    	Group messagePanelVerticalGroup = messagePanelLayout.createSequentialGroup();
    	messagePanelVerticalGroup.addComponent( messageLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
    	messagePanelVerticalGroup.addComponent( messageContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
		
    	Group messagePanelHorizontalGroup = messagePanelLayout.createParallelGroup();
    	messagePanelHorizontalGroup.addComponent( messageLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
    	messagePanelHorizontalGroup.addComponent( messageContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE );
    	
    	messagePanelLayout.setAutoCreateContainerGaps( true );
    	messagePanelLayout.setAutoCreateGaps( true );
    	messagePanelLayout.setVerticalGroup( messagePanelVerticalGroup );
    	messagePanelLayout.setHorizontalGroup( messagePanelHorizontalGroup );
    	
    	Action action = new Action() {
    		@Override public void actionPerformed( ActionEvent e ) {}
    		@Override public void setEnabled( boolean b ) {}
    		@Override public void removePropertyChangeListener( PropertyChangeListener listener ) {}
    		@Override public void putValue( String key, Object value ) {}
    		@Override public void addPropertyChangeListener( PropertyChangeListener listener ) {}
    		@Override public boolean isEnabled() { return true; }
    		
    		@Override public Object getValue( String key ) {
    			return messageContentField.getText();
    		}
    	};
    	
    	messagePanel.setPreferredSize( new Dimension( 384, messagePanel.getPreferredSize().height ) );
    	messagePanel.setAction( action );
    	return messagePanel;
    }
}