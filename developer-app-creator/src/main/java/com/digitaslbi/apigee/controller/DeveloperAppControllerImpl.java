package com.digitaslbi.apigee.controller;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.digitaslbi.apigee.model.Attribute;
import com.digitaslbi.apigee.model.AttributesList;
import com.digitaslbi.apigee.model.Credential;
import com.digitaslbi.apigee.model.DeveloperApp;
import com.digitaslbi.apigee.model.DeveloperAppCreateRequest;
import com.digitaslbi.apigee.model.DeveloperAppUpdateRequest;
import com.digitaslbi.apigee.model.LoginCredentials;
import com.digitaslbi.apigee.model.Product;
import com.digitaslbi.apigee.model.ProductsList;
import com.digitaslbi.apigee.tools.AttributeComparator;
import com.digitaslbi.apigee.tools.ProductComparator;
import com.digitaslbi.apigee.view.DeveloperAppView;
import com.google.gson.Gson;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of DeveloperAppCOntroller.
 * 
 * @author Victor Ortiz
 */
@Slf4j
@Component
public class DeveloperAppControllerImpl implements DeveloperAppController {
	private static final String NAME = "name.props";
	private static final String ATTRIBUTES = "attributes.json";

	private static final String PROPERTY_NAME = "name";
	private static final String PROPERTY_EXPAND = "expand";
	private static final String PROPERTY_PRODUCTS = "products";

	private static final String HTTP_HOST = "api.enterprise.apigee.com";
	private static final String HTTP_ENDPOINT = "/v1/organizations";
	private static final String HTTP_PRODUCTS_RESOURCE = "/%s/apiproducts";
	private static final String HTTP_APPS_RESOURCE = "/%s/developers/%s/apps/%s";
	private static final String HTTP_APPCR_RESOURCE = "/%s/developers/%s/apps";
	private static final String HTTP_KEYS_RESOURCE = "/keys/%s";
	private static final String HTTP_ATTS_RESOURCE = "/attributes";
	private static final String HTTP_PRODS_RESOURCE = "/apiproducts/%s";
	private static final String HTTP_PROTOCOL = "https";
	private static final String HTTP_CONTENT_TYPE = "Content-Type";
	private static final String HTTP_CONTENT_VALUE = "application/json";

	private static final String ATTRIBUTE_PLACEHOLDER = "Please change this value";
	private static final String PRODUCT_SEPARATOR = ",";

	private static final String MSG_ADD_APP = "Please input the new developer app name:";
	private static final String MSG_ADD_ATTRIBUTE = "Please input the new attribute name:";
	private static final String MSG_ADD_PRODUCT = "Please input the new product name:";
	private static final String MSG_APP_ERROR = "The developer app name cannot be empty";
	private static final String MSG_ATTRIB_ERROR = "The attribute name cannot be empty";
	private static final String MSG_ATTRIB_EXISTS_ERROR = "The attribute [%s] already exists in the developer app";
	private static final String MSG_ATTRIB_VALUE_ERROR = "The attribute [%s] cannot be empty";
	private static final String MSG_APP_DATA_ERROR = "The developer app [%s] does not contain the required structure";
	private static final String MSG_APP_EXISTS_ERROR = "The developer app [%s] does not exist in the organization [%s], for the developer [%s]";
	private static final String MSG_APP_KEY = "The key for the developer app [%s] is: [%s]. It has been copied to the clipboard";
	private static final String MSG_APP_UPDATED = "Developer app updated, please verify APIGEE Edge";
	private static final String MSG_APP_CREATED = "Developer app created, please verify APIGEE Edge";
	private static final String MSG_CRED_ERROR = "All credential fields are required";
	private static final String MSG_DATA_ERROR = "An error has ocurred";
	private static final String MSG_EXISTS_WARN = "The file [%s] already exists in the directory [%s] and will be overwritten";
	private static final String MSG_FILE_NOT_EXISTS_ERROR = "The file [%s] does not exist under the directory [%s]";
	private static final String MSG_INP_CREDENTIALS = "Please input your credentials:";
	private static final String MSG_LOAD_APP = "Please input the developer app name:";
	private static final String MSG_LOSE_CHANGES = "Reloading the current app will lose all unsaved changes, are you sure you want to continue?";
	private static final String MSG_NO_APP_LOADED = "The current developer app was not loaded from the filesystem";
	private static final String MSG_NO_KNOWN_ACTION = "An unknown action [%s] was executed";
	private static final String MSG_NO_PROPERTY = "The attribute [%s] was not found, it was added with the default value";
	private static final String MSG_NO_PROD_ERROR = "A developer app must contain at least one product";
	private static final String MSG_NOT_DIR_ERROR = "The path [%s] is not a directory";
	private static final String MSG_NOT_EXIST_PROP_ERROR = "The reader does not contain the required property [%s]";
	private static final String MSG_NOT_IMPORT_WARN = ", no attributes will be imported";
	private static final String MSG_NULL_ERROR = "The app object or the path [%s] are null";
	private static final String MSG_NULL_PROP_ERROR = "The property [%s] cannot be null or empty";
	private static final String MSG_PROD_ERROR = "The product cannot be empty or only blank spaces";
	private static final String MSG_PROD_EXISTS_ERROR = "The product [%s] already exists in the developer app";
	private static final String MSG_PROD_EXISTS_WARN = "All the defined producs exist in the organization [%s]";
	private static final String MSG_PROD_NOT_EXISTS_WARN = "The following products don't exist in the organization [%s]:";
	private static final String MSG_PROD_WARN = "The product at index [%s] is empty and will be ignored";
	private static final String MSG_PROD_ZERO = "A developer app cannot contain no products";
	private static final String MSG_SURE_SAVE = "Do you want to save your changes?";
	private static final String MSG_SURE_OVERWRITE = "A developer app already exists in this location, do you want to overwrite it?";
	private static final String MSG_VALUE_SEP_ERROR = "The property [%s] must contain one or more values, separated by [%s]";

