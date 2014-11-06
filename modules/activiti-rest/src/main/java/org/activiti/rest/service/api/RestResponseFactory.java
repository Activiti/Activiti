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

package org.activiti.rest.service.api;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.form.FormData;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.bpmn.deployer.BpmnDeployer;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.rest.service.api.engine.AttachmentResponse;
import org.activiti.rest.service.api.engine.CommentResponse;
import org.activiti.rest.service.api.engine.EventResponse;
import org.activiti.rest.service.api.engine.RestIdentityLink;
import org.activiti.rest.service.api.engine.variable.BooleanRestVariableConverter;
import org.activiti.rest.service.api.engine.variable.DateRestVariableConverter;
import org.activiti.rest.service.api.engine.variable.DoubleRestVariableConverter;
import org.activiti.rest.service.api.engine.variable.IntegerRestVariableConverter;
import org.activiti.rest.service.api.engine.variable.LongRestVariableConverter;
import org.activiti.rest.service.api.engine.variable.QueryVariable;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.activiti.rest.service.api.engine.variable.RestVariable.RestVariableScope;
import org.activiti.rest.service.api.engine.variable.RestVariableConverter;
import org.activiti.rest.service.api.engine.variable.ShortRestVariableConverter;
import org.activiti.rest.service.api.engine.variable.StringRestVariableConverter;
import org.activiti.rest.service.api.form.FormDataResponse;
import org.activiti.rest.service.api.form.RestEnumFormProperty;
import org.activiti.rest.service.api.form.RestFormProperty;
import org.activiti.rest.service.api.history.HistoricActivityInstanceResponse;
import org.activiti.rest.service.api.history.HistoricDetailResponse;
import org.activiti.rest.service.api.history.HistoricIdentityLinkResponse;
import org.activiti.rest.service.api.history.HistoricProcessInstanceResponse;
import org.activiti.rest.service.api.history.HistoricTaskInstanceResponse;
import org.activiti.rest.service.api.history.HistoricVariableInstanceResponse;
import org.activiti.rest.service.api.identity.GroupResponse;
import org.activiti.rest.service.api.identity.MembershipResponse;
import org.activiti.rest.service.api.identity.UserInfoResponse;
import org.activiti.rest.service.api.identity.UserResponse;
import org.activiti.rest.service.api.management.JobResponse;
import org.activiti.rest.service.api.management.TableResponse;
import org.activiti.rest.service.api.repository.DeploymentResourceResponse;
import org.activiti.rest.service.api.repository.DeploymentResponse;
import org.activiti.rest.service.api.repository.ModelResponse;
import org.activiti.rest.service.api.repository.ProcessDefinitionResponse;
import org.activiti.rest.service.api.runtime.process.ExecutionResponse;
import org.activiti.rest.service.api.runtime.process.ProcessInstanceResponse;
import org.activiti.rest.service.api.runtime.task.TaskResponse;
import org.apache.commons.lang3.StringUtils;


/**
 * Default implementation of a {@link RestResponseFactory}.
 * 
 * Added a new "createProcessInstanceResponse" method (with a different signature) to conditionally
 *   return the process variables that exist within the process instance when the first wait state 
 *   is encountered (or when the process instance completes). Also added the population of a
 *   "completed" flag - within both the original "createProcessInstanceResponse" method and
 *   the new one with the different signature - to let the caller know whether the process
 *   instance has completed or not.
 * 
 * @author Frederik Heremans
 * @author Ryan Johnston (@rjfsu)
 */
public class RestResponseFactory {

  public static final int VARIABLE_TASK = 1;
  public static final int VARIABLE_EXECUTION = 2;
  public static final int VARIABLE_PROCESS = 3;
  public static final int VARIABLE_HISTORY_TASK = 4;
  public static final int VARIABLE_HISTORY_PROCESS = 5;
  public static final int VARIABLE_HISTORY_VARINSTANCE = 6;
  public static final int VARIABLE_HISTORY_DETAIL = 7;
  
  public static final String BYTE_ARRAY_VARIABLE_TYPE = "binary";
  public static final String SERIALIZABLE_VARIABLE_TYPE = "serializable";
  
  protected List<RestVariableConverter> variableConverters = new ArrayList<RestVariableConverter>();
  
  public RestResponseFactory() {
    initializeVariableConverters();
  }
  
  public TaskResponse createTaskResponse(Task task, String serverRootUrl) {
    TaskResponse response = new TaskResponse(task);
    response.setUrl(formatUrl(serverRootUrl, RestUrls.URL_TASK, task.getId()));

    // Add references to other resources, if needed
    if (response.getParentTaskId() != null) {
      response.setParentTaskUrl(formatUrl(serverRootUrl, RestUrls.URL_TASK, response.getParentTaskId()));
    }
    if (response.getProcessDefinitionId() != null) {
      response.setProcessDefinitionUrl(formatUrl(serverRootUrl, RestUrls.URL_PROCESS_DEFINITION, response.getProcessDefinitionId()));
    }
    if (response.getExecutionId() != null) {
      response.setExecutionUrl(formatUrl(serverRootUrl, RestUrls.URL_EXECUTION, response.getExecutionId()));
    }
    if (response.getProcessInstanceId() != null) {
      response.setProcessInstanceUrl(formatUrl(serverRootUrl, RestUrls.URL_PROCESS_INSTANCE, response.getProcessInstanceId()));
    }
    
    if (task.getProcessVariables() != null) {
      Map<String, Object> variableMap = task.getProcessVariables();
      for (String name : variableMap.keySet()) {
        response.addVariable(createRestVariable(name, variableMap.get(name), 
            RestVariableScope.GLOBAL, task.getId(), VARIABLE_TASK, false, serverRootUrl));
      }
    }
    if (task.getTaskLocalVariables() != null) {
      Map<String, Object> variableMap = task.getTaskLocalVariables();
      for (String name : variableMap.keySet()) {
        response.addVariable(createRestVariable(name, variableMap.get(name), 
            RestVariableScope.LOCAL, task.getId(), VARIABLE_TASK, false, serverRootUrl));
      }
    }
    
    return response;
  }
  
