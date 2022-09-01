package com.florencenext.connectors.healthcheck.internal.operations;

import com.florencenext.connectors.healthcheck.api.model.entities.Healthcheck;
import com.florencenext.connectors.healthcheck.api.model.entities.HealthcheckError;
import com.florencenext.connectors.healthcheck.api.model.entities.ServiceStatus;
import com.florencenext.connectors.healthcheck.internal.configurations.HealthcheckConnectorConfig;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.florencenext.connectors.healthcheck.internal.helper.HealthcheckNormalizer.normalizeHealthCheck;
import static org.mule.runtime.api.meta.ExpressionSupport.REQUIRED;


/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class HealthCheckOperations {

	private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckOperations.class);


	/***heartbeat***/
	@MediaType(value = "application/java", strict = false)
	@Alias("CreateHealthcheckObject")
	public Healthcheck healthcheck(@Config HealthcheckConnectorConfig c,
								   @DisplayName("Dependencies")
								  @Expression(REQUIRED)
								  @Optional() List<Healthcheck> services){

		Healthcheck outputHealthcheck = new Healthcheck(
				c.getApplicationName(),
				c.getApplicationType(),
				ServiceStatus.HEALTHY);
		Integer outputTime = 0;

		if(!(services == null || services.size() == 0)) {

//
			for (Healthcheck dep : services) {
				LOGGER.debug("Computing dependency:"+dep);
				normalizeHealthCheck(dep);

				outputTime = dep.getTime() > outputTime ?  dep.getTime() : outputTime;

				if (!isHcHealty(dep)) {
					/**
					 * An error occured in one of the dependencies
					 * **/
					outputHealthcheck.setStatus(ServiceStatus.UNHEALTHY);
					LOGGER.debug("Bubbling up of dependency errors");
					for (HealthcheckError innerDepError : dep.getDependenciesErrors()) {
						outputHealthcheck.addHealthcheckError(innerDepError);
					}
					if(!(dep.getError() == null) && !dep.getError().isEmpty()){
						outputHealthcheck.addHealthcheckError(dep.getName(),dep.getError());
					}
				}
				outputHealthcheck.addDependency(dep);
			}
		}
		LOGGER.debug("FINAL HEALTHCHECK:"+ outputHealthcheck);
		outputHealthcheck.setTime(outputTime);
		return outputHealthcheck;
	}

	private boolean isHcHealty(Healthcheck hc){
		return (hc.getStatus() == ServiceStatus.HEALTHY) &&
				(hc.getDependenciesErrors() == null || hc.getDependenciesErrors().size() == 0 || hc.getDependenciesErrors() == null) &&
				(hc.getError() == null || hc.getError().isEmpty());
	}
}
