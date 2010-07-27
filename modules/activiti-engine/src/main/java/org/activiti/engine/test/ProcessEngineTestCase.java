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

package org.activiti.engine.test;

import junit.framework.TestCase;

import org.activiti.HistoricDataService;
import org.activiti.IdentityService;
import org.activiti.ManagementService;
import org.activiti.ProcessEngine;
import org.activiti.ProcessService;
import org.activiti.TaskService;
import org.junit.Rule;


/** JUnit 3 style base class that only exposes the public API services. 
 * 
 * @author Tom Baeyens
 */
public class ProcessEngineTestCase extends TestCase {

  @Rule 
  ProcessEngineRule processEngineRule;
  
  public ProcessEngineTestCase() {
    processEngineRule = new ProcessEngineRule();
  }
  
  public ProcessEngineTestCase(String configurationResource) {
    processEngineRule = new ProcessEngineRule(configurationResource);
  }
  
  public ProcessEngine getProcessEngine() {
    return processEngineRule.getProcessEngine();
  }

  public ProcessService getProcessService() {
    return processEngineRule.getProcessEngine().getProcessService();
  }

  public HistoricDataService getHistoricDataService() {
    return processEngineRule.getProcessEngine().getHistoricDataService();  
  }

  public IdentityService getIdentityService() {
    return processEngineRule.getProcessEngine().getIdentityService();
  }

  public TaskService getTaskService() {
    return processEngineRule.getProcessEngine().getTaskService();
  }

  public ManagementService getManagementService() {
    return processEngineRule.getProcessEngine().getManagementService();
  }
}
