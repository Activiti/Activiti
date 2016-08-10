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
package org.activiti.camel;

/**
 * @author Saeid Mirzaei  
 * @author Maciej Próchniak
 * @author Arnold Schrijver
 */

import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;

public class ActivitiProducer extends DefaultProducer {

  protected IdentityService identityService;
  
  protected RuntimeService runtimeService;

  public static final String PROCESS_KEY_PROPERTY = "PROCESS_KEY_PROPERTY";

  public static final String PROCESS_ID_PROPERTY = "PROCESS_ID_PROPERTY";
  
  public static final String EXECUTION_ID_PROPERTY = "EXECUTION_ID_PROPERTY";
  
  private final long timeout;

  private final long timeResolution;
  
  private String processKey = null;

  private String activity = null;

  public ActivitiProducer(ActivitiEndpoint endpoint, long timeout, long timeResolution) {
    super(endpoint);
    String[] path = endpoint.getEndpointKey().split(":");
    processKey = path[1].replace("//", "");
    if (path.length > 2) {
      activity = path[2];
    }
    this.timeout = timeout;
    this.timeResolution = timeResolution;
  }
  
  public void process(Exchange exchange) throws Exception {
    if (shouldStartProcess()) {
      ProcessInstance pi = startProcess(exchange);
      copyResultToCamel(exchange, pi);
    } else {
      signal(exchange);
    }
  }

  public void setIdentityService(IdentityService identityService) {
    this.identityService = identityService;
  }
  
  public void setRuntimeService(RuntimeService runtimeService) {
    this.runtimeService = runtimeService;
  }

  protected void copyResultToCamel(Exchange exchange, ProcessInstance pi) {
    exchange.setProperty(PROCESS_ID_PROPERTY, pi.getProcessInstanceId());
    
    Map<String, Object> returnVars = getActivitiEndpoint().getReturnVarMap();
    
    if (returnVars != null && returnVars.size() > 0) {
      
      Map<String, Object> processVariables = ((ExecutionEntity) pi).getVariableValues();
      if (processVariables != null) {
        for (String variableName : returnVars.keySet()) {
          if (processVariables.containsKey(variableName)) {
            exchange.setProperty(variableName, processVariables.get(variableName));
          }
        }
      }
    }
  }

  protected boolean shouldStartProcess() {
    return activity == null;
  }
  
  protected void signal(Exchange exchange) {
    String processInstanceId = findProcessInstanceId(exchange);
    String executionId = exchange.getProperty(EXECUTION_ID_PROPERTY, String.class);
    
    boolean firstTime = true; 
    long initialTime  = System.currentTimeMillis();
   
    Execution execution = null;
    while (firstTime || (timeout > 0 && (System.currentTimeMillis() - initialTime < timeout))) {
      try {
        Thread.sleep(timeResolution);
      } catch (InterruptedException e) {
        throw new ActivitiException("error occured while waiting for activiti=" + activity + " for processInstanceId=" + processInstanceId);
      }
      firstTime = false;
      
      if (executionId != null) {
        execution = runtimeService.createExecutionQuery()
            .executionId(executionId)
            .activityId(activity)
            .singleResult();
        
      } else {
        execution = runtimeService.createExecutionQuery()
            .processDefinitionKey(processKey)
            .processInstanceId(processInstanceId)
            .activityId(activity)
            .singleResult();
      }
       
      if (execution != null) {
        break;
      }
    }
    if (execution == null) {
      throw new ActivitiException("Couldn't find activity "+activity+" for processId " + processInstanceId + " in defined timeout.");
    }
    
    runtimeService.setVariables(execution.getId(), ExchangeUtils.prepareVariables(exchange, getActivitiEndpoint()));
    runtimeService.signal(execution.getId());
  }

  protected String findProcessInstanceId(Exchange exchange) {
    String processInstanceId = exchange.getProperty(PROCESS_ID_PROPERTY, String.class);
    if (processInstanceId != null) {
      return processInstanceId;
    }
    
    String processInstanceKey = exchange.getProperty(PROCESS_KEY_PROPERTY, String.class);
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceBusinessKey(processInstanceKey).singleResult();

    if (processInstance == null) {
      throw new ActivitiException("Could not find activiti with key " + processInstanceKey);
    }
    return processInstance.getId();
  }

  protected ProcessInstance startProcess(Exchange exchange) {
    ActivitiEndpoint endpoint = getActivitiEndpoint();
    String key = exchange.getProperty(PROCESS_KEY_PROPERTY, String.class);
    try {
      if (endpoint.isSetProcessInitiator()) {
        setProcessInitiator(ExchangeUtils.prepareInitiator(exchange, endpoint));
      }
      
      if (key == null) {
        return runtimeService.startProcessInstanceByKey(processKey, ExchangeUtils.prepareVariables(exchange, endpoint));
      } else {
        return runtimeService.startProcessInstanceByKey(processKey, key, ExchangeUtils.prepareVariables(exchange, endpoint));
      }
      
    } finally {
      if (endpoint.isSetProcessInitiator()) {
        setProcessInitiator(null);
      }
    }
  }
  
  protected void setProcessInitiator(String processInitiator) {
    if (identityService == null) {
      throw new ActivitiException("IdentityService is missing and must be provided to set process initiator.");
    }
    identityService.setAuthenticatedUserId(processInitiator);
  }

  protected ActivitiEndpoint getActivitiEndpoint() {
    return (ActivitiEndpoint) getEndpoint();
  }
}