  public DeploymentResponse createDeploymentResponse(Deployment deployment, String serverRootUrl) {
    return new DeploymentResponse(deployment, formatUrl(serverRootUrl, RestUrls.URL_DEPLOYMENT, deployment.getId()));
  }
  
  public DeploymentResourceResponse createDeploymentResourceResponse(String deploymentId, String resourceId, String contentType, String serverRootUrl) {
    // Create URL's
    String resourceUrl = formatUrl(serverRootUrl, RestUrls.URL_DEPLOYMENT_RESOURCE, deploymentId, resourceId);
    String resourceContentUrl = formatUrl(serverRootUrl, RestUrls.URL_DEPLOYMENT_RESOURCE_CONTENT, deploymentId, resourceId);
    
    // Determine type
    String type = "resource";
    for (String suffix : BpmnDeployer.BPMN_RESOURCE_SUFFIXES) {
      if (resourceId.endsWith(suffix)) {
        type = "processDefinition";
        break;
      }
    }
    return new DeploymentResourceResponse(resourceId, resourceUrl, resourceContentUrl, contentType, type);
  }
  
  public ProcessDefinitionResponse createProcessDefinitionResponse(ProcessDefinition processDefinition, boolean graphicalNotationDefined, String serverRootUrl) {
    ProcessDefinitionResponse response = new ProcessDefinitionResponse();
    response.setUrl(formatUrl(serverRootUrl, RestUrls.URL_PROCESS_DEFINITION, processDefinition.getId()));
    response.setId(processDefinition.getId());
    response.setKey(processDefinition.getKey());
    response.setVersion(processDefinition.getVersion());
    response.setCategory(processDefinition.getCategory());
    response.setName(processDefinition.getName());
    response.setDescription(processDefinition.getDescription());
    response.setSuspended(processDefinition.isSuspended());
    response.setStartFormDefined(processDefinition.hasStartFormKey());
    response.setGraphicalNotationDefined(graphicalNotationDefined);
    response.setTenantId(processDefinition.getTenantId());
    
    // Links to other resources
    response.setDeploymentId(processDefinition.getDeploymentId());
    response.setDeploymentUrl(formatUrl(serverRootUrl, RestUrls.URL_DEPLOYMENT, processDefinition.getDeploymentId()));
    response.setResource(formatUrl(serverRootUrl, RestUrls.URL_DEPLOYMENT_RESOURCE, processDefinition.getDeploymentId(), processDefinition.getResourceName()));
    if(processDefinition.getDiagramResourceName() != null) {
      response.setDiagramResource(formatUrl(serverRootUrl, RestUrls.URL_DEPLOYMENT_RESOURCE,
              processDefinition.getDeploymentId(), processDefinition.getDiagramResourceName()));
    }
    return response;
  }
  
  public List<RestVariable> createRestVariables(Map<String, Object> variables, String id, int variableType, RestVariableScope scope, String serverRootUrl) {
    List<RestVariable> result = new ArrayList<RestVariable>();
   
    for (Entry<String, Object> pair : variables.entrySet()) {
      result.add(createRestVariable(pair.getKey(), pair.getValue(), scope, id, variableType, false, serverRootUrl));
    }
   
    return result;
  }
  
