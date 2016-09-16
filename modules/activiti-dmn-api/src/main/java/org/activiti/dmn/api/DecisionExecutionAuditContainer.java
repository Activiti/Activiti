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
package org.activiti.dmn.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.dmn.model.HitPolicy;
import org.joda.time.DateTime;

/**
 * @author Yvo Swillens
 * @author Erik Winlof
 */
public class DecisionExecutionAuditContainer {

  protected String decisionKey;
  protected String decisionName;
  protected String hitPolicy;
  protected String dmnDeploymentId;
  protected Date startTime;
  protected Date endTime;
  protected Map<String, Object> inputVariables;
  protected Map<String, String> inputVariableTypes;
  protected Map<String, Object> outputVariables;
  protected List<RuleExecutionAuditContainer> ruleExecutions = new ArrayList<RuleExecutionAuditContainer>();
  protected Boolean failed = Boolean.FALSE;
  protected String exceptionMessage;

  public DecisionExecutionAuditContainer() {

  }

  public DecisionExecutionAuditContainer(String decisionKey, String decisionName, HitPolicy hitPolicy, Map<String, Object> inputVariables) {
    startTime = new Date();
    this.decisionKey = decisionKey;
    this.decisionName = decisionName;
    this.hitPolicy = hitPolicy.name();

    this.inputVariableTypes = getVariablesTypeMap(inputVariables);

    // create defensive copy of input variables
    this.inputVariables = createDefensiveCopyInputVariables(inputVariables);
  }

  protected Map<String, String> getVariablesTypeMap(Map<String, Object> variableValuesMap) {
    Map<String, String> variablesTypesMap = new HashMap<String, String>();
    for (String name : variableValuesMap.keySet()) {
      Object value = variableValuesMap.get(name);
      String type = null;

      if (value != null) {
        if (isString(value)) {
          type = "string";
        } else if (isDate(value)) {
          type = "date";
        } else if (isNumber(value)) {
          type = "number";
        } else if (isBoolean(value)) {
          type = "boolean";
        }
      }

      variablesTypesMap.put(name, type);
    }
    return variablesTypesMap;
  }

  public void stopAudit(Map<String, Object> result) {
    endTime = new Date();
    outputVariables = result;
  }

  public void addRuleEntry() {
    ruleExecutions.add(new RuleExecutionAuditContainer());
  }

  public void markRuleEnd(int ruleRowIndex) {
    ruleExecutions.get(ruleRowIndex).markRuleEnd();
  }

  public void addInputEntry(int ruleRowIndex, String inputEntryId, Boolean executionResult) {
    ruleExecutions.get(ruleRowIndex).addConditionResult(new ExpressionExecution(inputEntryId, executionResult));
  }

  public void addInputEntry(int ruleRowIndex, String inputEntryId, String exceptionMessage, Boolean executionResult) {
    ruleExecutions.get(ruleRowIndex).addConditionResult(new ExpressionExecution(inputEntryId, exceptionMessage, executionResult));
  }

  public void addOutputEntry(int ruleRowIndex, String outputEntryId, Object executionResult) {
    ruleExecutions.get(ruleRowIndex).addConclusionResult(new ExpressionExecution(outputEntryId, executionResult));
  }

  public void addOutputEntry(int ruleRowIndex, String outputEntryId, String exceptionMessage, Object executionResult) {
    ruleExecutions.get(ruleRowIndex).addConclusionResult(new ExpressionExecution(outputEntryId, exceptionMessage, executionResult));
  }

  public String getDecisionKey() {
    return decisionKey;
  }

  public String getDecisionName() {
    return decisionName;
  }

  public String getHitPolicy() {
    return hitPolicy;
  }

  public Date getStartTime() {
    return startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public Map<String, Object> getInputVariables() {
    return inputVariables;
  }

  public Map<String, Object> getOutputVariables() {
    return outputVariables;
  }

  public List<RuleExecutionAuditContainer> getRuleExecutions() {
    return ruleExecutions;
  }

  public String getDmnDeploymentId() {
    return dmnDeploymentId;
  }

  public void setDmnDeploymentId(String dmnDeploymentId) {
    this.dmnDeploymentId = dmnDeploymentId;
  }

  public Boolean isFailed() {
    return failed;
  }

  public void setFailed() {
    this.failed = Boolean.TRUE;
  }

  public String getExceptionMessage() {
    return exceptionMessage;
  }

  public void setExceptionMessage(String exceptionMessage) {
    this.exceptionMessage = exceptionMessage;
  }

  public Map<String, String> getInputVariableTypes() {
    return inputVariableTypes;
  }

  public void setInputVariableTypes(Map<String, String> inputVariableTypes) {
    this.inputVariableTypes = inputVariableTypes;
  }

  public static boolean isBoolean(Object obj) {
    return obj instanceof Boolean;
  }

  public static boolean isDate(Object obj) {
    return (obj instanceof Date || obj instanceof DateTime);
  }

  public static boolean isString(Object obj) {
    return obj instanceof String;
  }

  public static boolean isNumber(Object obj) {
    return obj instanceof Number;
  }

  protected Map<String, Object> createDefensiveCopyInputVariables(Map<String, Object> inputVariables) {

    Map<String, Object> defensiveCopyMap = new HashMap<String, Object>();

    if (inputVariables != null) {

      for (Map.Entry<String, Object> entry : inputVariables.entrySet()) {

        Object newValue = null;
        if (entry.getValue() == null) {
          // do nothing
        } else if (entry.getValue() instanceof Long) {
          newValue = new Long(((Long) entry.getValue()).longValue());
        } else if (entry.getValue() instanceof Double) {
          newValue = new Double(((Double) entry.getValue()).doubleValue());
        } else if (entry.getValue() instanceof Integer) {
          newValue = new Integer(((Integer) entry.getValue()).intValue());
        } else if (entry.getValue() instanceof Date) {
          newValue = new Date(((Date) entry.getValue()).getTime());
        } else if (entry.getValue() instanceof Boolean) {
          newValue = new Boolean(((Boolean) entry.getValue()).booleanValue());
        } else {
          newValue = new String(entry.getValue().toString());
        }
        defensiveCopyMap.put(entry.getKey(), newValue);
      }
    }
    return defensiveCopyMap;
  }
}
