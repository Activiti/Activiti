package org.activiti.bpmn.converter.util;

import java.util.List;

import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.FieldExtension;
import org.apache.commons.lang.StringUtils;

public class FieldExtensionUtil implements BpmnXMLConstants {

  public static boolean writeFieldExtensions(List<FieldExtension> fieldExtensionList, 
      boolean didWriteExtensionStartElement, XMLStreamWriter xtw) throws Exception {
    
    for (FieldExtension fieldExtension : fieldExtensionList) {
      
      if (StringUtils.isNotEmpty(fieldExtension.getFieldName())) {
        
        if (StringUtils.isNotEmpty(fieldExtension.getStringValue()) || StringUtils.isNotEmpty(fieldExtension.getExpression())) {
          
          if (didWriteExtensionStartElement == false) { 
            xtw.writeStartElement(ELEMENT_EXTENSIONS);
            didWriteExtensionStartElement = true;
          }
          
          xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, ELEMENT_FIELD, ACTIVITI_EXTENSIONS_NAMESPACE);
          BpmnXMLUtil.writeDefaultAttribute(ATTRIBUTE_FIELD_NAME, fieldExtension.getFieldName(), xtw);
          
          if (StringUtils.isNotEmpty(fieldExtension.getStringValue())) {
            xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, ELEMENT_FIELD_STRING, ACTIVITI_EXTENSIONS_NAMESPACE);
            xtw.writeCharacters(fieldExtension.getStringValue());
          } else {
            xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, ATTRIBUTE_FIELD_EXPRESSION, ACTIVITI_EXTENSIONS_NAMESPACE);
            xtw.writeCharacters(fieldExtension.getExpression());
          }
          xtw.writeEndElement();
          xtw.writeEndElement(); 
        }
      }
    }
    return didWriteExtensionStartElement;
  }
}