  public RestVariable createRestVariable(String name, Object value, RestVariableScope scope, 
      String id, int variableType, boolean includeBinaryValue, String serverRootUrl) {
    
    RestVariableConverter converter = null;
    RestVariable restVar = new RestVariable();
    restVar.setVariableScope(scope);
    restVar.setName(name);
    
    if (value != null) {
      // Try converting the value
      for (RestVariableConverter c : variableConverters) {
        if (c.getVariableType().isAssignableFrom(value.getClass())) {
          converter = c;
          break;
        }
      }
      
      if (converter != null) {
        converter.convertVariableValue(value, restVar);
        restVar.setType(converter.getRestTypeName());
      } else {
        // Revert to default conversion, which is the serializable/byte-array form
        if (value instanceof Byte[] || value instanceof byte[]) {
          restVar.setType(BYTE_ARRAY_VARIABLE_TYPE);
        } else {
          restVar.setType(SERIALIZABLE_VARIABLE_TYPE);
        }
        
        if (includeBinaryValue) {
          restVar.setValue(value);
        }
        
        if (variableType == VARIABLE_TASK) {
          restVar.setValueUrl(formatUrl(serverRootUrl, RestUrls.URL_TASK_VARIABLE_DATA, id, name));
        } else if (variableType == VARIABLE_EXECUTION) {
          restVar.setValueUrl(formatUrl(serverRootUrl, RestUrls.URL_EXECUTION_VARIABLE_DATA, id, name));
        } else if (variableType == VARIABLE_PROCESS) {
          restVar.setValueUrl(formatUrl(serverRootUrl, RestUrls.URL_PROCESS_INSTANCE_VARIABLE_DATA, id, name));
        } else if (variableType == VARIABLE_HISTORY_TASK) {
          restVar.setValueUrl(formatUrl(serverRootUrl, RestUrls.URL_HISTORIC_TASK_INSTANCE_VARIABLE_DATA, id, name));
        } else if (variableType == VARIABLE_HISTORY_PROCESS) {
          restVar.setValueUrl(formatUrl(serverRootUrl, RestUrls.URL_HISTORIC_PROCESS_INSTANCE_VARIABLE_DATA, id, name));
        } else if (variableType == VARIABLE_HISTORY_VARINSTANCE) {
          restVar.setValueUrl(formatUrl(serverRootUrl, RestUrls.URL_HISTORIC_VARIABLE_INSTANCE_DATA, id));
        } else if (variableType == VARIABLE_HISTORY_DETAIL) {
          restVar.setValueUrl(formatUrl(serverRootUrl, RestUrls.URL_HISTORIC_DETAIL_VARIABLE_DATA, id));
        }
      }
    }
    return restVar;
  }
  
  public RestVariable createBinaryRestVariable(String name, RestVariableScope scope, String type, String taskId, 
      String executionId, String processInstanceId, String serverRootUrl) {
    
    RestVariable restVar = new RestVariable();
    restVar.setVariableScope(scope);
    restVar.setName(name);
    restVar.setType(type);
    
    if (taskId != null) {
      restVar.setValueUrl(formatUrl(serverRootUrl, RestUrls.URL_TASK_VARIABLE_DATA, taskId, name));
    }
    if (executionId != null) {
      restVar.setValueUrl(formatUrl(serverRootUrl, RestUrls.URL_EXECUTION_VARIABLE_DATA, executionId, name));
    }
    if (processInstanceId != null) {
      restVar.setValueUrl(formatUrl(serverRootUrl, RestUrls.URL_PROCESS_INSTANCE_VARIABLE_DATA, processInstanceId, name));
    }
    
    return restVar;
  }
  
  public Object getVariableValue(RestVariable restVariable) {
    Object value = null;
    
    if(restVariable.getType() != null) {
      // Try locating a converter if the type has been specified
      RestVariableConverter converter = null;
      for(RestVariableConverter conv : variableConverters) {
        if(conv.getRestTypeName().equals(restVariable.getType())) {
          converter = conv;
          break;
        }
      }
      if(converter == null) {
        throw new ActivitiIllegalArgumentException("Variable '" + restVariable.getName() + "' has unsupported type: '" + restVariable.getType() + "'.");
      }
      value = converter.getVariableValue(restVariable);
      
    } else {
      // Revert to type determined by REST-to-Java mapping when no explicit type has been provided
      value = restVariable.getValue();
    }
    return value;
  }
  
  public Object getVariableValue(QueryVariable restVariable) {
    Object value = null;
    
    if(restVariable.getType() != null) {
      // Try locating a converter if the type has been specified
      RestVariableConverter converter = null;
      for(RestVariableConverter conv : variableConverters) {
        if(conv.getRestTypeName().equals(restVariable.getType())) {
          converter = conv;
          break;
        }
      }
      if(converter == null) {
        throw new ActivitiIllegalArgumentException("Variable '" + restVariable.getName() + "' has unsupported type: '" + restVariable.getType() + "'.");
      }
      
      RestVariable temp = new RestVariable();
      temp.setValue(restVariable.getValue());
      temp.setType(restVariable.getType());
      temp.setName(restVariable.getName());
      value = converter.getVariableValue(temp);
      
    } else {
      // Revert to type determined by REST-to-Java mapping when no explicit type has been provided
      value = restVariable.getValue();
    }
    return value;
  }
  
  public RestIdentityLink createRestIdentityLink(IdentityLink link, String serverRootUrl) {
    return createRestIdentityLink(link.getType(), link.getUserId(), link.getGroupId(), link.getTaskId(), 
        link.getProcessDefinitionId(), link.getProcessInstanceId(), serverRootUrl);
  }
  
  public RestIdentityLink createRestIdentityLink(String type, String userId, String groupId, String taskId, String processDefinitionId, String processInstanceId, String serverRootUrl) {
    RestIdentityLink result = new RestIdentityLink();
    result.setUser(userId);
    result.setGroup(groupId);
    result.setType(type);
    
    String family = null;
    if (userId != null) {
      family = RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS;
    } else {
      family = RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_GROUPS;
    }
    if (processDefinitionId != null) {
      result.setUrl(formatUrl(serverRootUrl, RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, processDefinitionId, family, (userId != null ? userId : groupId)));
    } else if(taskId != null){
      result.setUrl(formatUrl(serverRootUrl, RestUrls.URL_TASK_IDENTITYLINK, taskId, family, (userId != null ? userId : groupId), type));
    } else {
      result.setUrl(formatUrl(serverRootUrl, RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINK, processInstanceId, (userId != null ? userId : groupId), type));
    }
    return result;
  }
  
