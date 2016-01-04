package com.digitaslbi.apigee.controller;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.digitaslbi.apigee.model.DeveloperApp;
import com.digitaslbi.apigee.view.DeveloperAppView;

public class DeveloperAppControllerImplTest {
    private static final String TEST_STRING = "QWERTY";
    
    private Path appDirectory;
    private DeveloperAppController controller;
    
    @Before public void setup() throws IOException, URISyntaxException {
        appDirectory = Paths.get( DeveloperAppControllerImplTest.class.getResource( "/correct/" ).toURI() );
        try { Files.copy( appDirectory.resolve( "name.props" ), appDirectory.resolve( "copy_name.props" ) ); } catch ( Exception e ) {}
        try { Files.copy( appDirectory.resolve( "attributes.json" ), appDirectory.resolve( "copy_attributes.json" ) ); } catch ( Exception e ) {}
        
        DeveloperAppView view = mock( DeveloperAppView.class );
        when( view.getFileOrDirectory( eq( true ) ) ).thenReturn( appDirectory );
        when( view.getFileOrDirectory( eq( false ) ) ).thenReturn( appDirectory.resolve( "name.props" ) );
        when( view.saveFileOrDirectory( eq( true ) ) ).thenReturn( appDirectory.resolve( "output" ) );
        when( view.saveFileOrDirectory( eq( false ) ) ).thenReturn( appDirectory.resolve( "output" ).resolve( "name.props" ) );
        when( view.showMessage( any( String.class ), any( Boolean.class ), any( Boolean.class ) ) ).thenReturn( true );
        when( view.requestNewInput( any( String.class ) ) ).thenReturn( TEST_STRING );
        
        controller = new DeveloperAppControllerImpl( view );
        controller.importDeveloperApp( appDirectory );
    }
    
    @After public void teardown() {
        try { Files.delete( appDirectory.resolve( "output/name.props" ) ); } catch ( Exception e ) {}
        try { Files.delete( appDirectory.resolve( "output/attributes.json" ) ); } catch ( Exception e ) {}
        try { Files.move( appDirectory.resolve( "copy_name.props" ), appDirectory.resolve( "name.props" ), StandardCopyOption.REPLACE_EXISTING ); } catch ( Exception e ) {}
        try { Files.move( appDirectory.resolve( "copy_attributes.json" ), appDirectory.resolve( "attributes.json" ), StandardCopyOption.REPLACE_EXISTING ); } catch ( Exception e ) {}
    }
    
    @Test public void testImportDeveloperAppCorrect() {
		assertNotNull( controller.getModel() );
    }
    
    @Test( expected = RuntimeException.class ) public void testImportDeveloperAppNull() throws IOException {
		controller.importDeveloperApp( null );
    }
    
    @Test( expected = RuntimeException.class ) public void testImportDeveloperAppNotDir() throws IOException, URISyntaxException {
		appDirectory = Paths.get( DeveloperAppControllerImplTest.class.getResource( "/correct/name.props" ).toURI() );
		controller.importDeveloperApp( appDirectory );
		assertNull( controller.getModel() );
    }
    
    @Test( expected = RuntimeException.class ) public void testImportDeveloperAppNoNameProps() throws IOException, URISyntaxException {
		appDirectory = Paths.get( DeveloperAppControllerImplTest.class.getResource( "/" ).toURI() );
		controller.importDeveloperApp( appDirectory );
		assertNull( controller.getModel() );
    }
    
    @Test( expected = RuntimeException.class ) public void testImportDeveloperAppEmptyName() throws IOException, URISyntaxException {
		appDirectory = Paths.get( DeveloperAppControllerImplTest.class.getResource( "/emptyname/" ).toURI() );
		controller.importDeveloperApp( appDirectory );
		assertNull( controller.getModel() );
    }
    
    @Test( expected = RuntimeException.class ) public void testImportDeveloperAppNoName() throws IOException, URISyntaxException {
		appDirectory = Paths.get( DeveloperAppControllerImplTest.class.getResource( "/noname/" ).toURI() );
		controller.importDeveloperApp( appDirectory );
		assertNull( controller.getModel() );
    }
    
    @Test( expected = RuntimeException.class ) public void testImportDeveloperAppEmptyProduct() throws IOException, URISyntaxException {
		appDirectory = Paths.get( DeveloperAppControllerImplTest.class.getResource( "/emptyproduct/" ).toURI() );
		controller.importDeveloperApp( appDirectory );
		assertNull( controller.getModel() );
    }
    
    @Test( expected = RuntimeException.class ) public void testImportDeveloperAppNoProduct() throws IOException, URISyntaxException {
		appDirectory = Paths.get( DeveloperAppControllerImplTest.class.getResource( "/noproduct/" ).toURI() );
        controller.importDeveloperApp( appDirectory );
        assertNull( controller.getModel() );
    }
    
    @Test( expected = RuntimeException.class ) public void testImportDeveloperAppSpacesProduct() throws IOException, URISyntaxException {
        appDirectory = Paths.get( DeveloperAppControllerImplTest.class.getResource( "/blankproduct/" ).toURI() );
        controller.importDeveloperApp( appDirectory );
        assertNull( controller.getModel() );
	}
	
	@Test public void testExportDeveloperAppCorrect() throws IOException {
        controller.exportDeveloperApp( appDirectory.resolve( "output" ) );
        assertTrue( appDirectory.resolve( "output/name.props" ).toFile().exists() );
        assertTrue( appDirectory.resolve( "output/attributes.json" ).toFile().exists() );
    }
    
