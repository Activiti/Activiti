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
package org.activiti.form.engine.impl;

import org.activiti.form.api.FormRepositoryService;
import org.activiti.form.api.FormService;
import org.activiti.form.engine.FormEngine;
import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.FormEngines;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tijs Rademakers
 */
public class FormEngineImpl implements FormEngine {

  private static Logger log = LoggerFactory.getLogger(FormEngineImpl.class);

  protected String name;
  protected FormRepositoryService repositoryService;
  protected FormService formService;
  protected FormEngineConfiguration engineConfiguration;

  public FormEngineImpl(FormEngineConfiguration engineConfiguration) {
    this.engineConfiguration = engineConfiguration;
    this.name = engineConfiguration.getFormEngineName();
    this.repositoryService = engineConfiguration.getFormRepositoryService();
    this.formService = engineConfiguration.getFormService();

    if (name == null) {
      log.info("default activiti FormEngine created");
    } else {
      log.info("FormEngine {} created", name);
    }

    FormEngines.registerDmnEngine(this);
  }

  public void close() {
    FormEngines.unregister(this);
  }

  // getters and setters
  // //////////////////////////////////////////////////////

  public String getName() {
    return name;
  }

  public FormRepositoryService getFormRepositoryService() {
    return repositoryService;
  }

  public FormService getFormService() {
    return formService;
  }

  public FormEngineConfiguration getFormEngineConfiguration() {
    return engineConfiguration;
  }
}