  public CommentResponse createRestComment(Comment comment, String serverRootUrl) {
    CommentResponse result = new CommentResponse();
    result.setAuthor(comment.getUserId());
    result.setMessage(comment.getFullMessage());
    result.setId(comment.getId());
    result.setTime(comment.getTime());
    result.setTaskId(comment.getTaskId());
    result.setProcessInstanceId(comment.getProcessInstanceId());
    
    if (comment.getTaskId() != null) {
      result.setTaskUrl(formatUrl(serverRootUrl, RestUrls.URL_TASK_COMMENT, comment.getTaskId(), comment.getId()));
    }
    
    if (comment.getProcessInstanceId() != null) {
      result.setProcessInstanceUrl(formatUrl(serverRootUrl, RestUrls.URL_HISTORIC_PROCESS_INSTANCE_COMMENT, comment.getProcessInstanceId(), comment.getId()));
    }
    
    return result;
  }
  
  public EventResponse createEventResponse(Event event, String serverRootUrl) {
    EventResponse result = new EventResponse();
    result.setAction(event.getAction());
    result.setId(event.getId());
    result.setMessage(event.getMessageParts());
    result.setTime(event.getTime());
    result.setUserId(event.getUserId());
    
    result.setUrl(formatUrl(serverRootUrl, RestUrls.URL_TASK_EVENT, event.getTaskId(), event.getId()));
    result.setTaskUrl(formatUrl(serverRootUrl, RestUrls.URL_TASK, event.getTaskId()));
    
    if(event.getProcessInstanceId() != null) {
      result.setTaskUrl(formatUrl(serverRootUrl, RestUrls.URL_PROCESS_INSTANCE, event.getProcessInstanceId()));
    }
    return result ;
  }
  
  public AttachmentResponse createAttachmentResponse(Attachment attachment, String serverRootUrl) {
    AttachmentResponse result = new AttachmentResponse();
    result.setId(attachment.getId());
    result.setName(attachment.getName());
    result.setDescription(attachment.getDescription());
    result.setType(attachment.getType());
    result.setUserId(attachment.getUserId());
    
    if (attachment.getUrl() == null && attachment.getTaskId() != null) {
      // Attachment content can be streamed
      result.setContentUrl(formatUrl(serverRootUrl, RestUrls.URL_TASK_ATTACHMENT_DATA, attachment.getTaskId(), attachment.getId()));
    } else {
      result.setExternalUrl(attachment.getUrl());
    }
    
    if (attachment.getTaskId() != null) {
      result.setUrl(formatUrl(serverRootUrl, RestUrls.URL_TASK_ATTACHMENT, attachment.getTaskId(), attachment.getId()));
      result.setTaskUrl(formatUrl(serverRootUrl, RestUrls.URL_TASK, attachment.getTaskId()));
    }
    if (attachment.getProcessInstanceId() != null) {
      result.setProcessInstanceUrl(formatUrl(serverRootUrl, RestUrls.URL_PROCESS_INSTANCE, attachment.getProcessInstanceId()));
    }
    return result ;
  }
  
  public ProcessInstanceResponse createProcessInstanceResponse(ProcessInstance processInstance, String serverRootUrl) {
    ProcessInstanceResponse result = new ProcessInstanceResponse();
    result.setActivityId(processInstance.getActivityId());
    result.setBusinessKey(processInstance.getBusinessKey());
    result.setId(processInstance.getId());
    result.setProcessDefinitionId(processInstance.getProcessDefinitionId());
    result.setProcessDefinitionUrl(formatUrl(serverRootUrl, RestUrls.URL_PROCESS_DEFINITION, processInstance.getProcessDefinitionId()));
    result.setEnded(processInstance.isEnded());
    result.setSuspended(processInstance.isSuspended());
    result.setUrl(formatUrl(serverRootUrl, RestUrls.URL_PROCESS_INSTANCE, processInstance.getId()));
    result.setTenantId(processInstance.getTenantId());
    
    //Added by Ryan Johnston
    if (processInstance.isEnded()) {
      //Process complete. Note the same in the result.
      result.setCompleted(true);
    } else {
    	//Process not complete. Note the same in the result.
    	result.setCompleted(false);
    }
    //End Added by Ryan Johnston
    
    if (processInstance.getProcessVariables() != null) {
      Map<String, Object> variableMap = processInstance.getProcessVariables();
      for (String name : variableMap.keySet()) {
        result.addVariable(createRestVariable(name, variableMap.get(name), 
            RestVariableScope.LOCAL, processInstance.getId(), VARIABLE_PROCESS, false, serverRootUrl));
      }
    }
    
    return result;
  }
  
