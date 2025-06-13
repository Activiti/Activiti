package org.activiti.spring.resolver;

import jakarta.el.ELResolver;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
public class ELResolverAutoConfiguration {

/*
    @ConditionalOnProperty(name = "spring.activiti.env-var-el-resolver.enabled", havingValue = "true", matchIfMissing = false)
*/
    @Bean
    public EnvironmentVariableELResolver environmentVariableELResolver() {
        return new EnvironmentVariableELResolver();
    }

    @Bean
    public ProcessEngineConfigurationConfigurer environmentVariablesELResolverConfigurer(List<ELResolver> customELResolvers) {
        return processEngineConfiguration -> {
            if(customELResolvers!=null) {
                customELResolvers.forEach(elResolver -> {
                    System.out.println("ELResolver added: " + elResolver.getClass().getName());
                });
                processEngineConfiguration.setCustomELResolvers(customELResolvers);
            }
        };
    }

}
