/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.editor.language.json.converter;

import java.util.Map;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BusinessRuleTask;
import org.activiti.bpmn.model.FlowElement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class BusinessRuleTaskJsonConverter extends BaseBpmnJsonConverter {

  public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap, Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {

    fillJsonTypes(convertersToBpmnMap);
    fillBpmnTypes(convertersToJsonMap);
  }

  public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
    convertersToBpmnMap.put(STENCIL_TASK_BUSINESS_RULE, BusinessRuleTaskJsonConverter.class);
  }

  public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    convertersToJsonMap.put(BusinessRuleTask.class, BusinessRuleTaskJsonConverter.class);
  }

  protected String getStencilId(BaseElement baseElement) {
    return STENCIL_TASK_BUSINESS_RULE;
  }

  protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
    BusinessRuleTask ruleTask = (BusinessRuleTask) baseElement;
    propertiesNode.put(PROPERTY_RULETASK_CLASS, ruleTask.getClassName());
    propertiesNode.put(PROPERTY_RULETASK_VARIABLES_INPUT, convertListToCommaSeparatedString(ruleTask.getInputVariables()));
    propertiesNode.put(PROPERTY_RULETASK_RESULT, ruleTask.getResultVariableName());
    propertiesNode.put(PROPERTY_RULETASK_RULES, convertListToCommaSeparatedString(ruleTask.getRuleNames()));
    if (ruleTask.isExclude()) {
      propertiesNode.put(PROPERTY_RULETASK_EXCLUDE, PROPERTY_VALUE_YES);
    }
  }

  protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
    BusinessRuleTask task = new BusinessRuleTask();
    task.setClassName(getPropertyValueAsString(PROPERTY_RULETASK_CLASS, elementNode));
    task.setInputVariables(getPropertyValueAsList(PROPERTY_RULETASK_VARIABLES_INPUT, elementNode));
    task.setResultVariableName(getPropertyValueAsString(PROPERTY_RULETASK_RESULT, elementNode));
    task.setRuleNames(getPropertyValueAsList(PROPERTY_RULETASK_RULES, elementNode));
    task.setExclude(getPropertyValueAsBoolean(PROPERTY_RULETASK_EXCLUDE, elementNode));
    return task;
  }
}
