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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.behavior.BpmnActivityBehavior;
import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;

@Deprecated
public class CamelBehaviour extends BpmnActivityBehavior implements ActivityBehavior {

  private static final long serialVersionUID = 1L;
  
  private Collection<ContextProvider> contextProviders;

  public CamelBehaviour(Collection<ContextProvider> camelContext) {
    this.contextProviders = camelContext;
  }

  public void execute(ActivityExecution execution) throws Exception {
    ActivitiEndpoint ae = createEndpoint(execution);
    Exchange ex = createExchange(execution, ae);
    ae.process(ex);
    execution.setVariables(ExchangeUtils.prepareVariables(ex, ae));
    performDefaultOutgoingBehavior(execution);
  }

  private ActivitiEndpoint createEndpoint(ActivityExecution execution) {
    String uri = "activiti://" + getProcessName(execution) + ":" + execution.getActivity().getId();
    return getEndpoint(getContext(execution), uri);
  }

  private ActivitiEndpoint getEndpoint(CamelContext ctx, String key) {
    for (Endpoint e : ctx.getEndpoints()) {
      if (e.getEndpointKey().equals(key) && (e instanceof ActivitiEndpoint)) {
        return (ActivitiEndpoint) e;
      }
    }
    throw new ActivitiException("Activiti endpoint not defined for " + key);    
  }

  private CamelContext getContext(ActivityExecution execution) {
    String processName = getProcessName(execution);
    String names = "";
    for (ContextProvider provider : contextProviders) {
      CamelContext ctx = provider.getContext(processName);
      if (ctx != null) {
        return ctx;
      }
    }
    throw new ActivitiException("Could not find camel context for " + processName + " names are " + names);
  }


  private Exchange createExchange(ActivityExecution activityExecution, ActivitiEndpoint endpoint) {
    Exchange ex = new DefaultExchange(getContext(activityExecution));
    Map<String, Object> variables = activityExecution.getVariables();
    if (endpoint.isCopyVariablesToProperties()) {
      for (Map.Entry<String, Object> var : variables.entrySet()) {
        ex.setProperty(var.getKey(), var.getValue());
      }
    }
    if (endpoint.isCopyVariablesToBodyAsMap()) {
      ex.getIn().setBody(new HashMap<String,Object>(variables));
    }
    return ex;
  }

  private String getProcessName(ActivityExecution execution) {
    PvmProcessDefinition processDefinition = execution.getActivity().getProcessDefinition();
    return processDefinition.getKey();
  }

}
