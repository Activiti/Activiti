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
package org.activiti.kickstart.service;

import org.activiti.engine.ProcessEngines;

/**
 * 
 * Yes, i know Service Locator is considered a 'bad design' nowadays. But given
 * the limited services we currently need, it's thrown away time to introduce DI
 * for it...
 * 
 * @author Joram Barrez
 */
public class ServiceLocator {

  protected static KickstartService ahodcWorkflowServiceInstance;

  public static KickstartService getAdhocWorkflowService() {
    if (ahodcWorkflowServiceInstance == null) {
      synchronized (ServiceLocator.class) {
        if (ahodcWorkflowServiceInstance == null) {
          ahodcWorkflowServiceInstance = new KickstartServiceImpl(ProcessEngines.getDefaultProcessEngine());
        }
      }
    }
    return ahodcWorkflowServiceInstance;
  }

}
