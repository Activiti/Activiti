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
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.apache.camel.CamelContext;
import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.commons.lang3.StringUtils;

/**
 * This class has been modified to be consistent with the changes to CamelBehavior and its implementations. The set of changes
 * significantly increases the flexibility of our Camel integration, as you can either choose one of three "out-of-the-box" modes,
 * or you can choose to create your own. Please reference the comments for the "CamelBehavior" class for more information on the 
 * out-of-the-box implementation class options.  
 * 
 * @author Ryan Johnston (@rjfsu), Tijs Rademakers, Arnold Schrijver
 */
public class ActivitiEndpoint extends DefaultEndpoint {

  protected IdentityService identityService;

  protected RuntimeService runtimeService;

  protected ActivitiConsumer activitiConsumer;

  protected boolean copyVariablesToProperties;

  protected boolean copyVariablesToBodyAsMap;

  protected boolean copyCamelBodyToBody;
  
  protected String copyVariablesFromProperties;

  protected String copyVariablesFromHeader;
  
  protected boolean copyCamelBodyToBodyAsString;
  
  protected String processInitiatorHeaderName;
  
  protected Map<String, Object> returnVarMap = new HashMap<String, Object>();
  
  protected long timeout = 5000;
  
  protected int timeResolution = 100;

  public ActivitiEndpoint(String uri, CamelContext camelContext) {
    super();
    setCamelContext(camelContext);
    setEndpointUri(uri);
  }

  public void process(Exchange ex) throws Exception {
    if (activitiConsumer == null) {
      throw new ActivitiException("Activiti consumer not defined for " + getEndpointUri());
    }
    activitiConsumer.getProcessor().process(ex);
  }

  public Producer createProducer() throws Exception {
    ActivitiProducer producer = new ActivitiProducer(this, getTimeout(), getTimeResolution());
    producer.setRuntimeService(runtimeService);
    producer.setIdentityService(identityService);
    return producer;
  }

  public Consumer createConsumer(Processor processor) throws Exception {
    return new ActivitiConsumer(this, processor);
  }
  
  protected void addConsumer(ActivitiConsumer consumer) {
    if (activitiConsumer != null) {
      throw new ActivitiException("Activiti consumer already defined for " + getEndpointUri() + "!");
    }
    activitiConsumer = consumer;
  }
  
  protected void removeConsumer() {
    activitiConsumer = null;
  }

  public boolean isSingleton() {
    return true;
  }
  
  public void setIdentityService(IdentityService identityService) {
    this.identityService = identityService;
  }

  public void setRuntimeService(RuntimeService runtimeService) {
    this.runtimeService = runtimeService;
  }

  public boolean isCopyVariablesToProperties() {
    return copyVariablesToProperties;
  }

  public void setCopyVariablesToProperties(boolean copyVariablesToProperties) {
    this.copyVariablesToProperties = copyVariablesToProperties;
  }

  public boolean isCopyCamelBodyToBody() {
    return copyCamelBodyToBody;
  }

  public void setCopyCamelBodyToBody(boolean copyCamelBodyToBody) {
    this.copyCamelBodyToBody = copyCamelBodyToBody;
  }

  public boolean isCopyVariablesToBodyAsMap() {
    return copyVariablesToBodyAsMap;
  }

  public void setCopyVariablesToBodyAsMap(boolean copyVariablesToBodyAsMap) {
    this.copyVariablesToBodyAsMap = copyVariablesToBodyAsMap;
  }


  public String getCopyVariablesFromProperties() {
    return copyVariablesFromProperties ;
  }

  
  public void setCopyVariablesFromProperties(String copyVariablesFromProperties) {
    this.copyVariablesFromProperties = copyVariablesFromProperties;
  }

  
  public String getCopyVariablesFromHeader() {
    return copyVariablesFromHeader;
    
  }
  
   public void setCopyVariablesFromHeader(String copyVariablesFromHeader) {
    this.copyVariablesFromHeader = copyVariablesFromHeader;
  }
  
  public boolean isCopyCamelBodyToBodyAsString() {
    return copyCamelBodyToBodyAsString;
  }
  
  public void setCopyCamelBodyToBodyAsString(boolean copyCamelBodyToBodyAsString) {
    this.copyCamelBodyToBodyAsString = copyCamelBodyToBodyAsString;
  }
  
  public boolean isSetProcessInitiator() {
      return StringUtils.isNotEmpty(getProcessInitiatorHeaderName());
  }
  
  public Map<String, Object> getReturnVarMap() {
    return returnVarMap;
  }

  public void setReturnVarMap(Map<String, Object> returnVarMap) {
    this.returnVarMap = returnVarMap;
  }
  
  public String getProcessInitiatorHeaderName() {
      return processInitiatorHeaderName;
  }
  
  public void setProcessInitiatorHeaderName(String processInitiatorHeaderName) {
      this.processInitiatorHeaderName = processInitiatorHeaderName;
  }
  
  @Override
  public boolean isLenientProperties() {
    return true;
  }
  
  public long getTimeout() {
    return timeout;
  }
  
  public int getTimeResolution() {
    return timeResolution;
  }

}
