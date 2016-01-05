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
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.digitaslbi.apigee.model.DeveloperApp;
import com.digitaslbi.apigee.tools.DeveloperAppValueChange;
import com.digitaslbi.apigee.view.DeveloperAppView;

public class DeveloperAppControllerImplTest {
    private static final String TEST_STRING = "QWERTY";
    private static final String EDIT_STRING = "AZERTY";
    
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
        
        controller = new DeveloperAppControllerImpl();
        controller.setView( view );
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
    
    @Test public void commandExecutedAddProductTest() {
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_ADDPR, null ), null );
        assertTrue( controller.getModel().getProducts().contains( TEST_STRING ) );
    }
    
    @Test public void commandExecutedSaveTest() {
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_ADDPR, null ), null );
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_SAVE, null ), null );
        assertTrue( appDirectory.resolve( "name.props" ).toFile().exists() );
        assertTrue( appDirectory.resolve( "attributes.json" ).toFile().exists() );
    }
    
    @Test public void commandExecutedSaveAsTest() {
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_ADDPR, null ), null );
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_SAVEAS, null ), null );
        assertTrue( appDirectory.resolve( "output/name.props" ).toFile().exists() );
        assertTrue( appDirectory.resolve( "output/attributes.json" ).toFile().exists() );
    }
    
    @Test public void commandExecutedAddAttributeTest() {
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_ADDAT, null ), null );
        assertTrue( controller.getModel().getAttributes().containsKey( TEST_STRING ) );
    }
    
    @Test public void commandExecutedReloadTest() {
        DeveloperApp model = controller.getModel();
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_RELOAD, null ), null );
        assertEquals( model, controller.getModel() );
    }
    
    @Test public void commandExecutedOpenTest() {
        DeveloperApp model = controller.getModel();
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_OPEN, null ), null );
        assertEquals( model, controller.getModel() );
    }
    
    @Test public void commandExecutedNewTest() {
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_NEW, null ), null );
        assertEquals( TEST_STRING, controller.getModel().getName() );
        assertTrue( !controller.getModel().getProducts().isEmpty() );
        assertTrue( !controller.getModel().getAttributes().isEmpty() );
    }
    
    @Test public void commandExecutedDeleteProductTest() {
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_ADDPR, null ), null );
        
        int index = controller.getModel().getProducts().indexOf( TEST_STRING );
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_DELPR, index ), null );
        assertTrue( !controller.getModel().getProducts().contains( TEST_STRING ) );
    }
    
    @Test public void commandExecutedDeleteAttributeTest() {
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_ADDAT, null ), null );
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_DELAT, TEST_STRING ), null );
        assertTrue( !controller.getModel().getAttributes().containsKey( TEST_STRING ) );
    }
    
    @Test public void commandExecutedEditProductTest() {
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_ADDPR, null ), null );
        
        int index = controller.getModel().getProducts().indexOf( TEST_STRING );
        DeveloperAppValueChange change = new DeveloperAppValueChange();
        change.setOldValue( TEST_STRING );
        change.setNewValue( EDIT_STRING );
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_EDIPR, index ), change );
        assertTrue( !controller.getModel().getProducts().contains( TEST_STRING ) );
        assertTrue( controller.getModel().getProducts().contains( EDIT_STRING ) );
    }
    
    @Test public void commandExecutedEditAttributeTest() {
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_ADDAT, null ), null );
        
        Map<String,DeveloperAppValueChange> change = new HashMap<>();
        DeveloperAppValueChange realChange = new DeveloperAppValueChange();
        realChange.setOldValue( TEST_STRING );
        realChange.setNewValue( EDIT_STRING );
        change.put( DeveloperAppController.KEY, realChange );
        realChange = new DeveloperAppValueChange();
        realChange.setOldValue( TEST_STRING );
        realChange.setNewValue( EDIT_STRING );
        change.put( DeveloperAppController.VALUE, realChange );
        
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_EDIAT, TEST_STRING ), change );
        assertTrue( !controller.getModel().getAttributes().containsKey( TEST_STRING ) );
        assertTrue( controller.getModel().getAttributes().containsKey( EDIT_STRING ) );
        assertEquals( EDIT_STRING, controller.getModel().getAttributes().get( EDIT_STRING ) );
    }
}