	private static final long PRODUCTS_TTL = 3600000L;

	private List<Product> apigeeProducts;
	private LoginCredentials loginCredentials;

	private Path path;
	private boolean modified;
	private long productsBorn;

	@Getter private DeveloperApp model;

	@Autowired private DeveloperAppView view;
	@Autowired private Gson gson;
	@Autowired private HttpClientBuilder clientBuilder;

	@Override public void commandExecuted( String command, Object source ) {
		String index = null;

		if ( !StringUtils.isEmpty( command ) && ( command.contains( CMD_DELAT ) || command.contains( CMD_DELPR ) || command.contains( CMD_EDIAT ) || command.contains( CMD_EDIPR ) ) ) {
			index = command.replace( CMD_DELAT, StringUtils.EMPTY ).replace( CMD_DELPR, StringUtils.EMPTY ).replace( CMD_EDIAT, StringUtils.EMPTY ).replace( CMD_EDIPR, StringUtils.EMPTY );

			if ( command.contains( CMD_DELAT ) )
				command = CMD_DELAT;
			else if ( command.contains( CMD_DELPR ) )
				command = CMD_DELPR;
			else if ( command.contains( CMD_EDIAT ) )
				command = CMD_EDIAT;
			else if ( command.contains( CMD_EDIPR ) )
				command = CMD_EDIPR;
		}

		switch ( command ) {
		case CMD_NEW:
			newApp(); break;
		case CMD_OPEN:
			open(); break;
		case CMD_RELOAD:
			reload(); break;
		case CMD_SAVE:
			save(); break;
		case CMD_SAVEAS:
			saveAs(); break;
		case CMD_ADDAT:
			addAttribute(); break;
		case CMD_ADDPR:
			addProduct(); break;
		case CMD_DELAT:
			deleteAttribute( index ); break;
		case CMD_DELPR:
			deleteProduct( index ); break;
		case CMD_EDIAT:
			editAttribute( source, index ); break;
		case CMD_EDIPR:
			editProduct( source, index ); break;
		case CMD_SETCR:
			setCredentials(); break;
		case CMD_GETWB:
			openWeb(); break;
		case CMD_GETAP:
			Runnable apiTask = () -> getApiKey();
			new Thread( apiTask ).start(); break;
		case CMD_TSTUP:
			Runnable uploadTask = () -> testUpload();
			new Thread( uploadTask ).start(); break;
		case CMD_VRPRO:
			Runnable testTask = () -> testProducts();
			new Thread( testTask ).start(); break;
		default:
			log.warn( String.format( MSG_NO_KNOWN_ACTION, command ) ); break;
		}
	}

	@Override public void importDeveloperApp( Path directory ) throws IOException {
		if ( null != directory && Files.isDirectory( directory, LinkOption.NOFOLLOW_LINKS ) ) {
			Path nameProps = directory.resolve( NAME );
			Path attributesJson = directory.resolve( ATTRIBUTES );

			if ( Files.exists( nameProps, LinkOption.NOFOLLOW_LINKS ) ) {
				FileInputStream propertiesInputStream = new FileInputStream( nameProps.toFile() );
				Properties properties = new Properties();
				properties.load( propertiesInputStream );
				propertiesInputStream.close();

				if ( !properties.containsKey( PROPERTY_NAME ) ) {
					String error = String.format( MSG_NOT_EXIST_PROP_ERROR, NAME, PROPERTY_NAME );
					log.error( error );
					throw new RuntimeException( error );
				}

				String name = properties.getProperty( PROPERTY_NAME );

				if ( StringUtils.isEmpty( name ) ) {
					String error = String.format( MSG_NULL_PROP_ERROR, PROPERTY_NAME );
					log.error( error );
					throw new RuntimeException( error );
				}

				if ( !properties.containsKey( PROPERTY_PRODUCTS ) ) {
					String error = String.format( MSG_NOT_EXIST_PROP_ERROR, NAME, PROPERTY_PRODUCTS );
					log.error( error );
					throw new RuntimeException( error );
				}

				String[] productsArray = StringUtils.split( properties.getProperty( PROPERTY_PRODUCTS ), PRODUCT_SEPARATOR );

				if ( 0 == productsArray.length ) {
					String error = String.format( MSG_VALUE_SEP_ERROR, PROPERTY_PRODUCTS, PRODUCT_SEPARATOR );
					log.error( error );
					throw new RuntimeException( error );
				}

				List<Product> products = new ArrayList<>();

				for ( int i = 0 ; i < productsArray.length ; i++ ) {
					productsArray[i] = StringUtils.trim( productsArray[i] );

					if ( StringUtils.isEmpty( productsArray[i] ) )
						log.warn( String.format( MSG_PROD_WARN, i ) );
					else {
						Product product = new Product();
						product.setDisplayName( productsArray[i] );
						products.add( product );
					}
				}

				if ( 0 == products.size() ) {
					String error = String.format( MSG_VALUE_SEP_ERROR, PROPERTY_PRODUCTS, PRODUCT_SEPARATOR );
					log.error( error );
					throw new RuntimeException( error );
				}

				Collections.sort( products, new ProductComparator() );
				DeveloperApp tempModel = new DeveloperApp();
				tempModel.setName( name );
				tempModel.setCredentials( new ArrayList<>() );
				tempModel.getCredentials().add( new Credential() );
				tempModel.getCredentials().get( 0 ).setApiProducts( products );

				if ( Files.exists( attributesJson, LinkOption.NOFOLLOW_LINKS ) ) {
					FileReader reader = new FileReader( attributesJson.toFile() );
					AttributesList attributeList = gson.fromJson( reader, AttributesList.class );
					tempModel.setAttributes( attributeList.getAttributes() );
					reader.close();
				} else {
					tempModel.setAttributes( new ArrayList<>() );
					log.warn( String.format( MSG_FILE_NOT_EXISTS_ERROR + MSG_NOT_IMPORT_WARN, ATTRIBUTES, directory ) );
				}

				boolean displayName = false;
				boolean notes = false;

				for ( Attribute attribute : tempModel.getAttributes() ) {
					if ( DISPLAY_NAME.equals( attribute.getName() ) )
						displayName = true;
					else if ( NOTES.equals( attribute.getName() ) )
						notes = true;
				}

				if ( !displayName ) {
					Attribute attribute = new Attribute();
					attribute.setName( DISPLAY_NAME );
					attribute.setValue( tempModel.getName() );

					log.warn( String.format( MSG_NO_PROPERTY, DISPLAY_NAME ), false, false );
					tempModel.getAttributes().add( attribute );
					modified = true;
				}

				if ( !notes ) {
					Attribute attribute = new Attribute();
					attribute.setName( NOTES );
					attribute.setValue( StringUtils.EMPTY );

					log.warn( String.format( MSG_NO_PROPERTY, NOTES ), false, false );
					tempModel.getAttributes().add( attribute );
					modified = true;
				}

				Collections.sort( tempModel.getAttributes(), new AttributeComparator() );
				path = directory;
				modified = false;
				model = tempModel;
			} else {
				String error = String.format( MSG_FILE_NOT_EXISTS_ERROR, NAME, directory );
				log.error( error );
				throw new RuntimeException( error );
			}
		} else {
			String error = String.format( MSG_NOT_DIR_ERROR, directory );
			log.error( error );
			throw new RuntimeException( error );
		}
	}

