package com.florencenext.connectors.healthcheck.internal.helper;

import com.florencenext.connectors.healthcheck.api.model.entities.Healthcheck;
import com.florencenext.connectors.healthcheck.api.model.entities.HealthcheckError;

import java.util.ArrayList;

public class HealthcheckNormalizer {


    public static void normalizeHealthCheck(Healthcheck hc){

        if (hc.getTime() == null) hc.setTime(0);
        if (hc.getDependenciesErrors() == null) hc.setDependenciesErrors(new ArrayList<HealthcheckError>());

        if (hc.getDependencies() == null) {
            hc.setDependencies(new ArrayList<Healthcheck>());
        }
        else {
            for(Healthcheck dep: hc.getDependencies()){
                normalizeHealthCheck(dep);
            }
        }

    }
}
