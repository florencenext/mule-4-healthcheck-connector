package com.florencenext.connectors.healthcheck.internal;

import com.florencenext.connectors.healthcheck.internal.configurations.*;
import com.florencenext.connectors.healthcheck.internal.operations.AmqpOperations;
import com.florencenext.connectors.healthcheck.internal.operations.DatabaseOperations;
import com.florencenext.connectors.healthcheck.internal.operations.HttpOperations;
import com.florencenext.connectors.healthcheck.internal.operations.JmsOperations;
import org.mule.runtime.extension.api.annotation.Export;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.runtime.extension.api.annotation.error.ErrorTypes;

import com.florencenext.connectors.healthcheck.api.model.errors.ExtensionErrorTypes;



/**
 * This is the main class of an extension, is the entry point from which configurations, connection providers, operations
 * and sources are going to be declared.
 */
@Xml(prefix = "healthcheck")
@Extension(name = "Health Check Connector")
@ErrorTypes(ExtensionErrorTypes.class)
@Configurations({HealthcheckConnectorConfig.class})
@Export(resources = {"modules/HealthcheckConnectorModule.dwl"})
@Operations(
        {
            DatabaseOperations.class,
            AmqpOperations.class,
            JmsOperations.class,
            HttpOperations.class
        })
public class HealthCheckExtension {

}
