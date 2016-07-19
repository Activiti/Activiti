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
package com.activiti.conf;

import javax.sql.DataSource;

import org.activiti.form.engine.FormEngine;
import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.FormRepositoryService;
import org.activiti.form.engine.FormService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActivitiFormEngineConfiguration {

  private final Logger logger = LoggerFactory.getLogger(ActivitiFormEngineConfiguration.class);

  @Autowired
  private DataSource dataSource;

  @Bean(name = "activitiFormEngine")
  public FormEngine formEngine() {
    return formEngineConfiguration().buildFormEngine();
  }

  @Bean(name = "formEngineConfiguration")
  public FormEngineConfiguration formEngineConfiguration() {
    FormEngineConfiguration formEngineConfiguration = new FormEngineConfiguration();
    formEngineConfiguration.setDataSource(dataSource);
    formEngineConfiguration.setDatabaseSchemaUpdate(FormEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
    // formEngineConfiguration.setTransactionManager(transactionManager);

    return formEngineConfiguration;
  }

  @Bean
  public FormRepositoryService activitiFormRepositoryService() {
    return formEngine().getFormRepositoryService();
  }

  @Bean
  public FormService activitiFormService() {
    return formEngine().getFormService();
  }
}
