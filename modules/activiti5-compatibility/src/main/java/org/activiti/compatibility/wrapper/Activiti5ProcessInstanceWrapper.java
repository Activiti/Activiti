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

package org.activiti.compatibility.wrapper;

import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;

/**
 * Wraps an Activiti 5 process instance to an Activiti 6 {@link ProcessInstance}.
 * 
 * @author Joram Barrez
 */
public class Activiti5ProcessInstanceWrapper implements ProcessInstance {

  private org.activiti5.engine.runtime.ProcessInstance activit5ProcessInstance;
  
  public Activiti5ProcessInstanceWrapper(org.activiti5.engine.runtime.ProcessInstance activit5ProcessInstance) {
    this.activit5ProcessInstance = activit5ProcessInstance;
  }

  @Override
  public String getId() {
    return activit5ProcessInstance.getId();
  }

  @Override
  public boolean isEnded() {
    return activit5ProcessInstance.isEnded();
  }

  @Override
  public String getActivityId() {
    return activit5ProcessInstance.getActivityId();
  }

  @Override
  public String getProcessInstanceId() {
    return activit5ProcessInstance.getProcessInstanceId();
  }

  @Override
  public String getParentId() {
    return activit5ProcessInstance.getParentId();
  }

  @Override
  public String getProcessDefinitionId() {
    return activit5ProcessInstance.getProcessDefinitionId();
  }

  @Override
  public String getProcessDefinitionName() {
    return activit5ProcessInstance.getProcessDefinitionName();
  }

  @Override
  public String getProcessDefinitionKey() {
    return activit5ProcessInstance.getProcessDefinitionKey();
  }

  @Override
  public Integer getProcessDefinitionVersion() {
    return activit5ProcessInstance.getProcessDefinitionVersion();
  }

  @Override
  public String getDeploymentId() {
    return activit5ProcessInstance.getDeploymentId();
  }

  @Override
  public String getBusinessKey() {
    return activit5ProcessInstance.getBusinessKey();
  }

  @Override
  public boolean isSuspended() {
    return activit5ProcessInstance.isSuspended();
  }

  @Override
  public Map<String, Object> getProcessVariables() {
    return activit5ProcessInstance.getProcessVariables();
  }

  @Override
  public String getTenantId() {
    return activit5ProcessInstance.getTenantId();
  }

  @Override
  public String getName() {
    return activit5ProcessInstance.getName();
  }
  
  public org.activiti5.engine.runtime.ProcessInstance getRawObject() {
    return activit5ProcessInstance;
  }

}