  public ProcessInstanceResponse createProcessInstanceResponse(ProcessInstance processInstance, boolean returnVariables, 
      Map<String, Object> runtimeVariableMap, List<HistoricVariableInstance> historicVariableList, String serverRootUrl) {
    
    ProcessInstanceResponse result = new ProcessInstanceResponse();
    result.setActivityId(processInstance.getActivityId());
    result.setBusinessKey(processInstance.getBusinessKey());
    result.setId(processInstance.getId());
    result.setProcessDefinitionId(processInstance.getProcessDefinitionId());
    result.setProcessDefinitionUrl(formatUrl(serverRootUrl, RestUrls.URL_PROCESS_DEFINITION, processInstance.getProcessDefinitionId()));
    result.setEnded(processInstance.isEnded());
    result.setSuspended(processInstance.isSuspended());
    result.setUrl(formatUrl(serverRootUrl, RestUrls.URL_PROCESS_INSTANCE, processInstance.getId()));
    result.setTenantId(processInstance.getTenantId());
    
    //Added by Ryan Johnston
    if (processInstance.isEnded()) {
      //Process complete. Note the same in the result.
      result.setCompleted(true);
    } else {
    	//Process not complete. Note the same in the result.
    	result.setCompleted(false);
    }
    
    if (returnVariables) {
    	
    	if (processInstance.isEnded()) {
    	  if (historicVariableList != null) {
      		for (HistoricVariableInstance historicVariable : historicVariableList) {
      		  result.addVariable(createRestVariable(historicVariable.getVariableName(), historicVariable.getValue(), 
      		      RestVariableScope.LOCAL, processInstance.getId(), VARIABLE_PROCESS, false, serverRootUrl));
      		}
    	  }
    		
    	} else {
    	  if (runtimeVariableMap != null) {
      		for (String name : runtimeVariableMap.keySet()) {
      			result.addVariable(createRestVariable(name, runtimeVariableMap.get(name), 
      			    RestVariableScope.LOCAL, processInstance.getId(), VARIABLE_PROCESS, false, serverRootUrl));
          }
    	  }
    	}
    }
    //End Added by Ryan Johnston
    
    return result;
  }
  
  
  public ExecutionResponse createExecutionResponse(Execution execution, String serverRootUrl) {
    ExecutionResponse result = new ExecutionResponse();
    result.setActivityId(execution.getActivityId());
    result.setId(execution.getId());
    result.setUrl(formatUrl(serverRootUrl, RestUrls.URL_EXECUTION, execution.getId()));
    result.setSuspended(execution.isSuspended());
    result.setTenantId(execution.getTenantId());
    
    result.setParentId(execution.getParentId());
    if(execution.getParentId() != null) {
      result.setParentUrl(formatUrl(serverRootUrl, RestUrls.URL_EXECUTION, execution.getParentId()));
    }
    
    result.setProcessInstanceId(execution.getProcessInstanceId());
    if(execution.getProcessInstanceId() != null) {
      result.setProcessInstanceUrl(formatUrl(serverRootUrl, RestUrls.URL_PROCESS_INSTANCE, execution.getProcessInstanceId()));
    }
    return result;
  }
  
  public FormDataResponse createFormDataResponse(FormData formData, String serverRootUrl) {
    FormDataResponse result = new FormDataResponse();
    result.setDeploymentId(formData.getDeploymentId());
    result.setFormKey(formData.getFormKey());
    if (formData.getFormProperties() != null) {
      for (FormProperty formProp : formData.getFormProperties()) {
        RestFormProperty restFormProp = new RestFormProperty();
        restFormProp.setId(formProp.getId());
        restFormProp.setName(formProp.getName());
        if (formProp.getType() != null) {
          restFormProp.setType(formProp.getType().getName());
        }
        restFormProp.setValue(formProp.getValue());
        restFormProp.setReadable(formProp.isReadable());
        restFormProp.setRequired(formProp.isRequired());
        restFormProp.setWritable(formProp.isWritable());
        if ("enum".equals(restFormProp.getType())) {
          Object values = formProp.getType().getInformation("values");
          if (values != null) {
            @SuppressWarnings("unchecked")
            Map<String, String> enumValues = (Map<String, String>) values;
            for (String enumId : enumValues.keySet()) {
              RestEnumFormProperty enumProperty = new RestEnumFormProperty();
              enumProperty.setId(enumId);
              enumProperty.setName(enumValues.get(enumId));
              restFormProp.addEnumValue(enumProperty);
            }
          }
        } else if ("date".equals(restFormProp.getType())) {
          restFormProp.setDatePattern((String) formProp.getType().getInformation("datePattern"));
        }
        result.addFormProperty(restFormProp);
      }
    }
    if (formData instanceof StartFormData) {
      StartFormData startFormData = (StartFormData) formData;
      if (startFormData.getProcessDefinition() != null) {
        result.setProcessDefinitionId(startFormData.getProcessDefinition().getId());
        result.setProcessDefinitionUrl(formatUrl(serverRootUrl, RestUrls.URL_PROCESS_DEFINITION, startFormData.getProcessDefinition().getId()));
      }
    } else if (formData instanceof TaskFormData) {
      TaskFormData taskFormData = (TaskFormData) formData;
      if (taskFormData.getTask() != null) {
        result.setTaskId(taskFormData.getTask().getId());
        result.setTaskUrl(formatUrl(serverRootUrl, RestUrls.URL_TASK, taskFormData.getTask().getId()));
      }
    }
    return result;
  }
  