	@Override public void exportDeveloperApp( Path directory ) throws IOException {
		if ( null != model && null != directory ) {
			if ( !Files.exists( directory, LinkOption.NOFOLLOW_LINKS ) )
				Files.createDirectory( directory );
			else if ( !Files.isDirectory( directory, LinkOption.NOFOLLOW_LINKS ) ) {
				String error = String.format( MSG_NOT_DIR_ERROR, directory );
				log.error( error );
				throw new IOException( error );
			}

			Credential credential = null;
			List<Product> products = new ArrayList<>();

			if ( null != ( credential = getCredential( model ) ) )
				products = credential.getApiProducts();

			for ( int i = 0 ; i < products.size() ; i++ ) {
				String product = StringUtils.trim( products.get( i ).getDisplayName() );

				if ( StringUtils.isEmpty( product ) ) {
					String error = MSG_PROD_ERROR;
					log.error( error );
					throw new RuntimeException( error );
				}
			}

			if ( 0 == products.size() ) {
				String error = String.format( MSG_VALUE_SEP_ERROR, PROPERTY_PRODUCTS, PRODUCT_SEPARATOR );
				log.error( error );
				throw new RuntimeException( error );
			}

			Collections.sort( products, new ProductComparator() );
			Path nameProps = directory.resolve( NAME );
			Path attributesJson = directory.resolve( ATTRIBUTES );

			Properties properties = new Properties();
			properties.put( PROPERTY_NAME, model.getName() );
			properties.put( PROPERTY_PRODUCTS, StringUtils.join( getProductDisplayNameList( products ), PRODUCT_SEPARATOR ) );

			if ( Files.exists( nameProps, LinkOption.NOFOLLOW_LINKS ) )
				log.warn( String.format( MSG_EXISTS_WARN, NAME, directory ) );

			FileOutputStream propertiesOutputStream = new FileOutputStream( nameProps.toFile() );
			properties.store( propertiesOutputStream, null );
			propertiesOutputStream.close();

			if ( null != model.getAttributes() ) {
				List<Attribute> attributes = model.getAttributes();

				for ( Attribute attribute : attributes ) {
					if ( StringUtils.isEmpty( attribute.getName() ) ) {
						String error = MSG_ATTRIB_ERROR;
						log.error( error );
						throw new RuntimeException( error );
					}

					if ( !NOTES.equals( attribute.getName() ) && StringUtils.isEmpty( attribute.getValue() ) ) {
						String error = String.format( MSG_ATTRIB_VALUE_ERROR, attribute.getName() );
						log.error( error );
						throw new RuntimeException( error );
					}
				}

				Collections.sort( attributes, new AttributeComparator() );

				if ( Files.exists( attributesJson, LinkOption.NOFOLLOW_LINKS ) )
					log.warn( String.format( MSG_EXISTS_WARN, ATTRIBUTES, directory ) );

				FileWriter writer = new FileWriter( attributesJson.toFile() );
				AttributesList attributeList = new AttributesList();
				attributeList.setAttributes( attributes );
				gson.toJson( attributeList, AttributesList.class, writer );
				writer.close();
			}

			path = directory;
			modified = false;
		} else {
			String error = String.format( MSG_NULL_ERROR, directory );
			log.error( error );
			throw new IOException( error );
		}
	}

	@Override public String getCommandForIndex( String command, Object index ) {
		if ( null != index )
			return command + index.toString();
		else
			return command;
	}

