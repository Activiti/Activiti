package org.activiti.spring.boot;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.spring.boot.actuate.endpoint.ProcessEngineEndpoint;
import org.activiti.spring.boot.actuate.endpoint.ProcessEngineMvcEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
@ConditionalOnClass (name = "org.springframework.boot.actuate.endpoint.AbstractEndpoint")
public class EndpointAutoConfiguration {

    @Bean
    public ProcessEngineEndpoint processEngineEndpoint(ProcessEngine engine) {
        return new ProcessEngineEndpoint(engine);
    }

    @Bean
    public ProcessEngineMvcEndpoint processEngineMvcEndpoint(
            ProcessEngineEndpoint engineEndpoint, RepositoryService repositoryService) {
        return new ProcessEngineMvcEndpoint(engineEndpoint, repositoryService);
    }
}
