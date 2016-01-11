package com.digitaslbi.apigee.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.GroupLayout;
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
import com.digitaslbi.apigee.model.DeveloperApp;
import com.digitaslbi.apigee.model.LoginCredentials;
import com.digitaslbi.apigee.model.Product;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of DeveloperAppView
 * 
 * @author Victor Ortiz
 */
@Slf4j
@Component
public class DeveloperAppViewImpl extends InputVerifier implements DeveloperAppView {
    private static final String APP_ATTRIBUTES = "Attributes";
    private static final String APP_DETAILS = "Details";
    private static final String APP_PRODUCTS = "Products";
    private static final String DELETE = "Delete";
    private static final String DISPLAY_NAME = "Display name:";
    private static final String MNIT_NEW = "New";
    private static final String MNIT_OPEN = "Open";
    private static final String MNIT_SAVE = "Save";
    private static final String MNIT_SAVEAS = "Save as";
    private static final String MNIT_RELOAD = "Reload";
    private static final String MNIT_ADDAT = "Add attribute";
    private static final String MNIT_ADDPR = "Add product";
    private static final String MNIT_TSTUP = "Test upload";
    private static final String MNIT_VRPRO = "Verify products";
    private static final String MNIT_GETAP = "Get API key";
    private static final String MNIT_SETCR = "Set credentials";
    private static final String MNIT_GETWB = "Load from web";
    private static final String MENU_FILE = "File";
    private static final String MENU_EDIT = "Edit";
    private static final String MENU_WEB = "Web";
    private static final String NAME = "Name:";
    private static final String NIMBUS = "Nimbus";
    private static final String NOTES = "Notes:";
    private static final String PROPERTY_NAME = "PROPERTY";
    private static final String PROPERTY_TYPE = "TYPE";
    private static final String PROPERTY_TYPE_ATT = "ATTRIB";
    private static final String PROPERTY_TYPE_PRO = "PRODUC";
    private static final String PROPERTY_KV = "KEYVALUE";
    
    private static final String LF_APPLE_MENU = "apple.laf.useScreenMenuBar";
    private static final String LF_APPLE_FULLSCREEN = "setWindowCanFullScreen";
    private static final String LF_APPLE_UTIL = "com.apple.eawt.FullScreenUtilities";
    
    private static final String MSG_ADD_TITLE = "New";
    private static final String MSG_EMPTY_NOT_ALLOWED = "This field does not accept empty values";
    private static final String MSG_ERROR = "Error";
    private static final String MSG_FILE_DIR_TITLE = "Please select a file/directory";
    private static final String MSG_FS_OSX_ERROR = "Could not initialize fullscreen for OS X";
    private static final String MSG_GRAPHICAL_ERROR = "A graphical interface error has ocurred";
    private static final String MSG_WARNING = "Warning";
    
    private static final String USER = "User:";
    private static final String PASSWORD = "Password";
    private static final String ORGANIZATION = "Organization:";
    
    private JFrame frame;
    
    private JLabel nameLabel;
    private JLabel displayNameLabel;
    private JLabel notesLabel;
    
    private JTextField nameContentField;
    private JTextField displayNameContentField;
    private JTextArea notesContentArea;
    
    private JScrollPane notesContentScrollPane;
    private JScrollPane panelScrollPane;
    
    private JPanel namePanel;
    private JPanel productsPanel;
    private JPanel attributesPanel;
    private JPanel panel;
    
    private boolean loading;
    private String lastChange;
    private String lastType;
    private String lastProperty;
    private String lastKv;
    private String lastUser;
    private String lastPassword;
    private String lastOrganization;
    private Path currentPath;

    @Autowired private DeveloperAppController controller;
    
    public DeveloperAppViewImpl() {
        this.lastChange = StringUtils.EMPTY;
        this.lastType = StringUtils.EMPTY;
        this.lastProperty = StringUtils.EMPTY;
        this.lastKv = StringUtils.EMPTY;
        this.lastUser = StringUtils.EMPTY;
        this.lastPassword = StringUtils.EMPTY;
        this.lastOrganization = StringUtils.EMPTY;
        
        System.setProperty( LF_APPLE_MENU, Boolean.TRUE.toString() );
        boolean nimbusSet = false;
        
        for ( LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
        	if ( NIMBUS.equals( info.getName() ) ) {
        		try {
        			UIManager.setLookAndFeel( info.getClassName() );
        			nimbusSet = true;
        		} catch ( Exception e ) {
        			log.error( MSG_GRAPHICAL_ERROR, e );
        		}
        		
        		break;
        	}
        }
        
        if ( !nimbusSet )
        	try {
            	UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
			} catch ( Exception ex ) {
				log.error( MSG_GRAPHICAL_ERROR, ex );
			}
    }
    
