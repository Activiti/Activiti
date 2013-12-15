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
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
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
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
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
import org.activiti.rest.service.api.engine.variable.RestVariableConverter;
import org.activiti.rest.service.api.engine.variable.ShortRestVariableConverter;
import org.activiti.rest.service.api.engine.variable.StringRestVariableConverter;
import org.activiti.rest.service.api.engine.variable.RestVariable.RestVariableScope;
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
import org.activiti.rest.service.api.repository.DeploymentResourceResponse.DeploymentResourceType;
import org.activiti.rest.service.api.runtime.process.ExecutionResponse;
import org.activiti.rest.service.api.runtime.process.ProcessInstanceResponse;
import org.activiti.rest.service.api.runtime.task.TaskResponse;
import org.apache.commons.lang3.StringUtils;
import org.restlet.data.MediaType;


/**
 * Default implementation of a {@link RestResponseFactory}.
 * 
 * @author Frederik Heremans
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
  
  public TaskResponse createTaskResponse(SecuredResource securedResource, Task task) {
    TaskResponse response = new TaskResponse(task);
    response.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_TASK, task.getId()));

    // Add references to other resources, if needed
    if (response.getParentTaskId() != null) {
      response.setParentTaskUrl(securedResource.createFullResourceUrl(RestUrls.URL_TASK, response.getParentTaskId()));
    }
    if (response.getProcessDefinitionId() != null) {
      response.setProcessDefinitionUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_DEFINITION, response.getProcessDefinitionId()));
    }
    if (response.getExecutionId() != null) {
      response.setExecutionUrl(securedResource.createFullResourceUrl(RestUrls.URL_EXECUTION, response.getExecutionId()));
    }
    if (response.getProcessInstanceId() != null) {
      response.setProcessInstanceUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_INSTANCE, response.getProcessInstanceId()));
    }
    
    if (task.getProcessVariables() != null) {
      Map<String, Object> variableMap = task.getProcessVariables();
      for (String name : variableMap.keySet()) {
        response.addVariable(createRestVariable(securedResource, name, variableMap.get(name), 
            RestVariableScope.GLOBAL, task.getId(), VARIABLE_TASK, false));
      }
    }
    if (task.getTaskLocalVariables() != null) {
      Map<String, Object> variableMap = task.getTaskLocalVariables();
      for (String name : variableMap.keySet()) {
        response.addVariable(createRestVariable(securedResource, name, variableMap.get(name), 
            RestVariableScope.LOCAL, task.getId(), VARIABLE_TASK, false));
      }
    }
    
    return response;
  }
  
  public DeploymentResponse createDeploymentResponse(SecuredResource resourceContext, Deployment deployment) {
    return new DeploymentResponse(deployment, resourceContext.createFullResourceUrl(RestUrls.URL_DEPLOYMENT, deployment.getId()));
  }
  
  public DeploymentResourceResponse createDeploymentResourceResponse(SecuredResource resourceContext, String deploymentId, String resourceId) {
    // Create URL's
    String resourceUrl = resourceContext.createFullResourceUrl(RestUrls.URL_DEPLOYMENT_RESOURCE, deploymentId, resourceId);
    String resourceContentUrl = resourceContext.createFullResourceUrl(RestUrls.URL_DEPLOYMENT_RESOURCE_CONTENT, deploymentId, resourceId);
    
    // Fetch media-type
    MediaType mediaType = resourceContext.resolveMediaType(resourceId);
    String mediaTypeString = (mediaType != null) ? mediaType.toString() : null;
    
    // Determine type
    DeploymentResourceType type = DeploymentResourceType.RESOURCE;
    for(String suffix : BpmnDeployer.BPMN_RESOURCE_SUFFIXES) {
      if(resourceId.endsWith(suffix)) {
        type = DeploymentResourceType.PROCESS_DEFINITION;
        break;
      }
    }
    return new DeploymentResourceResponse(resourceId, resourceUrl, resourceContentUrl, mediaTypeString, type);
  }
  
  public ProcessDefinitionResponse createProcessDefinitionResponse(SecuredResource resourceContext, ProcessDefinition processDefinition) {
    ProcessDefinitionResponse response = new ProcessDefinitionResponse();
    response.setUrl(resourceContext.createFullResourceUrl(RestUrls.URL_PROCESS_DEFINITION, processDefinition.getId()));
    response.setId(processDefinition.getId());
    response.setKey(processDefinition.getKey());
    response.setVersion(processDefinition.getVersion());
    response.setCategory(processDefinition.getCategory());
    response.setName(processDefinition.getName());
    response.setDescription(processDefinition.getDescription());
    response.setSuspended(processDefinition.isSuspended());
    response.setStartFormDefined(processDefinition.hasStartFormKey());
    
    // Check if graphical notation defined
    // This method does an additional check to see if the process-definition exists which causes an additional query on top
    // of the one we already did to retrieve the processdefinition in the first place.
    ProcessDefinition deployedDefinition = ActivitiUtil.getRepositoryService().getProcessDefinition(processDefinition.getId());
    response.setGraphicalNotationDefined(((ProcessDefinitionEntity) deployedDefinition).isGraphicalNotationDefined());
    
    // Links to other resources
    response.setDeploymentId(processDefinition.getDeploymentId());
    response.setDeploymentUrl(resourceContext.createFullResourceUrl(RestUrls.URL_DEPLOYMENT, processDefinition.getDeploymentId()));
    response.setResource(resourceContext.createFullResourceUrl(RestUrls.URL_DEPLOYMENT_RESOURCE, processDefinition.getDeploymentId(), processDefinition.getResourceName()));
    if(processDefinition.getDiagramResourceName() != null) {
      response.setDiagramResource(resourceContext.createFullResourceUrl(RestUrls.URL_DEPLOYMENT_RESOURCE,
              processDefinition.getDeploymentId(), processDefinition.getDiagramResourceName()));
    }
    return response;
  }
  
  public List<RestVariable> createRestVariables(SecuredResource securedResource, Map<String, Object> variables, String id, int variableType, RestVariableScope scope) {
   List<RestVariable> result = new ArrayList<RestVariable>();
   
   for(Entry<String, Object> pair : variables.entrySet()) {
     result.add(createRestVariable(securedResource, pair.getKey(), pair.getValue(), scope, id, variableType, false));
   }
   
   return result;
  }
  
  public RestVariable createRestVariable(SecuredResource securedResource, String name, Object value, RestVariableScope scope, 
      String id, int variableType, boolean includeBinaryValue) {
    
    RestVariableConverter converter = null;
    RestVariable restVar = new RestVariable();
    restVar.setVariableScope(scope);
    restVar.setName(name);
    
    if (value != null) {
      // Try converting the value
      for (RestVariableConverter c : variableConverters) {
        if (value.getClass().isAssignableFrom(c.getVariableType())) {
          converter = c;
          break;
        }
      }
      
      if (converter != null) {
        converter.convertVariableValue(value, restVar);
        restVar.setType(converter.getRestTypeName());
      } else {
        // Revert to default conversion, which is the serializable/byte-array form
        if(value instanceof Byte[] || value instanceof byte[]) {
          restVar.setType(BYTE_ARRAY_VARIABLE_TYPE);
        } else {
          restVar.setType(SERIALIZABLE_VARIABLE_TYPE);
        }
        
        if (includeBinaryValue) {
          restVar.setValue(value);
        }
        
        if (variableType == VARIABLE_TASK) {
          restVar.setValueUrl(securedResource.createFullResourceUrl(RestUrls.URL_TASK_VARIABLE_DATA, id, name));
        } else if (variableType == VARIABLE_EXECUTION) {
          restVar.setValueUrl(securedResource.createFullResourceUrl(RestUrls.URL_EXECUTION_VARIABLE_DATA, id, name));
        } else if (variableType == VARIABLE_PROCESS) {
          restVar.setValueUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_DATA, id, name));
        } else if (variableType == VARIABLE_HISTORY_TASK) {
          restVar.setValueUrl(securedResource.createFullResourceUrl(RestUrls.URL_HISTORIC_TASK_INSTANCE_VARIABLE_DATA, id, name));
        } else if (variableType == VARIABLE_HISTORY_PROCESS) {
          restVar.setValueUrl(securedResource.createFullResourceUrl(RestUrls.URL_HISTORIC_PROCESS_INSTANCE_VARIABLE_DATA, id, name));
        } else if (variableType == VARIABLE_HISTORY_VARINSTANCE) {
          restVar.setValueUrl(securedResource.createFullResourceUrl(RestUrls.URL_HISTORIC_VARIABLE_INSTANCE_DATA, id));
        } else if (variableType == VARIABLE_HISTORY_DETAIL) {
          restVar.setValueUrl(securedResource.createFullResourceUrl(RestUrls.URL_HISTORIC_DETAIL_VARIABLE_DATA, id));
        }
      }
    }
    return restVar;
  }
  
  public RestVariable createBinaryRestVariable(SecuredResource securedResource, String name, RestVariableScope scope, String type, String taskId, String executionId, String processInstanceId) {
    RestVariable restVar = new RestVariable();
    restVar.setVariableScope(scope);
    restVar.setName(name);
    restVar.setType(type);
    
    if(taskId != null) {
      restVar.setValueUrl(securedResource.createFullResourceUrl(RestUrls.URL_TASK_VARIABLE_DATA, taskId, name));
    }
    if(executionId != null) {
      restVar.setValueUrl(securedResource.createFullResourceUrl(RestUrls.URL_EXECUTION_VARIABLE_DATA, executionId, name));
    }
    if(processInstanceId != null) {
      restVar.setValueUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_DATA, processInstanceId, name));
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
  
  public RestIdentityLink createRestIdentityLink(SecuredResource securedResource, IdentityLink link) {
    return createRestIdentityLink(securedResource, link.getType(), link.getUserId(), link.getGroupId(), link.getTaskId(), link.getProcessDefinitionId(), link.getProcessInstanceId());
  }
  
  public RestIdentityLink createRestIdentityLink(SecuredResource securedResource, String type, String userId, String groupId, String taskId, String processDefinitionId, String processInstanceId) {
    RestIdentityLink result = new RestIdentityLink();
    result.setUser(userId);
    result.setGroup(groupId);
    result.setType(type);
    
    String family = null;
    if(userId != null) {
      family = RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS;
    } else {
      family = RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_GROUPS;
    }
    if(processDefinitionId != null) {
      result.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, processDefinitionId, family, (userId != null ? userId : groupId)));
    } else if(taskId != null){
      result.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_TASK_IDENTITYLINK, taskId, family, (userId != null ? userId : groupId), type));
    } else {
      result.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINK, processInstanceId, (userId != null ? userId : groupId), type));
    }
    return result;
  }
  
  public CommentResponse createRestComment(SecuredResource securedResource, Comment comment) {
    return createCommentResponse(securedResource, comment.getTaskId(), comment.getProcessInstanceId(), comment.getUserId(), comment.getFullMessage(), comment.getId());
  }
  
  public CommentResponse createCommentResponse(SecuredResource securedResource, String taskId, String processInstanceId, String author,
          String message, String commentId) {
    CommentResponse result = new CommentResponse();
    result.setAuthor(author);
    result.setMessage(message);
    result.setId(commentId);
    
    if(taskId != null) {
      result.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_TASK_COMMENT, taskId, commentId));
    } else if(processInstanceId != null) {
      result.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_INSTANCE_COMMENT, processInstanceId, commentId));
    }
    return result;
  }
  
  public EventResponse createEventResponse(SecuredResource securedResource, Event event) {
    EventResponse result = new EventResponse();
    result.setAction(event.getAction());
    result.setId(event.getId());
    result.setMessage(event.getMessageParts());
    result.setTime(event.getTime());
    result.setUserId(event.getUserId());
    
    result.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_TASK_EVENT, event.getTaskId(), event.getId()));
    result.setTaskUrl(securedResource.createFullResourceUrl(RestUrls.URL_TASK, event.getTaskId()));
    
    if(event.getProcessInstanceId() != null) {
      result.setTaskUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_INSTANCE, event.getProcessInstanceId()));
    }
    return result ;
  }
  
  public AttachmentResponse createAttachmentResponse(SecuredResource securedResource, Attachment attachment) {
    AttachmentResponse result = new AttachmentResponse();
    result.setId(attachment.getId());
    result.setName(attachment.getName());
    result.setDescription(attachment.getDescription());
    result.setType(attachment.getType());
    
    if(attachment.getUrl() == null && attachment.getTaskId() != null) {
      // Attachment content can be streamed
      result.setContentUrl(securedResource.createFullResourceUrl(RestUrls.URL_TASK_ATTACHMENT_DATA, attachment.getTaskId(), attachment.getId()));
    } else {
      result.setExternalUrl(attachment.getUrl());
    }
    
    if(attachment.getTaskId() != null) {
      result.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_TASK_ATTACHMENT, attachment.getTaskId(), attachment.getId()));
      result.setTaskUrl(securedResource.createFullResourceUrl(RestUrls.URL_TASK, attachment.getTaskId()));
    }
    if(attachment.getProcessInstanceId() != null) {
      result.setTaskUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_INSTANCE, attachment.getProcessInstanceId()));
    }
    return result ;
  }
  
  public ProcessInstanceResponse createProcessInstanceResponse(SecuredResource securedResource, ProcessInstance processInstance) {
    ProcessInstanceResponse result = new ProcessInstanceResponse();
    result.setActivityId(processInstance.getActivityId());
    result.setBusinessKey(processInstance.getBusinessKey());
    result.setId(processInstance.getId());
    result.setProcessDefinitionId(processInstance.getProcessDefinitionId());
    result.setProcessDefinitionUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_DEFINITION, processInstance.getProcessDefinitionId()));
    result.setSuspended(processInstance.isSuspended());
    result.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_INSTANCE, processInstance.getId()));
    if (processInstance.getProcessVariables() != null) {
      Map<String, Object> variableMap = processInstance.getProcessVariables();
      for (String name : variableMap.keySet()) {
        result.addVariable(createRestVariable(securedResource, name, variableMap.get(name), 
            RestVariableScope.LOCAL, processInstance.getId(), VARIABLE_PROCESS, false));
      }
    }
    return result;
  }
  
  
  public ExecutionResponse createExecutionResponse(SecuredResource securedResource, Execution execution) {
    ExecutionResponse result = new ExecutionResponse();
    result.setActivityId(execution.getActivityId());
    result.setId(execution.getId());
    result.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_EXECUTION, execution.getId()));
    result.setSuspended(execution.isSuspended());
    
    result.setParentId(execution.getParentId());
    if(execution.getParentId() != null) {
      result.setParentUrl(securedResource.createFullResourceUrl(RestUrls.URL_EXECUTION, execution.getParentId()));
    }
    
    result.setProcessInstanceId(execution.getProcessInstanceId());
    if(execution.getProcessInstanceId() != null) {
      result.setProcessInstanceUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_INSTANCE, execution.getProcessInstanceId()));
    }
    return result;
  }
  
  public FormDataResponse createFormDataResponse(SecuredResource securedResource, FormData formData) {
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
        result.setProcessDefinitionUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_DEFINITION, startFormData.getProcessDefinition().getId()));
      }
    } else if (formData instanceof TaskFormData) {
      TaskFormData taskFormData = (TaskFormData) formData;
      if (taskFormData.getTask() != null) {
        result.setTaskId(taskFormData.getTask().getId());
        result.setTaskUrl(securedResource.createFullResourceUrl(RestUrls.URL_TASK, taskFormData.getTask().getId()));
      }
    }
    return result;
  }
  
  @SuppressWarnings("deprecation")
  public HistoricProcessInstanceResponse createHistoricProcessInstanceResponse(SecuredResource securedResource, HistoricProcessInstance processInstance) {
    HistoricProcessInstanceResponse result = new HistoricProcessInstanceResponse();
    result.setBusinessKey(processInstance.getBusinessKey());
    result.setDeleteReason(processInstance.getDeleteReason());
    result.setDurationInMillis(processInstance.getDurationInMillis());
    result.setEndActivityId(processInstance.getEndActivityId());
    result.setEndTime(processInstance.getEndTime());
    result.setId(processInstance.getId());
    result.setProcessDefinitionId(processInstance.getProcessDefinitionId());
    result.setProcessDefinitionUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_DEFINITION, processInstance.getProcessDefinitionId()));
    result.setStartActivityId(processInstance.getStartActivityId());
    result.setStartTime(processInstance.getStartTime());
    result.setStartUserId(processInstance.getStartUserId());
    result.setSuperProcessInstanceId(processInstance.getSuperProcessInstanceId());
    result.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_HISTORIC_PROCESS_INSTANCE, processInstance.getId()));
    if (processInstance.getProcessVariables() != null) {
      Map<String, Object> variableMap = processInstance.getProcessVariables();
      for (String name : variableMap.keySet()) {
        result.addVariable(createRestVariable(securedResource, name, variableMap.get(name), 
            RestVariableScope.LOCAL, processInstance.getId(), VARIABLE_HISTORY_PROCESS, false));
      }
    }
    return result;
  }
  
  public HistoricTaskInstanceResponse createHistoricTaskInstanceResponse(SecuredResource securedResource, HistoricTaskInstance taskInstance) {
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
    if (taskInstance.getProcessDefinitionId() != null) {
      result.setProcessDefinitionUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_DEFINITION, taskInstance.getProcessDefinitionId()));
    }
    result.setProcessInstanceId(taskInstance.getProcessInstanceId());
    if (taskInstance.getProcessInstanceId() != null) {
      result.setProcessInstanceUrl(securedResource.createFullResourceUrl(RestUrls.URL_HISTORIC_PROCESS_INSTANCE, taskInstance.getProcessInstanceId()));
    }
    result.setStartTime(taskInstance.getStartTime());
    result.setTaskDefinitionKey(taskInstance.getTaskDefinitionKey());
    result.setWorkTimeInMillis(taskInstance.getWorkTimeInMillis());
    result.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_HISTORIC_TASK_INSTANCE, taskInstance.getId()));
    if (taskInstance.getProcessVariables() != null) {
      Map<String, Object> variableMap = taskInstance.getProcessVariables();
      for (String name : variableMap.keySet()) {
        result.addVariable(createRestVariable(securedResource, name, variableMap.get(name), 
            RestVariableScope.GLOBAL, taskInstance.getId(), VARIABLE_HISTORY_TASK, false));
      }
    }
    if (taskInstance.getTaskLocalVariables() != null) {
      Map<String, Object> variableMap = taskInstance.getTaskLocalVariables();
      for (String name : variableMap.keySet()) {
        result.addVariable(createRestVariable(securedResource, name, variableMap.get(name), 
            RestVariableScope.LOCAL, taskInstance.getId(), VARIABLE_HISTORY_TASK, false));
      }
    }
    return result;
  }
  
  public HistoricActivityInstanceResponse createHistoricActivityInstanceResponse(SecuredResource securedResource, HistoricActivityInstance activityInstance) {
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
    result.setProcessDefinitionUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_DEFINITION, activityInstance.getProcessDefinitionId()));
    result.setProcessInstanceId(activityInstance.getProcessInstanceId());
    result.setProcessInstanceUrl(securedResource.createFullResourceUrl(RestUrls.URL_HISTORIC_PROCESS_INSTANCE, activityInstance.getId()));
    result.setStartTime(activityInstance.getStartTime());
    result.setTaskId(activityInstance.getTaskId());
    return result;
  }
  
  public HistoricVariableInstanceResponse createHistoricVariableInstanceResponse(SecuredResource securedResource, HistoricVariableInstance variableInstance) {
    HistoricVariableInstanceResponse result = new HistoricVariableInstanceResponse();
    result.setId(variableInstance.getId());
    result.setProcessInstanceId(variableInstance.getProcessInstanceId());
    if(variableInstance.getProcessInstanceId() != null) {
      result.setProcessInstanceUrl(securedResource.createFullResourceUrl(RestUrls.URL_HISTORIC_PROCESS_INSTANCE, variableInstance.getProcessInstanceId()));
    }
    result.setTaskId(variableInstance.getTaskId());
    result.setVariable(createRestVariable(securedResource, variableInstance.getVariableName(), variableInstance.getValue(), 
        null, variableInstance.getId(), VARIABLE_HISTORY_VARINSTANCE, false));
    return result;
  }
  
  public HistoricDetailResponse createHistoricDetailResponse(SecuredResource securedResource, HistoricDetail detail) {
    HistoricDetailResponse result = new HistoricDetailResponse();
    result.setId(detail.getId());
    result.setProcessInstanceId(detail.getProcessInstanceId());
    if (StringUtils.isNotEmpty(detail.getProcessInstanceId())) {
      result.setProcessInstanceUrl(securedResource.createFullResourceUrl(RestUrls.URL_HISTORIC_PROCESS_INSTANCE, detail.getProcessInstanceId()));
    }
    result.setExecutionId(detail.getExecutionId());
    result.setActivityInstanceId(detail.getActivityInstanceId());
    result.setTaskId(detail.getTaskId());
    if (StringUtils.isNotEmpty(detail.getTaskId())) {
      result.setTaskUrl(securedResource.createFullResourceUrl(RestUrls.URL_HISTORIC_TASK_INSTANCE, detail.getTaskId()));
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
      result.setVariable(createRestVariable(securedResource, variableUpdate.getVariableName(), variableUpdate.getValue(), 
          null, detail.getId(), VARIABLE_HISTORY_DETAIL, false));
    }
    return result;
  }
  
  public HistoricIdentityLinkResponse createHistoricIdentityLinkResponse(SecuredResource securedResource, HistoricIdentityLink identityLink) {
    HistoricIdentityLinkResponse result = new HistoricIdentityLinkResponse();
    result.setType(identityLink.getType());
    result.setUserId(identityLink.getUserId());
    result.setGroupId(identityLink.getGroupId());
    result.setTaskId(identityLink.getTaskId());
    if (StringUtils.isNotEmpty(identityLink.getTaskId())) {
      result.setTaskUrl(securedResource.createFullResourceUrl(RestUrls.URL_HISTORIC_TASK_INSTANCE, identityLink.getTaskId()));
    }
    result.setProcessInstanceId(identityLink.getProcessInstanceId());
    if (StringUtils.isNotEmpty(identityLink.getProcessInstanceId())) {
      result.setProcessInstanceUrl(securedResource.createFullResourceUrl(RestUrls.URL_HISTORIC_PROCESS_INSTANCE, identityLink.getProcessInstanceId()));
    }
    return result;
  }
  
  public TableResponse createTableResponse(SecuredResource securedResource, String name, Long count) {
    TableResponse result = new TableResponse();
    result.setName(name);
    result.setCount(count);
    result.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_TABLE, name));
    return result;
  }
  
  public JobResponse createJobResponse(SecuredResource securedResource, Job job) {
    JobResponse response = new JobResponse();
    response.setId(job.getId());
    response.setDueDate(job.getDuedate());
    response.setExceptionMessage(job.getExceptionMessage());
    response.setExecutionId(job.getExecutionId());
    response.setProcessDefinitionId(job.getProcessDefinitionId());
    response.setProcessInstanceId(job.getProcessInstanceId());
    response.setRetries(job.getRetries());
    
    response.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_JOB, job.getId()));
    
    if(job.getProcessDefinitionId() != null) {
      response.setProcessDefinitionUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_DEFINITION, job.getProcessDefinitionId()));
    }
    
    if(job.getProcessInstanceId() != null) {
      response.setProcessInstanceUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_INSTANCE, job.getProcessInstanceId()));
    }
    
    if(job.getExecutionId() != null) {
      response.setExecutionUrl(securedResource.createFullResourceUrl(RestUrls.URL_EXECUTION, job.getExecutionId()));
    }
    
    return response;
  }
  
  public UserResponse createUserResponse(SecuredResource securedResource, User user, boolean incudePassword) {
    UserResponse response = new UserResponse();
    response.setFirstName(user.getFirstName());
    response.setLastName(user.getLastName());
    response.setId(user.getId());
    response.setEmail(user.getEmail());
    response.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_USER, user.getId()));
    
    if(incudePassword) {
      response.setPassword(user.getPassword());
    }
    return response;
  }
  
  public UserInfoResponse createUserInfoResponse(SecuredResource securedResource, String key, String value, String userId) {
    UserInfoResponse response = new UserInfoResponse();
    response.setKey(key);
    response.setValue(value);
    response.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_USER_INFO, userId, key));
    return response;
  }
  
  public GroupResponse createGroupResponse(SecuredResource securedResource, Group group) {
    GroupResponse response = new GroupResponse();
    response.setId(group.getId());
    response.setName(group.getName());
    response.setType(group.getType());
    response.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_GROUP, group.getId()));
    
    return response;
  }
  
  public MembershipResponse createMembershipResponse(SecuredResource securedResource, String userId, String groupId) {
    MembershipResponse response = new MembershipResponse();
    response.setGroupId(groupId);
    response.setUserId(userId);
    response.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_GROUP_MEMBERSHIP, groupId, userId));
    return response;
  }
  
  public ModelResponse createModelResponse(SecuredResource securedResource, Model model) {
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
    
    response.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_MODEL, model.getId()));
    if(model.getDeploymentId() != null) {
      response.setDeploymentUrl(securedResource.createFullResourceUrl(RestUrls.URL_DEPLOYMENT, model.getDeploymentId()));
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

}
