package com.digitaslbi.apigee.view;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.Document;
import javax.swing.undo.UndoableEdit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.digitaslbi.apigee.controller.DeveloperAppController;
import com.digitaslbi.apigee.model.DeveloperApp;
import com.digitaslbi.apigee.tools.DeveloperAppValueChange;

import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of DeveloperAppView
 * 
 * @author Victor Ortiz
 */
@Slf4j
public class DeveloperAppViewImpl implements DeveloperAppView {
    private static final String APP_ATTRIBUTES = "Attributes";
    private static final String APP_DETAILS = "Details";
    private static final String APP_PRODUCTS = "Products";
    private static final String DELETE = "Delete";
    private static final String DISPLAY_NAME = "Display name:";
    private static final String DISPLAY_NAME_VALUE = "DisplayName";
    private static final String MNIT_NEW = "New";
    private static final String MNIT_OPEN = "Open";
    private static final String MNIT_SAVE = "Save";
    private static final String MNIT_SAVEAS = "Save as";
    private static final String MNIT_RELOAD = "Reload";
    private static final String MNIT_ADDAT = "Add attribute";
    private static final String MNIT_ADDPR = "Add product";
    private static final String MENU_FILE = "File";
    private static final String MENU_EDIT = "Edit";
    private static final String NAME = "Name:";
    private static final String NOTES = "Notes:";
    private static final String NOTES_VALUE = "Notes";
    private static final String PROPERTY_NAME = "PROPERTY";
    private static final String PROPERTY_TYPE = "TYPE";
    private static final String PROPERTY_TYPE_ATT = "ATTRIB";
    private static final String PROPERTY_TYPE_PRO = "PRODUC";
    private static final String PROPERTY_KV = "KEYVALUE";
    
    private static final String LF_APPLE_MENU = "apple.laf.useScreenMenuBar";
    private static final String LF_APPLE_FULLSCREEN = "setWindowCanFullScreen";
    private static final String LF_APPLE_UTIL = "com.apple.eawt.FullScreenUtilities";
    
    private static final String MSG_ADD_TITLE = "New";
    private static final String MSG_ERROR = "Error";
    private static final String MSG_FILE_DIR_TITLE = "Please select a file/directory";
    private static final String MSG_FS_OSX_ERROR = "Could not initialize fullscreen for OS X";
    private static final String MSG_GRAPHICAL_ERROR = "A graphical interface error has ocurred";
    private static final String MSG_WARNING = "Warning";
    
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
    
    private DeveloperAppController controller;
    private boolean loading;

    public DeveloperAppViewImpl( DeveloperAppController controller ) {
        this.controller = controller;
        this.controller.setView( this );
        
        System.setProperty( LF_APPLE_MENU, Boolean.TRUE.toString() );
        initialize( true );
        enableOSX( frame );
    }
    
