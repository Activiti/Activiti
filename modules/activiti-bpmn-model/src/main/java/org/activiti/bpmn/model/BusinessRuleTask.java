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
package org.activiti.bpmn.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tijs Rademakers
 */
public class BusinessRuleTask extends Task {

  protected String resultVariableName;
  protected boolean exclude;
  protected List<String> ruleNames = new ArrayList<String>();
  protected List<String> inputVariables = new ArrayList<String>();
  protected String className;

  public boolean isExclude() {
    return exclude;
  }
  public void setExclude(boolean exclude) {
    this.exclude = exclude;
  }
  public String getResultVariableName() {
    return resultVariableName;
  }
  public void setResultVariableName(String resultVariableName) {
    this.resultVariableName = resultVariableName;
  }
  public List<String> getRuleNames() {
    return ruleNames;
  }
  public void setRuleNames(List<String> ruleNames) {
    this.ruleNames = ruleNames;
  }
  public List<String> getInputVariables() {
    return inputVariables;
  }
  public void setInputVariables(List<String> inputVariables) {
    this.inputVariables = inputVariables;
  }
  public String getClassName() {
    return className;
  }
  public void setClassName(String className) {
    this.className = className;
  }
  
  public BusinessRuleTask clone() {
    BusinessRuleTask clone = new BusinessRuleTask();
    clone.setValues(this);
    return clone;
  }
  
  public void setValues(BusinessRuleTask otherElement) {
    super.setValues(otherElement);
    setResultVariableName(otherElement.getResultVariableName());
    setExclude(otherElement.isExclude());
    setClassName(otherElement.getClassName());
    ruleNames = new ArrayList<String>(otherElement.getRuleNames());
    inputVariables = new ArrayList<String>(otherElement.getInputVariables());
  }
}
