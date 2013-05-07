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
import org.activiti.engine.impl.bpmn.deployer.BpmnDeployer;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.engine.variable.BooleanRestVariableConverter;
import org.activiti.rest.api.engine.variable.DateRestVariableConverter;
import org.activiti.rest.api.engine.variable.DoubleRestVariableConverter;
import org.activiti.rest.api.engine.variable.IntegerRestVariableConverter;
import org.activiti.rest.api.engine.variable.LongRestVariableConverter;
import org.activiti.rest.api.engine.variable.RestVariable;
import org.activiti.rest.api.engine.variable.RestVariable.RestVariableScope;
import org.activiti.rest.api.engine.variable.RestVariableConverter;
import org.activiti.rest.api.engine.variable.ShortRestVariableConverter;
import org.activiti.rest.api.engine.variable.StringRestVariableConverter;
import org.activiti.rest.api.repository.DeploymentResourceResponse;
import org.activiti.rest.api.repository.DeploymentResourceResponse.DeploymentResourceType;
import org.activiti.rest.api.repository.DeploymentResponse;
import org.activiti.rest.api.repository.ProcessDefinitionResponse;
import org.activiti.rest.api.task.TaskResponse;
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
  
  public List<RestVariable> createRestVariables(SecuredResource securedResource, Map<String, Object> variables, String taskId, String executionId, RestVariableScope scope) {
   List<RestVariable> result = new ArrayList<RestVariable>();
   
   for(Entry<String, Object> pair : variables.entrySet()) {
     result.add(createRestVariable(securedResource, pair.getKey(), pair.getValue(), scope, taskId, executionId, false));
   }
   
   return result;
  }
  
  public RestVariable createRestVariable(SecuredResource securedResource, String name, Object value, RestVariableScope scope, String taskId, String executionId, boolean includeBinaryValue) {
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
        // TODO: execution variables
      }
    }
    return restVar;
  }
  
  public RestVariable createBinaryRestVariable(SecuredResource securedResource, String name, RestVariableScope scope, String type, String taskId, String executionId) {
    RestVariable restVar = new RestVariable();
    restVar.setVariableScope(scope);
    restVar.setName(name);
    restVar.setType(type);
    restVar.setValueUrl(securedResource.createFullResourceUrl(RestUrls.URL_TASK_VARIABLE_DATA, taskId, name));
    
    if(taskId != null) {
      restVar.setValueUrl(securedResource.createFullResourceUrl(RestUrls.URL_TASK_VARIABLE_DATA, taskId, name));
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
