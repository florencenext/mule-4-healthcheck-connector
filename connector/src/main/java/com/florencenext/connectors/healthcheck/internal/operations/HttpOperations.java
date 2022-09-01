package com.florencenext.connectors.healthcheck.internal.operations;

import com.florencenext.connectors.healthcheck.api.model.entities.Healthcheck;
import com.florencenext.connectors.healthcheck.api.model.entities.ServiceStatus;
import com.florencenext.connectors.healthcheck.api.model.entities.ServiceType;
import com.florencenext.connectors.healthcheck.internal.providers.ExtensionErrorProviders;
import com.florencenext.connectors.healthcheck.internal.providers.MethodTypeProvider;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.param.*;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.annotation.values.OfValues;
import org.mule.runtime.extension.api.client.DefaultOperationParameters;
import org.mule.runtime.extension.api.client.ExtensionsClient;
import org.mule.runtime.extension.api.client.OperationParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

import static com.florencenext.connectors.healthcheck.internal.helper.ErrorFormatterHelper.createErrorStringFromException;
import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;


public class HttpOperations {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpOperations.class);	
	private static final String httpExtension = "HTTP";
	private static final String httpOperation = "request";
	private static ServiceType type = ServiceType.HTTP;
	private ServiceStatus status = ServiceStatus.UNHEALTHY;
	
	public enum Method {
		POST, GET, HEAD;
	}
	
	@Inject
	ExtensionsClient extensionsClient;

	@Parameter
	@DisplayName("Service Name")
	@Placement(order = 1)
	private String serviceName;

	@Parameter
	@DisplayName("HTTP Module Configuration")
	@Summary("Indicate which HTTP config should be associated with this healthcheck.")
	@Placement(order = 2)
	private String configRef;

	@Parameter
	@DisplayName("Method")
	@Optional(defaultValue = "GET")
	@Placement(order = 3)
	@Expression(NOT_SUPPORTED)
	@OfValues(MethodTypeProvider.class)
	private String method;

	@Parameter
	@Optional(defaultValue = "/")
	@Placement(order = 4)
	private String path;

			
	
	/***http healthcheck for external systems****/
	@MediaType(value = "application/java", strict = false)
	@Alias("HTTP")
	@Throws(ExtensionErrorProviders.class)
	public Healthcheck getHttpHealthCheck(@Placement(order = 5) @Optional @NullSafe @Content(primary = true) Map<String, String> httpHeaders,
										  @Placement(order = 6) @Optional @NullSafe @Content Map<String, String> httpUriParams,
										  @Placement(order = 7) @Optional @NullSafe @Content Map<String, String> httpQueryParams) {

		String httpMethod = this.method;
		String httpPath = this.path;
		
		MultiMap<String, String> headers = new MultiMap<String, String>(httpHeaders);		
		MultiMap<String, String> queryParams = new MultiMap<String, String>(httpQueryParams);
		MultiMap<String, String> uriParams = new MultiMap<String, String>(httpUriParams);
		String errorString = "";
		long startTime = System.currentTimeMillis(), elapsedTime = 0;

		try {
			OperationParameters parameters = DefaultOperationParameters.builder().configName(configRef)
					.addParameter("path", httpPath)
					.addParameter("method", httpMethod)
					.addParameter("headers", headers)
					.addParameter("queryParams", queryParams)
					.addParameter("uriParams", uriParams)
					.build();
			extensionsClient.execute(httpExtension, httpOperation, parameters);
			elapsedTime = System.currentTimeMillis() - startTime;
			LOGGER.info("http request executed");
			status = ServiceStatus.HEALTHY;
		} catch (Exception e) {
			status = ServiceStatus.UNHEALTHY;
			elapsedTime = System.currentTimeMillis() - startTime;
			errorString = createErrorStringFromException(e);
			LOGGER.error("Error: " + e.getMessage());
			e.printStackTrace();
		}

		return new Healthcheck(serviceName, type, status,(Integer) Math.toIntExact(elapsedTime),errorString);
	}

}
