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
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.InclusiveGateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**

 */
public class InclusiveGatewayJsonConverter extends BaseBpmnJsonConverter {

  public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap, Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {

    fillJsonTypes(convertersToBpmnMap);
    fillBpmnTypes(convertersToJsonMap);
  }

  public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
    convertersToBpmnMap.put(STENCIL_GATEWAY_INCLUSIVE, InclusiveGatewayJsonConverter.class);
  }

  public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    convertersToJsonMap.put(InclusiveGateway.class, InclusiveGatewayJsonConverter.class);
  }

  protected String getStencilId(BaseElement baseElement) {
    return STENCIL_GATEWAY_INCLUSIVE;
  }

  protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
  }

  protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
    InclusiveGateway gateway = new InclusiveGateway();
    return gateway;
  }
}
