package com.florencenext.connectors.healthcheck.internal.scopes;

import com.florencenext.connectors.healthcheck.api.model.entities.Healthcheck;
import com.florencenext.connectors.healthcheck.api.model.entities.ServiceStatus;
import com.florencenext.connectors.healthcheck.api.model.entities.ServiceType;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.extension.api.runtime.route.Chain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mule.runtime.api.meta.ExpressionSupport.SUPPORTED;


public class HealthcheckScope {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthcheckScope.class);


    @Parameter
    @Expression(SUPPORTED)
    private String serviceName;

    @Parameter
    @Expression(SUPPORTED)
    private ServiceType serviceType;


    public void hcScope(Chain operations,
                        CompletionCallback<Healthcheck, Void> callback) {

        LOGGER.debug("Invoking child operations");


        Healthcheck outputHc = new Healthcheck(
                serviceName,
                serviceType,
                ServiceStatus.UNHEALTHY);
        long startTime = System.currentTimeMillis();

        operations.process(
                result -> {
                    LOGGER.debug("SCOPE completed without error,output"+outputHc);
                    long elapsedTime = System.currentTimeMillis() - startTime;

                    outputHc.setStatus(ServiceStatus.HEALTHY);
                    outputHc.setTime(Math.toIntExact(elapsedTime));
                    callback.success(Result.
                            <Healthcheck,Void>builder()
                            .output(outputHc).build());
                },
                (error, previous) -> {
                    LOGGER.debug("SCOPE completed with error");
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    outputHc.setTime(Math.toIntExact(elapsedTime));
                    outputHc.setError(error.getMessage());
                    callback.success(Result.<Healthcheck,Void>builder().output(outputHc).build());
                });
    }
}
