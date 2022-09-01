package com.florencenext.connectors.healthcheck.internal.connection;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.HttpConstants.Method;

import java.net.URI;

import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpClientConfiguration;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents an extension connection just as example (there is no real connection with anything here c:).
 */
public final class HealthCheckConnection {

    private final Logger LOGGER = LoggerFactory.getLogger(HealthCheckConnection.class);


    private int connectionTimeout;
    private String uri;
    private HttpClient httpClient;
    private HttpRequestBuilder httpRequestBuilder;
    MultiMap<String, String> headers;
    
    
    public HealthCheckConnection(HttpService httpService, String uri, MultiMap<String, String> headers, int cTimeout) {
    	
    	LOGGER.debug("debug:" + uri);
        this.uri = uri;
        this.headers = headers;
        connectionTimeout = cTimeout;      
        initHttpClient(httpService);
  }
    
    public void initHttpClient(HttpService httpService){
    	
          HttpClientConfiguration.Builder builder = new HttpClientConfiguration.Builder();
          builder.setName("testConfig");
          httpClient = httpService.getClientFactory().create(builder.build());
          httpRequestBuilder = HttpRequest.builder();         
          httpClient.start();
    }
    
  public void invalidate() {
    // do something to invalidate this connection!
	  httpClient.stop();
  }
  
  public boolean isConnected() throws Exception{
	   
	   URI uri = new URI(this.uri);
	   HttpRequest request = httpRequestBuilder
			   .method(Method.GET) 
			   .uri(uri)
			   .headers(headers)
			   .build();

	   LOGGER.debug("debug in connection:" + request);
	   HttpResponse httpResponse = httpClient.send(request,connectionTimeout,false,null);

	   if (httpResponse.getStatusCode() >= 200 && httpResponse.getStatusCode() < 300)
		   return true;
	   else
		   throw new ConnectionException("Error connecting to the server: Error Code " + httpResponse.getStatusCode()
		   + "~" + httpResponse);
	    }
}
