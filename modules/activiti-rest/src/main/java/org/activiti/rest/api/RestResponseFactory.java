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

package org.activiti.rest.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.bpmn.deployer.BpmnDeployer;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.engine.AttachmentResponse;
import org.activiti.rest.api.engine.CommentResponse;
import org.activiti.rest.api.engine.EventResponse;
import org.activiti.rest.api.engine.variable.BooleanRestVariableConverter;
import org.activiti.rest.api.engine.variable.DateRestVariableConverter;
import org.activiti.rest.api.engine.variable.DoubleRestVariableConverter;
import org.activiti.rest.api.engine.variable.IntegerRestVariableConverter;
import org.activiti.rest.api.engine.variable.LongRestVariableConverter;
import org.activiti.rest.api.engine.variable.QueryVariable;
import org.activiti.rest.api.engine.variable.RestVariable;
import org.activiti.rest.api.engine.variable.RestVariable.RestVariableScope;
import org.activiti.rest.api.engine.variable.RestVariableConverter;
import org.activiti.rest.api.engine.variable.ShortRestVariableConverter;
import org.activiti.rest.api.engine.variable.StringRestVariableConverter;
import org.activiti.rest.api.history.HistoricActivityInstanceResponse;
import org.activiti.rest.api.history.HistoricProcessInstanceResponse;
import org.activiti.rest.api.history.HistoricTaskInstanceResponse;
import org.activiti.rest.api.history.HistoricVariableInstanceResponse;
import org.activiti.rest.api.identity.RestIdentityLink;
import org.activiti.rest.api.management.TableResponse;
import org.activiti.rest.api.repository.DeploymentResourceResponse;
import org.activiti.rest.api.repository.DeploymentResourceResponse.DeploymentResourceType;
import org.activiti.rest.api.repository.DeploymentResponse;
import org.activiti.rest.api.repository.ProcessDefinitionResponse;
import org.activiti.rest.api.runtime.process.ExecutionResponse;
import org.activiti.rest.api.runtime.process.ProcessInstanceResponse;
import org.activiti.rest.api.runtime.task.TaskResponse;
import org.restlet.data.MediaType;


/**
 * Default implementation of a {@link RestResponseFactory}.
 * 
 * @author Frederik Heremans
 */
public class RestResponseFactory {

  public static final String BYTE_ARRAY_VARIABLE_TYPE = "binary";
  public static final String SERIALIZABLE_VARIABLE_TYPE = "serializable";
  
  private List<RestVariableConverter> variableConverters = new ArrayList<RestVariableConverter>();
  
  public RestResponseFactory() {
    initializeVariableConverters();
  }
  
