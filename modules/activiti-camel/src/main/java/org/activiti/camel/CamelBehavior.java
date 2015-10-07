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
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.MapExceptionEntry;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.bpmn.helper.ErrorPropagation;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.apache.commons.lang3.StringUtils;

/**
* This abstract class takes the place of the now-deprecated CamelBehaviour class (which can still be used for legacy compatibility)
* and significantly improves on its flexibility. Additional implementations can be created that change the way in which Activiti
* interacts with Camel per your specific needs.
* 
* Three out-of-the-box implementations of CamelBehavior are provided:
*   (1) CamelBehaviorDefaultImpl: Works just like CamelBehaviour does; copies variables into and out of Camel as or from properties.
*   (2) CamelBehaviorBodyAsMapImpl: Works by copying variables into and out of Camel using a Map<String,Object> object in the body.
*   (3) CamelBehaviorCamelBodyImpl: Works by copying a single variable value into Camel as a String body and copying the Camel
*      body into that same Activiti variable. The variable in Activiti must be named "camelBody".
* 
* The chosen implementation should be set within your ProcessEngineConfiguration. To specify the implementation using Spring, include
* the following line in your configuration file as part of the properties for "org.activiti.spring.SpringProcessEngineConfiguration":
* 
*   <property name="camelBehaviorClass" value="org.activiti.camel.impl.CamelBehaviorCamelBodyImpl"/>
* 
* Note also that the manner in which variables are copied to Activiti from Camel has changed. It will always copy Camel
* properties to the Activiti variable set; they can safely be ignored, of course, if not required. It will conditionally
* copy the Camel body to the "camelBody" variable if it is of type java.lang.String, OR it will copy the Camel body to
* individual variables within Activiti if it is of type Map<String,Object>.
* 
* @author Ryan Johnston (@rjfsu), Tijs Rademakers, Saeid Mirzaei
* @version 5.12
*/
public abstract class CamelBehavior extends AbstractBpmnActivityBehavior implements ActivityBehavior {

  private static final long serialVersionUID = 1L;
  protected Expression camelContext;
  protected CamelContext camelContextObj;
  protected SpringProcessEngineConfiguration springConfiguration;
  protected List<MapExceptionEntry> mapExceptions;
  
  protected abstract void setPropertTargetVariable(ActivitiEndpoint endpoint);
  
 
  public enum TargetType {
        BODY_AS_MAP, BODY, PROPERTIES
      }  

  protected TargetType toTargetType=null;
  
  protected void updateTargetVariables(ActivitiEndpoint endpoint) {
    toTargetType = null;
    if (endpoint.isCopyVariablesToBodyAsMap())
      toTargetType = TargetType.BODY_AS_MAP;
    else if (endpoint.isCopyCamelBodyToBody())
      toTargetType = TargetType.BODY;
    else if (endpoint.isCopyVariablesToProperties())
      toTargetType = TargetType.PROPERTIES;

    if (toTargetType == null)
      setPropertTargetVariable(endpoint);
  }
  
  protected void copyVariables(Map<String, Object> variables, Exchange exchange, ActivitiEndpoint endpoint) {
    switch (toTargetType) {
    case BODY_AS_MAP:
      copyVariablesToBodyAsMap(variables, exchange);
      break;

    case BODY:
      copyVariablesToBody(variables, exchange);
      break;

    case PROPERTIES:
      copyVariablesToProperties(variables, exchange);
    }
  }
  
  public void execute(ActivityExecution execution) throws Exception {
    setAppropriateCamelContext(execution);
    
    final ActivitiEndpoint endpoint = createEndpoint(execution);
    final Exchange exchange = createExchange(execution, endpoint);
    
    endpoint.process(exchange);
    execution.setVariables(ExchangeUtils.prepareVariables(exchange, endpoint));
    if (!handleCamelException(exchange, execution))
      leave(execution);
  }

  protected ActivitiEndpoint createEndpoint(ActivityExecution execution) {
    String uri = "activiti://" + getProcessDefinitionKey(execution) + ":" + execution.getActivity().getId();
    return getEndpoint(uri);
  }

