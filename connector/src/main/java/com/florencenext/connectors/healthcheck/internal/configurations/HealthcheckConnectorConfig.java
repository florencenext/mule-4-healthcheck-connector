package com.florencenext.connectors.healthcheck.internal.configurations;

import com.florencenext.connectors.healthcheck.api.model.entities.ServiceType;
import com.florencenext.connectors.healthcheck.internal.operations.*;
import com.florencenext.connectors.healthcheck.internal.scopes.HealthcheckScope;
import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.RefName;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;

@Configuration(name = "healthCheckConnectorConfig")
@Operations({
        HealthCheckOperations.class,
        InternalOperations.class,
        HealthcheckScope.class})

public class HealthcheckConnectorConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthcheckConnectorConfig.class);

    @RefName
    private String configName;


    public enum ExternalConfigType{
        DB,JMS,AMQP,HTTP
    }

    @Parameter
    @Optional(defaultValue="${app.name}")
    private String applicationName;

    @Parameter
    @Optional(defaultValue="SAPI")
    private ServiceType applicationType;

    @Parameter
    @Placement(tab = "Internal Services",order = 1)
    @DisplayName("Services")
    @Optional(defaultValue="#[[]]")
    private List<InternalServiceConfig> internalServicesConfigs;

    @Parameter
    @Placement(tab = "Internal Services",order = 1)
    @DisplayName("Healthcheck PATH")
    @Summary("Path where Healthcheck endpoint are implemented")
    @Optional(defaultValue="/api/healthcheck")
    private String healthcheckPath;


    @Parameter
    @DisplayName("Response Timeout")
    @Optional(defaultValue = "10000")
    @Expression(NOT_SUPPORTED)
    @Placement(tab = "Internal Services",order = 2)
    private Integer responseTimeout;

    public List<InternalServiceConfig> getInternalServicesConfigs() {
        return internalServicesConfigs;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public ServiceType getApplicationType() {
        return applicationType;
    }

    public Integer getResponseTimeout() {
        return responseTimeout;
    }

    public String getHealthcheckPath() {
        return healthcheckPath;
    }

}
