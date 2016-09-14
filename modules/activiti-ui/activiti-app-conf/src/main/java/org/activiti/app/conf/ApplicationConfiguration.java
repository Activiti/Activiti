/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.app.conf;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySources({
	
	@PropertySource("classpath:/META-INF/activiti-app/activiti-app.properties"),
	@PropertySource(value = "classpath:activiti-app.properties", ignoreResourceNotFound = true),
	@PropertySource(value = "file:activiti-app.properties", ignoreResourceNotFound = true),

})
@ComponentScan(basePackages = {
        "org.activiti.app.conf",
        "org.activiti.app.repository",
        "org.activiti.app.service",
        "org.activiti.app.security",
        "org.activiti.app.model.component"})
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
