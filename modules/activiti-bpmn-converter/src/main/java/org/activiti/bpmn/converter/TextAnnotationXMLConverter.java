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
package org.activiti.bpmn.converter;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.converter.child.TextAnnotationTextParser;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.TextAnnotation;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Tijs Rademakers
 */
public class TextAnnotationXMLConverter extends BaseBpmnXMLConverter {
  
  public TextAnnotationXMLConverter() {
    TextAnnotationTextParser annotationTextParser = new TextAnnotationTextParser();
    childElementParsers.put(annotationTextParser.getElementName(), annotationTextParser);
  }
  
	public static String getXMLType() {
    return ELEMENT_TEXT_ANNOTATION;
  }
  
  public static Class<? extends BaseElement> getBpmnElementType() {
    return TextAnnotation.class;
  }
  
  @Override
  protected String getXMLElementName() {
    return ELEMENT_TEXT_ANNOTATION;
  }
  
  @Override
  protected BaseElement convertXMLToElement(XMLStreamReader xtr) throws Exception {
    TextAnnotation textAnnotation = new TextAnnotation();
    BpmnXMLUtil.addXMLLocation(textAnnotation, xtr);
    textAnnotation.setTextFormat(xtr.getAttributeValue(null, ATTRIBUTE_TEXTFORMAT));
    parseChildElements(getXMLElementName(), textAnnotation, xtr);
    return textAnnotation;
  }

  @Override
  protected void writeAdditionalAttributes(BaseElement element, XMLStreamWriter xtw) throws Exception {
    TextAnnotation textAnnotation = (TextAnnotation) element;
    writeDefaultAttribute(ATTRIBUTE_TEXTFORMAT, textAnnotation.getTextFormat(), xtw);
  }
  
  @Override
  protected void writeExtensionChildElements(BaseElement element, XMLStreamWriter xtw) throws Exception {
  }

  @Override
  protected void writeAdditionalChildElements(BaseElement element, XMLStreamWriter xtw) throws Exception {
    TextAnnotation textAnnotation = (TextAnnotation) element;
    if (StringUtils.isNotEmpty(textAnnotation.getText())) {
      xtw.writeStartElement(ELEMENT_TEXT_ANNOTATION_TEXT);
      xtw.writeCharacters(textAnnotation.getText());
      xtw.writeEndElement();
    }
  }
}
