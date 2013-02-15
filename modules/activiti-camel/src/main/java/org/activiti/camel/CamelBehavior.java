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

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.behavior.BpmnActivityBehavior;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.apache.commons.lang.StringUtils;

public class CamelBehavior extends BpmnActivityBehavior implements ActivityBehavior {

  private static final long serialVersionUID = 1L;
  protected Expression camelContext;

  public void execute(ActivityExecution execution) throws Exception {
    ProcessEngineConfiguration engineConfiguration = Context.getProcessEngineConfiguration();
    if (engineConfiguration instanceof SpringProcessEngineConfiguration == false) {
      throw new ActivitiException("Expecting a Spring process engine configuration for the Activiti Camel module");
    }
    
    SpringProcessEngineConfiguration springConfiguration = (SpringProcessEngineConfiguration) engineConfiguration;
    String camelContextValue = getStringFromField(camelContext, execution);
    if (StringUtils.isEmpty(camelContextValue)) {
      camelContextValue = springConfiguration.getDefaultCamelContext();
    }
    
    ActivitiEndpoint endpoint = createEndpoint(execution, springConfiguration, camelContextValue);
    Exchange exchange = createExchange(execution, endpoint, springConfiguration, camelContextValue);
    endpoint.process(exchange);
    execution.setVariables(ExchangeUtils.prepareVariables(exchange, endpoint));
    performDefaultOutgoingBehavior(execution);
    
  }


  private ActivitiEndpoint createEndpoint(ActivityExecution execution, SpringProcessEngineConfiguration springConfiguration, String camelContext) {
    String uri = "activiti://" + getProcessDefinitionKey(execution) + ":" + execution.getActivity().getId();
    return getEndpoint(getContext(springConfiguration, camelContext), uri);
  }

  private ActivitiEndpoint getEndpoint(CamelContext ctx, String key) {
    for (Endpoint e : ctx.getEndpoints()) {
      if (e.getEndpointKey().equals(key) && (e instanceof ActivitiEndpoint)) {
        return (ActivitiEndpoint) e;
      }
    }
    throw new RuntimeException("Activiti endpoint not defined for " + key);    
  }

  private CamelContext getContext(SpringProcessEngineConfiguration springConfiguration, String camelContext) {
    Object ctx = springConfiguration.getApplicationContext().getBean(camelContext);
    if (ctx == null || ctx instanceof CamelContext == false) {
      throw new RuntimeException("Could not find camel context " + camelContext);
    }
    return (CamelContext) ctx;
  }


  private Exchange createExchange(ActivityExecution activityExecution, ActivitiEndpoint endpoint,
      SpringProcessEngineConfiguration springConfiguration, String camelContext) {
    
    Exchange ex = new DefaultExchange(getContext(springConfiguration, camelContext));
    ex.setProperty(ActivitiProducer.PROCESS_ID_PROPERTY, activityExecution.getProcessInstanceId());
    Map<String, Object> variables = activityExecution.getVariables();
    if (endpoint.isCopyVariablesToProperties()) {
      for (Map.Entry<String, Object> var : variables.entrySet()) {
        ex.setProperty(var.getKey(), var.getValue());
      }
    }
    if (endpoint.isCopyVariablesToBody()) {
      ex.getIn().setBody(new HashMap<String,Object>(variables));
    }
    return ex;
  }

  private String getProcessDefinitionKey(ActivityExecution execution) {
    String id = execution.getActivity().getProcessDefinition().getId();
    return id.substring(0, id.indexOf(":"));
  }
  
  protected String getStringFromField(Expression expression, DelegateExecution execution) {
    if (expression != null) {
      Object value = expression.getValue(execution);
      if (value != null) {
        return value.toString();
      }
    }
    return null;
  }

  public void setCamelContext(Expression camelContext) {
    this.camelContext = camelContext;
  }
}
