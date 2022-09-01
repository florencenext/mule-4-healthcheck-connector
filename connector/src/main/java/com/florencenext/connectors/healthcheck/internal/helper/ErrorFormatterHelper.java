package com.florencenext.connectors.healthcheck.internal.helper;

import com.florencenext.connectors.healthcheck.internal.scopes.HealthcheckScope;
import org.mule.runtime.api.exception.MuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

import static org.mule.runtime.api.exception.MuleException.INFO_ERROR_TYPE_KEY;

public class ErrorFormatterHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorFormatterHelper.class);


    public static String createErrorStringFromException(Exception ex){

        String result="";
        // code block
        try {

                if (ex instanceof ConnectException) {
                    result = "HTTP:CONNECTIVITY :"+ex.getMessage();
                } else if (ex instanceof TimeoutException) {
                    result = "HTTP:TIMEOUT:" + ex.getMessage();
                } else if (ex instanceof MuleException) {
                    MuleException mex = (MuleException) ex;
                    String errorType = mex.getInfo().get(INFO_ERROR_TYPE_KEY).toString();
                    LOGGER.error(mex.toString());
                    LOGGER.error(mex.getInfo().toString());
                    String errorDescription = mex.getMessage().toString();
                    result = errorType + ": " + errorDescription;
                } else if (ex instanceof IOException) {
                    result = "HTTP:TIMEOUT" + ": " + ex.getMessage();
                } else {
                    result = "MULE:UNKOWN:"+ ex.getMessage();
                }
        } catch(Exception e){
                LOGGER.error("Error while format of error");
                result = "HEALTHCHECK:"+ ex.getMessage();
                e.printStackTrace();
        }
        return result;
    }

}
