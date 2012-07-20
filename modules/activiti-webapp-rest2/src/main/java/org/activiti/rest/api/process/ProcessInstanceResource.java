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
import java.util.Map;

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
  public ObjectNode getProcessInstance() {
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
    if (instance.getBusinessKey() != null) {
      responseJSON.put("businessKey", instance.getBusinessKey());
    } else {
      responseJSON.putNull("businessKey");
    }
    responseJSON.put("processDefinitionId", instance.getProcessDefinitionId());
    responseJSON.put("startTime", RequestUtil.dateToString(instance.getStartTime()));
    responseJSON.put("startActivityId", instance.getStartActivityId());
    if (instance.getStartUserId() != null) {
      responseJSON.put("startUserId", instance.getStartUserId());
    } else {
      responseJSON.putNull("startUserId");
    }
    
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
        if (historicTaskInstance.getName() != null) {
          taskJSON.put("taskName", historicTaskInstance.getName());
        } else {
          taskJSON.putNull("taskName");
        }
        if (historicTaskInstance.getOwner() != null) {
          taskJSON.put("owner", historicTaskInstance.getOwner());
        } else {
          taskJSON.putNull("owner");
        }
        if (historicTaskInstance.getOwner() != null) {
          taskJSON.put("assignee", historicTaskInstance.getAssignee());
        } else {
          taskJSON.putNull("assignee");
        }
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
        if (historicActivityInstance.getActivityName() != null) {
          activityJSON.put("activityName", historicActivityInstance.getActivityName());
        } else {
          activityJSON.putNull("activityName");
        }
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
    
    try {
      Map<String, Object> variableMap = ActivitiUtil.getRuntimeService()
          .getVariables(processInstanceId);
      
      if(variableMap != null && variableMap.size() > 0) {
        ArrayNode variablesJSON = new ObjectMapper().createArrayNode();
        responseJSON.put("variables", variablesJSON);
        for (String key : variableMap.keySet()) {
          Object variableValue = variableMap.get(key);
          ObjectNode variableJSON = new ObjectMapper().createObjectNode();
          variableJSON.put("variableName", key);
          if (variableValue != null) {
            if (variableValue instanceof Boolean) {
              variableJSON.put("variableValue", (Boolean) variableValue);
            } else if (variableValue instanceof Long) {
              variableJSON.put("variableValue", (Long) variableValue);
            } else if (variableValue instanceof Double) {
              variableJSON.put("variableValue", (Double) variableValue);
            } else if (variableValue instanceof Float) {
              variableJSON.put("variableValue", (Float) variableValue);
            } else if (variableValue instanceof Integer) {
              variableJSON.put("variableValue", (Integer) variableValue);
            } else {
              variableJSON.put("variableValue", variableValue.toString());
            }
          } else {
            variableJSON.putNull("variableValue");
          }
          variablesJSON.add(variableJSON);
        }
      }
    } catch(Exception e) {
      // Absorb possible error that the execution could not be found
    }
    
    List<HistoricDetail> historyVariableList = ActivitiUtil.getHistoryService()
        .createHistoricDetailQuery()
        .processInstanceId(processInstanceId)
        .variableUpdates()
        .orderByTime()
        .desc()
        .list();
    
    if(historyVariableList != null && historyVariableList.size() > 0) {
      ArrayNode variablesJSON = new ObjectMapper().createArrayNode();
      responseJSON.put("historyVariables", variablesJSON);
      for (HistoricDetail historicDetail : historyVariableList) {
        HistoricVariableUpdate variableUpdate = (HistoricVariableUpdate) historicDetail;
        ObjectNode variableJSON = new ObjectMapper().createObjectNode();
        variableJSON.put("variableName", variableUpdate.getVariableName());
        if (variableUpdate.getValue() != null) {
          if (variableUpdate.getValue() instanceof Boolean) {
            variableJSON.put("variableValue", (Boolean) variableUpdate.getValue());
          } else if (variableUpdate.getValue() instanceof Long) {
            variableJSON.put("variableValue", (Long) variableUpdate.getValue());
          } else if (variableUpdate.getValue() instanceof Double) {
            variableJSON.put("variableValue", (Double) variableUpdate.getValue());
          } else if (variableUpdate.getValue() instanceof Float) {
            variableJSON.put("variableValue", (Float) variableUpdate.getValue());
          } else if (variableUpdate.getValue() instanceof Integer) {
            variableJSON.put("variableValue", (Integer) variableUpdate.getValue());
          } else {
            variableJSON.put("variableValue", variableUpdate.getValue().toString());
          }
        } else {
          variableJSON.putNull("variableValue");
        }
        variableJSON.put("variableType", variableUpdate.getVariableTypeName());
        variableJSON.put("revision", variableUpdate.getRevision());
        variableJSON.put("time", RequestUtil.dateToString(variableUpdate.getTime()));
        
        variablesJSON.add(variableJSON);
      }
    }
  }
}
