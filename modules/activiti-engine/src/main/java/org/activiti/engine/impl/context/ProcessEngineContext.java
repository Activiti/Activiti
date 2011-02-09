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

package org.activiti.engine.impl.context;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.variable.VariableTypes;

import java.util.Map;


/**
 * @author Tom Baeyens
 */
public class ProcessEngineContext {

  protected Map<Object, Object> processEngineContextObjects;
  protected VariableTypes variableTypes;
  protected String mailServerDefaultFrom;
  protected String mailServerHost;
  protected int mailServerPort;
  protected String mailServerUsername;
  protected String mailServerPassword;

  public ProcessEngineContext(ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.processEngineContextObjects = processEngineConfiguration.getProcessEngineObjects();
    this.variableTypes = processEngineConfiguration.getVariableTypes();
    this.mailServerDefaultFrom = processEngineConfiguration.getMailServerDefaultFrom();
    this.mailServerHost = processEngineConfiguration.getMailServerHost();
    this.mailServerPort = processEngineConfiguration.getMailServerPort();
    this.mailServerUsername = processEngineConfiguration.getMailServerUsername();
    this.mailServerPassword = processEngineConfiguration.getMailServerPassword();
  }

  public Map<Object, Object> getProcessEngineContextObjects() {
    return processEngineContextObjects;
  }

  public VariableTypes getVariableTypes() {
    return variableTypes;
  }

  public String getMailServerDefaultFrom() {
    return mailServerDefaultFrom;
  }

  public String getMailServerHost() {
    return mailServerHost;
  }

  public String getMailServerUsername() {
    return mailServerUsername;
  }

  public String getMailServerPassword() {
    return mailServerPassword;
  }

  public int getMailServerPort() {
    return mailServerPort;
  }
}
