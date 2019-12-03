package org.activiti.spring.conformance.set5;

import org.activiti.api.process.runtime.connector.Connector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Set5RuntimeTestConfiguration {

    @Bean(name = "connector-a")
    public Connector connectorA() {
        return integrationContext -> {

            return integrationContext;
        };
    }

    @Bean(name = "connector-b")
    public Connector connectorB() {
        return integrationContext -> {

            return integrationContext;
        };
    }


}