  public TaskResponse createTaskReponse(SecuredResource resourceContext, Task task) {
    TaskResponse response = new TaskResponse(task);
    response.setUrl(resourceContext.createFullResourceUrl(RestUrls.URL_TASK, task.getId()));

    // Add references to other resources, if needed
    if(task.getParentTaskId() != null) {
      response.setParentTask(resourceContext.createFullResourceUrl(RestUrls.URL_TASK, task.getParentTaskId()));
    }
    if(task.getProcessDefinitionId() != null) {
      response.setProcessDefinition(resourceContext.createFullResourceUrl(RestUrls.URL_PROCESS_DEFINITION, task.getProcessDefinitionId()));
    }
    if(task.getExecutionId() != null) {
      response.setExecution(resourceContext.createFullResourceUrl(RestUrls.URL_EXECUTION, task.getExecutionId()));
    }
    if(task.getProcessInstanceId() != null) {
      response.setProcessInstance(resourceContext.createFullResourceUrl(RestUrls.URL_PROCESS_INSTANCE, task.getProcessInstanceId()));
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
    // TODO: do based on the returned resource-POJO from the API once ready instead of doing it here
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
    // TODO: this method does an additional check to see if the process-definition exists which causes an additional query on top
    // of the one we already did to retrieve the processdefinition in the first place.
    ProcessDefinition deployedDefinition = ActivitiUtil.getRepositoryService().getProcessDefinition(processDefinition.getId());
    response.setGraphicalNotationDefined(((ProcessDefinitionEntity) deployedDefinition).isGraphicalNotationDefined());
    
    // Links to other resources
    response.setDeployment(resourceContext.createFullResourceUrl(RestUrls.URL_DEPLOYMENT, processDefinition.getDeploymentId()));
    response.setResource(resourceContext.createFullResourceUrl(RestUrls.URL_DEPLOYMENT_RESOURCE, processDefinition.getDeploymentId(), processDefinition.getResourceName()));
    if(processDefinition.getDiagramResourceName() != null) {
      response.setDiagramResource(resourceContext.createFullResourceUrl(RestUrls.URL_DEPLOYMENT_RESOURCE,
              processDefinition.getDeploymentId(), processDefinition.getDiagramResourceName()));
    }
    return response;
  }
  
  public List<RestVariable> createRestVariables(SecuredResource securedResource, Map<String, Object> variables, String taskId, String executionId, String processInstanceId, RestVariableScope scope) {
   List<RestVariable> result = new ArrayList<RestVariable>();
   
   for(Entry<String, Object> pair : variables.entrySet()) {
     result.add(createRestVariable(securedResource, pair.getKey(), pair.getValue(), scope, taskId, executionId, processInstanceId, false));
   }
   
   return result;
  }
  
  public RestVariable createRestVariable(SecuredResource securedResource, String name, Object value, RestVariableScope scope, String taskId, String executionId, String processInstanceId, boolean includeBinaryValue) {
    RestVariableConverter converter = null;
    RestVariable restVar = new RestVariable();
    restVar.setVariableScope(scope);
    restVar.setName(name);
    
    if(value != null) {
      // Try converting the value
      for(RestVariableConverter c : variableConverters) {
        if(value.getClass().isAssignableFrom(c.getVariableType())) {
          converter = c;
          break;
        }
      }
      
      if(converter != null) {
        converter.convertVariableValue(value, restVar);
        restVar.setType(converter.getRestTypeName());
      } else {
        // Revert to default conversion, which is the serializable/byte-array form
        if(value instanceof Byte[] || value instanceof byte[]) {
          restVar.setType(BYTE_ARRAY_VARIABLE_TYPE);
        } else {
          restVar.setType(SERIALIZABLE_VARIABLE_TYPE);
        }
        
        if(includeBinaryValue) {
          restVar.setValue(value);
        }
        
        if(taskId != null) {
          restVar.setValueUrl(securedResource.createFullResourceUrl(RestUrls.URL_TASK_VARIABLE_DATA, taskId, name));
        }
        
        if(executionId != null) {
          restVar.setValueUrl(securedResource.createFullResourceUrl(RestUrls.URL_EXECUTION_VARIABLE_DATA, executionId, name));
        }
        
        if(processInstanceId != null) {
          restVar.setValueUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_DATA, processInstanceId, name));
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
    return createRestIdentityLink(securedResource, link.getType(), link.getUserId(), link.getGroupId(), link.getTaskId());
  }
  
  public RestIdentityLink createRestIdentityLink(SecuredResource securedResource, String type, String userId, String groupId, String taskId) {
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
    result.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_TASK_IDENTITYLINK, taskId, family, (userId != null ? userId : groupId), type));
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
    result.setProcessDefinitionUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_DEFINITION, processInstance.getProcessDefinitionId()));
    result.setSuspended(processInstance.isSuspended());
    result.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_INSTANCE, processInstance.getId()));
    return result;
  }
  
  
  public ExecutionResponse createExecutionResponse(SecuredResource securedResource, Execution execution) {
    ExecutionResponse result = new ExecutionResponse();
    result.setActivityId(execution.getActivityId());
    result.setId(execution.getId());
    result.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_EXECUTION, execution.getId()));
    result.setSuspended(execution.isSuspended());
    
    if(execution.getParentId() != null) {
      result.setParentUrl(securedResource.createFullResourceUrl(RestUrls.URL_EXECUTION, execution.getParentId()));
    }
    
    if(execution.getProcessInstanceId() != null) {
      result.setProcessInstanceUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_INSTANCE, execution.getProcessInstanceId()));
    }
    return result;
  }
  
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
    result.setProcessDefinitionUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_DEFINITION, taskInstance.getProcessDefinitionId()));
    result.setProcessInstanceId(taskInstance.getProcessInstanceId());
    result.setProcessInstanceUrl(securedResource.createFullResourceUrl(RestUrls.URL_HISTORIC_PROCESS_INSTANCE, taskInstance.getProcessInstanceId()));
    result.setStartTime(taskInstance.getStartTime());
    result.setTaskDefinitionKey(taskInstance.getTaskDefinitionKey());
    result.setWorkTimeInMillis(taskInstance.getWorkTimeInMillis());
    result.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_HISTORIC_TASK_INSTANCE, taskInstance.getId()));
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
    result.setProcessInstanceUrl(securedResource.createFullResourceUrl(RestUrls.URL_HISTORIC_PROCESS_INSTANCE, variableInstance.getProcessInstanceId()));
    result.setTaskId(variableInstance.getTaskId());
    result.setValue(variableInstance.getValue());
    result.setVariableName(variableInstance.getVariableName());
    result.setVariableTypeName(variableInstance.getVariableTypeName());
    return result;
  }
  
  public TableResponse createTableResponse(SecuredResource securedResource, String name, Long count) {
    TableResponse result = new TableResponse();
    result.setName(name);
    result.setCount(count);
    result.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_TABLE, name));
    return result;
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
