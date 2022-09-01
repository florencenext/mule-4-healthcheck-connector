package com.florencenext.connectors.healthcheck.internal.operations;

import com.florencenext.connectors.healthcheck.api.model.entities.Healthcheck;
import com.florencenext.connectors.healthcheck.api.model.entities.ServiceStatus;
import com.florencenext.connectors.healthcheck.api.model.entities.ServiceType;
import com.florencenext.connectors.healthcheck.api.parameter.ExternalConfigRef;
import com.florencenext.connectors.healthcheck.internal.providers.ExtensionErrorProviders;
import com.mule.extensions.amqp.api.config.ConsumerAckMode;
import com.mule.extensions.amqp.api.message.AmqpMessageBuilder;
import com.mule.extensions.amqp.api.message.AmqpProperties;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.client.DefaultOperationParameters;
import org.mule.runtime.extension.api.client.ExtensionsClient;
import org.mule.runtime.extension.api.client.OperationParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static com.florencenext.connectors.healthcheck.internal.helper.ErrorFormatterHelper.createErrorStringFromException;
import static org.mule.runtime.api.metadata.DataType.JSON_STRING;
import static org.mule.runtime.extension.api.annotation.param.display.Placement.DEFAULT_TAB;


public class AmqpOperations {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AmqpOperations.class);
	private static final String amqpExtension = "AMQP";
	private static final String amqpConsumeOperation = "consume";
	private static final String amqpPublishOperation = "publish";
	private static ServiceType type = ServiceType.AMQP;
	private ServiceStatus status = ServiceStatus.UNHEALTHY;
	
	
	@Inject
	ExtensionsClient extensionsClient;

	@Parameter
	@DisplayName("Service Name")
	@Placement(order = 1)
	private String serviceName;
	
	@Parameter
	@DisplayName("AMQP Module Configuration")
	@Summary("Indicate which AMQP config should be associated with this healthcheck.")
	@Placement(tab = DEFAULT_TAB, order = 2)
	private String configRef;
	
    @Parameter
    @Optional
    @Summary("Name of the target exchange")
    @DisplayName("Exchange Destination")
    private String exchangeDestination;

	@Parameter
	@Optional
	@Summary("Name of the target queue destination")
	@DisplayName("Queue Destination")
	private String queueDestination;
	
	/***amqp healthcheck for external systems****/
	@MediaType(value = "application/java", strict = false)
	@Alias("AMQP")
	@Throws(ExtensionErrorProviders.class)
	public Healthcheck getAmqpHealthCheck() {

	String errorString = null;
	
	LOGGER.debug("START: AMQP HEALTHCHECK parameters:");	
	String destExchange = this.exchangeDestination;
	String destQueue = this.queueDestination;	
	LOGGER.debug("AMQP HEALTHCHECK on amqp: with config-ref: "+configRef+" to exchange: "+destExchange+ "to queue:"+destQueue);
	
	long startTime = System.currentTimeMillis(), elapsedTime = 0;
	
	try {
		//publish
		OperationParameters parameters = DefaultOperationParameters.builder().configName(configRef)
				.addParameter("exchangeName", destExchange)
				.addParameter("messageBuilder", AmqpMessageBuilder.class, DefaultOperationParameters.builder()
						.addParameter("body", new TypedValue<>("test body", JSON_STRING))
						.addParameter("properties", new AmqpProperties()))
				.build();					
		extensionsClient.execute(amqpExtension, amqpPublishOperation, parameters);									
		LOGGER.debug("amqp publish executed!");

		//consume
		OperationParameters conParameters = DefaultOperationParameters.builder().configName(configRef)
				.addParameter("queueName", destQueue)
				.addParameter("ackMode", ConsumerAckMode.IMMEDIATE)
				.build();

		extensionsClient.execute(amqpExtension, amqpConsumeOperation, conParameters);			
		elapsedTime = System.currentTimeMillis() - startTime;
		
		LOGGER.debug("amqp consume executed!");
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
