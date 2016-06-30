/**
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package org.activiti.test;

import com.activiti.conf.*;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySources({
    @PropertySource(value = "classpath:/META-INF/activiti-admin/TEST-db.properties"),
	@PropertySource(value = "classpath:/META-INF/activiti-admin/TEST-activiti-admin.properties"),
})
@ComponentScan(basePackages = {
		"com.activiti.license",
        "com.activiti.service",
        "com.activiti.security"})
@Import(value = {
        SecurityConfiguration.class,
        AsyncConfiguration.class,
        DatabaseConfiguration.class,
        JacksonConfiguration.class})
public class ApplicationTestConfiguration {

    /**
     * This is needed to make property resolving work on annotations ...
     * (see http://stackoverflow.com/questions/11925952/custom-spring-property-source-does-not-resolve-placeholders-in-value)
     *
     * @Scheduled(cron="${someProperty}")
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
