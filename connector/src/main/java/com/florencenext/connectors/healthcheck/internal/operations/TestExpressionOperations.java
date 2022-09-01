package com.florencenext.connectors.healthcheck.internal.operations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.florencenext.connectors.healthcheck.api.model.entities.Healthcheck;
import com.florencenext.connectors.healthcheck.internal.providers.ExtensionErrorProviders;
import org.mule.runtime.api.el.BindingContext;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.display.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

import static org.mule.runtime.api.metadata.MediaType.JSON;

public class TestExpressionOperations {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestExpressionOperations.class);

//    @Parameter
//    @Optional
//    @DisplayName("Name to say hello")
//    private String name;

//    @Parameter
//    @Optional
//    @DisplayName("Db to say hello")
//    @OfValues(DbReferencesProvider.class)
//    private String externalConfigName;

    @Inject
    ExpressionManager expressionManager;

    /***jms healthcheck for external systems****/
    @MediaType(value = "application/java", strict = false)
    @Alias("TestExpression")
    @Throws(ExtensionErrorProviders.class)
    public Healthcheck testExpression(
            InputStream inputData,
            @Text String baseExpression
    ) throws IOException, ClassNotFoundException {
        TypedValue output = null;
        DataType dataType = DataType.builder()
                .type(InputStream.class)
                .mediaType(JSON)
                .build();
        BindingContext bindingContext = BindingContext.builder().addBinding(
                "payload",
                new TypedValue(inputData, dataType)
        ).build();
        try {
            output = expressionManager.evaluate(baseExpression,bindingContext);
        } catch (RuntimeException e ) {
            e.printStackTrace();
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(output.getValue().toString());
        Healthcheck serviceHc = mapper.convertValue(output.getValue(), Healthcheck.class);
        return serviceHc;
    }

}
