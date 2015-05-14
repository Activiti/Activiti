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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.IOParameter;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class CallActivityJsonConverter extends BaseBpmnJsonConverter {

  public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
      Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    
    fillJsonTypes(convertersToBpmnMap);
    fillBpmnTypes(convertersToJsonMap);
  }
  
  public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
    convertersToBpmnMap.put(STENCIL_CALL_ACTIVITY, CallActivityJsonConverter.class);
  }
  
  public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    convertersToJsonMap.put(CallActivity.class, CallActivityJsonConverter.class);
  }
  
  protected String getStencilId(BaseElement baseElement) {
    return STENCIL_CALL_ACTIVITY;
  }
  
  protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
    CallActivity callActivity = (CallActivity) baseElement;
  	if (StringUtils.isNotEmpty(callActivity.getCalledElement())) {
  	  propertiesNode.put(PROPERTY_CALLACTIVITY_CALLEDELEMENT, callActivity.getCalledElement());
  	}
  	
  	addJsonParameters(PROPERTY_CALLACTIVITY_IN, "inParameters", callActivity.getInParameters(), propertiesNode);
  	addJsonParameters(PROPERTY_CALLACTIVITY_OUT, "outParameters", callActivity.getOutParameters(), propertiesNode);
  }
  
  private void addJsonParameters(String propertyName, String valueName, List<IOParameter> parameterList, ObjectNode propertiesNode) {
    ObjectNode parametersNode = objectMapper.createObjectNode();
    ArrayNode itemsNode = objectMapper.createArrayNode();
    for (IOParameter parameter : parameterList) {
      ObjectNode parameterItemNode = objectMapper.createObjectNode();
      if (StringUtils.isNotEmpty(parameter.getSource())) {
        parameterItemNode.put(PROPERTY_IOPARAMETER_SOURCE, parameter.getSource());
      } else {
        parameterItemNode.putNull(PROPERTY_IOPARAMETER_SOURCE);
      }
      if (StringUtils.isNotEmpty(parameter.getTarget())) {
        parameterItemNode.put(PROPERTY_IOPARAMETER_TARGET, parameter.getTarget());
      } else {
        parameterItemNode.putNull(PROPERTY_IOPARAMETER_TARGET);
      }
      if (StringUtils.isNotEmpty(parameter.getSourceExpression())) {
        parameterItemNode.put(PROPERTY_IOPARAMETER_SOURCE_EXPRESSION, parameter.getSourceExpression());
      } else {
        parameterItemNode.putNull(PROPERTY_IOPARAMETER_SOURCE_EXPRESSION);
      }
      
      itemsNode.add(parameterItemNode);
    }
    
    parametersNode.put(valueName, itemsNode);
    propertiesNode.put(propertyName, parametersNode);
  }
  
  protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
    CallActivity callActivity = new CallActivity();
    if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_CALLACTIVITY_CALLEDELEMENT, elementNode))) {
      callActivity.setCalledElement(getPropertyValueAsString(PROPERTY_CALLACTIVITY_CALLEDELEMENT, elementNode));
    } 
    
    callActivity.getInParameters().addAll(convertToIOParameters(PROPERTY_CALLACTIVITY_IN, "inParameters", elementNode));
    callActivity.getOutParameters().addAll(convertToIOParameters(PROPERTY_CALLACTIVITY_OUT, "outParameters", elementNode));
    
    return callActivity;
  }
  
  private List<IOParameter> convertToIOParameters(String propertyName, String valueName, JsonNode elementNode) {
    List<IOParameter> ioParameters = new ArrayList<IOParameter>();
    JsonNode parametersNode = getProperty(propertyName, elementNode);
    if (parametersNode != null) {
      parametersNode = BpmnJsonConverterUtil.validateIfNodeIsTextual(parametersNode);
      JsonNode itemsArrayNode = parametersNode.get(valueName);
      if (itemsArrayNode != null) {
        for (JsonNode itemNode : itemsArrayNode) {
          JsonNode sourceNode = itemNode.get(PROPERTY_IOPARAMETER_SOURCE);
          JsonNode sourceExpressionNode = itemNode.get(PROPERTY_IOPARAMETER_SOURCE_EXPRESSION);
          if ((sourceNode != null && StringUtils.isNotEmpty(sourceNode.asText())) ||
              (sourceExpressionNode != null && StringUtils.isNotEmpty(sourceExpressionNode.asText()))) {
            
            IOParameter parameter = new IOParameter();
            if (StringUtils.isNotEmpty(getValueAsString(PROPERTY_IOPARAMETER_SOURCE, itemNode))) {
              parameter.setSource(getValueAsString(PROPERTY_IOPARAMETER_SOURCE, itemNode));
            } else if (StringUtils.isNotEmpty(getValueAsString(PROPERTY_IOPARAMETER_SOURCE_EXPRESSION, itemNode))) {
              parameter.setSourceExpression(getValueAsString(PROPERTY_IOPARAMETER_SOURCE_EXPRESSION, itemNode));
            }
            if (StringUtils.isNotEmpty(getValueAsString(PROPERTY_IOPARAMETER_TARGET, itemNode))) {
              parameter.setTarget(getValueAsString(PROPERTY_IOPARAMETER_TARGET, itemNode));
            }
            ioParameters.add(parameter);
          }
        }
      }
    }
    return ioParameters;
  }
}
