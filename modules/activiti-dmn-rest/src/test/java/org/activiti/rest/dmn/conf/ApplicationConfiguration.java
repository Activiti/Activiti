package org.activiti.rest.dmn.conf;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = { "org.activiti.rest.dmn.conf.common", "org.activiti.rest.dmn.conf.engine" })
public class ApplicationConfiguration {

}
