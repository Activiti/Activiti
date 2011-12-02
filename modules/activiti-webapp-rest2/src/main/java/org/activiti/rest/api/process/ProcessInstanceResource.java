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

package org.activiti.rest.api.process;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.RequestUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
public class ProcessInstanceResource extends SecuredResource {
  
  @Get
  public ObjectNode getProcessInstances() {
    if(authenticate() == false) return null;
    
    String processInstanceId = (String) getRequest().getAttributes().get("processInstanceId");
    HistoricProcessInstance instance = ActivitiUtil.getHistoryService()
        .createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId)
        .singleResult();
    
    if(instance == null) {
      throw new ActivitiException("Process instance not found for id " + processInstanceId);
    }
    
    ObjectNode responseJSON = new ObjectMapper().createObjectNode();
    responseJSON.put("processInstanceId", instance.getId());
    responseJSON.put("businessKey", instance.getBusinessKey() != null ? instance.getBusinessKey() : "null");
    responseJSON.put("processDefinitionId", instance.getProcessDefinitionId());
    responseJSON.put("startTime", RequestUtil.dateToString(instance.getStartTime()));
    responseJSON.put("startActivityId", instance.getStartActivityId());
    responseJSON.put("startUserId", instance.getStartUserId() != null ? instance.getStartUserId() : "null");
    if(instance.getEndTime() == null) {
      responseJSON.put("completed", false);
    } else {
      responseJSON.put("completed", true);
      responseJSON.put("endTime", RequestUtil.dateToString(instance.getEndTime()));
      responseJSON.put("endActivityId", instance.getEndActivityId());
      responseJSON.put("duration", instance.getDurationInMillis());
    }
    
    addTaskList(processInstanceId, responseJSON);
    addActivityList(processInstanceId, responseJSON);
    addVariableList(processInstanceId, responseJSON);
    
    return responseJSON;
  }
  
  @Delete
  public ObjectNode deleteProcessInstance() {
    if(authenticate() == false) return null;
    
    String processInstanceId = (String) getRequest().getAttributes().get("processInstanceId");
    
    ActivitiUtil.getRuntimeService().deleteProcessInstance(processInstanceId, "REST API");
    
    ObjectNode successNode = new ObjectMapper().createObjectNode();
    successNode.put("success", true);
    return successNode;
  }
  
  private void addTaskList(String processInstanceId, ObjectNode responseJSON) {
    List<HistoricTaskInstance> taskList = ActivitiUtil.getHistoryService()
        .createHistoricTaskInstanceQuery()
        .processInstanceId(processInstanceId)
        .orderByHistoricActivityInstanceStartTime()
        .asc()
        .list();
    
    if(taskList != null && taskList.size() > 0) {
      ArrayNode tasksJSON = new ObjectMapper().createArrayNode();
      responseJSON.put("tasks", tasksJSON);
      for (HistoricTaskInstance historicTaskInstance : taskList) {
        ObjectNode taskJSON = new ObjectMapper().createObjectNode();
        taskJSON.put("taskId", historicTaskInstance.getId());
        taskJSON.put("taskName", historicTaskInstance.getName() != null ? historicTaskInstance.getName() : "null");
        taskJSON.put("owner", historicTaskInstance.getOwner() != null ? historicTaskInstance.getOwner() : "null");
        taskJSON.put("assignee", historicTaskInstance.getAssignee() != null ? historicTaskInstance.getAssignee() : "null");
        taskJSON.put("startTime", RequestUtil.dateToString(historicTaskInstance.getStartTime()));
        if(historicTaskInstance.getEndTime() == null) {
          taskJSON.put("completed", false);
        } else {
          taskJSON.put("completed", true);
          taskJSON.put("endTime", RequestUtil.dateToString(historicTaskInstance.getEndTime()));
          taskJSON.put("duration", historicTaskInstance.getDurationInMillis());
        }
        tasksJSON.add(taskJSON);
      }
    }
  }
  
  private void addActivityList(String processInstanceId, ObjectNode responseJSON) {
    List<HistoricActivityInstance> activityList = ActivitiUtil.getHistoryService()
        .createHistoricActivityInstanceQuery()
        .processInstanceId(processInstanceId)
        .orderByHistoricActivityInstanceStartTime()
        .asc()
        .list();
    
    if(activityList != null && activityList.size() > 0) {
      ArrayNode activitiesJSON = new ObjectMapper().createArrayNode();
      responseJSON.put("activities", activitiesJSON);
      for (HistoricActivityInstance historicActivityInstance : activityList) {
        ObjectNode activityJSON = new ObjectMapper().createObjectNode();
        activityJSON.put("activityId", historicActivityInstance.getActivityId());
        activityJSON.put("activityName", historicActivityInstance.getActivityName() != null ? historicActivityInstance.getActivityName() : "null");
        activityJSON.put("activityType", historicActivityInstance.getActivityType());
        activityJSON.put("startTime", RequestUtil.dateToString(historicActivityInstance.getStartTime()));
        if(historicActivityInstance.getEndTime() == null) {
          activityJSON.put("completed", false);
        } else {
          activityJSON.put("completed", true);
          activityJSON.put("endTime", RequestUtil.dateToString(historicActivityInstance.getEndTime()));
          activityJSON.put("duration", historicActivityInstance.getDurationInMillis());
        }
        activitiesJSON.add(activityJSON);
      }
    }
  }
  
  private void addVariableList(String processInstanceId, ObjectNode responseJSON) {
    List<HistoricDetail> variableList = ActivitiUtil.getHistoryService()
        .createHistoricDetailQuery()
        .processInstanceId(processInstanceId)
        .variableUpdates()
        .orderByTime()
        .desc()
        .list();
    
    if(variableList != null && variableList.size() > 0) {
      ArrayNode variablesJSON = new ObjectMapper().createArrayNode();
      responseJSON.put("variables", variablesJSON);
      for (HistoricDetail historicDetail : variableList) {
        HistoricVariableUpdate variableUpdate = (HistoricVariableUpdate) historicDetail;
        ObjectNode variableJSON = new ObjectMapper().createObjectNode();
        variableJSON.put("variableName", variableUpdate.getVariableName());
        variableJSON.put("variableValue", variableUpdate.getValue().toString());
        variableJSON.put("variableType", variableUpdate.getVariableTypeName());
        variableJSON.put("revision", variableUpdate.getRevision());
        variableJSON.put("time", RequestUtil.dateToString(variableUpdate.getTime()));
        
        variablesJSON.add(variableJSON);
      }
    }
  }
}
