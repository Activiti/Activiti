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
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.editor.language.json.model.ModelInfo;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class ServiceTaskJsonConverter extends BaseBpmnJsonConverter implements DecisionTableKeyAwareConverter {

  protected Map<String, ModelInfo> decisionTableKeyMap;

  public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap, Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {

    fillJsonTypes(convertersToBpmnMap);
    fillBpmnTypes(convertersToJsonMap);
  }

  public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
    convertersToBpmnMap.put(STENCIL_TASK_SERVICE, ServiceTaskJsonConverter.class);
  }

  public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    convertersToJsonMap.put(ServiceTask.class, ServiceTaskJsonConverter.class);
  }

  protected String getStencilId(BaseElement baseElement) {
    return STENCIL_TASK_SERVICE;
  }

  protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
    ServiceTask serviceTask = (ServiceTask) baseElement;

    if ("mail".equalsIgnoreCase(serviceTask.getType())) {
      setPropertyFieldValue(PROPERTY_MAILTASK_TO, serviceTask, propertiesNode);
      setPropertyFieldValue(PROPERTY_MAILTASK_FROM, serviceTask, propertiesNode);
      setPropertyFieldValue(PROPERTY_MAILTASK_SUBJECT, serviceTask, propertiesNode);
      setPropertyFieldValue(PROPERTY_MAILTASK_CC, serviceTask, propertiesNode);
      setPropertyFieldValue(PROPERTY_MAILTASK_BCC, serviceTask, propertiesNode);
      setPropertyFieldValue(PROPERTY_MAILTASK_TEXT, serviceTask, propertiesNode);
      setPropertyFieldValue(PROPERTY_MAILTASK_HTML, serviceTask, propertiesNode);
      setPropertyFieldValue(PROPERTY_MAILTASK_CHARSET, serviceTask, propertiesNode);

    } else if ("camel".equalsIgnoreCase(serviceTask.getType())) {
      setPropertyFieldValue(PROPERTY_CAMELTASK_CAMELCONTEXT, "camelContext", serviceTask, propertiesNode);

    } else if ("mule".equalsIgnoreCase(serviceTask.getType())) {
      setPropertyFieldValue(PROPERTY_MULETASK_ENDPOINT_URL, "endpointUrl", serviceTask, propertiesNode);
      setPropertyFieldValue(PROPERTY_MULETASK_LANGUAGE, "language", serviceTask, propertiesNode);
      setPropertyFieldValue(PROPERTY_MULETASK_PAYLOAD_EXPRESSION, "payloadExpression", serviceTask, propertiesNode);
      setPropertyFieldValue(PROPERTY_MULETASK_RESULT_VARIABLE, "resultVariable", serviceTask, propertiesNode);

    } else if ("dmn".equalsIgnoreCase(serviceTask.getType())) {
      for (FieldExtension fieldExtension : serviceTask.getFieldExtensions()) {
        if (PROPERTY_DECISIONTABLE_REFERENCE_KEY.equals(fieldExtension.getFieldName()) &&
            decisionTableKeyMap != null && decisionTableKeyMap.containsKey(fieldExtension.getStringValue())) {

          ObjectNode decisionReferenceNode = objectMapper.createObjectNode();
          propertiesNode.set(PROPERTY_DECISIONTABLE_REFERENCE, decisionReferenceNode);

          ModelInfo modelInfo = decisionTableKeyMap.get(fieldExtension.getStringValue());
          decisionReferenceNode.put("id", modelInfo.getId());
          decisionReferenceNode.put("name", modelInfo.getName());
          decisionReferenceNode.put("key", modelInfo.getKey());
        }
      }

    } else {

      if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(serviceTask.getImplementationType())) {
        propertiesNode.put(PROPERTY_SERVICETASK_CLASS, serviceTask.getImplementation());
      } else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equals(serviceTask.getImplementationType())) {
        propertiesNode.put(PROPERTY_SERVICETASK_EXPRESSION, serviceTask.getImplementation());
      } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(serviceTask.getImplementationType())) {
        propertiesNode.put(PROPERTY_SERVICETASK_DELEGATE_EXPRESSION, serviceTask.getImplementation());
      }

      if (StringUtils.isNotEmpty(serviceTask.getResultVariableName())) {
        propertiesNode.put(PROPERTY_SERVICETASK_RESULT_VARIABLE, serviceTask.getResultVariableName());
      }

      addFieldExtensions(serviceTask.getFieldExtensions(), propertiesNode);
    }
  }

  protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
    ServiceTask task = new ServiceTask();
    if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_SERVICETASK_CLASS, elementNode))) {
      task.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
      task.setImplementation(getPropertyValueAsString(PROPERTY_SERVICETASK_CLASS, elementNode));

    } else if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_SERVICETASK_EXPRESSION, elementNode))) {
      task.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION);
      task.setImplementation(getPropertyValueAsString(PROPERTY_SERVICETASK_EXPRESSION, elementNode));

    } else if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_SERVICETASK_DELEGATE_EXPRESSION, elementNode))) {
      task.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
      task.setImplementation(getPropertyValueAsString(PROPERTY_SERVICETASK_DELEGATE_EXPRESSION, elementNode));
    }

    if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_SERVICETASK_RESULT_VARIABLE, elementNode))) {
      task.setResultVariableName(getPropertyValueAsString(PROPERTY_SERVICETASK_RESULT_VARIABLE, elementNode));
    }

    JsonNode fieldsNode = getProperty(PROPERTY_SERVICETASK_FIELDS, elementNode);
    if (fieldsNode != null) {
      JsonNode itemsArrayNode = fieldsNode.get("fields");
      if (itemsArrayNode != null) {
        for (JsonNode itemNode : itemsArrayNode) {
          JsonNode nameNode = itemNode.get(PROPERTY_SERVICETASK_FIELD_NAME);
          if (nameNode != null && StringUtils.isNotEmpty(nameNode.asText())) {

            FieldExtension field = new FieldExtension();
            field.setFieldName(nameNode.asText());
            if (StringUtils.isNotEmpty(getValueAsString(PROPERTY_SERVICETASK_FIELD_STRING_VALUE, itemNode))) {
              field.setStringValue(getValueAsString(PROPERTY_SERVICETASK_FIELD_STRING_VALUE, itemNode));
            } else if (StringUtils.isNotEmpty(getValueAsString(PROPERTY_SERVICETASK_FIELD_STRING, itemNode))) {
              field.setStringValue(getValueAsString(PROPERTY_SERVICETASK_FIELD_STRING, itemNode));
            } else if (StringUtils.isNotEmpty(getValueAsString(PROPERTY_SERVICETASK_FIELD_EXPRESSION, itemNode))) {
              field.setExpression(getValueAsString(PROPERTY_SERVICETASK_FIELD_EXPRESSION, itemNode));
            }
            task.getFieldExtensions().add(field);
          }
        }
      }
    }

    return task;
  }

  protected void setPropertyFieldValue(String name, ServiceTask task, ObjectNode propertiesNode) {
    for (FieldExtension extension : task.getFieldExtensions()) {
      if (name.substring(8).equalsIgnoreCase(extension.getFieldName())) {
        if (StringUtils.isNotEmpty(extension.getStringValue())) {
          setPropertyValue(name, extension.getStringValue(), propertiesNode);
        } else if (StringUtils.isNotEmpty(extension.getExpression())) {
          setPropertyValue(name, extension.getExpression(), propertiesNode);
        }
      }
    }
  }

  protected void setPropertyFieldValue(String propertyName, String fieldName, ServiceTask task, ObjectNode propertiesNode) {
    for (FieldExtension extension : task.getFieldExtensions()) {
      if (fieldName.equalsIgnoreCase(extension.getFieldName())) {
        if (StringUtils.isNotEmpty(extension.getStringValue())) {
          setPropertyValue(propertyName, extension.getStringValue(), propertiesNode);
        } else if (StringUtils.isNotEmpty(extension.getExpression())) {
          setPropertyValue(propertyName, extension.getExpression(), propertiesNode);
        }
      }
    }
  }

  @Override
  public void setDecisionTableKeyMap(Map<String, ModelInfo> decisionTableKeyMap) {
    this.decisionTableKeyMap = decisionTableKeyMap;
  }
}
