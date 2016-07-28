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
package org.activiti.rest.dmn.conf.engine;

import org.activiti.dmn.api.DmnRepositoryService;
import org.activiti.dmn.api.DmnRuleService;
import org.activiti.dmn.engine.DmnEngine;
import org.activiti.dmn.engine.DmnEngineConfiguration;
import org.activiti.dmn.engine.impl.cfg.StandaloneInMemDmnEngineConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Yvo Swillens
 */
@Configuration
public class ActivitiDmnEngineConfiguration {

  @Bean
  public DmnEngine ruleEngine() {
    DmnEngineConfiguration dmnEngineConfiguration = new StandaloneInMemDmnEngineConfiguration();
    return dmnEngineConfiguration.buildDmnEngine();
  }

  @Bean
  public DmnRepositoryService dmnRepositoryService() {
    return ruleEngine().getDmnRepositoryService();
  }

  @Bean
  public DmnRuleService dmnRuleService() {
    return ruleEngine().getDmnRuleService();
  }
}
