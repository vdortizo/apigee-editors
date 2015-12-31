package com.digitaslbi.apigee.controller;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

public class DeveloperAppControllerTest {
    private Path appDirectory;
    private DeveloperAppController controller;
    
    @Before public void setup()  throws FileNotFoundException, IOException, URISyntaxException {
        appDirectory = Paths.get( DeveloperAppControllerTest.class.getResource( "/correct/" ).toURI() );
        controller = new DeveloperAppController( null );
        controller.importDeveloperApp( appDirectory );
    }
    
    @Test public void testImportDeveloperAppCorrect() {
		assertNotNull( controller.getModel() );
    }
    
    @Test( expected = RuntimeException.class ) public void testImportDeveloperAppNull() throws FileNotFoundException, IOException {
		controller.importDeveloperApp( null );
    }
    
    @Test( expected = RuntimeException.class ) public void testImportDeveloperAppNotDir() throws FileNotFoundException, IOException, URISyntaxException {
		appDirectory = Paths.get( DeveloperAppControllerTest.class.getResource( "/correct/name.props" ).toURI() );
		controller.importDeveloperApp( appDirectory );
		assertNull( controller.getModel() );
    }
    
    @Test( expected = RuntimeException.class ) public void testImportDeveloperAppNoNameProps() throws FileNotFoundException, IOException, URISyntaxException {
		appDirectory = Paths.get( DeveloperAppControllerTest.class.getResource( "/" ).toURI() );
		controller.importDeveloperApp( appDirectory );
		assertNull( controller.getModel() );
    }
    
    @Test( expected = RuntimeException.class ) public void testImportDeveloperAppEmptyName() throws FileNotFoundException, IOException, URISyntaxException {
		appDirectory = Paths.get( DeveloperAppControllerTest.class.getResource( "/emptyname/" ).toURI() );
		controller.importDeveloperApp( appDirectory );
		assertNull( controller.getModel() );
    }
    
    @Test( expected = RuntimeException.class ) public void testImportDeveloperAppNoName() throws FileNotFoundException, IOException, URISyntaxException {
		appDirectory = Paths.get( DeveloperAppControllerTest.class.getResource( "/noname/" ).toURI() );
		controller.importDeveloperApp( appDirectory );
		assertNull( controller.getModel() );
    }
    
    @Test( expected = RuntimeException.class ) public void testImportDeveloperAppEmptyProduct() throws FileNotFoundException, IOException, URISyntaxException {
		appDirectory = Paths.get( DeveloperAppControllerTest.class.getResource( "/emptyproduct/" ).toURI() );
		controller.importDeveloperApp( appDirectory );
		assertNull( controller.getModel() );
    }
    
    @Test( expected = RuntimeException.class ) public void testImportDeveloperAppNoProduct() throws FileNotFoundException, IOException, URISyntaxException {
		appDirectory = Paths.get( DeveloperAppControllerTest.class.getResource( "/noproduct/" ).toURI() );
        controller.importDeveloperApp( appDirectory );
        assertNull( controller.getModel() );
    }
    
    @Test( expected = RuntimeException.class ) public void testImportDeveloperAppSpacesProduct() throws FileNotFoundException, IOException, URISyntaxException {
        appDirectory = Paths.get( DeveloperAppControllerTest.class.getResource( "/blankproduct/" ).toURI() );
        controller.importDeveloperApp( appDirectory );
        assertNull( controller.getModel() );
	}
	
	@Test public void testExportDeveloperAppCorrect() throws FileNotFoundException, IOException {
        controller.exportDeveloperApp( appDirectory.resolve( "output" ) );
    }
    
    @Test( expected = IOException.class ) public void testExportDeveloperAppNullDir() throws FileNotFoundException, IOException {
        controller.exportDeveloperApp( null );
    }
}