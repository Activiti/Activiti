package org.activiti.spring.boot;

import java.util.Map;

import org.activiti.runtime.api.connector.Connector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProcessRuntimeTestConfiguraiton {

    public static boolean processImageConnectorExecuted = false;

    public static boolean tagImageConnectorExecuted = false;

    public static boolean discardImageConnectorExecuted = false;



    @Bean
    public Connector processImageConnector() {
        return integrationContext -> {
            Map<String, Object> inBoundVariables = integrationContext.getInBoundVariables();
            System.out.println("My inbound variables keys: " + inBoundVariables.keySet());
            System.out.println("My inbound variables values: " + inBoundVariables.values());
            boolean expectedValue = (Boolean) inBoundVariables.get("expectedKey");

            integrationContext.addOutBoundVariable("approved",
                                                   expectedValue);
            processImageConnectorExecuted = true;
            return integrationContext;
        };
    }

    @Bean
    public Connector tagImageConnector() {
        return integrationContext -> {
            tagImageConnectorExecuted = true;
            return integrationContext;
        };
    }

    @Bean
    public Connector discardImageConnector() {
        return integrationContext -> {
            discardImageConnectorExecuted = true;
            return integrationContext;
        };
    }
}
