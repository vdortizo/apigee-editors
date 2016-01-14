package com.digitaslbi.apigee.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.digitaslbi.apigee.model.Attribute;
import com.digitaslbi.apigee.model.DeveloperApp;
import com.digitaslbi.apigee.model.Product;
import com.digitaslbi.apigee.view.DeveloperAppView;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( classes = DeveloperAppControllerImplTest.ConfigurationClass.class )
public class DeveloperAppControllerImplTest {
    private static final String TEST_STRING = "QWERTY";
    private static final String EDIT_STRING = "AZERTY";
    
    private Path appDirectory;
    
    @Autowired private DeveloperAppController controller;
    
    @Before public void setup() throws IOException, URISyntaxException {
        appDirectory = Paths.get( DeveloperAppControllerImplTest.class.getResource( "/correct/" ).toURI() );
        try { Files.copy( appDirectory.resolve( "name.props" ), appDirectory.resolve( "copy_name.props" ) ); } catch ( Exception e ) { e.printStackTrace(); }
        try { Files.copy( appDirectory.resolve( "attributes.json" ), appDirectory.resolve( "copy_attributes.json" ) ); } catch ( Exception e ) { e.printStackTrace(); }
        
        controller.importDeveloperApp( appDirectory );
    }
    
    @After public void teardown() throws IOException, URISyntaxException {
    	appDirectory = Paths.get( DeveloperAppControllerImplTest.class.getResource( "/correct/" ).toURI() );
        try { Files.delete( appDirectory.resolve( "output/name.props" ) ); } catch ( Exception e ) {}
        try { Files.delete( appDirectory.resolve( "output/attributes.json" ) ); } catch ( Exception e ) {}
        try { Files.delete( appDirectory.resolve( "name.props" ) ); } catch ( Exception e ) {}
        try { Files.delete( appDirectory.resolve( "attributes.json" ) ); } catch ( Exception e ) {}
        try { Files.move( appDirectory.resolve( "copy_name.props" ), appDirectory.resolve( "name.props" ), StandardCopyOption.REPLACE_EXISTING ); } catch ( Exception e ) {}
        try { Files.move( appDirectory.resolve( "copy_attributes.json" ), appDirectory.resolve( "attributes.json" ), StandardCopyOption.REPLACE_EXISTING ); } catch ( Exception e ) {}
        
        appDirectory = null;
        controller = null;
    }
    
    @Test public void importDeveloperAppCorrectTest() {
		assertNotNull( controller.getModel() );
    }
    
    @Test( expected = RuntimeException.class ) public void importDeveloperAppNullTest() throws IOException {
		controller.importDeveloperApp( null );
    }
    
    @Test( expected = RuntimeException.class ) public void ImportDeveloperAppNotDirTest() throws IOException, URISyntaxException {
		appDirectory = Paths.get( DeveloperAppControllerImplTest.class.getResource( "/correct/name.props" ).toURI() );
		controller.importDeveloperApp( appDirectory );
		assertNull( controller.getModel() );
    }
    
    @Test( expected = RuntimeException.class ) public void importDeveloperAppNoNamePropsTest() throws IOException, URISyntaxException {
		appDirectory = Paths.get( DeveloperAppControllerImplTest.class.getResource( "/" ).toURI() );
		controller.importDeveloperApp( appDirectory );
		assertNull( controller.getModel() );
    }
    
    @Test( expected = RuntimeException.class ) public void importDeveloperAppEmptyNameTest() throws IOException, URISyntaxException {
		appDirectory = Paths.get( DeveloperAppControllerImplTest.class.getResource( "/emptyname/" ).toURI() );
		controller.importDeveloperApp( appDirectory );
		assertNull( controller.getModel() );
    }
    
    @Test( expected = RuntimeException.class ) public void importDeveloperAppNoNameTest() throws IOException, URISyntaxException {
		appDirectory = Paths.get( DeveloperAppControllerImplTest.class.getResource( "/noname/" ).toURI() );
		controller.importDeveloperApp( appDirectory );
		assertNull( controller.getModel() );
    }
    
    @Test( expected = RuntimeException.class ) public void importDeveloperAppEmptyProductTest() throws IOException, URISyntaxException {
		appDirectory = Paths.get( DeveloperAppControllerImplTest.class.getResource( "/emptyproduct/" ).toURI() );
		controller.importDeveloperApp( appDirectory );
		assertNull( controller.getModel() );
    }
    
    @Test( expected = RuntimeException.class ) public void importDeveloperAppNoProductTest() throws IOException, URISyntaxException {
		appDirectory = Paths.get( DeveloperAppControllerImplTest.class.getResource( "/noproduct/" ).toURI() );
        controller.importDeveloperApp( appDirectory );
        assertNull( controller.getModel() );
    }
    
    @Test( expected = RuntimeException.class ) public void importDeveloperAppSpacesProductTest() throws IOException, URISyntaxException {
        appDirectory = Paths.get( DeveloperAppControllerImplTest.class.getResource( "/blankproduct/" ).toURI() );
        controller.importDeveloperApp( appDirectory );
        assertNull( controller.getModel() );
	}
	
	@Test public void exportDeveloperAppCorrectTest() throws IOException {
        controller.exportDeveloperApp( appDirectory.resolve( "output" ) );
        assertTrue( appDirectory.resolve( "output/name.props" ).toFile().exists() );
        assertTrue( appDirectory.resolve( "output/attributes.json" ).toFile().exists() );
    }
    
