package com.florencenext.connectors.healthcheck.api.parameter;

import org.mule.runtime.extension.api.annotation.param.Parameter;

public class ExternalConfigRef {

    @Parameter
    private String serviceName;

    @Parameter
    private String configurationName;

    public String getName() {
        return serviceName;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }
}
