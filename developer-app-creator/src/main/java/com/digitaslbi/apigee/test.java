package com.digitaslbi.apigee;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.digitaslbi.apigee.controller.DeveloperAppControllerImpl;
import com.digitaslbi.apigee.model.Product;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class test {
    public static final String URL = "%s://%s%s%s%s";
    
    public static void main(String[] args) {
        try {
            String user = "george.taylor@digitaslbi.com";
            String password = "XXX";
            String protocol = "https";
            String host = "api.enterprise.apigee.com";
            String endpoint = "/v1/organizations";
            String resource = /*"/digitaslbi-nonprod/developers/qa.nissan@helios.v2.com/apps";*/"/digitaslbi-nonprod/apiproducts";

            List<NameValuePair> parameters = new ArrayList<>();
            parameters.add( new BasicNameValuePair( "expand", "true" ) );
            
            Credentials credentials = new UsernamePasswordCredentials( user, password );
            CredentialsProvider provider = new BasicCredentialsProvider();
            provider.setCredentials( AuthScope.ANY, credentials );
            
            URIBuilder builder = new URIBuilder();
            builder.setScheme( protocol );
            builder.setHost( host );
            builder.setPath( endpoint + resource );
            builder.setParameters( parameters );
            
            HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider( provider ).build();
            HttpGet request = new HttpGet( builder.build() );
            
            HttpResponse response = client.execute( request );
            log.info( response.getStatusLine().toString() );
            
            Map<String, List<LinkedTreeMap<String, Object>>> productsWrapper = new Gson().fromJson( EntityUtils.toString( response.getEntity() ), TreeMap.class );
            List<LinkedTreeMap<String, Object>> products = productsWrapper.get( "apiProduct" );
            
            ////////
            DeveloperAppControllerImpl test = new DeveloperAppControllerImpl();
            ////////
            
            for ( LinkedTreeMap<String, Object> linkedTreeMap : products ) {
                Set<String> mapKeys = linkedTreeMap.keySet();
                Product product = new Product( ( String ) linkedTreeMap.get( "name" ), ( String ) linkedTreeMap.get( "displayName" ), ( List<String> ) linkedTreeMap.get( "proxies" ) );

                if ( linkedTreeMap.containsKey( "attributes" ) ) {
                    LinkedTreeMap<String, Object> attributes = new LinkedTreeMap<>();
                    attributes.put( "attributes", linkedTreeMap.get( "attributes" ) );
                    StringReader reader = new StringReader( new Gson().toJson( attributes ) );
                    product.setAttributes( test.readAttributesMap( reader ) );
                    reader.close();
                }
                
                log.info( product.toString() );
            }
        } catch ( Exception e ) {
            log.error( e.getLocalizedMessage(), e );
        }
    }
}