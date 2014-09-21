package org.activiti.spring.boot.actuate.endpoint;

import org.activiti.engine.ProcessEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The idea behind this module is that Spring Security could
 * talk to the {@link org.activiti.engine.IdentityService}
 * as required.
 *
 * @author Josh Long
 */
@Configuration
public class EndpointAutoConfiguration {

    @Bean
    public ProcessEngineEndpoint processEngineMetric( ProcessEngine engine){
        return new ProcessEngineEndpoint( engine)  ;
    }

}