    @Override public void initialize( boolean firstTime ) {
        DeveloperApp model = controller.getModel();
        loading = true;
        
        if ( firstTime ) {
            nameLabel = new JLabel( NAME );
            nameLabel.setFont( nameLabel.getFont().deriveFont( Font.BOLD ) );
            displayNameLabel = new JLabel( DISPLAY_NAME );
            displayNameLabel.setFont( displayNameLabel.getFont().deriveFont( Font.BOLD ) );
            notesLabel = new JLabel( NOTES );
            notesLabel.setFont( notesLabel.getFont().deriveFont( Font.BOLD ) );
            nameContentField = new JTextField( 1 );
            nameContentField.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
            nameContentField.setEditable( false );
            displayNameContentField = new JTextField( 1 );
            displayNameContentField.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
            displayNameContentField.getDocument().addUndoableEditListener( this );
            displayNameContentField.getDocument().putProperty( PROPERTY_NAME, DISPLAY_NAME_VALUE );
            displayNameContentField.getDocument().putProperty( PROPERTY_TYPE, PROPERTY_TYPE_ATT );
            displayNameContentField.getDocument().putProperty( PROPERTY_KV, DeveloperAppController.VALUE );
            notesContentArea = new JTextArea( 3, 1 );
            notesContentArea.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
            notesContentArea.getDocument().addUndoableEditListener( this );
            notesContentArea.getDocument().putProperty( PROPERTY_NAME, NOTES_VALUE );
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
            
            JMenuBar menubar = new JMenuBar();
            menubar.add( fileMenu );
            menubar.add( editMenu );
            
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension window = new Dimension( 3 * screen.width / 4, 3 * screen.height / 4 );
            
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
        
        nameContentField.setText( null != model ? model.getName() : StringUtils.EMPTY );
        displayNameContentField.setText( null != model && null != model.getAttributes() && model.getAttributes().containsKey( DISPLAY_NAME_VALUE ) ? model.getAttributes().get( DISPLAY_NAME_VALUE ) : StringUtils.EMPTY );
        displayNameContentField.setEditable( null != model );
        notesContentArea.setText( null != model && null != model.getAttributes() && model.getAttributes().containsKey( NOTES_VALUE ) ? model.getAttributes().get( NOTES_VALUE ) : StringUtils.EMPTY );
        notesContentArea.setEditable( null != model );
        
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
            List<String> products = model.getProducts();
            
            for ( String product : products ) {
                JTextField productName = new JTextField( product, 1 );
                productName.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
                productName.getDocument().addUndoableEditListener( this );
                productName.getDocument().putProperty( PROPERTY_NAME, products.indexOf( product ) );
                productName.getDocument().putProperty( PROPERTY_TYPE, PROPERTY_TYPE_PRO );
                
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
            Set<String> attributes = model.getAttributes().keySet();
            
            for ( String attribute : attributes ) {
                if ( !DISPLAY_NAME_VALUE.equals( attribute ) && !NOTES_VALUE.equals( attribute ) ) {
                    JTextField attributeName = new JTextField( attribute, 1 );
                    attributeName.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
                    attributeName.getDocument().addUndoableEditListener( this );
                    attributeName.getDocument().putProperty( PROPERTY_NAME, attribute );
                    attributeName.getDocument().putProperty( PROPERTY_TYPE, PROPERTY_TYPE_ATT );
                    attributeName.getDocument().putProperty( PROPERTY_KV, DeveloperAppController.KEY );
                    
                    JTextField attributeContent = new JTextField( model.getAttributes().get( attribute ), 1 );
                    attributeContent.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
                    attributeContent.getDocument().addUndoableEditListener( this );
                    attributeContent.getDocument().putProperty( PROPERTY_NAME, attribute );
                    attributeContent.getDocument().putProperty( PROPERTY_TYPE, PROPERTY_TYPE_ATT );
                    attributeContent.getDocument().putProperty( PROPERTY_KV, DeveloperAppController.VALUE );
                    
                    JButton attributeDeleteButton = new JButton();
                    attributeDeleteButton.setText( DELETE );
                    attributeDeleteButton.setActionCommand( controller.getCommandForIndex( DeveloperAppController.CMD_DELAT, attribute ) );
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
        
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog( frame ) )
            return chooser.getSelectedFile().toPath();
        else
            return null;
    }
    
    @Override public Path saveFileOrDirectory( boolean directoyOnly ) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode( directoyOnly ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY );
        chooser.setDialogTitle( MSG_FILE_DIR_TITLE );
        chooser.setMultiSelectionEnabled( false );
        
        if ( JFileChooser.APPROVE_OPTION == chooser.showSaveDialog( frame ) )
            return chooser.getSelectedFile().toPath();
        else
            return null;
    }
    
    @Override public boolean showMessage( String message, boolean isError, boolean isYesNo ) {
        int response = -1;
        
        if ( isYesNo )
            response = JOptionPane.showConfirmDialog( frame, message, isError ? MSG_ERROR : MSG_WARNING, JOptionPane.OK_CANCEL_OPTION );
        else 
            JOptionPane.showMessageDialog( frame, message, isError ? MSG_ERROR : MSG_WARNING, isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.WARNING_MESSAGE );
        
        if ( JOptionPane.OK_OPTION == response )
            return true;
        else
            return false;
    }
    
    @Override public String requestNewInput( String message ) {
        return JOptionPane.showInputDialog( frame, message, MSG_ADD_TITLE, JOptionPane.QUESTION_MESSAGE );
    }
    
    @Override public void run() {
        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
            frame.setVisible( true );
        } catch ( Exception e ) {
            log.error( MSG_GRAPHICAL_ERROR, e );
            showMessage( MSG_GRAPHICAL_ERROR + IOUtils.LINE_SEPARATOR + e.getLocalizedMessage(), true, false );
            System.exit( -1 );
        }
    }
    
    @Override public void undoableEditHappened( UndoableEditEvent e ) {
        if ( !loading ) {
            try {
                Document document = ( Document ) e.getSource();
                String type = document.getProperty( PROPERTY_TYPE ).toString();
                String property = document.getProperty( PROPERTY_NAME ).toString();
                
                if ( PROPERTY_TYPE_ATT.equals( type ) ) {
                    String kv = document.getProperty( PROPERTY_KV ).toString();
                    Map<String, DeveloperAppValueChange> changes = new HashMap<>();
                    
                    DeveloperAppValueChange change = new DeveloperAppValueChange();
                    UndoableEdit edit = e.getEdit();
                    edit.undo();
                    change.setOldValue( document.getText( 0, document.getLength() ) );
                    edit.redo();
                    change.setNewValue( document.getText( 0, document.getLength() ) );
                    
                    if ( !StringUtils.isEmpty( kv ) )
                        changes.put( kv, change );
                    
                    ActionEvent action = new ActionEvent( changes, ActionEvent.ACTION_PERFORMED, controller.getCommandForIndex( DeveloperAppController.CMD_EDIAT, property ) );
                    actionPerformed( action );
                } else if ( PROPERTY_TYPE_PRO.equals( type ) ) {
                    DeveloperAppValueChange change = new DeveloperAppValueChange();
                    UndoableEdit edit = e.getEdit();
                    edit.undo();
                    change.setOldValue( document.getText( 0, document.getLength() ) );
                    edit.redo();
                    change.setNewValue( document.getText( 0, document.getLength() ) );
                    
                    ActionEvent action = new ActionEvent( change, ActionEvent.ACTION_PERFORMED, controller.getCommandForIndex( DeveloperAppController.CMD_EDIPR, property ) );
                    actionPerformed( action );
                }
            } catch ( Exception ex ) {
                log.error( MSG_GRAPHICAL_ERROR, e );
            }
        }
    }
    
    @Override public void actionPerformed( ActionEvent e ) {
        if ( !loading ) {
            controller.commandExecuted( e.getActionCommand(), e.getSource() );
            
            if ( !e.getActionCommand().contains( DeveloperAppController.CMD_EDIPR ) && !e.getActionCommand().contains( DeveloperAppController.CMD_EDIAT ) )
                initialize( false );
        }
    }
    
    private void enableOSX( JFrame frame ) {
        try {
            Class<?> utility = Class.forName( LF_APPLE_UTIL );
            Class<?> params[] = new Class[]{ Window.class, Boolean.TYPE };
            Method method = utility.getMethod( LF_APPLE_FULLSCREEN, params );
            method.invoke( utility, frame, true );
        } catch ( Exception e ) {
            log.warn ( MSG_FS_OSX_ERROR, e );
        }
    }
}