  protected ActivitiEndpoint getEndpoint(String key) {
    for (Endpoint e : camelContextObj.getEndpoints()) {
      if (e.getEndpointKey().equals(key) && (e instanceof ActivitiEndpoint)) {
        return (ActivitiEndpoint) e;
      }
    }
    throw new ActivitiException("Activiti endpoint not defined for " + key);    
  }

  protected Exchange createExchange(ActivityExecution activityExecution, ActivitiEndpoint endpoint) {
    Exchange ex = endpoint.createExchange();
    ex.setProperty(ActivitiProducer.PROCESS_ID_PROPERTY, activityExecution.getProcessInstanceId());
    ex.setProperty(ActivitiProducer.EXECUTION_ID_PROPERTY, activityExecution.getId());
    Map<String, Object> variables = activityExecution.getVariables();
    updateTargetVariables(endpoint);
    copyVariables(variables, ex, endpoint);
    return ex;
  }
  
  protected boolean handleCamelException(Exchange exchange, ActivityExecution execution) throws Exception {
    Exception camelException = exchange.getException();
    boolean notHandledByCamel = exchange.isFailed() && camelException != null;
    if (notHandledByCamel) {
      if (camelException instanceof BpmnError) {
        ErrorPropagation.propagateError((BpmnError) camelException,
            execution);
        return true;
      } else {
        if (ErrorPropagation.mapException(camelException, execution, mapExceptions))
          return true;
        else
          throw new ActivitiException("Unhandled exception on camel route", camelException);
      }
    }
    return false;
  }
  
  protected void copyVariablesToProperties(Map<String, Object> variables, Exchange exchange) {
    for (Map.Entry<String, Object> var : variables.entrySet()) {
      exchange.setProperty(var.getKey(), var.getValue());
    }
  }
  
  protected void copyVariablesToBodyAsMap(Map<String, Object> variables, Exchange exchange) {
    exchange.getIn().setBody(new HashMap<String,Object>(variables));
  }
  
  protected void copyVariablesToBody(Map<String, Object> variables, Exchange exchange) {
    Object camelBody = variables.get(ExchangeUtils.CAMELBODY);
    if(camelBody != null) {
      exchange.getIn().setBody(camelBody);
    }
  }

  protected String getProcessDefinitionKey(ActivityExecution execution) {
    PvmProcessDefinition processDefinition = execution.getActivity().getProcessDefinition();
    return processDefinition.getKey();
  }
  
  protected boolean isASync(ActivityExecution execution) {
     return execution.getActivity().isAsync();
  }
  
  protected void setAppropriateCamelContext(ActivityExecution execution) {
    //Check to see if the springConfiguration has been set. If not, set it.
    if (springConfiguration == null) {
      //Get the ProcessEngineConfiguration object.
      ProcessEngineConfiguration engineConfiguration = Context.getProcessEngineConfiguration();
          
      //Convert it to a SpringProcessEngineConfiguration. If this doesn't work, throw a RuntimeException.
      // (ActivitiException extends RuntimeException.)
      try {
        springConfiguration = (SpringProcessEngineConfiguration) engineConfiguration;
      } catch (Exception e) {
        throw new ActivitiException("Expecting a SpringProcessEngineConfiguration for the Activiti Camel module.", e);
      }
    }
          
    //Get the appropriate String representation of the CamelContext object from ActivityExecution (if available).
    String camelContextValue = getStringFromField(camelContext, execution);
          
    //If the String representation of the CamelContext object from ActivityExecution is empty, use the default.
    if (StringUtils.isEmpty(camelContextValue) && camelContextObj != null) {
      //No processing required. No custom CamelContext & the default is already set.
    }
    else {
      if (StringUtils.isEmpty(camelContextValue) && camelContextObj == null) {
        camelContextValue = springConfiguration.getDefaultCamelContext();
      }
      
      //Get the CamelContext object and set the super's member variable.
      Object ctx = springConfiguration.getApplicationContext().getBean(camelContextValue);
      if (ctx == null || ctx instanceof CamelContext == false) {
        throw new ActivitiException("Could not find CamelContext named " + camelContextValue + ".");
      }
      camelContextObj = (CamelContext)ctx;
    }
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
