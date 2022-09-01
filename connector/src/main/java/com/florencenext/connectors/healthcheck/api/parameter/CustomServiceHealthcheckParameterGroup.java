package com.florencenext.connectors.healthcheck.api.parameter;

import com.florencenext.connectors.healthcheck.api.model.entities.ServiceType;
import org.mule.runtime.extension.api.annotation.param.Parameter;

public class CustomServiceHealthcheckParameterGroup {

    @Parameter
    private String name;

    @Parameter
    private ServiceType type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ServiceType getType() {
        return type;
    }

    public void setType(ServiceType type) {
        this.type = type;
    }
}
