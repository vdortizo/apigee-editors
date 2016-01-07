package com.digitaslbi.apigee.controller;

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
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.client.HttpClient;

import com.digitaslbi.apigee.model.DeveloperApp;
import com.digitaslbi.apigee.tools.DeveloperAppComparator;
import com.digitaslbi.apigee.view.DeveloperAppView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of DeveloperAppCOntroller.
 * 
 * @author Victor Ortiz
 */
@Slf4j
public class DeveloperAppControllerImpl implements DeveloperAppController {
	private static final String NAME = "name.props";
	private static final String ATTRIBUTES = "attributes.json";
	
	private static final String PROPERTY_NAME = "name";
	private static final String PROPERTY_ATTRIBUTES = "attributes";
	private static final String PROPERTY_PRODUCTS = "products";
	private static final String PROPERTY_VALUE = "value";
	
	private static final String ATTRIBUTE_PLACEHOLDER = "Please change this value";
	private static final String NOTES_PROPERTY = "Notes";
	private static final String PRIMARY_PROPERTY = "DisplayName";
	private static final String PRODUCT_SEPARATOR = ",";
	
	private static final String MSG_ADD_APP = "Please input the new developer app name";
	private static final String MSG_ADD_ATTRIBUTE = "Please input the new attribute name";
	private static final String MSG_ADD_PRODUCT = "Please input the new product name";
	private static final String MSG_APP_ERROR = "The developer app name cannot be empty";
	private static final String MSG_ATTRIB_ERROR = "The attribute name cannot be empty";
    private static final String MSG_ATTRIB_EXISTS_ERROR = "The attribute [%s] already exists in the developer app";
	private static final String MSG_ATTRIB_VALUE_ERROR = "The attribute [%s] cannot be empty";
	private static final String MSG_ATTRIB_WARN = "An attribute with an empty name has been found and will be ignored";
	private static final String MSG_DATA_ERROR = "An error has ocurred";
	private static final String MSG_EXISTS_WARN = "The file [%s] already exists in the directory [%s] and will be overwritten";
	private static final String MSG_FILE_NOT_EXISTS_ERROR = "The file [%s] does not exist under the directory [%s]";
	private static final String MSG_LOSE_CHANGES = "Reloading the current app will lose all unsaved changes, are you sure you want to continue?";
	private static final String MSG_NO_APP_LOADED = "The current developer app was not loaded from the filesystem";
	private static final String MSG_NO_KNOWN_ACTION = "An unknown action [%s] was executed";
	private static final String MSG_NO_PRIMARY_PROPERTY = "The attribute [DisplayName] was not found, it was added with the default value";
	private static final String MSG_NO_PROD_ERROR = "A developer app must contain at least one product";
	private static final String MSG_NOT_DIR_ERROR = "The path [%s] is not a directory";
	private static final String MSG_NOT_EXIST_PROP_ERROR = "The file [%s] does not contain the required property [%s]";
	private static final String MSG_NOT_IMPORT_WARN = ", no attributes will be imported";
	private static final String MSG_NULL_ERROR = "The app object or the path [%s] are null";
	private static final String MSG_NULL_PROP_ERROR = "The property [%s] cannot be null or empty";
	private static final String MSG_PROD_ERROR = "The product cannot be empty or only blank spaces";
    private static final String MSG_PROD_EXISTS_ERROR = "The product [%s] already exists in the developer app";
    private static final String MSG_PROD_WARN = "The product at index [%s] is empty and will be ignored";
    private static final String MSG_PROD_ZERO = "A developer app cannot contain no products";
    private static final String MSG_SURE_SAVE = "Do you want to save your changes?";
    private static final String MSG_VALUE_SEP_ERROR = "The property [%s] must contain one or more values, separated by [%s]";
    
	private Path path;
	private boolean modified;
	
	@Getter private DeveloperApp model;
	@Setter private DeveloperAppView view;
	
