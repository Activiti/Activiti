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
package com.activiti.domain.generator;

import com.activiti.service.activiti.ServerConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;

/**
 * Generates the minimal data needed when the application is booted with no data at all.
 *
 * @author jbarrez
 */
public class MinimalDataGenerator implements ApplicationListener<ContextRefreshedEvent> {

    private final Logger log = LoggerFactory.getLogger(MinimalDataGenerator.class);

	@Autowired
	protected Environment environment;

    @Autowired
    protected ServerConfigService serverConfigService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) { // Using Spring MVC, there are multiple child contexts. We only care about the root
            log.info("Verifying if minimal data is present");

            if (serverConfigService.findAll().size() == 0) {
                log.info("No server configuration found, creating default server configuration");
                serverConfigService.createDefaultServerConfig();
            }
        }
    }

}
