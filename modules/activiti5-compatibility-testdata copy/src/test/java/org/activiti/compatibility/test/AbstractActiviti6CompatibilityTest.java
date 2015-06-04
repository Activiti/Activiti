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
package org.activiti.compatibility.test;

import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Before;
import org.junit.Rule;

public abstract class AbstractActiviti6CompatibilityTest {
  
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected ManagementService managementService;
  
  @Rule
  public ActivitiRule activitiRule = new ActivitiRule();
  
  @Before
  public void setupServices() {
    this.repositoryService = activitiRule.getRepositoryService();
    this.runtimeService = activitiRule.getRuntimeService();
    this.taskService = activitiRule.getTaskService();
    this.managementService = activitiRule.getManagementService();
  }

}
