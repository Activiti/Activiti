package org.activiti.spring.conformance.set1;

import org.activiti.api.process.model.IntegrationContext;
import org.activiti.api.process.runtime.connector.Connector;
import org.activiti.spring.conformance.util.RuntimeTestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Set1RuntimeTestConfiguration {

    private static boolean connector1Executed = false;

    private static boolean connector2Executed = false;

    private static IntegrationContext resultIntegrationContext = null;

    @Bean(name = "service-implementation")
    public Connector serviceImplementation() {
        return integrationContext -> {
            connector1Executed = true;
            
            resultIntegrationContext = integrationContext;
            
            return integrationContext;
        };
    }

    @Bean(name = "service-implementation-modify-data")
    public Connector serviceImplementationModifyData() {
        return integrationContext -> {
            connector2Executed = true;
            integrationContext.getOutBoundVariables().put("var1", integrationContext.getInBoundVariables().get("var1") + "-modified");
            return integrationContext;
        };
    }

    
    public static IntegrationContext getResultIntegrationContext() {
        return resultIntegrationContext;
    }

    
    public static void reset() {
        RuntimeTestConfiguration.collectedEvents.clear();

        resultIntegrationContext = null;
        connector1Executed = false;
        connector2Executed = false;
    }

    
    public static boolean isConnector1Executed() {
        return connector1Executed;
    }

    
    public static boolean isConnector2Executed() {
        return connector2Executed;
    }

}