	private void newApp() {
		save();

		DeveloperApp oldModel = model;
		String name = view.requestNewInput( CMD_NEW, MSG_ADD_APP );
		name = StringUtils.trim( name );

		if ( null == name )
			return;

		if ( !StringUtils.isEmpty( name ) ) {
			model = new DeveloperApp();
			model.setName( name );
			model.setCredentials( new ArrayList<>() );
			model.getCredentials().add( new Credential() );
			model.getCredentials().get( 0 ).setApiProducts( new ArrayList<>() );
			addProduct();

			Credential credential = null;
			List<Product> products = new ArrayList<>();

			if ( null != ( credential = getCredential( model ) ) )
				products = credential.getApiProducts();

			if ( 0 == products.size() ) {
				model = oldModel;
				view.showMessage( MSG_NO_PROD_ERROR, true, false );
				view.initialize( false );
			} else {
				Attribute displayName = new Attribute();
				displayName.setName( DISPLAY_NAME );
				displayName.setValue( name );

				Attribute notes = new Attribute();
				notes.setName( NOTES );
				notes.setValue( StringUtils.EMPTY );

				List<Attribute> attributes = new ArrayList<>();
				attributes.add( displayName );
				attributes.add( notes );

				model.setAttributes( attributes );
				path = null;
				modified = true;
			}
		} else {
			view.showMessage( MSG_APP_ERROR, true, false );
			view.initialize( false );
		}
	}

	private void open() {
		save();

		try {
			Path loadPath = null != view ? view.getFileOrDirectory( true ) : null ;

			if ( null != loadPath )
				importDeveloperApp( loadPath );
		} catch ( Exception ex ) {
			log.error( ex.getLocalizedMessage(), ex );
			view.showMessage( MSG_DATA_ERROR + IOUtils.LINE_SEPARATOR + ex.getLocalizedMessage(), true, false );
			view.initialize( false );
		}
	}

	private void openWeb() {
		save();

		try {
		    String user = null;
		    String password = null;
		    String organization = null;
		    String payload = null;
		    String developer = null;

		    if ( null == loginCredentials )
		        if ( setCredentials() ) {
		            user = loginCredentials.getUser();
		            password = loginCredentials.getPassword();
		            organization = loginCredentials.getOrganization();
		            developer = loginCredentials.getDeveloper();
		        } else
		            return;
		    else {
		        user = loginCredentials.getUser();
		        password = loginCredentials.getPassword();
		        organization = loginCredentials.getOrganization();
		        developer = loginCredentials.getDeveloper();
		    }

		    String name = view.requestNewInput( CMD_GETWB, MSG_LOAD_APP );
		    name = StringUtils.trim( name );

		    if ( null == name )
		        return;

		    if ( !StringUtils.isEmpty( name ) ) {
		        List<NameValuePair> parameters = new ArrayList<>();
		        parameters.add( new BasicNameValuePair( PROPERTY_EXPAND, Boolean.TRUE.toString() ) );
		        payload = executeHttpMethod( user, password, HTTP_PROTOCOL, HTTP_HOST, HTTP_ENDPOINT, String.format( HTTP_APPS_RESOURCE, organization, developer, name ), parameters, new HttpGet() );

		        if ( !StringUtils.isEmpty( payload ) ) {
		            DeveloperApp tempModel = gson.fromJson( payload, DeveloperApp.class );
		            Credential credential = null;

		            if ( null != ( credential = getCredential( tempModel ) )  && fillCorrectProducts( credential.getApiProducts() ) ) {
		                boolean displayName = false;
		                boolean notes = false;

		                for ( Attribute attribute : tempModel.getAttributes() ) {
		                    if ( DISPLAY_NAME.equals( attribute.getName() ) )
		                        displayName = true;
		                    else if ( NOTES.equals( attribute.getName() ) )
		                        notes = true;
		                }

		                if ( !displayName ) {
		                    Attribute attribute = new Attribute();
		                    attribute.setName( DISPLAY_NAME );
		                    attribute.setValue( tempModel.getName() );

		                    log.warn( String.format( MSG_NO_PROPERTY, DISPLAY_NAME ), false, false );
		                    tempModel.getAttributes().add( attribute );
		                    modified = true;
		                }

		                if ( !notes ) {
		                    Attribute attribute = new Attribute();
		                    attribute.setName( NOTES );
		                    attribute.setValue( StringUtils.EMPTY );

		                    log.warn( String.format( MSG_NO_PROPERTY, NOTES ), false, false );
		                    tempModel.getAttributes().add( attribute );
		                    modified = true;
		                }

		                Collections.sort( credential.getApiProducts(), new ProductComparator() );
		                Collections.sort( tempModel.getAttributes(), new AttributeComparator() );
		                model = tempModel;
		            } else {
		                view.showMessage( String.format( MSG_APP_DATA_ERROR, name ), true, false );
		                view.initialize( false );
		            }
		        } else {
		            view.showMessage( String.format( MSG_APP_EXISTS_ERROR, name, organization, developer ), true, false );
		            view.initialize( false );
		        }
		    } else {
		        view.showMessage( MSG_APP_ERROR, true, false );
		        view.initialize( false );
		    }
		} catch ( Exception e ) {
		    log.error( e.getLocalizedMessage(), e );
		    view.showMessage( MSG_DATA_ERROR + IOUtils.LINE_SEPARATOR + e.getLocalizedMessage(), true, false );
		    view.initialize( false );
		}
	}

