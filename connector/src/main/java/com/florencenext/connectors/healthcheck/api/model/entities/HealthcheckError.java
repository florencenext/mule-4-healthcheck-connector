package com.florencenext.connectors.healthcheck.api.model.entities;

import java.io.Serializable;
import java.util.List;

public class HealthcheckError implements Serializable {

    private static final long serialVersionUID = 5826570243388248788L;

    private String name;
    private String error;


    public HealthcheckError() {}

    public HealthcheckError(String name, String error) {
        this.name = name;
        this.error = error;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "HealthcheckError{" +
                "name='" + name + '\'' +
                ", error=" + error +
                '}';
    }
}
