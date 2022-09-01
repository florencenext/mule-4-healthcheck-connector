package com.florencenext.connectors.healthcheck.internal.operations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.florencenext.connectors.healthcheck.api.model.entities.Healthcheck;
import com.florencenext.connectors.healthcheck.api.model.entities.ServiceStatus;
import com.florencenext.connectors.healthcheck.api.model.entities.ServiceType;
import com.florencenext.connectors.healthcheck.internal.configurations.HealthcheckConnectorConfig;
import com.florencenext.connectors.healthcheck.internal.configurations.InternalServiceConfig;
import com.florencenext.connectors.healthcheck.internal.providers.ExtensionErrorProviders;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.el.BindingContext;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.annotation.param.display.Text;
import org.mule.runtime.extension.api.client.DefaultOperationParameters;
import org.mule.runtime.extension.api.client.ExtensionsClient;
import org.mule.runtime.extension.api.client.OperationParameters;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.florencenext.connectors.healthcheck.internal.helper.ErrorFormatterHelper.createErrorStringFromException;
import static com.florencenext.connectors.healthcheck.internal.helper.HealthcheckNormalizer.normalizeHealthCheck;


/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class InternalOperations {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalOperations.class);

    private static final String CLIENT_ID_HEADER_NAME = "X-Client-Id";
    private static final String CLIENT_SECRET_HEADER_NAME = "X-Client-Secret";

    private final String HEALTHCHECK_METHOD = "GET";
    private final String DEFAULT_EXPRESSION = "output application/json import * from modules::HealthcheckConnectorModule --- payload as HealthCheck";

    private static final String httpExtension = "HTTP";
    private static final String httpOperation = "request";

    @Inject
    ExtensionsClient extensionsClient;

    @Inject
    private HttpService httpService;

    @Inject
    ExpressionManager expressionManager;

    @Parameter
    @Text
    @Optional(defaultValue = "false")
    @DisplayName("Enable execution of custom script to extract healthcheck from dependent apis.")
    boolean applyCustomMapping = false;

    @Parameter
    @Text
    @Optional(defaultValue="output application/json --- payload")
    @DisplayName("Custom mapping")
    @Expression(ExpressionSupport.SUPPORTED)
    @Summary("Custom script content.Can be also a reference to the script file.")
    String customMapping;

    /*** healthcheck ***/
    @MediaType(value = "application/java", strict = false)
    @Alias("InternalServices")
    @Throws(ExtensionErrorProviders.class)
    public List<Healthcheck> getHealthCheckFromInternalServices(@Config HealthcheckConnectorConfig c) {

        LOGGER.debug("START: internalServices healthcheck");

        String healthcheckPath = c.getHealthcheckPath();
        List<InternalServiceConfig> internalServicesUris= c.getInternalServicesConfigs();

        List<Healthcheck> healthCheckListResult = new ArrayList<Healthcheck>();

        for (InternalServiceConfig serviceConfig : internalServicesUris) {

            String httpConfigName = serviceConfig.getHttpConfigModule();
            String internalServiceName = serviceConfig.getServiceName();

            long startTime = System.currentTimeMillis(), elapsedTime = 0;

            try {
                LOGGER.debug("Calling internal serivce with config named:"+httpConfigName);
                OperationParameters parameters = DefaultOperationParameters.builder().configName(httpConfigName)
                        .addParameter("path", healthcheckPath)
                        .addParameter("method", HEALTHCHECK_METHOD)
                        .build();
                Result<HttpEntity, HttpResponseAttributes> result = extensionsClient.execute(httpExtension, httpOperation, parameters);

                java.util.Optional<HttpResponseAttributes> attributes = result.getAttributes();

                LOGGER.info("request executed!");

                elapsedTime = System.currentTimeMillis() - startTime;

                    if(attributes.get().getStatusCode() == 200) {

                        LOGGER.debug("Received response from internal api:" +internalServiceName);
                        Object output = result.getOutput();
                        InputStream response = null;
                        if (output instanceof CursorStreamProvider) {
                            response =  ((CursorStreamProvider) output).openCursor();
                        } else if (output instanceof InputStream) {
                            response =  (InputStream) output;
                        }


                        TypedValue transformedHealthcheck = null;
                        DataType dataType = DataType.builder()
                                .type(Healthcheck.class)
                                .mediaType(MediaType.APPLICATION_JSON)
                                .build();

                        BindingContext bindingContext = BindingContext.builder()
                                .addBinding(
                                "payload", new TypedValue<>(response, dataType)
                                ).build();

                        String dwToRun = applyCustomMapping ? customMapping : DEFAULT_EXPRESSION;
                        try {
                            transformedHealthcheck = expressionManager.evaluate(dwToRun,bindingContext);
                        } catch (RuntimeException e ) {
                            e.printStackTrace();
                        }

                        LOGGER.debug("Output after apply internal dataweave: "+transformedHealthcheck.getValue());
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode actualObj = mapper.readTree(transformedHealthcheck.getValue().toString());
                        Healthcheck serviceHc = mapper.convertValue(actualObj, Healthcheck.class);
                        normalizeHealthCheck(serviceHc);

                        LOGGER.info("Response Received:" + serviceHc.toString());
                        LOGGER.debug("!!!!elapsedTime:"+elapsedTime);
                        serviceHc.setTime((Integer) Math.toIntExact(elapsedTime));
                        healthCheckListResult.add(serviceHc);
                        LOGGER.info("Response Appended");
                    }
                    else {
                        // error on internal http request
                        elapsedTime = System.currentTimeMillis() - startTime;
                        String errorString = "HTTP: " + attributes.get().getReasonPhrase() +": " + internalServiceName;
                        Healthcheck serviceInError = new Healthcheck(internalServiceName,
                                ServiceType.HTTP,
                                ServiceStatus.UNHEALTHY,
                                (Integer) Math.toIntExact(elapsedTime),
                                errorString);
                        healthCheckListResult.add(serviceInError);
                    }
            } catch(Exception e) {
                String errorString = createErrorStringFromException(e);
                Healthcheck serviceInError = new Healthcheck(
                        internalServiceName,
                        ServiceType.HTTP,
                        ServiceStatus.UNHEALTHY,
                        (Integer) Math.toIntExact(elapsedTime),
                        errorString);
                healthCheckListResult.add(serviceInError);
                LOGGER.error("ConnectException occured!");
                e.printStackTrace();
            }
        }
        LOGGER.info("Services checked " + healthCheckListResult.size());
        return healthCheckListResult;
    }

}