	private void reload() {
		if ( null != model && modified )
			if ( !view.showMessage( MSG_LOSE_CHANGES, false, true ) )
				return;

		try {
			Path loadPath = path;

			if ( null != loadPath )
				importDeveloperApp( loadPath );
			else {
				view.showMessage( MSG_NO_APP_LOADED, false, false );
				view.initialize( false );
			}
		} catch ( Exception ex ) {
			log.error( ex.getLocalizedMessage(), ex );
			view.showMessage( MSG_DATA_ERROR + IOUtils.LINE_SEPARATOR + ex.getLocalizedMessage(), true, false );
			view.initialize( false );
		}
	}

	private void save() {
		if ( null != model && modified ) {
			if ( !view.showMessage( MSG_SURE_SAVE, false, true ) )
				return;
		} else
			return;

		try {
			Path loadPath = path;

			if ( null == loadPath ) {
				loadPath = view.saveFileOrDirectory( true );

				if ( null != loadPath ) {
					if ( Files.exists( loadPath.resolve( ATTRIBUTES ), LinkOption.NOFOLLOW_LINKS ) || Files.exists( loadPath.resolve( NAME ), LinkOption.NOFOLLOW_LINKS ) )
						if ( !view.showMessage( MSG_SURE_OVERWRITE , false, true ) )
							return;
				}
			}

			if ( null != loadPath )
				exportDeveloperApp( loadPath );
		} catch ( Exception ex ) {
			log.error( ex.getLocalizedMessage(), ex );
			view.showMessage( MSG_DATA_ERROR + IOUtils.LINE_SEPARATOR + ex.getLocalizedMessage(), true, false );
			view.initialize( false );
		}
	}

	private void saveAs() {
		if ( null != model ) {
			if ( !view.showMessage( MSG_SURE_SAVE, false, true ) )
				return;
		} else
			return;

		try {
			Path loadPath = view.saveFileOrDirectory( true );

			if ( null != loadPath ) {
				if ( Files.exists( loadPath.resolve( ATTRIBUTES ), LinkOption.NOFOLLOW_LINKS ) || Files.exists( loadPath.resolve( NAME ), LinkOption.NOFOLLOW_LINKS ) )
					if ( !view.showMessage( MSG_SURE_OVERWRITE , false, true ) )
						return;

				exportDeveloperApp( loadPath );
			}
		} catch ( Exception ex ) {
			log.error( ex.getLocalizedMessage(), ex );
			view.showMessage( MSG_DATA_ERROR + IOUtils.LINE_SEPARATOR + ex.getLocalizedMessage(), true, false );
			view.initialize( false );
		}
	}

	private void addAttribute() {
		if ( null != model ) {
			String attributeName = view.requestNewInput( CMD_ADDAT, MSG_ADD_ATTRIBUTE );
			attributeName = StringUtils.trim( attributeName );

			if ( null == attributeName )
				return;

			if ( !StringUtils.isEmpty( attributeName ) ) {
				boolean exists = false;
				List<Attribute> attributes = model.getAttributes();

				for ( Attribute attribute : attributes )
					if ( attributeName.equals( attribute.getName() ) ) {
						exists = true;
						break;
					}

				if ( !exists ) {
					Attribute attribute = new Attribute();
					attribute.setName( attributeName );
					attribute.setValue( ATTRIBUTE_PLACEHOLDER );
					attributes.add( attribute );
					modified = true;
				} else {
					view.showMessage( String.format( MSG_ATTRIB_EXISTS_ERROR, attributeName ), true, false );
					view.initialize( false );
				}
			} else {
				view.showMessage( MSG_ATTRIB_ERROR, true, false );
				view.initialize( false );
			}
		}
	}

	private void addProduct() {
		if ( null != model ) {
			String productName = view.requestNewInput( CMD_ADDPR, MSG_ADD_PRODUCT );
			productName = StringUtils.trim( productName );

			if ( null == productName )
				return;

			if ( !StringUtils.isEmpty( productName ) ) {
				Credential credential = null;
				List<Product> products = new ArrayList<>();

				if ( null != ( credential = getCredential( model ) ) )
					products = credential.getApiProducts();

				boolean exists = false;
				
				for ( Product product : products )
					if ( productName.equals( product.getDisplayName() ) ) {
						exists = true;
						break;
					}
				
				if ( !exists ) {
					Product product = new Product();
					product.setDisplayName( productName );
					products.add( product );
					modified = true;
				} else {
					view.showMessage( String.format( MSG_PROD_EXISTS_ERROR, productName ), true, false );
					view.initialize( false );
				}
			} else {
				view.showMessage( MSG_PROD_ERROR, true, false );
				view.initialize( false );
			}
		}
	}

	private void deleteAttribute( String index ) {
		if ( null != model ) {
			List<Attribute> attributes = model.getAttributes();

			try {
				attributes.remove( Integer.parseInt( index ) );
				modified = true;
			} catch ( Exception e ) {
				log.error( e.getLocalizedMessage(), e );
				view.showMessage( MSG_DATA_ERROR + IOUtils.LINE_SEPARATOR + e.getLocalizedMessage(), true, false );
				view.initialize( false );
			}
		}
	}

	private void deleteProduct( String index ) {
		if ( null != model ) {
			Credential credential = null;
			List<Product> products = new ArrayList<>();

			if ( null != ( credential = getCredential( model ) ) )
				products = credential.getApiProducts();

			try {
				if ( 1 == products.size() && null != products.get( Integer.parseInt( index ) ) ) {
					view.showMessage( MSG_PROD_ZERO, true, false );
					view.initialize( false );
				} else {
					products.remove( Integer.parseInt( index ) );
					modified = true;
				}
			} catch ( Exception e ) {
				log.error( e.getLocalizedMessage(), e );
				view.showMessage( MSG_DATA_ERROR + IOUtils.LINE_SEPARATOR + e.getLocalizedMessage(), true, false );
				view.initialize( false );
			}
		}
	}