    @Test( expected = IOException.class ) public void testExportDeveloperAppNullDir() throws IOException {
        controller.exportDeveloperApp( null );
    }
    
    @Test public void getCommandForIndexCorrect() {
        assertEquals( DeveloperAppController.CMD_DELAT + "1", controller.getCommandForIndex( DeveloperAppController.CMD_DELAT, 1 ) );
    }
    
    @Test public void getCommandForIndexNull() {
        assertEquals( DeveloperAppController.CMD_DELAT, controller.getCommandForIndex( DeveloperAppController.CMD_DELAT, null ) );
    }
    
    @Test public void actionPerformedAddProductTest() {
        ActionEvent event = new ActionEvent( new Object(), ActionEvent.ACTION_PERFORMED, controller.getCommandForIndex( DeveloperAppController.CMD_ADDPR, null ) );
        controller.actionPerformed( event );
        assertTrue( controller.getModel().getProducts().contains( TEST_STRING ) );
    }
    
    @Test public void actionPerformedSaveTest() {
        ActionEvent event = new ActionEvent( new Object(), ActionEvent.ACTION_PERFORMED, controller.getCommandForIndex( DeveloperAppController.CMD_ADDPR, null ) );
        controller.actionPerformed( event );
        event = new ActionEvent( new Object(), ActionEvent.ACTION_PERFORMED, controller.getCommandForIndex( DeveloperAppController.CMD_SAVE, null ) );
        controller.actionPerformed( event );
        assertTrue( appDirectory.resolve( "name.props" ).toFile().exists() );
        assertTrue( appDirectory.resolve( "attributes.json" ).toFile().exists() );
    }
    
    @Test public void actionPerformedSaveAsTest() {
        ActionEvent event = new ActionEvent( new Object(), ActionEvent.ACTION_PERFORMED, controller.getCommandForIndex( DeveloperAppController.CMD_ADDPR, null ) );
        controller.actionPerformed( event );
        event = new ActionEvent( new Object(), ActionEvent.ACTION_PERFORMED, controller.getCommandForIndex( DeveloperAppController.CMD_SAVEAS, null ) );
        controller.actionPerformed( event );
        assertTrue( appDirectory.resolve( "output/name.props" ).toFile().exists() );
        assertTrue( appDirectory.resolve( "output/attributes.json" ).toFile().exists() );
    }
    
    @Test public void actionPerformedAddAttributeTest() {
        ActionEvent event = new ActionEvent( new Object(), ActionEvent.ACTION_PERFORMED, controller.getCommandForIndex( DeveloperAppController.CMD_ADDAT, null ) );
        controller.actionPerformed( event );
        assertTrue( controller.getModel().getAttributes().containsKey( TEST_STRING ) );
    }
    
    @Test public void actionPerformedReloadTest() {
        DeveloperApp model = controller.getModel();
        ActionEvent event = new ActionEvent( new Object(), ActionEvent.ACTION_PERFORMED, controller.getCommandForIndex( DeveloperAppController.CMD_RELOAD, null ) );
        controller.actionPerformed( event );
        assertEquals( model, controller.getModel() );
    }
    
    @Test public void actionPerformedOpenTest() {
        DeveloperApp model = controller.getModel();
        ActionEvent event = new ActionEvent( new Object(), ActionEvent.ACTION_PERFORMED, controller.getCommandForIndex( DeveloperAppController.CMD_OPEN, null ) );
        controller.actionPerformed( event );
        assertEquals( model, controller.getModel() );
    }
    
    @Test public void actionPerformedNewTest() {
        ActionEvent event = new ActionEvent( new Object(), ActionEvent.ACTION_PERFORMED, controller.getCommandForIndex( DeveloperAppController.CMD_NEW, null ) );
        controller.actionPerformed( event );
        assertEquals( TEST_STRING, controller.getModel().getName() );
        assertTrue( !controller.getModel().getProducts().isEmpty() );
        assertTrue( !controller.getModel().getAttributes().isEmpty() );
    }
    
    @Test public void actionPerformedDeleteProductTest() {
        ActionEvent event = new ActionEvent( new Object(), ActionEvent.ACTION_PERFORMED, controller.getCommandForIndex( DeveloperAppController.CMD_ADDPR, null ) );
        controller.actionPerformed( event );
        
        int index = controller.getModel().getProducts().indexOf( TEST_STRING );
        event = new ActionEvent( new Object(), ActionEvent.ACTION_PERFORMED, controller.getCommandForIndex( DeveloperAppController.CMD_DELPR, index ) );
        controller.actionPerformed( event );
        assertTrue( !controller.getModel().getProducts().contains( TEST_STRING ) );
    }
    
    @Test public void actionPerformedDeleteAttributeTest() {
        ActionEvent event = new ActionEvent( new Object(), ActionEvent.ACTION_PERFORMED, controller.getCommandForIndex( DeveloperAppController.CMD_ADDAT, null ) );
        controller.actionPerformed( event );
        event = new ActionEvent( new Object(), ActionEvent.ACTION_PERFORMED, controller.getCommandForIndex( DeveloperAppController.CMD_DELAT, TEST_STRING ) );
        controller.actionPerformed( event );
        assertTrue( !controller.getModel().getAttributes().containsKey( TEST_STRING ) );
    }
}