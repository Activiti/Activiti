package org.activiti.rest.conf;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "org.activiti.rest.conf.common", "org.activiti.rest.conf.engine"})
public class ApplicationConfiguration {
  
}
