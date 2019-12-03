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

package org.activiti.engine.impl.test;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**


 */
public abstract class ResourceActivitiTestCase extends AbstractActivitiTestCase {
  
  private static final Logger logger = LoggerFactory.getLogger(ResourceActivitiTestCase.class);

  protected String activitiConfigurationResource;
  protected String processEngineName;

  public ResourceActivitiTestCase(String activitiConfigurationResource) {
    this(activitiConfigurationResource, null);
  }
  
  public ResourceActivitiTestCase(String activitiConfigurationResource, String processEngineName) {
    this.activitiConfigurationResource = activitiConfigurationResource;
    this.processEngineName = processEngineName;
  }

  @Override
  protected void closeDownProcessEngine() {
    super.closeDownProcessEngine();
    ProcessEngines.unregister(processEngine);
    processEngine = null;
  }

  @Override
  protected void initializeProcessEngine() {
    ProcessEngineConfiguration config = ProcessEngineConfiguration.createProcessEngineConfigurationFromResource(activitiConfigurationResource);
    if (processEngineName != null) {
      logger.info("Initializing process engine with name '" + processEngineName + "'");
      config.setProcessEngineName(processEngineName);
    }
    additionalConfiguration(config);
    processEngine = config.buildProcessEngine();
  }
  
  protected void additionalConfiguration(ProcessEngineConfiguration processEngineConfiguration) {
    
  }

}