    @Test( expected = IOException.class ) public void exportDeveloperAppNullDirTest() throws IOException {
        controller.exportDeveloperApp( null );
    }
    
    @Test public void getCommandForIndexCorrectTest() {
        assertEquals( DeveloperAppController.CMD_DELAT + "1", controller.getCommandForIndex( DeveloperAppController.CMD_DELAT, 1 ) );
    }
    
    @Test public void getCommandForIndexNullTest() {
        assertEquals( DeveloperAppController.CMD_DELAT, controller.getCommandForIndex( DeveloperAppController.CMD_DELAT, null ) );
    }
    
    @Test public void commandExecutedAddProductTest() {
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_ADDPR, null ), null );
        assertTrue( getProducts( controller.getModel() ).contains( TEST_STRING ) );
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
        assertNotNull( getAttribute( TEST_STRING, controller.getModel().getAttributes() ).getName() );
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
        assertTrue( !getProducts( controller.getModel() ).isEmpty() );
        assertTrue( !controller.getModel().getAttributes().isEmpty() );
    }
    
    @Test public void commandExecutedDeleteProductTest() {
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_ADDPR, null ), null );
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_DELPR, getProducts( controller.getModel() ).size() - 1 ), null );
        assertTrue( !getProducts( controller.getModel() ).contains( TEST_STRING ) );
    }
    
    @Test public void commandExecutedDeleteAttributeTest() {
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_ADDAT, null ), null );
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_DELAT, controller.getModel().getAttributes().size() - 1 ), null );
        assertNull( getAttribute( TEST_STRING, controller.getModel().getAttributes() ).getName() );
    }
    
    @Test public void commandExecutedEditProductTest() {
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_ADDPR, null ), null );
        
        String change = EDIT_STRING;
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_EDIPR, getProducts( controller.getModel() ).size() - 1 ), change );
        assertTrue( !getProducts( controller.getModel() ).contains( TEST_STRING ) );
        assertTrue( getProducts( controller.getModel() ).contains( EDIT_STRING ) );
    }
    
    @Test public void commandExecutedEditAttributeTest() {
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_ADDAT, null ), null );
        
        Attribute change = new Attribute();
        change.setName( EDIT_STRING );
        change.setValue( EDIT_STRING );
        
        controller.commandExecuted( controller.getCommandForIndex( DeveloperAppController.CMD_EDIAT, controller.getModel().getAttributes().size() - 1 ), change );
        assertNull( getAttribute( TEST_STRING, controller.getModel().getAttributes() ).getName() );
        assertNotNull( getAttribute( EDIT_STRING, controller.getModel().getAttributes() ).getName() );
        assertEquals( EDIT_STRING, getAttribute( EDIT_STRING, controller.getModel().getAttributes() ).getName() );
        assertEquals( EDIT_STRING, getAttribute( EDIT_STRING, controller.getModel().getAttributes() ).getValue() );
    }
    
    //TODO: Test CMD_TSTUP
    //TODO: Test CMD_VRPRO
    //TODO: Test CMD_SETCR
    //TODO: Test CMD_GETAP
    //TODO: Test CMD_GETWB
    
    private List<String> getProducts( DeveloperApp developerApp ) {
		if ( null != developerApp ) {
			if ( null != developerApp.getCredentials() && 1 == developerApp.getCredentials().size() ) {
				List<String> products = new ArrayList<>();
				
				for ( Product product : developerApp.getCredentials().get( 0 ).getApiProducts() )
					products.add( product.getDisplayName() );
				
				return products;
			} else
				return null;
		}
		
		return null;
	}
    
    private Attribute getAttribute( String name, List<Attribute> attributes ) {
    	for ( Attribute attribute : attributes )
    		if ( name.equals( attribute.getName() ) )
    			return attribute;
		
		return new Attribute();
	}
    
    @Configuration
    public static class ConfigurationClass {
    	@Bean public DeveloperAppController controller() {
    		return new DeveloperAppControllerImpl();
    	}
    	
    	@Bean public DeveloperAppView view() throws IOException, URISyntaxException {
    		Path appDirectory = Paths.get( ConfigurationClass.class.getResource( "/correct/" ).toURI() );
    		
    		DeveloperAppView view = mock( DeveloperAppView.class );
            when( view.getFileOrDirectory( eq( true ) ) ).thenReturn( appDirectory );
            when( view.getFileOrDirectory( eq( false ) ) ).thenReturn( appDirectory.resolve( "name.props" ) );
            when( view.saveFileOrDirectory( eq( true ) ) ).thenReturn( appDirectory.resolve( "output" ) );
            when( view.saveFileOrDirectory( eq( false ) ) ).thenReturn( appDirectory.resolve( "output" ).resolve( "name.props" ) );
            when( view.showMessage( any( String.class ), any( Boolean.class ), any( Boolean.class ) ) ).thenReturn( true );
            when( view.requestNewInput( any( String.class ), any( String.class ) ) ).thenReturn( TEST_STRING );
            
            return view;
    	}
    	
    	@Bean public HttpClientBuilder clientBuilder() throws IOException, URISyntaxException {
    		CloseableHttpResponse response = mock( CloseableHttpResponse.class );
    		
    		CloseableHttpClient client = mock( CloseableHttpClient.class );
    		when( client.execute( any( HttpUriRequest.class ) ) ).thenReturn( response );
    		
    		HttpClientBuilder clientBuilder = mock( HttpClientBuilder.class );
    		when( clientBuilder.build() ).thenReturn( client );
    		
    		return clientBuilder;
    	}
    }
}