    @Override public void initialize( boolean firstTime ) {
        DeveloperApp model = controller.getModel();
        loading = true;
        
        if ( firstTime ) {
            nameLabel = new JLabel( NAME );
            displayNameLabel = new JLabel( DISPLAY_NAME );
            notesLabel = new JLabel( NOTES );
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
            namePanel = new JPanel( true );
            namePanel.setBorder( new TitledBorder( APP_DETAILS ) );

            GroupLayout namePanelLayout = new GroupLayout( namePanel );
            namePanel.setLayout( namePanelLayout );
            namePanelLayout.setAutoCreateContainerGaps( true );
            namePanelLayout.setAutoCreateGaps( true );
            namePanelLayout.setVerticalGroup( namePanelLayout.createSequentialGroup().addGroup( namePanelLayout.createParallelGroup().addComponent( nameLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ).addComponent( nameContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ) ).addGroup( namePanelLayout.createParallelGroup().addComponent( displayNameLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ).addComponent( displayNameContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ) ).addGroup( namePanelLayout.createParallelGroup().addComponent( notesLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ).addComponent( notesContentScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ) ) );
            namePanelLayout.setHorizontalGroup( namePanelLayout.createSequentialGroup().addGroup( namePanelLayout.createParallelGroup().addComponent( nameLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ).addComponent( displayNameLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ).addComponent( notesLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ) ).addGroup( namePanelLayout.createParallelGroup().addComponent( nameContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ).addComponent( displayNameContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ).addComponent( notesContentScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) ) );
            
            productsPanel = new JPanel( true );
            productsPanel.setBorder( new TitledBorder( APP_PRODUCTS ) );
            attributesPanel = new JPanel( true );
            attributesPanel.setBorder( new TitledBorder( APP_ATTRIBUTES ) );
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
            
            JMenuItem newApp = new JMenuItem( MNIT_NEW, KeyEvent.VK_N );
            newApp.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_NEW, null ) );
            newApp.addActionListener( this );
            
