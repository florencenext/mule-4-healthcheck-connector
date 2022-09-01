package com.florencenext.connectors.healthcheck.internal.operations;

import com.florencenext.connectors.healthcheck.api.model.entities.Healthcheck;
import com.florencenext.connectors.healthcheck.api.model.entities.ServiceStatus;
import com.florencenext.connectors.healthcheck.api.model.entities.ServiceType;
import com.florencenext.connectors.healthcheck.internal.providers.ExtensionErrorProviders;
import org.mule.runtime.api.streaming.object.CursorIterator;
import org.mule.runtime.api.streaming.object.CursorIteratorProvider;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.client.DefaultOperationParameters;
import org.mule.runtime.extension.api.client.ExtensionsClient;
import org.mule.runtime.extension.api.client.OperationParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

import static com.florencenext.connectors.healthcheck.internal.helper.ErrorFormatterHelper.createErrorStringFromException;

public class DatabaseOperations {	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseOperations.class);
	private static final String dbExtension = "Database";
	private static final String dbOperation = "select";
	private static ServiceType type = ServiceType.DB;
	private ServiceStatus status = ServiceStatus.UNHEALTHY;


	@Inject
	ExtensionsClient extensionsClient;

	@Parameter
	@DisplayName("Service Name")
	@Placement(order = 1)
	private String serviceName;

	@Parameter
	@DisplayName("DB Module Configuration")
	@Summary("Indicate which DB config should be associated with this healthcheck.")
	@Placement(order = 2)
	private String configRef;
			
	
	/***database healthcheck for external systems****/
	@MediaType(value = "application/java", strict = false)
	@Alias("DB")
	@Throws(ExtensionErrorProviders.class)
	public Healthcheck getDbHealthCheck(
			@DisplayName("SQL Query Text") String inputSqlQuery) {
		
		String inputQuery = inputSqlQuery;
		String errorString = null;

		long startTime = System.currentTimeMillis(), elapsedTime = 0;

		try {

			OperationParameters dbParameters = DefaultOperationParameters.builder()
					.configName(configRef)
					.addParameter("sql", inputQuery)
					.build();

			CursorIteratorProvider input = (CursorIteratorProvider) extensionsClient.execute(dbExtension, dbOperation, dbParameters).getOutput();
			elapsedTime = System.currentTimeMillis() - startTime;
			
			CursorIterator<Map<String, Object>> iterator = input.openCursor();									
		    while (iterator.hasNext()) {
		        Map<String, Object> row = (Map<String, Object>) iterator.next();
		        LOGGER.debug("db iterator" + row );
		      }
		    	
		    LOGGER.info("db executed!");				
			status = ServiceStatus.HEALTHY;

		} catch (Exception e) {
			status = ServiceStatus.UNHEALTHY;
			elapsedTime = System.currentTimeMillis() - startTime;
			errorString = createErrorStringFromException(e);
			LOGGER.error("Error: " + e.getMessage());
			e.printStackTrace();
		}
		Healthcheck response = new Healthcheck(serviceName, type, status,(Integer) Math.toIntExact(elapsedTime),errorString);
		LOGGER.info("DB Healthcheck computed:"+response);
		return response;
		
	}	
}
