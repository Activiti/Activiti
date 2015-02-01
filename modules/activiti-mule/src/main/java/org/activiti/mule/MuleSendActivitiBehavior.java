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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.MuleMessage;
import org.mule.api.client.LocalMuleClient;
import org.mule.util.IOUtils;

/**
 * @author Esteban Robles Luna
 */
public class MuleSendActivitiBehavior extends AbstractBpmnActivityBehavior {

  private static final long serialVersionUID = 1L;
  
  private MuleContext muleContext;

  private Expression endpointUrl;
  private Expression language;
  private Expression payloadExpression;
  private Expression resultVariable;
  private Expression username;
  private Expression password;

  public void execute(ActivityExecution execution) throws Exception {
    String endpointUrlValue = this.getStringFromField(this.endpointUrl, execution);
    String languageValue = this.getStringFromField(this.language, execution);
    String payloadExpressionValue = this.getStringFromField(this.payloadExpression, execution);
    String resultVariableValue = this.getStringFromField(this.resultVariable, execution);
    String usernameValue = this.getStringFromField(this.username, execution);
    String passwordValue = this.getStringFromField(this.password, execution);

    ScriptingEngines scriptingEngines = Context.getProcessEngineConfiguration().getScriptingEngines();
    Object payload = scriptingEngines.evaluate(payloadExpressionValue, languageValue, execution);
    
    if (endpointUrlValue.startsWith("vm:")) {
      LocalMuleClient client = this.getMuleContext().getClient();
      MuleMessage message = new DefaultMuleMessage(payload, this.getMuleContext());
      MuleMessage resultMessage = client.send(endpointUrlValue, message);
      Object result = resultMessage.getPayload();
      if (resultVariableValue != null) {
        execution.setVariable(resultVariableValue, result);
      }
      
    } else {
    
      HttpClientBuilder clientBuilder = HttpClientBuilder.create();
      
      if (usernameValue != null && passwordValue != null) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(usernameValue, passwordValue);
        provider.setCredentials(new AuthScope("localhost", -1, "mule-realm"), credentials);
        clientBuilder.setDefaultCredentialsProvider(provider);
      }
      
      HttpClient client = clientBuilder.build();
      
      HttpPost request = new HttpPost(endpointUrlValue);
      
      try {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(payload);
        oos.flush();
        oos.close();
        
        request.setEntity(new ByteArrayEntity(baos.toByteArray()));
        
      } catch (Exception e) {
        throw new ActivitiException("Error setting message payload", e);
      }
      
      byte[] responseBytes = null;
      try {
        // execute the POST request
        HttpResponse response = client.execute(request);
        responseBytes = IOUtils.toByteArray(response.getEntity().getContent());
        
      } finally {
        // release any connection resources used by the method
        request.releaseConnection();
      }
  
      if (responseBytes != null) {
        try {
          ByteArrayInputStream in = new ByteArrayInputStream(responseBytes);
          ObjectInputStream is = new ObjectInputStream(in);
          Object result = is.readObject();
          if (resultVariableValue != null) {
            execution.setVariable(resultVariableValue, result);
          }
        } catch (Exception e) {
          throw new ActivitiException("Failed to read response value", e);
        }
      }
    }

    this.leave(execution);
  }
  
  protected MuleContext getMuleContext() {
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

  public Expression getUsername() {
    return username;
  }

  public void setUsername(Expression username) {
    this.username = username;
  }

  public Expression getPassword() {
    return password;
  }

  public void setPassword(Expression password) {
    this.password = password;
  }
}
