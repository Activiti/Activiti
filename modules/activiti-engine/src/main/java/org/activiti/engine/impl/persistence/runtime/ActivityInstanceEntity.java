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

package org.activiti.engine.impl.persistence.runtime;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivityInstance;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.PersistentObject;
import org.activiti.engine.impl.persistence.repository.ProcessDefinitionEntity;
import org.activiti.pvm.impl.process.ActivityImpl;
import org.activiti.pvm.impl.runtime.ActivityInstanceImpl;
import org.activiti.pvm.impl.runtime.ScopeInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class ActivityInstanceEntity extends ActivityInstanceImpl implements ActivityInstance, PersistentObject {

  protected String id;
  protected int revision;
  protected String parentId;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String activityId;
  
  public ActivityInstanceEntity() {
  }

  ActivityInstanceEntity(ActivityImpl activity, ScopeInstanceImpl parent) {
    super(activity, parent);
  }
  
  public static ActivityInstanceEntity createAndInsert(ActivityImpl activity, ActivityInstanceEntity parent) {
    ActivityInstanceEntity activityInstance = new ActivityInstanceEntity(activity, parent);
    activityInstance.setProcessDefinitionId(parent.getProcessDefinitionId());
    activityInstance.setProcessInstanceId(parent.getProcessInstanceId());
    activityInstance.setActivityId(activity.getId());
    insert(activityInstance);
    return activityInstance;
  }

  public static ActivityInstanceEntity createAndInsert(ActivityImpl activity, ProcessInstanceEntity parent) {
    ActivityInstanceEntity activityInstance = new ActivityInstanceEntity(activity, parent);
    activityInstance.setProcessDefinitionId(parent.getProcessDefinitionId());
    activityInstance.setProcessInstanceId(parent.getId());
    activityInstance.setActivityId(activity.getId());
    insert(activityInstance);
    return activityInstance;
  }

  private static void insert(ActivityInstanceEntity activityInstance) {
    CommandContext
      .getCurrent()
      .getRuntimeSession()
      .insertActivityInstance(activityInstance);
  }
  
  @Override
  protected ActivityInstanceEntity createActivityInstance(ActivityImpl activity) {
    ActivityInstanceEntity activityInstance = createAndInsert(activity, this);
    activityInstances.add(activityInstance);
    return activityInstance;
  }

  @Override
  public void removeActivityInstance(ActivityInstanceImpl activityInstance) {
    super.removeActivityInstance(activityInstance);
    ((ActivityInstanceEntity)activityInstance).delete();
  }

  public void delete() {
    CommandContext
      .getCurrent()
      .getRuntimeSession()
      .deleteActivityInstance(id);
  }
  
  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("activityId", activityId);
    persistentState.put("isActive", isActive);
    return persistentState;
  }
  
  public int getRevisionNext() {
    return revision+1;
  }
  
  public ProcessDefinitionEntity getProcessDefinition() {
    if (processDefinitionId!=null && processDefinition==null) {
      processDefinition = CommandContext
        .getCurrent()
        .getRepositorySession()
        .findDeployedProcessDefinitionById(processDefinitionId);
    }
    return (ProcessDefinitionEntity) processDefinition;
  }
  
  public ActivityImpl getActivity() {
    if (activityId!=null && activity==null) {
      activity = getProcessDefinition().findActivity(activityId);
    }
    return activity;
  }
  
  public void setActivity(ActivityImpl activity) {
    this.activity = activity;
    if (activity!=null) {
      this.activityId = activity.getId();
    } else {
      this.activityId = null;
    }
  }
  
  public String toString() {
    return "ActivityInstanceEntity["+id+"]";
  }
  
  // getters and setters //////////////////////////////////////////////////////
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  public String getParentId() {
    return parentId;
  }
  
  public void setParentId(String parentId) {
    this.parentId = parentId;
  }
  
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
  
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }
  
  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public String getActivityId() {
    return activityId;
  }
  
  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }
}