	private void editAttribute( Object source, String index ) {
		if ( null != model ) {
			List<Attribute> attributes = model.getAttributes();

			try {
				Attribute changes = ( Attribute ) source;
				int idx = Integer.parseInt( index );

				if ( null != changes.getValue() ) {
					attributes.get( Integer.parseInt( index ) ).setValue( changes.getValue() );
					modified = true;
				}

				if ( null != changes.getName() ) {
					boolean contained = false;

					for ( int i = 0 ; i < attributes.size() ; i++ )
						contained = i != idx ? attributes.get( i ).getName().equals( changes.getName() ) : contained;

						if ( !contained ) {
							Attribute finalChange = attributes.remove( Integer.parseInt( index ) );
							finalChange.setName( changes.getName() );
							attributes.add( Integer.parseInt( index ), finalChange );
							modified = true;
						} else {
							view.showMessage( String.format( MSG_ATTRIB_EXISTS_ERROR, changes.getName() ), true, false );
							view.initialize( false );
						}
				}
			} catch ( Exception e ) {
				log.error( e.getLocalizedMessage(), e );
				view.showMessage( MSG_DATA_ERROR + IOUtils.LINE_SEPARATOR + e.getLocalizedMessage(), true, false );
				view.initialize( false );
			}
		}
	}

	private void editProduct( Object source, String index ) {
		if ( null != model ) {
			Credential credential = null;
			List<Product> products = new ArrayList<>();

			if ( null != ( credential = getCredential( model ) ) )
				products = credential.getApiProducts();

			try {
				String change = ( String ) source;
				int idx = Integer.parseInt( index );
				boolean contained = false;

				for ( int i = 0 ; i < products.size() ; i++ )
					contained = i != idx ? products.get( i ).equals( change ) : contained;

					if ( !contained ) {
						Product changed = products.remove( Integer.parseInt( index ) );
						changed.setDisplayName( change );
						products.add( Integer.parseInt( index ), changed );
						modified = true;
					} else {
						view.showMessage( String.format( MSG_PROD_EXISTS_ERROR, change ), true, false );
						view.initialize( false );
					}
			} catch ( Exception e ) {
				log.error( e.getLocalizedMessage(), e );
				view.showMessage( MSG_DATA_ERROR + IOUtils.LINE_SEPARATOR + e.getLocalizedMessage(), true, false );
				view.initialize( false );
			}
		}
	}

	private boolean setCredentials() {
		String oldOrganization = null != loginCredentials ? loginCredentials.getOrganization() : StringUtils.EMPTY;
		
		String tempCredentials = view.requestNewInput( CMD_SETCR, MSG_INP_CREDENTIALS );
		tempCredentials = StringUtils.trim( tempCredentials );

		if ( null == tempCredentials )
			return false;

		if ( !StringUtils.isEmpty( tempCredentials ) ) {
			loginCredentials = gson.fromJson( tempCredentials, LoginCredentials.class );
			
			if ( !oldOrganization.equals( loginCredentials.getOrganization() ) ) {
				apigeeProducts = null;
				productsBorn = 0;
			}
			
			return true;
		} else {
			view.showMessage( MSG_CRED_ERROR, true, false );
			view.initialize( false );
			return false;
		}
	}

	private void getApiKey() {
		try {
		    String user = null;
		    String password = null;
		    String organization = null;
		    String developer = null;
		    String payload = null;

		    if ( null == loginCredentials )
		        if ( setCredentials() ) {
		            user = loginCredentials.getUser();
		            password = loginCredentials.getPassword();
		            organization = loginCredentials.getOrganization();
		            developer = loginCredentials.getDeveloper();
		        } else
		            return;
		    else {
		        user = loginCredentials.getUser();
		        password = loginCredentials.getPassword();
		        organization = loginCredentials.getOrganization();
		        developer = loginCredentials.getDeveloper();
		    }

		    String name = view.requestNewInput( CMD_GETAP, MSG_LOAD_APP );
		    name = StringUtils.trim( name );

		    if ( null == name )
		        return;

		    if ( !StringUtils.isEmpty( name ) ) {
		        List<NameValuePair> parameters = new ArrayList<>();
		        parameters.add( new BasicNameValuePair( PROPERTY_EXPAND, Boolean.TRUE.toString() ) );
		        payload = executeHttpMethod( user, password, HTTP_PROTOCOL, HTTP_HOST, HTTP_ENDPOINT, String.format( HTTP_APPS_RESOURCE, organization, developer, name ), parameters, new HttpGet() );

		        if ( !StringUtils.isEmpty( payload ) ) {
		            DeveloperApp developerApp = gson.fromJson( payload, DeveloperApp.class );
		            Credential credential = null;
		            String consumerKey = null;

		            if ( null != ( credential = getCredential( developerApp ) ) ) {
		                consumerKey = credential.getConsumerKey();

		                StringSelection contents = new StringSelection( consumerKey );
		                Toolkit.getDefaultToolkit().getSystemClipboard().setContents( contents, contents );
		                view.showMessage( String.format( MSG_APP_KEY, name, consumerKey ), false, false );
		                view.initialize( false );
		            } else {
		                view.showMessage( String.format( MSG_APP_DATA_ERROR, name ), true, false );
		                view.initialize( false );
		            }
		        } else {
		            view.showMessage( String.format( MSG_APP_EXISTS_ERROR, name, organization, developer ), true, false );
		            view.initialize( false );
		        }
		    } else {
		        view.showMessage( MSG_APP_ERROR, true, false );
		        view.initialize( false );
		    }
		} catch ( Exception e ) {
		    log.error( e.getLocalizedMessage(), e );
		    view.showMessage( MSG_DATA_ERROR + IOUtils.LINE_SEPARATOR + e.getLocalizedMessage(), true, false );
		    view.initialize( false );
		}
	}

