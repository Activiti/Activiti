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
package org.activiti.editor.language.json.converter;

import java.util.Map;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.ExtensionAttribute;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.ServiceTask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Erik Winlof
 * @author Yvo Swillens
 */
public class DecisionTaskJsonConverter extends BaseBpmnJsonConverter implements DecisionTableAwareConverter {

  protected static final String EXECUTE_DECISION_DELEGATE_EXPRESSION = "${activiti_executeDecisionDelegate}";

  protected Map<Long, JsonNode> decisionTableMap;

  public static boolean isExecuteDecisionServiceTask(Activity activity) {

    if (activity instanceof ServiceTask == false) {
      return false;
    }

    ServiceTask serviceTask = (ServiceTask) activity;
    return ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(serviceTask.getImplementationType())
        && EXECUTE_DECISION_DELEGATE_EXPRESSION.equals(serviceTask.getImplementation());
  }

  public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
      Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {

    fillJsonTypes(convertersToBpmnMap);
    fillBpmnTypes(convertersToJsonMap);
  }

  public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
    convertersToBpmnMap.put(STENCIL_TASK_DECISION, DecisionTaskJsonConverter.class);
  }

  public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
  }

  protected String getStencilId(BaseElement baseElement) {
    return STENCIL_TASK_DECISION;
  }

  protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {

    ServiceTask serviceTask = new ServiceTask();
    serviceTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
    serviceTask.setImplementation(EXECUTE_DECISION_DELEGATE_EXPRESSION);
    
    JsonNode decisionTableReferenceNode = getProperty(PROPERTY_DECISIONTABLE_REFERENCE, elementNode);
    if (decisionTableReferenceNode != null && decisionTableReferenceNode.isNull() == false) {

      ExtensionElement extensionElement = new ExtensionElement();
      extensionElement.setNamespace(NAMESPACE);
      extensionElement.setNamespacePrefix("modeler");
      extensionElement.setName("decisiontable-reference");

      if (decisionTableReferenceNode.has("id") && decisionTableReferenceNode.isNull() == false) {

        addExtensionAttributeToExtension(extensionElement, PROPERTY_DECISIONTABLE_REFERENCE_ID,
            decisionTableReferenceNode.get("id").asText());

        Long decisionTableId = decisionTableReferenceNode.get("id").asLong();
        if (decisionTableMap != null) {
          JsonNode decisionTableJsonNode = decisionTableMap.get(decisionTableId);

          if (decisionTableJsonNode != null && decisionTableJsonNode.has("key") && decisionTableJsonNode.get("key").isNull() == false) {

            FieldExtension decisionTableKeyField = new FieldExtension();
            decisionTableKeyField.setFieldName(PROPERTY_DECISIONTABLE_REFERENCE_KEY);
            decisionTableKeyField.setStringValue(decisionTableJsonNode.get("key").asText());
            serviceTask.getFieldExtensions().add(decisionTableKeyField);
          }
        }
      }

      if (decisionTableReferenceNode.has("name") && decisionTableReferenceNode.get("name").isNull() == false) {
        addExtensionAttributeToExtension(extensionElement, PROPERTY_DECISIONTABLE_REFERENCE_NAME,
            decisionTableReferenceNode.get("name").asText());
      }

      serviceTask.addExtensionElement(extensionElement);
    }

    return serviceTask;
  }

  @Override
  protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {

  }
  
  @Override
  public void setDecisionTableMap(Map<Long, JsonNode> decisionTableMap) {
    this.decisionTableMap = decisionTableMap;
  }

  protected void addExtensionAttributeToExtension(ExtensionElement element, String attributeName, String value) {
    ExtensionAttribute extensionAttribute = new ExtensionAttribute(NAMESPACE, attributeName);
    extensionAttribute.setNamespacePrefix("modeler");
    extensionAttribute.setValue(value);
    element.addAttribute(extensionAttribute);
  }

}
