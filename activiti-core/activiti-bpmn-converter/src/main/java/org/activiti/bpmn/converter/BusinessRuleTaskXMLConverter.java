/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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
package org.activiti.bpmn.converter;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.BusinessRuleTask;

public class BusinessRuleTaskXMLConverter extends BaseBpmnXMLConverter {

  public Class<? extends BaseElement> getBpmnElementType() {
    return BusinessRuleTask.class;
  }

  @Override
  protected String getXMLElementName() {
    return ELEMENT_TASK_BUSINESSRULE;
  }

  @Override
  protected BaseElement convertXMLToElement(XMLStreamReader xtr, BpmnModel model) throws Exception {
    BusinessRuleTask businessRuleTask = new BusinessRuleTask();
    BpmnXMLUtil.addXMLLocation(businessRuleTask, xtr);
    businessRuleTask.setInputVariables(parseDelimitedList(
      xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_RULE_VARIABLES_INPUT)));
    businessRuleTask.setRuleNames(parseDelimitedList(
      xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_RULE_RULES)));
    businessRuleTask.setResultVariableName(
      xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_RULE_RESULT_VARIABLE));
    businessRuleTask.setClassName(
      xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_RULE_CLASS));
    String exclude = xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE,
      ATTRIBUTE_TASK_RULE_EXCLUDE);
    if (ATTRIBUTE_VALUE_TRUE.equalsIgnoreCase(exclude)) {
      businessRuleTask.setExclude(true);
    }
    parseChildElements(getXMLElementName(), businessRuleTask, model, xtr);
    return businessRuleTask;
  }

  @Override
  protected void writeAdditionalAttributes(
    BaseElement element,
    BpmnModel model,
    XMLStreamWriter xtw) throws Exception {

    var businessRuleTask = (BusinessRuleTask) element;
    var inputVariables = convertToDelimitedString(businessRuleTask.getInputVariables());
    if (isNotEmpty(inputVariables)) {
      writeQualifiedAttribute(ATTRIBUTE_TASK_RULE_VARIABLES_INPUT, inputVariables, xtw);
    }
    var ruleNames = convertToDelimitedString(businessRuleTask.getRuleNames());
    if (isNotEmpty(ruleNames)) {
      writeQualifiedAttribute(ATTRIBUTE_TASK_RULE_RULES, ruleNames, xtw);
    }
    if (isNotEmpty(businessRuleTask.getResultVariableName())) {
      writeQualifiedAttribute(ATTRIBUTE_TASK_RULE_RESULT_VARIABLE,
        businessRuleTask.getResultVariableName(), xtw);
    }
    if (isNotEmpty(businessRuleTask.getClassName())) {
      writeQualifiedAttribute(ATTRIBUTE_TASK_RULE_CLASS, businessRuleTask.getClassName(), xtw);
    }
    if (businessRuleTask.isExclude()) {
      writeQualifiedAttribute(ATTRIBUTE_TASK_RULE_EXCLUDE, ATTRIBUTE_VALUE_TRUE, xtw);
    }
  }

  @Override
  protected void writeAdditionalChildElements(BaseElement element, BpmnModel model,
    XMLStreamWriter xtw) throws Exception {
  }
}