	private void testUpload() {
		if ( testProducts() ) {
		    try {
		        String user = null;
		        String password = null;
		        String organization = null;
		        String developer = null;
		        String payload = null;

		        if ( null == loginCredentials )
		            if ( setCredentials() ) {
		                user = loginCredentials.getUser();
		                password = loginCredentials.getPassword();
		                organization = loginCredentials.getOrganization();
		                developer = loginCredentials.getDeveloper();
		            } else
		                return;
		        else {
		            user = loginCredentials.getUser();
		            password = loginCredentials.getPassword();
		            organization = loginCredentials.getOrganization();
		            developer = loginCredentials.getDeveloper();
		        }

		        List<NameValuePair> parameters = new ArrayList<>();
		        parameters.add( new BasicNameValuePair( PROPERTY_EXPAND, Boolean.TRUE.toString() ) );
		        payload = executeHttpMethod( user, password, HTTP_PROTOCOL, HTTP_HOST, HTTP_ENDPOINT, String.format( HTTP_APPS_RESOURCE, organization, developer, model.getName() ), parameters, new HttpGet() );

		        if ( !StringUtils.isEmpty( payload ) ) {
		            DeveloperApp webModel = gson.fromJson( payload, DeveloperApp.class );
		            
		            Credential credential = null;
                    String consumerKey = null;

                    if ( null != ( credential = getCredential( webModel ) ) ) {
                        consumerKey = credential.getConsumerKey();
                        
                        DeveloperAppUpdateRequest updateRequest = new DeveloperAppUpdateRequest();
                        updateRequest.setAttribute( model.getAttributes() );
                        updateRequest.setApiProducts( getProductNameList( model.getCredentials().get( 0 ).getApiProducts() ) );
                        
                        List<String> productsToRemove = new ArrayList<>(); 
                        
                        for ( Product product : credential.getApiProducts() ) {
                            boolean exists = false;
                            
                            for ( String productName : updateRequest.getApiProducts() ) {
                                if ( productName.equals( product.getApiproduct() ) ) {
                                    exists = true;
                                    break;
                                }
                            }
                            
                            if ( !exists )
                                productsToRemove.add( product.getApiproduct() );
                        }
                        
                        HttpPost request = new HttpPost();
                        request.addHeader( new BasicHeader( HTTP_CONTENT_TYPE, HTTP_CONTENT_VALUE ) );
                        request.setEntity( new StringEntity( gson.toJson( updateRequest, DeveloperAppUpdateRequest.class ), Consts.UTF_8 ) );

                        executeHttpMethod( user, password, HTTP_PROTOCOL, HTTP_HOST, HTTP_ENDPOINT, String.format( HTTP_APPS_RESOURCE, organization, developer, model.getName() ) + HTTP_ATTS_RESOURCE, null, request );
                       
                        for ( String product : productsToRemove )
                            executeHttpMethod( user, password, HTTP_PROTOCOL, HTTP_HOST, HTTP_ENDPOINT, String.format( HTTP_APPS_RESOURCE, organization, developer, model.getName() ) + String.format( HTTP_KEYS_RESOURCE, consumerKey ) + String.format( HTTP_PRODS_RESOURCE, product ), null, new HttpDelete() );
                        
                        executeHttpMethod( user, password, HTTP_PROTOCOL, HTTP_HOST, HTTP_ENDPOINT, String.format( HTTP_APPS_RESOURCE, organization, developer, model.getName() ) + String.format( HTTP_KEYS_RESOURCE, consumerKey ), null, request );
                        view.showMessage( MSG_APP_UPDATED, false, false );
                    } else {
                        view.showMessage( String.format( MSG_APP_DATA_ERROR, webModel.getName() ), true, false );
                        view.initialize( false );
                    }
                } else {
                    Credential credential = null;

                    if ( null != ( credential = getCredential( model ) ) ) {
                        DeveloperAppCreateRequest createRequest = new DeveloperAppCreateRequest();
                        createRequest.setName( model.getName() );
                        createRequest.setKeyExpiresIn( -1L );
                        createRequest.setAttributes( model.getAttributes() );
                        createRequest.setApiProducts( getProductNameList( credential.getApiProducts() ) );
                        
                        HttpPost request = new HttpPost();
                        request.addHeader( new BasicHeader( HTTP_CONTENT_TYPE, HTTP_CONTENT_VALUE ) );
                        request.setEntity( new StringEntity( gson.toJson( createRequest, DeveloperAppCreateRequest.class ), Consts.UTF_8 ) );

                        executeHttpMethod( user, password, HTTP_PROTOCOL, HTTP_HOST, HTTP_ENDPOINT, String.format( HTTP_APPCR_RESOURCE, organization, developer ), null, request );
                        view.showMessage( MSG_APP_CREATED, false, false );
                    } else {
                        view.showMessage( String.format( MSG_APP_DATA_ERROR, model.getName() ), true, false );
                        view.initialize( false );
                    }
		        }
		    } catch ( Exception e ) {
		        log.error( e.getLocalizedMessage(), e );
		        view.showMessage( MSG_DATA_ERROR + IOUtils.LINE_SEPARATOR + e.getLocalizedMessage(), true, false );
		        view.initialize( false );
		    }
		}
	}