            JMenuItem open = new JMenuItem( MNIT_OPEN, KeyEvent.VK_O );
            open.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_OPEN, null ) );
            open.addActionListener( this );
            
            JMenuItem save = new JMenuItem( MNIT_SAVE, KeyEvent.VK_S );
            save.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_SAVE, null ) );
            save.addActionListener( this );
            
            JMenuItem saveAs = new JMenuItem( MNIT_SAVEAS, KeyEvent.VK_V );
            saveAs.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_SAVEAS, null ) );
            saveAs.addActionListener( this );
            
            JMenuItem reload = new JMenuItem( MNIT_RELOAD, KeyEvent.VK_R );
            reload.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_RELOAD, null ) );
            reload.addActionListener( this );
            
            JMenuItem addAttrib = new JMenuItem( MNIT_ADDAT, KeyEvent.VK_A );
            addAttrib.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_ADDAT, null ) );
            addAttrib.addActionListener( this );
            
            JMenuItem addProduct = new JMenuItem( MNIT_ADDPR, KeyEvent.VK_P );
            addProduct.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_ADDPR, null ) );
            addProduct.addActionListener( this );
            
            JMenuItem verifyProducts = new JMenuItem( MNIT_VRPRO, KeyEvent.VK_V );
            verifyProducts.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_VRPRO, null ) );
            verifyProducts.addActionListener( this );
            
            JMenuItem testUpload = new JMenuItem( MNIT_TSTUP, KeyEvent.VK_T );
            testUpload.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_TSTUP, null ) );
            testUpload.addActionListener( this );
            
            JMenuItem setCredentials = new JMenuItem( MNIT_SETCR, KeyEvent.VK_S );
            setCredentials.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_SETCR, null ) );
            setCredentials.addActionListener( this );
            
            JMenuItem getApiKey = new JMenuItem( MNIT_GETAP, KeyEvent.VK_G );
            getApiKey.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_GETAP, null ) );
            getApiKey.addActionListener( this );
            
            JMenuItem loadWeb = new JMenuItem( MNIT_GETWB, KeyEvent.VK_L );
            loadWeb.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_GETWB, null ) );
            loadWeb.addActionListener( this );
            
            JMenu fileMenu = new JMenu( MENU_FILE );
            fileMenu.setMnemonic( KeyEvent.VK_F );
            fileMenu.add( newApp );
            fileMenu.add( open );
            fileMenu.add( save );
            fileMenu.add( saveAs );
            fileMenu.add( reload );
            
            JMenu editMenu = new JMenu( MENU_EDIT );
            editMenu.setMnemonic( KeyEvent.VK_E );
            editMenu.add( addAttrib );
            editMenu.add( addProduct );
            
            JMenu webMenu = new JMenu( MENU_WEB );
            webMenu.setMnemonic( KeyEvent.VK_W );
            webMenu.add( setCredentials );
            webMenu.add( loadWeb );
            webMenu.add( getApiKey );
            webMenu.add( verifyProducts );
            webMenu.add( testUpload );
            
            JMenuBar menubar = new JMenuBar();
            menubar.add( fileMenu );
            menubar.add( editMenu );
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
            
            GroupLayout frameLayout = new GroupLayout( frame.getContentPane() );
            frame.getContentPane().invalidate();
            frame.getContentPane().setLayout( frameLayout );
            frameLayout.setAutoCreateContainerGaps( true );
            frameLayout.setAutoCreateGaps( true );
            frameLayout.setVerticalGroup( frameLayout.createSequentialGroup().addComponent( panelScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) );
            frameLayout.setHorizontalGroup( frameLayout.createSequentialGroup().addComponent( panelScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) );
        }
        
        if ( null != model ) {
        	List<Attribute> attributes = model.getAttributes();

        	for ( Attribute attribute : attributes )
				if ( DeveloperAppController.DISPLAY_NAME.equals( attribute.getName() ) ) {
					displayNameContentField.setText( attribute.getValue() );
		            displayNameContentField.getDocument().putProperty( PROPERTY_NAME, attributes.indexOf( attribute ) );
					break;
				}
        	
        	for ( Attribute attribute : attributes )
				if ( DeveloperAppController.NOTES.equals( attribute.getName() ) ) {
					notesContentArea.setText( attribute.getValue() );
					notesContentArea.getDocument().putProperty( PROPERTY_NAME, attributes.indexOf( attribute ) );
					break;
				}
        } else {
        	displayNameContentField.setText( StringUtils.EMPTY );
            notesContentArea.setText( StringUtils.EMPTY );
        }
        
        nameContentField.setText( null != model ? model.getName() : StringUtils.EMPTY );
        displayNameContentField.setEditable( null != model );
        notesContentArea.setEditable( null != model );
        notesContentArea.setBackground( null != model ? null : nameContentField.getBackground() );
        
        if ( null != controller.getModel() )
            verify( displayNameContentField );
        
        productsPanel.removeAll();
        productsPanel.validate();
        
        GroupLayout productsPanelLayout = new GroupLayout( productsPanel );
        productsPanel.setLayout( productsPanelLayout );
        productsPanelLayout.setAutoCreateContainerGaps( true );
        productsPanelLayout.setAutoCreateGaps( true );
        
        Group productsPanelLayoutVerticalGroup = productsPanelLayout.createSequentialGroup();
        Group productsPanelLayoutHorizontalGroup = productsPanelLayout.createSequentialGroup();
        Group productsPanelLayoutHorizontalProductGroup = productsPanelLayout.createParallelGroup();
        Group productsPanelLayoutHorizontalDeleteGroup = productsPanelLayout.createParallelGroup();
        
        if ( null != model ) {
            List<Product> products = new ArrayList<>();
            
            if ( null != model.getCredentials() && 1 == model.getCredentials().size() )
            	products = model.getCredentials().get( 0 ).getApiProducts();

    		for ( Product product : products ) {
                JTextField productName = new JTextField( product.getDisplayName(), 1 );
                productName.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
                productName.setInputVerifier( this );
                productName.getDocument().addDocumentListener( this );
                productName.getDocument().putProperty( PROPERTY_NAME, products.indexOf( product ) );
                productName.getDocument().putProperty( PROPERTY_TYPE, PROPERTY_TYPE_PRO );
                productName.getDocument().putProperty( PROPERTY_KV, DeveloperAppController.KEY );
                verify( productName );
                
                JButton productDeleteButton = new JButton();
                productDeleteButton.setText( DELETE );
                productDeleteButton.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_DELPR, products.indexOf( product ) ) );
                productDeleteButton.addActionListener( this );
                
                productsPanelLayoutVerticalGroup.addGroup( productsPanelLayout.createParallelGroup().addComponent( productName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ).addComponent( productDeleteButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ) );
                productsPanelLayoutHorizontalProductGroup.addComponent( productName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE );
                productsPanelLayoutHorizontalDeleteGroup.addComponent( productDeleteButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
            }
        }
        
        productsPanelLayout.setVerticalGroup( productsPanelLayoutVerticalGroup );
        productsPanelLayout.setHorizontalGroup( productsPanelLayoutHorizontalGroup );
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
        Group attributesPanelLayoutHorizontalDeleteGroup = attributesPanelLayout.createParallelGroup();
        
        if ( null != model && null != model.getAttributes() ) {
            List<Attribute> attributes = model.getAttributes();
            
            for ( Attribute attribute : attributes ) {
                if ( !DeveloperAppController.DISPLAY_NAME.equals( attribute.getName() ) && !DeveloperAppController.NOTES.equals( attribute.getName() ) ) {
                    JTextField attributeName = new JTextField( attribute.getName(), 1 );
                    attributeName.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
                    attributeName.setInputVerifier( this );
                    attributeName.getDocument().addDocumentListener( this );
                    attributeName.getDocument().putProperty( PROPERTY_NAME, attributes.indexOf( attribute ) );
                    attributeName.getDocument().putProperty( PROPERTY_TYPE, PROPERTY_TYPE_ATT );
                    attributeName.getDocument().putProperty( PROPERTY_KV, DeveloperAppController.KEY );
                    
                    JTextField attributeContent = new JTextField( attribute.getValue(), 1 );
                    attributeContent.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
                    attributeContent.setInputVerifier( this );
                    attributeContent.getDocument().addDocumentListener( this );
                    attributeContent.getDocument().putProperty( PROPERTY_NAME, attributes.indexOf( attribute ) );
                    attributeContent.getDocument().putProperty( PROPERTY_TYPE, PROPERTY_TYPE_ATT );
                    attributeContent.getDocument().putProperty( PROPERTY_KV, DeveloperAppController.VALUE );
                    verify( attributeName );
                    verify( attributeContent );
                    
                    JButton attributeDeleteButton = new JButton();
                    attributeDeleteButton.setText( DELETE );
                    attributeDeleteButton.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_DELAT, attributes.indexOf( attribute ) ) );
                    attributeDeleteButton.addActionListener( this );
                    
                    attributesPanelLayoutVerticalGroup.addGroup( attributesPanelLayout.createParallelGroup().addComponent( attributeName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ).addComponent( attributeContent, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ).addComponent( attributeDeleteButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ) );
                    attributesPanelLayoutHorizontalNameGroup.addComponent( attributeName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE );
                    attributesPanelLayoutHorizontalContentGroup.addComponent( attributeContent, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE );
                    attributesPanelLayoutHorizontalDeleteGroup.addComponent( attributeDeleteButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE );
                }
            }
        }
        
        attributesPanelLayout.setVerticalGroup( attributesPanelLayoutVerticalGroup );
        attributesPanelLayout.setHorizontalGroup( attributesPanelLayoutHorizontalGroup.addGroup( attributesPanelLayoutHorizontalNameGroup ).addGroup( attributesPanelLayoutHorizontalContentGroup ).addGroup( attributesPanelLayoutHorizontalDeleteGroup ) );
        loading = false;
    }
    
    @Override public Path getFileOrDirectory( boolean directoyOnly ) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode( directoyOnly ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY );
        chooser.setDialogTitle( MSG_FILE_DIR_TITLE );
        chooser.setMultiSelectionEnabled( false );
        
        if ( null != currentPath )
        	chooser.setCurrentDirectory( currentPath.toFile() );
        
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog( frame ) ) {
        	currentPath = chooser.getCurrentDirectory().toPath();
            return chooser.getSelectedFile().toPath();
    	} else
            return null;
    }
    
    @Override public Path saveFileOrDirectory( boolean directoyOnly ) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode( directoyOnly ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY );
        chooser.setDialogTitle( MSG_FILE_DIR_TITLE );
        chooser.setMultiSelectionEnabled( false );
        
        if ( JFileChooser.APPROVE_OPTION == chooser.showSaveDialog( frame ) ) {
        	currentPath = chooser.getCurrentDirectory().toPath();
            return chooser.getSelectedFile().toPath();
        } else
            return null;
    }
    
    @Override public boolean showMessage( Object message, boolean isError, boolean isYesNo ) {
        int response = -1;
        
        if ( isYesNo )
        	response = JOptionPane.showConfirmDialog( frame, message, isError ? MSG_ERROR : MSG_WARNING, JOptionPane.YES_NO_OPTION );
        else 
        	JOptionPane.showMessageDialog( frame, message, isError ? MSG_ERROR : MSG_WARNING, isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.WARNING_MESSAGE );

        if ( JOptionPane.YES_OPTION == response )
        	return true;
        else
        	return false;
    }
    
    @Override public String requestNewInput( String message ) {
    	int response = -1;
    	
    	if ( null != message ) {
    		InputPanel inputPanel = getMessagePanel( message );
    		response = JOptionPane.showConfirmDialog( frame, inputPanel, MSG_ADD_TITLE, JOptionPane.OK_CANCEL_OPTION );
    		
    		if ( JOptionPane.OK_OPTION == response )
	            return inputPanel.getInput();
	        else
	            return null;
        } else {
        	InputPanel inputPanel = getLoginPanel();
        	response = JOptionPane.showConfirmDialog( frame, inputPanel, MSG_ADD_TITLE, JOptionPane.OK_CANCEL_OPTION );
        	
        	if ( JOptionPane.OK_OPTION == response )
	            return inputPanel.getInput();
	        else
	            return null;
        }
    }
    
    @Override public void run() {
    	initialize( true );
        enableOSX( frame );
        frame.setVisible( true );
    }
    
    @Override public void actionPerformed( ActionEvent e ) {
        EventQueue.invokeLater( () -> {
            if ( !loading ) {
                controller.commandExecuted( e.getActionCommand(), e.getSource() );
                
                if ( !e.getActionCommand().contains( DeveloperAppController.CMD_EDIPR ) && !e.getActionCommand().contains( DeveloperAppController.CMD_EDIAT ) && !e.getActionCommand().contains( DeveloperAppController.CMD_TSTUP ) && !e.getActionCommand().contains( DeveloperAppController.CMD_VRPRO ) && !e.getActionCommand().contains( DeveloperAppController.CMD_SETCR ) && !e.getActionCommand().contains( DeveloperAppController.CMD_GETAP ))
                    initialize( false );
            }
        } );
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
    
    @Override public boolean verify( JComponent input ) {
        if ( input instanceof JTextField ) {
            JTextField component = ( JTextField ) input;
            
            if ( StringUtils.isEmpty( component.getText() ) ) {
                component.setBackground( Color.PINK );
                component.setToolTipText( MSG_EMPTY_NOT_ALLOWED );
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
    
    private void enableOSX( JFrame frame ) {
        try {
            Class<?> utility = Class.forName( LF_APPLE_UTIL );
            Class<?> params[] = new Class[]{ Window.class, Boolean.TYPE };
            Method method = utility.getMethod( LF_APPLE_FULLSCREEN, params );
            method.invoke( utility, frame, true );
        } catch ( Exception e ) {
            log.warn( MSG_FS_OSX_ERROR );
        }
    }
    
    private void documentUpdate( Document document ) {
        if ( !loading ) {
            try {
                String change = document.getText( 0, document.getLength() );
                String type = document.getProperty( PROPERTY_TYPE ).toString();
                String property = document.getProperty( PROPERTY_NAME ).toString();
                String kv = document.getProperty( PROPERTY_KV ).toString();
                
                if ( lastChange.equals( change ) && lastType.equals( type ) && lastProperty.equals( property ) && lastKv.equals( kv ) )
                    return;
                else {
                    if ( PROPERTY_TYPE_ATT.equals( type ) ) {
                        Attribute changes = new Attribute();
                        
                        if ( DeveloperAppController.KEY.equals( kv ) )
                        	changes.setName( change );
                        else if ( DeveloperAppController.VALUE.equals( kv ) )
                        	changes.setValue( change );
    
                        ActionEvent action = new ActionEvent( changes, ActionEvent.ACTION_PERFORMED, controller.getCommandForIndex( DeveloperAppController.CMD_EDIAT, property ) );
                        actionPerformed( action );
                    } else if ( PROPERTY_TYPE_PRO.equals( type ) ) {
                        ActionEvent action = new ActionEvent( change, ActionEvent.ACTION_PERFORMED, controller.getCommandForIndex( DeveloperAppController.CMD_EDIPR, property ) );
                        actionPerformed( action );
                    }
                    
                    lastChange = change;
                    lastType = type;
                    lastProperty = property;
                    lastKv = kv;
                }
            } catch ( Exception ex ) {
                log.error( MSG_GRAPHICAL_ERROR, ex );
            }
        }
    }
    
    private InputPanel getLoginPanel() {
    	JLabel userLabel = new JLabel( USER );
    	JLabel passwordLabel = new JLabel( PASSWORD );
    	JLabel organizationLabel = new JLabel( ORGANIZATION );

    	JTextField userContentField = new JTextField( lastUser, 1 );
    	JPasswordField passwordContentField = new JPasswordField( lastPassword, 1 );
    	JTextField organizationContentField = new JTextField( lastOrganization, 1 );
    	
    	userContentField.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
    	passwordContentField.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
    	organizationContentField.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
    	
    	InputPanel loginPanel = new InputPanel( true );
    	GroupLayout loginPanelLayout = new GroupLayout( loginPanel );
    	loginPanel.setLayout( loginPanelLayout );
    	loginPanelLayout.setAutoCreateContainerGaps( true );
    	loginPanelLayout.setAutoCreateGaps( true );
    	loginPanelLayout.setVerticalGroup( loginPanelLayout.createSequentialGroup().addGroup( loginPanelLayout.createParallelGroup().addComponent( userLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ).addComponent( userContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ) ).addGroup( loginPanelLayout.createParallelGroup().addComponent( passwordLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ).addComponent( passwordContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ) ).addGroup( loginPanelLayout.createParallelGroup().addComponent( organizationLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ).addComponent( organizationContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ) ).addGroup( loginPanelLayout.createParallelGroup() ) );
    	loginPanelLayout.setHorizontalGroup( loginPanelLayout.createSequentialGroup().addGroup( loginPanelLayout.createParallelGroup().addComponent( userLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ).addComponent( passwordLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ).addComponent( organizationLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ) ).addGroup( loginPanelLayout.createParallelGroup().addComponent( userContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ).addComponent( passwordContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ).addComponent( organizationContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) ) );
    	
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
    			
    			if ( !StringUtils.isEmpty( user ) && !StringUtils.isEmpty( password ) && !StringUtils.isEmpty( organization ) ) {
    				LoginCredentials loginCredentials = new LoginCredentials();
    				loginCredentials.setUser( user );
    				loginCredentials.setPassword( password );
    				loginCredentials.setOrganization( organization );
    				
    				lastUser = user;
    				lastPassword = password;
    				lastOrganization = organization;
    				
    				Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    				return gson.toJson( loginCredentials, LoginCredentials.class );
    			} else
    				return StringUtils.EMPTY;
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
    	messagePanelLayout.setAutoCreateContainerGaps( true );
    	messagePanelLayout.setAutoCreateGaps( true );
    	messagePanelLayout.setVerticalGroup( messagePanelLayout.createSequentialGroup().addComponent( messageLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ).addComponent( messageContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ) );
    	messagePanelLayout.setHorizontalGroup( messagePanelLayout.createParallelGroup().addComponent( messageLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ).addComponent( messageContentField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) );
    	
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
    
    private static class InputPanel extends JPanel {
    	private static final long serialVersionUID = -2337376928008422799L;
    	
    	@Getter @Setter private Action action;
    	
    	public InputPanel( boolean isDoubleBuffered ) {
			super( isDoubleBuffered );
		}
    	
    	public String getInput() {
    		return ( String ) action.getValue( null );
    	}
    }
}