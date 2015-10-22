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

package org.activiti.explorer.ui.task.data;

import java.util.Date;
import java.util.Map;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;

/**
 * TODO: This class is a hack, to quickly convert a {@link HistoricTaskInstance}
 * to a Task, so we can reuse the existing components for a task. Obviously,
 * this is not a good approach in the long run.
 * 
 * @author Joram Barrez
 */
public class HistoricTaskWrapper implements Task {

  protected String id;
  protected String name;
  protected String description;
  protected int priority;
  protected String owner;
  protected String assignee;
  protected Date dueDate;
  protected String category;
  protected String parentTaskId;
  protected String tenantId;
  protected String formKey;

  public HistoricTaskWrapper(HistoricTaskInstance historicTaskInstance) {
    this.id = historicTaskInstance.getId();
    setName(historicTaskInstance.getName());
    setDescription(historicTaskInstance.getDescription());
    setDueDate(historicTaskInstance.getDueDate());
    setPriority(historicTaskInstance.getPriority());
    setOwner(historicTaskInstance.getOwner());
    setAssignee(historicTaskInstance.getAssignee());
    setTenantId(historicTaskInstance.getTenantId());
    setFormKey(historicTaskInstance.getFormKey());
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getAssignee() {
    return assignee;
  }

  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }

  public DelegationState getDelegationState() {
    return null;
  }

  public void setDelegationState(DelegationState delegationState) {
  }

  public String getProcessInstanceId() {
    return null;
  }

  public String getExecutionId() {
    return null;
  }

  public String getProcessDefinitionId() {
    return null;
  }

  public Date getCreateTime() {
    return null;
  }

  public String getTaskDefinitionKey() {
    return null;
  }

  public Date getDueDate() {
    return dueDate;
  }

  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }
  
  public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void delegate(String userId) {
  }

  public void setParentTaskId(String parentTaskId) {
    this.parentTaskId = parentTaskId;
  }

  public String getParentTaskId() {
    return parentTaskId;
  }
  
  public boolean isSuspended() {
    return false;
  }
  
  public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	
	@Override
  public String getFormKey() {
		return formKey;
  }

	@Override
  public void setFormKey(String formKey) {
		this.formKey = formKey;
	}
	

	public Map<String, Object> getTaskLocalVariables() {
    return null;
  }

  public Map<String, Object> getProcessVariables() {
    return null;
  }

  @Override
  public void setLocalizedName(String name) {
    
  }

  @Override
  public void setLocalizedDescription(String description) {
    
  }

}
