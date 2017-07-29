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

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.ExtensionAttribute;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ServiceTask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**


 */
public class DecisionTaskJsonConverter extends BaseBpmnJsonConverter implements DecisionTableAwareConverter {

  protected Map<String, String> decisionTableMap;

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
    serviceTask.setType(ServiceTask.DMN_TASK);
    
    JsonNode decisionTableReferenceNode = getProperty(PROPERTY_DECISIONTABLE_REFERENCE, elementNode);
    if (decisionTableReferenceNode != null && decisionTableReferenceNode.has("id") && !(decisionTableReferenceNode.get("id").isNull())) {

      String decisionTableId = decisionTableReferenceNode.get("id").asText();
      if (decisionTableMap != null) {
        String decisionTableKey = decisionTableMap.get(decisionTableId);

        FieldExtension decisionTableKeyField = new FieldExtension();
        decisionTableKeyField.setFieldName(PROPERTY_DECISIONTABLE_REFERENCE_KEY);
        decisionTableKeyField.setStringValue(decisionTableKey);
        serviceTask.getFieldExtensions().add(decisionTableKeyField);
      }
    }

    return serviceTask;
  }

  @Override
  protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {

  }
  
  @Override
  public void setDecisionTableMap(Map<String, String> decisionTableMap) {
    this.decisionTableMap = decisionTableMap;
  }

  protected void addExtensionAttributeToExtension(ExtensionElement element, String attributeName, String value) {
    ExtensionAttribute extensionAttribute = new ExtensionAttribute(NAMESPACE, attributeName);
    extensionAttribute.setNamespacePrefix("modeler");
    extensionAttribute.setValue(value);
    element.addAttribute(extensionAttribute);
  }

}
