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
package org.activiti.bpmn.converter;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.converter.child.BaseChildElementParser;
import org.activiti.bpmn.converter.child.TextAnnotationTextParser;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.TextAnnotation;
import org.apache.commons.lang3.StringUtils;


public class TextAnnotationXMLConverter extends BaseBpmnXMLConverter {

  protected Map<String, BaseChildElementParser> childParserMap = new HashMap<String, BaseChildElementParser>();

  public TextAnnotationXMLConverter() {
    TextAnnotationTextParser annotationTextParser = new TextAnnotationTextParser();
    childParserMap.put(annotationTextParser.getElementName(), annotationTextParser);
  }

  public Class<? extends BaseElement> getBpmnElementType() {
    return TextAnnotation.class;
  }

  @Override
  protected String getXMLElementName() {
    return ELEMENT_TEXT_ANNOTATION;
  }

  @Override
  protected BaseElement convertXMLToElement(XMLStreamReader xtr, BpmnModel model) throws Exception {
    TextAnnotation textAnnotation = new TextAnnotation();
    BpmnXMLUtil.addXMLLocation(textAnnotation, xtr);
    textAnnotation.setTextFormat(xtr.getAttributeValue(null, ATTRIBUTE_TEXTFORMAT));
    parseChildElements(getXMLElementName(), textAnnotation, childParserMap, model, xtr);
    return textAnnotation;
  }

  @Override
  protected void writeAdditionalAttributes(BaseElement element, BpmnModel model, XMLStreamWriter xtw) throws Exception {
    TextAnnotation textAnnotation = (TextAnnotation) element;
    writeDefaultAttribute(ATTRIBUTE_TEXTFORMAT, textAnnotation.getTextFormat(), xtw);
  }

  @Override
  protected void writeAdditionalChildElements(BaseElement element, BpmnModel model, XMLStreamWriter xtw) throws Exception {
    TextAnnotation textAnnotation = (TextAnnotation) element;
    if (StringUtils.isNotEmpty(textAnnotation.getText())) {
      xtw.writeStartElement(BPMN2_PREFIX, ELEMENT_TEXT_ANNOTATION_TEXT, BPMN2_NAMESPACE);
      xtw.writeCharacters(textAnnotation.getText());
      xtw.writeEndElement();
    }
  }
}