  @SuppressWarnings("deprecation")
  public HistoricProcessInstanceResponse createHistoricProcessInstanceResponse(HistoricProcessInstance processInstance, String serverRootUrl) {
    HistoricProcessInstanceResponse result = new HistoricProcessInstanceResponse();
    result.setBusinessKey(processInstance.getBusinessKey());
    result.setDeleteReason(processInstance.getDeleteReason());
    result.setDurationInMillis(processInstance.getDurationInMillis());
    result.setEndActivityId(processInstance.getEndActivityId());
    result.setEndTime(processInstance.getEndTime());
    result.setId(processInstance.getId());
    result.setProcessDefinitionId(processInstance.getProcessDefinitionId());
    result.setProcessDefinitionUrl(formatUrl(serverRootUrl, RestUrls.URL_PROCESS_DEFINITION, processInstance.getProcessDefinitionId()));
    result.setStartActivityId(processInstance.getStartActivityId());
    result.setStartTime(processInstance.getStartTime());
    result.setStartUserId(processInstance.getStartUserId());
    result.setSuperProcessInstanceId(processInstance.getSuperProcessInstanceId());
    result.setUrl(formatUrl(serverRootUrl, RestUrls.URL_HISTORIC_PROCESS_INSTANCE, processInstance.getId()));
    if (processInstance.getProcessVariables() != null) {
      Map<String, Object> variableMap = processInstance.getProcessVariables();
      for (String name : variableMap.keySet()) {
        result.addVariable(createRestVariable(name, variableMap.get(name), 
            RestVariableScope.LOCAL, processInstance.getId(), VARIABLE_HISTORY_PROCESS, false, serverRootUrl));
      }
    }
    result.setTenantId(processInstance.getTenantId());
    return result;
  }
  
  public HistoricTaskInstanceResponse createHistoricTaskInstanceResponse(HistoricTaskInstance taskInstance, String serverRootUrl) {
    HistoricTaskInstanceResponse result = new HistoricTaskInstanceResponse();
    result.setAssignee(taskInstance.getAssignee());
    result.setClaimTime(taskInstance.getClaimTime());
    result.setDeleteReason(taskInstance.getDeleteReason());
    result.setDescription(taskInstance.getDescription());
    result.setDueDate(taskInstance.getDueDate());
    result.setDurationInMillis(taskInstance.getDurationInMillis());
    result.setEndTime(taskInstance.getEndTime());
    result.setExecutionId(taskInstance.getExecutionId());
    result.setFormKey(taskInstance.getFormKey());
    result.setId(taskInstance.getId());
    result.setName(taskInstance.getName());
    result.setOwner(taskInstance.getOwner());
    result.setParentTaskId(taskInstance.getParentTaskId());
    result.setPriority(taskInstance.getPriority());
    result.setProcessDefinitionId(taskInstance.getProcessDefinitionId());
    result.setTenantId(taskInstance.getTenantId());
    result.setCategory(taskInstance.getCategory());
    if (taskInstance.getProcessDefinitionId() != null) {
      result.setProcessDefinitionUrl(formatUrl(serverRootUrl, RestUrls.URL_PROCESS_DEFINITION, taskInstance.getProcessDefinitionId()));
    }
    result.setProcessInstanceId(taskInstance.getProcessInstanceId());
    if (taskInstance.getProcessInstanceId() != null) {
      result.setProcessInstanceUrl(formatUrl(serverRootUrl, RestUrls.URL_HISTORIC_PROCESS_INSTANCE, taskInstance.getProcessInstanceId()));
    }
    result.setStartTime(taskInstance.getStartTime());
    result.setTaskDefinitionKey(taskInstance.getTaskDefinitionKey());
    result.setWorkTimeInMillis(taskInstance.getWorkTimeInMillis());
    result.setUrl(formatUrl(serverRootUrl, RestUrls.URL_HISTORIC_TASK_INSTANCE, taskInstance.getId()));
    if (taskInstance.getProcessVariables() != null) {
      Map<String, Object> variableMap = taskInstance.getProcessVariables();
      for (String name : variableMap.keySet()) {
        result.addVariable(createRestVariable(name, variableMap.get(name), 
            RestVariableScope.GLOBAL, taskInstance.getId(), VARIABLE_HISTORY_TASK, false, serverRootUrl));
      }
    }
    if (taskInstance.getTaskLocalVariables() != null) {
      Map<String, Object> variableMap = taskInstance.getTaskLocalVariables();
      for (String name : variableMap.keySet()) {
        result.addVariable(createRestVariable(name, variableMap.get(name), 
            RestVariableScope.LOCAL, taskInstance.getId(), VARIABLE_HISTORY_TASK, false, serverRootUrl));
      }
    }
    return result;
  }
  
