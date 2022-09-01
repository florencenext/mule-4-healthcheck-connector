package com.florencenext.connectors.healthcheck.internal.connection;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.api.connection.ConnectionProvider;

import java.util.List;

import javax.inject.Inject;

import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.http.api.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*
 * This class (as it's name implies) provides connection instances and the funcionality to disconnect and validate those
 * connections.
 * <p>
 * All connection related parameters (values required in order to create a connection) must be
 * declared in the connection providers.
 * <p>
 * This particular example is a {@link PoolingConnectionProvider} which declares that connections resolved by this provider
 * will be pooled and reused. There are other implementations like {@link CachedConnectionProvider} which lazily creates and
 * caches connections or simply {@link ConnectionProvider} if you want a new connection each time something requires one.
 */
public class HealthCheckConnectionProvider implements PoolingConnectionProvider<HealthCheckConnection> {

  private final Logger LOGGER = LoggerFactory.getLogger(HealthCheckConnectionProvider.class);

	 @Parameter
	 @Placement(tab = "Advanced")
	 @Optional(defaultValue = "10000")
	 int connectionTimeout;

	@Parameter
	@Optional
	private String protocol;
	@Parameter
	@Optional
	private String clientId;
	@Parameter
	@Optional
	private String clientSecret;
	@Parameter
	@Optional
	private String host;
	@Parameter
	@Optional
	private String port;
	@Parameter
	@Optional
	private String basepath;
	@Parameter
	@Optional
	private String path;

		protected MultiMap<String, String> headers = new MultiMap<String, String>();
		
	 @Inject
	 private HttpService httpService; 
 /**
  * A parameter that is always required to be configured.
  */

  @Override
  public HealthCheckConnection connect() throws ConnectionException {
	  
	  String uri = protocol + "://" + host + ":" + port + basepath + path;
	  headers.put("X-Client-Id", clientId);
	  headers.put("X-Client-Secret", clientSecret);
	  return new HealthCheckConnection(httpService, uri,headers, connectionTimeout);
  }

  @Override
  public void disconnect(HealthCheckConnection connection) {
    try {
      connection.invalidate();
    } catch (Exception e) {
      LOGGER.error("Error while disconnecting to Weather Channel " + e.getMessage(), e);
    }
  }

  @Override
  public ConnectionValidationResult validate(HealthCheckConnection connection) {
	  ConnectionValidationResult result;
      try {
           if(connection.isConnected()){
                  result = ConnectionValidationResult.success();
            } else {
                  result = ConnectionValidationResult.failure("Connection Failed", new Exception());
            }
     } catch (Exception e) {
           result = ConnectionValidationResult.failure("Connection failed: " + e.getMessage(), new Exception());
     }
   return result;
  }
}
