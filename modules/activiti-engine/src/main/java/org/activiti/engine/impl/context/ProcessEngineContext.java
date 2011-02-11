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
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.activiti.engine.impl.variable.VariableTypes;

import java.util.Map;


/**
 * @author Tom Baeyens
 */
public class ProcessEngineContext {

  protected int historyLevel;
  protected Map<Object, Object> beans;
  protected VariableTypes variableTypes;
  protected ScriptingEngines scriptingEngines;
  protected String mailServerDefaultFrom;
  protected String mailServerHost;
  protected int mailServerPort;
  protected String mailServerUsername;
  protected String mailServerPassword;
  protected boolean isDbIdentityUsed = true;
  protected boolean isDbHistoryUsed = true;
  protected boolean isDbCycleUsed = true;

  public ProcessEngineContext(ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.beans = processEngineConfiguration.getBeans();
    this.variableTypes = processEngineConfiguration.getVariableTypes();
    this.mailServerDefaultFrom = processEngineConfiguration.getMailServerDefaultFrom();
    this.mailServerHost = processEngineConfiguration.getMailServerHost();
    this.mailServerPort = processEngineConfiguration.getMailServerPort();
    this.mailServerUsername = processEngineConfiguration.getMailServerUsername();
    this.mailServerPassword = processEngineConfiguration.getMailServerPassword();
    this.historyLevel = processEngineConfiguration.getHistoryLevel();
    this.isDbIdentityUsed = processEngineConfiguration.isDbIdentityUsed();
    this.isDbHistoryUsed = processEngineConfiguration.isDbHistoryUsed();
    this.isDbCycleUsed = processEngineConfiguration.isDbCycleUsed();
  }

  public Map<Object, Object> getBeans() {
    return beans;
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

  public int getHistoryLevel() {
    return historyLevel;
  }

  public void setHistoryLevel(int historyLevel) {
    this.historyLevel = historyLevel;
  }

  public boolean isDbIdentityUsed() {
    return isDbIdentityUsed;
  }

  public boolean isDbHistoryUsed() {
    return isDbHistoryUsed;
  }

  public boolean isDbCycleUsed() {
    return isDbCycleUsed;
  }
}