	@Override public synchronized void commandExecuted( String command, Object source ) {
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
            case CMD_TSTUP:
            	testDeveloperAppUpload(); break;
            default:
                log.warn( String.format( MSG_NO_KNOWN_ACTION, command ) ); break;
        }
    }
	
	@SuppressWarnings( "unchecked" )
	@Override public synchronized void importDeveloperApp( Path directory ) throws IOException {
		if ( null != directory && Files.isDirectory( directory, LinkOption.NOFOLLOW_LINKS ) ) {
			Path nameProps = directory.resolve( NAME );
			Path attributesJson = directory.resolve( ATTRIBUTES );
			
			if ( Files.exists( nameProps, LinkOption.NOFOLLOW_LINKS ) ) {
				Properties properties = new Properties();
				properties.load( new FileInputStream( nameProps.toFile() ) );
				
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
				
				List<String> products = new ArrayList<>();
				
				for ( int i = 0 ; i < productsArray.length ; i++ ) {
					productsArray[i] = StringUtils.trim( productsArray[i] );
					
					if ( StringUtils.isEmpty( productsArray[i] ) )
						log.warn( String.format( MSG_PROD_WARN, i ) );
					else
						products.add( productsArray[i] );
				}
				
				if ( 0 == products.size() ) {
				    String error = String.format( MSG_VALUE_SEP_ERROR, PROPERTY_PRODUCTS, PRODUCT_SEPARATOR );
					log.error( error );
					throw new RuntimeException( error );
				}
				
				Collections.sort( products );
				DeveloperApp tempModel = new DeveloperApp( name, products );
				
				if ( Files.exists( attributesJson, LinkOption.NOFOLLOW_LINKS ) ) {
					FileReader reader = new FileReader( attributesJson.toFile() );
					Gson gson = new Gson();
					Map<String, List<LinkedTreeMap<String, String>>> attributesWrapper = null;
					
					try {
					    attributesWrapper = gson.fromJson( reader, TreeMap.class );
	                    reader.close();
                    } catch ( Exception e ) {
                        log.warn( e.getLocalizedMessage() );
                    }
					
					if ( null != attributesWrapper ) {
					    if ( !attributesWrapper.containsKey( PROPERTY_ATTRIBUTES ) ) {
					        tempModel.setAttributes( new TreeMap<>( new DeveloperAppComparator() ) );
	                        log.warn( String.format( MSG_NOT_EXIST_PROP_ERROR, ATTRIBUTES, PROPERTY_ATTRIBUTES ) );
					    } else {
	                        List<LinkedTreeMap<String, String>> attributesList = attributesWrapper.get( PROPERTY_ATTRIBUTES );
	                        Map<String, String> attributes = new TreeMap<>( new DeveloperAppComparator() );
	                        
	                        for ( LinkedTreeMap<String, String> linkedTreeMap : attributesList ) {
	                            Set<String> mapKeys = linkedTreeMap.keySet();
	                            
	                            String key = null;
	                            String value = null;
	                            
	                            for ( String mapKey : mapKeys ) {
	                                if ( mapKey.equals( PROPERTY_NAME ) )
	                                    key = linkedTreeMap.get( mapKey );
	                                
	                                if ( mapKey.equals( PROPERTY_VALUE ) )
	                                    value = linkedTreeMap.get( mapKey );
	                            }
	                            
	                            if ( null != value && null != key ) {
	                                if ( StringUtils.isEmpty( key ) )
	                                    log.warn( MSG_ATTRIB_WARN );
	                                else
	                                    attributes.put( key, value );
	                            }
	                            
	                            key = value = null;
	                        }

	                        tempModel.setAttributes( attributes );
	                    }
					} else {
					    tempModel.setAttributes( new TreeMap<>( new DeveloperAppComparator() ) );
					    log.warn( String.format( MSG_NOT_EXIST_PROP_ERROR, ATTRIBUTES, PROPERTY_ATTRIBUTES ) );
                    }
				} else {
					log.warn( String.format( MSG_FILE_NOT_EXISTS_ERROR + MSG_NOT_IMPORT_WARN, ATTRIBUTES, directory ) );
				}
				
				if ( !tempModel.getAttributes().containsKey( PRIMARY_PROPERTY ) ) {
				    log.warn( MSG_NO_PRIMARY_PROPERTY, false, false );
				    tempModel.getAttributes().put( PRIMARY_PROPERTY, tempModel.getName() );
                    modified = true;
                }
				
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
	
	@Override public synchronized void exportDeveloperApp( Path directory ) throws IOException {
		if ( null != model && null != directory ) {
			if ( !Files.exists( directory, LinkOption.NOFOLLOW_LINKS ) )
				Files.createDirectory( directory );
			else if ( !Files.isDirectory( directory, LinkOption.NOFOLLOW_LINKS ) ) {
			    String error = String.format( MSG_NOT_DIR_ERROR, directory );
				log.error( error );
				throw new IOException( error );
			}
			
            for ( int i = 0 ; i < model.getProducts().size() ; i++ ) {
                String product = StringUtils.trim( model.getProducts().get( i ) );
                
                if ( StringUtils.isEmpty( product ) ) {
                    log.warn( String.format( MSG_PROD_WARN, i ) );
                    model.getProducts().remove( i );
                    i--;
                }
            }
            
            if ( 0 == model.getProducts().size() ) {
                String error = String.format( MSG_VALUE_SEP_ERROR, PROPERTY_PRODUCTS, PRODUCT_SEPARATOR );
                log.error( error );
                throw new RuntimeException( error );
            }
			
			Collections.sort( model.getProducts() );
			Path nameProps = directory.resolve( NAME );
			Path attributesJson = directory.resolve( ATTRIBUTES );
			
			Properties properties = new Properties();
			properties.put( PROPERTY_NAME, model.getName() );
			properties.put( PROPERTY_PRODUCTS, StringUtils.join( model.getProducts(), PRODUCT_SEPARATOR ) );
			
			if ( Files.exists( nameProps, LinkOption.NOFOLLOW_LINKS ) )
				log.warn( String.format( MSG_EXISTS_WARN, NAME, directory ) );
			
			properties.store( new FileOutputStream( nameProps.toFile() ), null );
			
			if ( null != model.getAttributes() ) {
				Map<String, List<LinkedTreeMap<String, String>>> attributesWrapper = new TreeMap<>();
				List<LinkedTreeMap<String, String>> attributesList = new ArrayList<>();
				Map<String, String> attributes = model.getAttributes();
				
				for ( String key : attributes.keySet() ) {
					LinkedTreeMap<String, String> linkedTreeMap = new LinkedTreeMap<>();
					linkedTreeMap.put( PROPERTY_NAME, key );
					linkedTreeMap.put( PROPERTY_VALUE, attributes.get( key ) );
					
					if ( !NOTES_PROPERTY.equals( linkedTreeMap.get( PROPERTY_NAME ) ) && StringUtils.isEmpty( linkedTreeMap.get( PROPERTY_VALUE ) ) ) {
					    String error = String.format( MSG_ATTRIB_VALUE_ERROR, linkedTreeMap.get( PROPERTY_NAME ) );
			            log.error( error );
			            throw new RuntimeException( error );
					}
					
					attributesList.add( linkedTreeMap );
				}
				
				attributesWrapper.put( PROPERTY_ATTRIBUTES, attributesList );
				
				if ( Files.exists( attributesJson, LinkOption.NOFOLLOW_LINKS ) )
					log.warn( String.format( MSG_EXISTS_WARN, ATTRIBUTES, directory ) );
				
				FileWriter writer = new FileWriter( attributesJson.toFile() );
				Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
				gson.toJson( attributesWrapper, writer );
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
	
	@Override public synchronized String getCommandForIndex( String command, Object index ) {
	    if ( null != index )
	        return command + index.toString();
	    else
	        return command;
	}
	
	private synchronized void newApp() {
	    save();

	    DeveloperApp oldModel = model;
	    String name = view.requestNewInput( MSG_ADD_APP );
        name = StringUtils.trim( name );
        
        if ( null == name )
            return;
        
        if ( !StringUtils.isEmpty( name ) ) {
            model = new DeveloperApp( name, new ArrayList<>() );
            addProduct();
            
            List<String> products = model.getProducts();
            
            if ( 0 == products.size() ) {
                model = oldModel;
                view.showMessage( MSG_NO_PROD_ERROR, true, false );
                view.initialize( false );
            } else {
                Map<String, String> attributes = new TreeMap<>( new DeveloperAppComparator() );
                attributes.put( PRIMARY_PROPERTY, name );
                
                model.setAttributes( attributes );
                path = null;
                modified = true;
            }
        } else {
            view.showMessage( MSG_APP_ERROR, true, false );
            view.initialize( false );
        }
	}
	
	private synchronized void open() {
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
	
	private synchronized void reload() {
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
	
	private synchronized void save() {
	    if ( null != model && modified ) {
            if ( !view.showMessage( MSG_SURE_SAVE, false, true ) )
                return;
        } else
            return;
        
        try {
            Path loadPath = path;
            
            if ( null == loadPath )
                loadPath = view.saveFileOrDirectory( true );
                
            if ( null != loadPath )
                exportDeveloperApp( loadPath );
        } catch ( Exception ex ) {
            log.error( ex.getLocalizedMessage(), ex );
            view.showMessage( MSG_DATA_ERROR + IOUtils.LINE_SEPARATOR + ex.getLocalizedMessage(), true, false );
            view.initialize( false );
        }
	}
	
	private synchronized void saveAs() {
        if ( null != model ) {
            if ( !view.showMessage( MSG_SURE_SAVE, false, true ) )
                return;
        } else
            return;
        
        try {
            Path loadPath = view.saveFileOrDirectory( true );
                
            if ( null != loadPath )
                exportDeveloperApp( loadPath );
        } catch ( Exception ex ) {
            log.error( ex.getLocalizedMessage(), ex );
            view.showMessage( MSG_DATA_ERROR + IOUtils.LINE_SEPARATOR + ex.getLocalizedMessage(), true, false );
            view.initialize( false );
        }
    }
	
	private synchronized void addAttribute() {
	    if ( null != model ) {
            String attribute = view.requestNewInput( MSG_ADD_ATTRIBUTE );
            attribute = StringUtils.trim( attribute );
            
            if ( null == attribute )
                return;
            
            if ( !StringUtils.isEmpty( attribute ) ) {
                Map<String, String> attributes = model.getAttributes();
                
                if ( !attributes.containsKey( attribute ) ) {
                    attributes.put( attribute, ATTRIBUTE_PLACEHOLDER );
                    modified = true;
                } else {
                    view.showMessage( String.format( MSG_ATTRIB_EXISTS_ERROR, attribute ), true, false );
                    view.initialize( false );
                }
            } else {
                view.showMessage( MSG_ATTRIB_ERROR, true, false );
                view.initialize( false );
            }
        }
	}
	
	private synchronized void addProduct() {
	    if ( null != model ) {
	        String product = view.requestNewInput( MSG_ADD_PRODUCT );
	        product = StringUtils.trim( product );
	        
	        if ( null == product )
	            return;
	        
	        if ( !StringUtils.isEmpty( product ) ) {
	            List<String> products = model.getProducts();
	            
	            if ( !products.contains( product ) ) {
	                products.add( product );
	                modified = true;
	            } else {
	                view.showMessage( String.format( MSG_PROD_EXISTS_ERROR, product ), true, false );
	                view.initialize( false );
	            }
	        } else {
	            view.showMessage( MSG_PROD_ERROR, true, false );
	            view.initialize( false );
	        }
	    }
	}
	
	private synchronized void deleteProduct( String index ) {
	    if ( null != model ) {
	        List<String> products = model.getProducts();
	        
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
	
	private synchronized void deleteAttribute( String index ) {
	    if ( null != model ) {
            Map<String, String> attributes = model.getAttributes();
            
            try {
                attributes.remove( index );
                modified = true;
            } catch ( Exception e ) {
                log.error( e.getLocalizedMessage(), e );
                view.showMessage( MSG_DATA_ERROR + IOUtils.LINE_SEPARATOR + e.getLocalizedMessage(), true, false );
                view.initialize( false );
            }
        }
	}
	
	private synchronized void editProduct( Object source, String index ) {
	    if ( null != model ) {
            List<String> products = model.getProducts();
            
            try {
                String change = ( String ) source;
                int idx = Integer.parseInt( index );
                boolean contained = false;
                
                for ( int i = 0 ; i < products.size() ; i++ )
                    contained = i != idx ? products.get( i ).equals( change ) : contained;
                
                
                if ( !contained ) {
                    products.remove( Integer.parseInt( index ) );
                    products.add( Integer.parseInt( index ), change );
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
	
	@SuppressWarnings( "unchecked" )
	private synchronized void editAttribute( Object source, String index ) {
	    if ( null != model ) {
	        Map<String, String> attributes = model.getAttributes();

	        try {
	            Map<String, String> changes = ( Map<String, String> ) source;

	            String key = changes.get( KEY );
	            String value = changes.get( VALUE );

	            if ( null != value ) {
	                if ( attributes.containsKey( index ) ) {
	                    attributes.replace( index, value );
	                    modified = true;
	                } else {
	                    attributes.put( index, value );
	                    modified = true;
	                }
	            }

	            if ( null != key ) {
	            	if ( !key.equals( index ) ) {
		                if ( !attributes.containsKey( key ) ) {
		                    String keyValue = attributes.remove( index );
		                    attributes.put( key, keyValue );
		                    modified = true;
		                    view.updateDocumentProperty( index, key );
		                } else {
		                    view.showMessage( String.format( MSG_ATTRIB_EXISTS_ERROR, key ), true, false );
		                    view.initialize( false );
		                }
	                }
	            }
	        } catch ( Exception e ) {
	            log.error( e.getLocalizedMessage(), e );
	            view.showMessage( MSG_DATA_ERROR + IOUtils.LINE_SEPARATOR + e.getLocalizedMessage(), true, false );
	            view.initialize( false );
	        }
	    }
	}
	
	private void testDeveloperAppUpload() {
		if ( null != model ) {
			//validate model
			//verify all products
			//test upload
			try {
				HttpClient client = new HttpClient(  );
				client.start();

				log.info( client.GET( "http://www.google.com" )//https://api.enterprise.apigee.com/v1/organizations/digitaslbi-nonprod/apiproducts" )
						.getContentAsString() );

				client.stop();
			} catch ( Exception e ) {
				log.error( e.getLocalizedMessage(), e );
				view.showMessage( MSG_DATA_ERROR + IOUtils.LINE_SEPARATOR + e.getLocalizedMessage(), true, false );
			}
		}
	}
}