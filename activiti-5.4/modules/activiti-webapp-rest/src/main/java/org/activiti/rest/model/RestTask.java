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

package org.activiti.rest.model;

import org.activiti.engine.impl.task.TaskEntity;


/**
 * Model used in REST-API, adding field formResourceKey to task.
 * 
 * @author Frederik Heremans
 */
public class RestTask extends TaskEntity {

  private static final long serialVersionUID = 1L;
  
  private String formResourceKey;
  
  public RestTask(TaskEntity task) {
    this.setCreateTime(task.getCreateTime());
    this.setDescription(task.getDescription());
    this.setId(task.getId());
    this.setName(task.getName());
    this.setPriority(task.getPriority());
    this.setProcessDefinitionId(task.getProcessDefinitionId());
    
    // Using field instead of setter to prevent errors about commandcontext
    assignee = task.getAssignee();
    executionId = task.getExecutionId();
    processInstanceId = task.getProcessInstanceId();
  }

  public String getFormResourceKey() {
    return formResourceKey;
  }
  
  public void setFormResourceKey(String formResourceKey) {
    this.formResourceKey = formResourceKey;
  }

}