  public HistoricActivityInstanceResponse createHistoricActivityInstanceResponse(HistoricActivityInstance activityInstance, String serverRootUrl) {
    HistoricActivityInstanceResponse result = new HistoricActivityInstanceResponse();
    result.setActivityId(activityInstance.getActivityId());
    result.setActivityName(activityInstance.getActivityName());
    result.setActivityType(activityInstance.getActivityType());
    result.setAssignee(activityInstance.getAssignee());
    result.setCalledProcessInstanceId(activityInstance.getCalledProcessInstanceId());
    result.setDurationInMillis(activityInstance.getDurationInMillis());
    result.setEndTime(activityInstance.getEndTime());
    result.setExecutionId(activityInstance.getExecutionId());
    result.setId(activityInstance.getId());
    result.setProcessDefinitionId(activityInstance.getProcessDefinitionId());
    result.setProcessDefinitionUrl(formatUrl(serverRootUrl, RestUrls.URL_PROCESS_DEFINITION, activityInstance.getProcessDefinitionId()));
    result.setProcessInstanceId(activityInstance.getProcessInstanceId());
    result.setProcessInstanceUrl(formatUrl(serverRootUrl, RestUrls.URL_HISTORIC_PROCESS_INSTANCE, activityInstance.getId()));
    result.setStartTime(activityInstance.getStartTime());
    result.setTaskId(activityInstance.getTaskId());
    result.setTenantId(activityInstance.getTenantId());
    return result;
  }
  
  public HistoricVariableInstanceResponse createHistoricVariableInstanceResponse(HistoricVariableInstance variableInstance, String serverRootUrl) {
    HistoricVariableInstanceResponse result = new HistoricVariableInstanceResponse();
    result.setId(variableInstance.getId());
    result.setProcessInstanceId(variableInstance.getProcessInstanceId());
    if (variableInstance.getProcessInstanceId() != null) {
      result.setProcessInstanceUrl(formatUrl(serverRootUrl, RestUrls.URL_HISTORIC_PROCESS_INSTANCE, variableInstance.getProcessInstanceId()));
    }
    result.setTaskId(variableInstance.getTaskId());
    result.setVariable(createRestVariable(variableInstance.getVariableName(), variableInstance.getValue(), 
        null, variableInstance.getId(), VARIABLE_HISTORY_VARINSTANCE, false, serverRootUrl));
    return result;
  }
  
  public HistoricDetailResponse createHistoricDetailResponse(HistoricDetail detail, String serverRootUrl) {
    HistoricDetailResponse result = new HistoricDetailResponse();
    result.setId(detail.getId());
    result.setProcessInstanceId(detail.getProcessInstanceId());
    if (StringUtils.isNotEmpty(detail.getProcessInstanceId())) {
      result.setProcessInstanceUrl(formatUrl(serverRootUrl, RestUrls.URL_HISTORIC_PROCESS_INSTANCE, detail.getProcessInstanceId()));
    }
    result.setExecutionId(detail.getExecutionId());
    result.setActivityInstanceId(detail.getActivityInstanceId());
    result.setTaskId(detail.getTaskId());
    if (StringUtils.isNotEmpty(detail.getTaskId())) {
      result.setTaskUrl(formatUrl(serverRootUrl, RestUrls.URL_HISTORIC_TASK_INSTANCE, detail.getTaskId()));
    }
    result.setTime(detail.getTime());
    if (detail instanceof HistoricFormProperty) {
      HistoricFormProperty formProperty = (HistoricFormProperty) detail;
      result.setDetailType(HistoricDetailResponse.FORM_PROPERTY);
      result.setPropertyId(formProperty.getPropertyId());
      result.setPropertyValue(formProperty.getPropertyValue());
    } else if (detail instanceof HistoricVariableUpdate) {
      HistoricVariableUpdate variableUpdate = (HistoricVariableUpdate) detail;
      result.setDetailType(HistoricDetailResponse.VARIABLE_UPDATE);
      result.setRevision(variableUpdate.getRevision());
      result.setVariable(createRestVariable(variableUpdate.getVariableName(), variableUpdate.getValue(), 
          null, detail.getId(), VARIABLE_HISTORY_DETAIL, false, serverRootUrl));
    }
    return result;
  }
  
  public HistoricIdentityLinkResponse createHistoricIdentityLinkResponse(HistoricIdentityLink identityLink, String serverRootUrl) {
    HistoricIdentityLinkResponse result = new HistoricIdentityLinkResponse();
    result.setType(identityLink.getType());
    result.setUserId(identityLink.getUserId());
    result.setGroupId(identityLink.getGroupId());
    result.setTaskId(identityLink.getTaskId());
    if (StringUtils.isNotEmpty(identityLink.getTaskId())) {
      result.setTaskUrl(formatUrl(serverRootUrl, RestUrls.URL_HISTORIC_TASK_INSTANCE, identityLink.getTaskId()));
    }
    result.setProcessInstanceId(identityLink.getProcessInstanceId());
    if (StringUtils.isNotEmpty(identityLink.getProcessInstanceId())) {
      result.setProcessInstanceUrl(formatUrl(serverRootUrl, RestUrls.URL_HISTORIC_PROCESS_INSTANCE, identityLink.getProcessInstanceId()));
    }
    return result;
  }
  
  public TableResponse createTableResponse(String name, Long count, String serverRootUrl) {
    TableResponse result = new TableResponse();
    result.setName(name);
    result.setCount(count);
    result.setUrl(formatUrl(serverRootUrl, RestUrls.URL_TABLE, name));
    return result;
  }
  
