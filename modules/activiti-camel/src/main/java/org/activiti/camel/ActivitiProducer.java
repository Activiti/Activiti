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

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;

public class ActivitiProducer extends DefaultProducer {

  private RuntimeService runtimeService;

  public static final String PROCESS_KEY_PROPERTY = "PROCESS_KEY_PROPERTY";

  public static final String PROCESS_ID_PROPERTY = "PROCESS_ID_PROPERTY";
  
  public static final String ACTIVITI_TIMEOUT_PROPERTY = "ACTIVITI_JOIN_TIMEOUT";

  public static final String ACTIVITI_TIMEOUT_RESOLUTION_PROPERTY = "ACTIVITI_JOIN_RESOLUTION";

  public static final Integer DEFAULT_TIMEOUT_RESOLUTION = 100;
  
  
  public static final Integer DEFAULT_TIMEOUT = 5000;
  
  
  
  private String processKey = null;

  private String activity = null;

  public ActivitiProducer(ActivitiEndpoint endpoint, RuntimeService runtimeService) {
    super(endpoint);
    this.runtimeService = runtimeService;
    String[] path = endpoint.getEndpointKey().split(":");
    processKey = path[1].replace("//", "");
    if (path.length > 2) {
      activity = path[2];
    }
  }

  public void process(Exchange exchange) throws Exception {
    if (shouldStartProcess()) {
      ProcessInstance pi = startProcess(exchange);
      exchange.setProperty(PROCESS_ID_PROPERTY, pi.getProcessInstanceId());
      exchange.getOut().setBody(pi.getId());
    } else {
      signal(exchange);
    }
  }

  private boolean shouldStartProcess() {
    return activity == null;
  }

  
  private Integer getPropertyWithDefault(Exchange exchange, String propertyName, int defaultValue) {    
    Object integerObject =  exchange.getProperty(propertyName);
    
    if   (integerObject != null) 
      return  Integer.parseInt((String) integerObject);
    else
      return  defaultValue;
    
  }
  
  private void signal(Exchange exchange) {
    String processInstanceId = findProcessInstanceId(exchange);
    
    
    Integer activitiTimeout =   getPropertyWithDefault(exchange, ACTIVITI_TIMEOUT_PROPERTY, DEFAULT_TIMEOUT);   
    Integer timeRsolution   =   getPropertyWithDefault(exchange, ACTIVITI_TIMEOUT_RESOLUTION_PROPERTY, DEFAULT_TIMEOUT_RESOLUTION);   
            
    boolean firstTime = true;
    
    long initialTime  = System.currentTimeMillis();
    
    
    Execution execution = null;
    while (firstTime || (activitiTimeout != null && (System.currentTimeMillis()-initialTime  < activitiTimeout))) {
       execution = runtimeService.createExecutionQuery()
          .processDefinitionKey(processKey)
          .processInstanceId(processInstanceId)
          .activityId(activity).singleResult();
        try {
          Thread.sleep(timeRsolution);
        } catch (InterruptedException e) {
          throw new RuntimeException("error occured while waiting for activiti=" + activity + " for processInstanceId=" + processInstanceId);
        }
        firstTime = false;
        if (execution != null)
            break;
    }
    if (execution == null) {
      throw new RuntimeException("Couldn't find activity "+activity+" for processId " + processInstanceId + " in defined timeout.");
    }
    

    runtimeService.setVariables(execution.getId(), ExchangeUtils.prepareVariables(exchange, getActivitiEndpoint()));
    runtimeService.signal(execution.getId());

  }

  private String findProcessInstanceId(Exchange exchange) {
    String processInstanceId = exchange.getProperty(PROCESS_ID_PROPERTY, String.class);
    if (processInstanceId != null) {
      return processInstanceId;
    }
    String processInstanceKey = exchange.getProperty(PROCESS_KEY_PROPERTY, String.class);
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceBusinessKey(processInstanceKey).singleResult();

    if (processInstance == null) {
      throw new RuntimeException("Could not find activiti with key " + processInstanceKey);
    }
    return processInstance.getId();
  }


  private ProcessInstance startProcess(Exchange exchange) {
    String key = exchange.getProperty(PROCESS_KEY_PROPERTY, String.class);
    if (key == null) {
      return runtimeService.startProcessInstanceByKey(processKey, ExchangeUtils.prepareVariables(exchange, getActivitiEndpoint()));
    } else {
      return runtimeService.startProcessInstanceByKey(processKey, key, ExchangeUtils.prepareVariables(exchange, getActivitiEndpoint()));
    }

  }

  protected ActivitiEndpoint getActivitiEndpoint() {
    return (ActivitiEndpoint) getEndpoint();
  }
}