	private boolean testProducts() {
		if ( null != model ) {
			try {
				String user = null;
				String password = null;
				String organization = null;
				String errorProducts = StringUtils.EMPTY;

				if ( null == loginCredentials )
					if ( setCredentials() ) {
						user = loginCredentials.getUser();
						password = loginCredentials.getPassword();
						organization = loginCredentials.getOrganization();
					} else
						return false;
				else {
					user = loginCredentials.getUser();
					password = loginCredentials.getPassword();
					organization = loginCredentials.getOrganization();
				}

				initApigeeProducts( user, password, organization );
				
				Credential credential = null;
				List<Product> modelProducts = null;

				if ( null != ( credential = getCredential( model ) ) )
					modelProducts = credential.getApiProducts();

				for ( Product modelProduct : modelProducts ) {
					boolean exists = false;

					for ( Product product : apigeeProducts )
						if ( modelProduct.getDisplayName().equals( product.getDisplayName() ) ) {
						    modelProduct.setName( product.getName() );
							exists = true;
							break;
						}

					if ( !exists )
						errorProducts += "\"" + modelProduct.getDisplayName() + "\"" + IOUtils.LINE_SEPARATOR;
				}

				if ( !StringUtils.isEmpty( errorProducts ) ) {
					view.showMessage( String.format( MSG_PROD_NOT_EXISTS_WARN, organization ) + IOUtils.LINE_SEPARATOR + IOUtils.LINE_SEPARATOR + errorProducts, true, false );
					return false;
				} else {
					view.showMessage( String.format( MSG_PROD_EXISTS_WARN, organization ), false, false );
					return true;
				}
			} catch ( Exception e ) {
				log.error( e.getLocalizedMessage(), e );
				view.showMessage( MSG_DATA_ERROR + IOUtils.LINE_SEPARATOR + e.getLocalizedMessage(), true, false );
				view.initialize( false );
				return false;
			}
		}

		return false;
	}

	private String executeHttpMethod( String user, String password, String protocol, String host, String endpoint, String resource, List<NameValuePair> parameters, HttpRequestBase request ) {
		Credentials credentials = new UsernamePasswordCredentials( user, password );
		CredentialsProvider provider = new BasicCredentialsProvider();
		provider.setCredentials( AuthScope.ANY, credentials );

		URIBuilder builder = new URIBuilder();
		builder.setScheme( protocol );
		builder.setHost( host );
		builder.setPath( endpoint + resource );
		
		if ( null != parameters )
		    builder.setParameters( parameters );

		try {
		    log.info( request.getMethod() + ": " + builder.build().toString() );
			request.setURI( builder.build() );
			HttpClient client = clientBuilder.setDefaultCredentialsProvider( provider ).build();
			HttpResponse response = client.execute( request );

			if ( 2 == ( response.getStatusLine().getStatusCode() / 100 ) )
				return EntityUtils.toString( response.getEntity() );
			else if ( 404 == response.getStatusLine().getStatusCode() )
				return StringUtils.EMPTY;
			else {
				String error = response.getStatusLine().toString();
				log.error( error );
				throw new RuntimeException( error );
			}
		} catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	private Credential getCredential( DeveloperApp developerApp ) {
		if ( null != developerApp ) {
			if ( null != developerApp.getCredentials() && 1 == developerApp.getCredentials().size() )
				return developerApp.getCredentials().get( 0 );
			else
				return null;
		}

		return null;
	}
	
	private boolean fillCorrectProducts( List<Product> products ) {
		try {
			String user = null;
			String password = null;
			String organization = null;

			if ( null != loginCredentials ) {
				user = loginCredentials.getUser();
				password = loginCredentials.getPassword();
				organization = loginCredentials.getOrganization();
			} else
				return false;

			initApigeeProducts( user, password, organization );
						
			for ( int i = 0 ; i < products.size() ; i++ ) {
				boolean exists = false;
				Product product = products.get( i );
				
				for ( Product apigeeProduct : apigeeProducts ) {
					if ( product.getApiproduct().equals( apigeeProduct.getName() ) ) {
						products.remove( i );
						products.add( i, apigeeProduct );
						exists = true;
						break;
					}
				}
				
				if ( !exists )
					return false;
			}
			
			return true;
		} catch ( Exception e ) {
			log.error( e.getLocalizedMessage(), e );
			view.showMessage( MSG_DATA_ERROR + IOUtils.LINE_SEPARATOR + e.getLocalizedMessage(), true, false );
			view.initialize( false );
			return false;
		}
	}
	
	private void initApigeeProducts( String user, String password, String organization ) {
		if ( System.currentTimeMillis() > PRODUCTS_TTL + productsBorn || null == apigeeProducts ) {
			List<NameValuePair> parameters = new ArrayList<>();
			parameters.add( new BasicNameValuePair( PROPERTY_EXPAND, Boolean.TRUE.toString() ) );

			String payload = executeHttpMethod( user, password, HTTP_PROTOCOL, HTTP_HOST, HTTP_ENDPOINT, String.format( HTTP_PRODUCTS_RESOURCE, organization ), parameters, new HttpGet() );

			if ( !StringUtils.isEmpty( payload ) )
			    apigeeProducts = gson.fromJson( payload, ProductsList.class ).getApiProduct();
			else
				apigeeProducts = new ArrayList<>();

			productsBorn = System.currentTimeMillis();
		}
	}
	
	private List<String> getProductDisplayNameList( List<Product> products ) {
		List<String> displayNameList = new ArrayList<>();
		
		for ( Product product : products )
			displayNameList.add( product.getDisplayName() );
		
		return displayNameList;
	}
	
	private List<String> getProductNameList( List<Product> products ) {
        List<String> nameList = new ArrayList<>();
        
        for ( Product product : products )
            nameList.add( product.getName() );
        
        return nameList;
    }
}