  public JobResponse createJobResponse(Job job, String serverRootUrl) {
    JobResponse response = new JobResponse();
    response.setId(job.getId());
    response.setDueDate(job.getDuedate());
    response.setExceptionMessage(job.getExceptionMessage());
    response.setExecutionId(job.getExecutionId());
    response.setProcessDefinitionId(job.getProcessDefinitionId());
    response.setProcessInstanceId(job.getProcessInstanceId());
    response.setRetries(job.getRetries());
    response.setTenantId(job.getTenantId());
    
    response.setUrl(formatUrl(serverRootUrl, RestUrls.URL_JOB, job.getId()));
    
    if (job.getProcessDefinitionId() != null) {
      response.setProcessDefinitionUrl(formatUrl(serverRootUrl, RestUrls.URL_PROCESS_DEFINITION, job.getProcessDefinitionId()));
    }
    
    if (job.getProcessInstanceId() != null) {
      response.setProcessInstanceUrl(formatUrl(serverRootUrl, RestUrls.URL_PROCESS_INSTANCE, job.getProcessInstanceId()));
    }
    
    if (job.getExecutionId() != null) {
      response.setExecutionUrl(formatUrl(serverRootUrl, RestUrls.URL_EXECUTION, job.getExecutionId()));
    }
    
    return response;
  }
  
  public UserResponse createUserResponse(User user, boolean incudePassword, String serverRootUrl) {
    UserResponse response = new UserResponse();
    response.setFirstName(user.getFirstName());
    response.setLastName(user.getLastName());
    response.setId(user.getId());
    response.setEmail(user.getEmail());
    response.setUrl(formatUrl(serverRootUrl, RestUrls.URL_USER, user.getId()));
    
    if(incudePassword) {
      response.setPassword(user.getPassword());
    }
    
    if(user.isPictureSet()){
      response.setPictureUrl(formatUrl(serverRootUrl, RestUrls.URL_USER_PICTURE, user.getId()));
    }
    return response;
  }
  
  public UserInfoResponse createUserInfoResponse(String key, String value, String userId, String serverRootUrl) {
    UserInfoResponse response = new UserInfoResponse();
    response.setKey(key);
    response.setValue(value);
    response.setUrl(formatUrl(serverRootUrl, RestUrls.URL_USER_INFO, userId, key));
    return response;
  }
  
  public GroupResponse createGroupResponse(Group group, String serverRootUrl) {
    GroupResponse response = new GroupResponse();
    response.setId(group.getId());
    response.setName(group.getName());
    response.setType(group.getType());
    response.setUrl(formatUrl(serverRootUrl, RestUrls.URL_GROUP, group.getId()));
    
    return response;
  }
  
  public MembershipResponse createMembershipResponse(String userId, String groupId, String serverRootUrl) {
    MembershipResponse response = new MembershipResponse();
    response.setGroupId(groupId);
    response.setUserId(userId);
    response.setUrl(formatUrl(serverRootUrl, RestUrls.URL_GROUP_MEMBERSHIP, groupId, userId));
    return response;
  }
  
  public ModelResponse createModelResponse(Model model, String serverRootUrl) {
    ModelResponse response = new ModelResponse();
    
    response.setCategory(model.getCategory());
    response.setCreateTime(model.getCreateTime());
    response.setId(model.getId());
    response.setKey(model.getKey());
    response.setLastUpdateTime(model.getLastUpdateTime());
    response.setMetaInfo(model.getMetaInfo());
    response.setName(model.getName());
    response.setDeploymentId(model.getDeploymentId());
    response.setVersion(model.getVersion());
    response.setTenantId(model.getTenantId());
    
    response.setUrl(formatUrl(serverRootUrl, RestUrls.URL_MODEL, model.getId()));
    if(model.getDeploymentId() != null) {
      response.setDeploymentUrl(formatUrl(serverRootUrl, RestUrls.URL_DEPLOYMENT, model.getDeploymentId()));
    }
    
    if(model.hasEditorSource()) {
      response.setSourceUrl(formatUrl(serverRootUrl, RestUrls.URL_MODEL_SOURCE, model.getId()));
    }
    
    if(model.hasEditorSourceExtra()) {
      response.setSourceExtraUrl(formatUrl(serverRootUrl, RestUrls.URL_MODEL_SOURCE_EXTRA, model.getId()));
    }
    
    return response;
  }
  
  /**
   * @return list of {@link RestVariableConverter} which are used by this factory. Additional
   * converters can be added and existing ones replaced ore removed.
   */
  public List<RestVariableConverter> getVariableConverters() {
	  return variableConverters;
  }
  
  /**
   * Called once when the converters need to be initialized. Override of custom conversion
   * needs to be done between java and rest.
   */
  protected void initializeVariableConverters() {
    variableConverters.add(new StringRestVariableConverter());
    variableConverters.add(new IntegerRestVariableConverter());
    variableConverters.add(new LongRestVariableConverter());
    variableConverters.add(new ShortRestVariableConverter());
    variableConverters.add(new DoubleRestVariableConverter());
    variableConverters.add(new BooleanRestVariableConverter());
    variableConverters.add(new DateRestVariableConverter());
  }
  
  protected String formatUrl(String serverRootUrl, String[] fragments, Object ... arguments) {
    StringBuilder urlBuilder = new StringBuilder(serverRootUrl);
    for(String urlFragment : fragments) {
      urlBuilder.append("/");
      urlBuilder.append(MessageFormat.format(urlFragment, arguments));
    }
    return urlBuilder.toString();
  }

}