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


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public abstract class ResourceActivitiTestCase extends AbstractActivitiTestCase {
  
  protected String activitiConfigurationResource;
  
  public ResourceActivitiTestCase(String activitiConfigurationResource) {
    this.activitiConfigurationResource = activitiConfigurationResource;
  }
  
  @Override
  protected void closeDownProcessEngine() {
    super.closeDownProcessEngine();
    ProcessEngines.unregister(processEngine);
    processEngine = null;
  }

  @Override
  protected void initializeProcessEngine() {
    processEngine = ProcessEngineConfiguration
            .createProcessEngineConfigurationFromResource(activitiConfigurationResource)
            .buildProcessEngine();
  }

}
