package com.florencenext.connectors.healthcheck.internal.configurations;

import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

public class InternalServiceConfig {

        @Parameter
        @DisplayName("Service Name")
        private String serviceName;

        @Parameter
        @DisplayName("HTTP Module Configuration")
        @Summary("Indicate which HTTP config should be associated with this healthcheck.")
        private String httpConfigModule;

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getHttpConfigModule() {
            return httpConfigModule;
        }

        public void setHttpConfigModul(String httpConfigModul) {
            this.httpConfigModule = httpConfigModul;
        }
}
