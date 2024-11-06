package com.florencenext.connectors.healthcheck.internal.operations;

import com.florencenext.connectors.healthcheck.api.model.entities.Healthcheck;
import com.florencenext.connectors.healthcheck.api.model.entities.ServiceStatus;
import com.florencenext.connectors.healthcheck.api.model.entities.ServiceType;
import com.florencenext.connectors.healthcheck.internal.providers.ExtensionErrorProviders;
import org.mule.extensions.jms.api.config.ConsumerAckMode;
import org.mule.extensions.jms.api.destination.QueueConsumer;
import org.mule.extensions.jms.api.message.JmsxProperties;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.client.ExtensionsClient;
import org.mule.runtime.extension.api.client.OperationParameterizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.mule.runtime.extension.api.runtime.operation.Result;


import static com.florencenext.connectors.healthcheck.internal.helper.ErrorFormatterHelper.createErrorStringFromException;
import static org.mule.runtime.api.metadata.DataType.JSON_STRING;

public class JmsOperations {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JmsOperations.class);

	private static final String jmsExtension = "JMS";
	private static final String jmsConsumeOperation = "consume";
	private static final String jmsPublishOperation = "publish";
	private static ServiceType type = ServiceType.JMS;
	private ServiceStatus status = ServiceStatus.UNHEALTHY;

	@Inject
	ExtensionsClient extensionsClient;

	@Inject
	private MuleContext muleContext;


	@Parameter
	@DisplayName("Service Name")
	@Placement(order = 1)
	private String serviceName;

	@Parameter
	@DisplayName("JMS Module Configuration")
	@Summary("Indicate which JMS config should be associated with this healthcheck.")
	@Placement(order = 2)
	private String configRef;
		
	@Parameter
	@Summary("Name of the target queue destination")
	@DisplayName("Queue Destination")
	private String queueDestination;
	
	/***jms healthcheck for external systems****/
	@MediaType(value = "application/java", strict = false)
	@Alias("JMS")
	@Throws(ExtensionErrorProviders.class)
	public Healthcheck getJmsHealthCheck() {

		LOGGER.debug("START: JMS HEALTHCHECK parameters:");


		String destQueue = this.queueDestination;

		LOGGER.debug("JMS HEALTHCHECK on jms: with config-ref: "+configRef+" to queue:"+destQueue);
		String errorString = null;

		long to = ((Number) 1).longValue();
		long startTime = System.currentTimeMillis(), elapsedTime = 0;


		try {

			Consumer<OperationParameterizer> parameters = operationParameterizer ->
					operationParameterizer.withConfigRef(configRef)
							.withParameter("destination", destQueue)
							.withParameter("body", new TypedValue<>("this is test", JSON_STRING))
							.withParameter("jmsxProperties", new JmsxProperties())
							.withParameter("properties", new HashMap<String, Object>())
							.withParameter("sendEncoding", true)
							.withParameter("sendContentType", true);



			LOGGER.debug("JMS HEALTHCHECK parameters:"+parameters);

			CompletableFuture<Result<Void, Void>> futureResultPublish = extensionsClient.execute(jmsExtension, jmsPublishOperation, parameters);

			Result<Void, Void> resultsPublish = futureResultPublish.get(10, TimeUnit.SECONDS);

			LOGGER.info("jms publish executed!");

			Consumer<OperationParameterizer> conParameters = operationParameterizer ->
					operationParameterizer.withConfigRef(configRef)
							.withParameter("destination", destQueue)
							.withParameter("consumerType", new QueueConsumer())
							.withParameter("ackMode", ConsumerAckMode.IMMEDIATE);

			CompletableFuture<Result<Void, Void>> futureResultConsume = extensionsClient.execute(jmsExtension, jmsConsumeOperation, conParameters);

			Result<Void, Void> resultsConsume = futureResultConsume.get(10, TimeUnit.SECONDS);

			elapsedTime = System.currentTimeMillis() - startTime;
			
			LOGGER.info("jms consume executed!");
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
