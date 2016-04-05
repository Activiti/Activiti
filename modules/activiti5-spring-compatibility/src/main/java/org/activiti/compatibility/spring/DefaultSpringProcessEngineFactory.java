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

package org.activiti.compatibility.spring;

import org.activiti.compatibility.DefaultProcessEngineFactory;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti5.engine.ProcessEngine;


public class DefaultSpringProcessEngineFactory extends DefaultProcessEngineFactory {

  /**
   * Takes in an Activiti 6 process engine config, gives back an Activiti 5 Process engine.
   */
  @Override
  public ProcessEngine buildProcessEngine(ProcessEngineConfigurationImpl activiti6Configuration) {

    org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5Configuration = null;
    if (activiti6Configuration instanceof SpringProcessEngineConfiguration) {
      activiti5Configuration = new org.activiti5.spring.SpringProcessEngineConfiguration();
      super.copyConfigItems(activiti6Configuration, activiti5Configuration);
      copySpringConfigItems((SpringProcessEngineConfiguration) activiti6Configuration, (org.activiti5.spring.SpringProcessEngineConfiguration) activiti5Configuration);
      return activiti5Configuration.buildProcessEngine();
    
    } else {
      return super.buildProcessEngine(activiti6Configuration);
    }
      
  }
  
  protected void copySpringConfigItems(SpringProcessEngineConfiguration activiti6Configuration, org.activiti5.spring.SpringProcessEngineConfiguration activiti5Configuration) {
    activiti5Configuration.setApplicationContext(activiti6Configuration.getApplicationContext());
    activiti5Configuration.setTransactionManager(activiti6Configuration.getTransactionManager());
  }
  
}
