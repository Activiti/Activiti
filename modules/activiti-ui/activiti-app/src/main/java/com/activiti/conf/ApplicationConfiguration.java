/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.activiti.conf;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySources({
	
	@PropertySource("classpath:/META-INF/activiti-app/activiti-app.properties"),
	@PropertySource(value = "classpath:activiti-app.properties", ignoreResourceNotFound = true),
	@PropertySource(value = "file:activiti-app.properties", ignoreResourceNotFound = true),

})
@ComponentScan(basePackages = {
        "com.activiti.conf",
        "com.activiti.repository",
        "com.activiti.service",
        "com.activiti.security",
        "com.activiti.model.component"})
public class ApplicationConfiguration {
	
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
