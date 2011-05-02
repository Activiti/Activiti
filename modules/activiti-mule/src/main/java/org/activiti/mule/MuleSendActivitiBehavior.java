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
package org.activiti.mule;

import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.mule.DefaultMuleMessage;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleContext;
import org.mule.api.MuleMessage;
import org.mule.api.client.LocalMuleClient;

/**
 * @author Esteban Robles Luna
 */
public class MuleSendActivitiBehavior extends AbstractBpmnActivityBehavior {

  private MuleContext muleContext;

  private MessageExchangePattern mep = MessageExchangePattern.REQUEST_RESPONSE;
  private Expression endpointUrl;
  private Expression language;
  private Expression payloadExpression;
  private Expression resultVariable;

  public void execute(ActivityExecution execution) throws Exception {
    String endpointUrlValue = this.getStringFromField(this.endpointUrl, execution);
    String languageValue = this.getStringFromField(this.language, execution);
    String payloadExpressionValue = this.getStringFromField(this.payloadExpression, execution);
    String resultVariableValue = this.getStringFromField(this.resultVariable, execution);

    LocalMuleClient client = this.getMuleContext().getClient();

    ScriptingEngines scriptingEngines = Context.getProcessEngineConfiguration().getScriptingEngines();

    Object payload = scriptingEngines.evaluate(payloadExpressionValue, languageValue, execution);

    MuleMessage message = new DefaultMuleMessage(payload, this.getMuleContext());

    switch (mep) {
    case REQUEST_RESPONSE:
      MuleMessage resultMessage = client.send(endpointUrlValue, message);
      Object result = resultMessage.getPayload();
      if (resultVariableValue != null) {
        execution.setVariable(resultVariableValue, result);
      }
      break;
    case ONE_WAY:
      client.dispatch(endpointUrlValue, message);
      break;
    default:
      break;
    }

    this.leave(execution);
  }

  private MuleContext getMuleContext() {
    if (this.muleContext == null) {
      Map<Object, Object> beans = Context.getProcessEngineConfiguration().getBeans();
      this.muleContext = (MuleContext) beans.get("muleContext");
    }
    return this.muleContext;
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

  public MessageExchangePattern getMep() {
    return mep;
  }

  public void setMep(MessageExchangePattern mep) {
    this.mep = mep;
  }

  public Expression getEndpointUrl() {
    return endpointUrl;
  }

  public void setEndpointUrl(Expression endpointUrl) {
    this.endpointUrl = endpointUrl;
  }

  public Expression getPayloadExpression() {
    return payloadExpression;
  }

  public void setPayloadExpression(Expression payloadExpression) {
    this.payloadExpression = payloadExpression;
  }

  public Expression getResultVariable() {
    return resultVariable;
  }

  public void setResultVariable(Expression resultVariable) {
    this.resultVariable = resultVariable;
  }

  public Expression getLanguage() {
    return language;
  }

  public void setLanguage(Expression language) {
    this.language = language;
  }
}
