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

import java.util.Date;
import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;

/**
 * Wraps an Activiti 5 process instance to an Activiti 6 {@link ProcessInstance}.
 * 
 * @author Joram Barrez
 */
public class Activiti5ProcessInstanceWrapper implements ProcessInstance {

  private org.activiti5.engine.runtime.ProcessInstance activiti5ProcessInstance;
  
  public Activiti5ProcessInstanceWrapper(org.activiti5.engine.runtime.ProcessInstance activiti5ProcessInstance) {
    this.activiti5ProcessInstance = activiti5ProcessInstance;
  }

  @Override
  public String getId() {
    return activiti5ProcessInstance.getId();
  }

  @Override
  public boolean isEnded() {
    return activiti5ProcessInstance.isEnded();
  }

  @Override
  public String getActivityId() {
    return activiti5ProcessInstance.getActivityId();
  }

  @Override
  public String getProcessInstanceId() {
    return activiti5ProcessInstance.getProcessInstanceId();
  }
  
  @Override
  public String getRootProcessInstanceId() {
    return null;
  }

  @Override
  public String getParentId() {
    return activiti5ProcessInstance.getParentId();
  }
  
  @Override
  public String getSuperExecutionId() {
    return activiti5ProcessInstance.getSuperExecutionId();
  }

  @Override
  public String getProcessDefinitionId() {
    return activiti5ProcessInstance.getProcessDefinitionId();
  }

  @Override
  public String getProcessDefinitionName() {
    return activiti5ProcessInstance.getProcessDefinitionName();
  }

  @Override
  public String getProcessDefinitionKey() {
    return activiti5ProcessInstance.getProcessDefinitionKey();
  }

  @Override
  public Integer getProcessDefinitionVersion() {
    return activiti5ProcessInstance.getProcessDefinitionVersion();
  }

  @Override
  public String getDeploymentId() {
    return activiti5ProcessInstance.getDeploymentId();
  }

  @Override
  public String getBusinessKey() {
    return activiti5ProcessInstance.getBusinessKey();
  }

  @Override
  public boolean isSuspended() {
    return activiti5ProcessInstance.isSuspended();
  }

  @Override
  public Map<String, Object> getProcessVariables() {
    return activiti5ProcessInstance.getProcessVariables();
  }

  @Override
  public String getTenantId() {
    return activiti5ProcessInstance.getTenantId();
  }

  @Override
  public String getName() {
    return activiti5ProcessInstance.getName();
  }
  
  @Override
  public String getDescription() {
    return activiti5ProcessInstance.getDescription();
  }

  @Override
  public String getLocalizedName() {
    return activiti5ProcessInstance.getLocalizedName();
  }

  @Override
  public String getLocalizedDescription() {
    return activiti5ProcessInstance.getLocalizedDescription();
  }

  public org.activiti5.engine.runtime.ProcessInstance getRawObject() {
    return activiti5ProcessInstance;
  }

  @Override
  public Date getStartTime() {
    return null;
  }

  @Override
  public String getStartUserId() {
    return null;
  }

}
