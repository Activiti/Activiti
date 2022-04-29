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
import org.activiti.bpmn.model.TextAnnotation;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class TextAnnotationJsonConverter extends BaseBpmnJsonConverter {

  public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap, Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {

    fillJsonTypes(convertersToBpmnMap);
    fillBpmnTypes(convertersToJsonMap);
  }

  public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
    convertersToBpmnMap.put(STENCIL_TEXT_ANNOTATION, TextAnnotationJsonConverter.class);
  }

  public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    convertersToJsonMap.put(TextAnnotation.class, TextAnnotationJsonConverter.class);
  }

  protected String getStencilId(BaseElement baseElement) {
    return STENCIL_TEXT_ANNOTATION;
  }

  protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
    TextAnnotation annotation = (TextAnnotation) baseElement;
    if (StringUtils.isNotEmpty(annotation.getText())) {
      setPropertyValue("text", annotation.getText(), propertiesNode);
    }
  }

  protected BaseElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
    TextAnnotation annotation = new TextAnnotation();
    String text = getPropertyValueAsString("text", elementNode);
    if (StringUtils.isNotEmpty(text)) {
      annotation.setText(text);
    }
    return annotation;
  }
}
