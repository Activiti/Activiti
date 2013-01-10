package org.activiti.bpmn.converter.export;

import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.apache.commons.lang.StringUtils;

public class MultiInstanceExport implements BpmnXMLConstants {

  public static void writeMultiInstance(Activity activity, XMLStreamWriter xtw) throws Exception {
    if (activity.getLoopCharacteristics() != null) {
      MultiInstanceLoopCharacteristics multiInstanceObject = activity.getLoopCharacteristics();
      if (StringUtils.isNotEmpty(multiInstanceObject.getLoopCardinality()) ||
          StringUtils.isNotEmpty(multiInstanceObject.getInputDataItem()) ||
          StringUtils.isNotEmpty(multiInstanceObject.getCompletionCondition())) {
        
        xtw.writeStartElement(ELEMENT_MULTIINSTANCE);
        BpmnXMLUtil.writeDefaultAttribute(ATTRIBUTE_MULTIINSTANCE_SEQUENTIAL, String.valueOf(multiInstanceObject.isSequential()).toLowerCase(), xtw);
        if (StringUtils.isNotEmpty(multiInstanceObject.getInputDataItem())) {
          BpmnXMLUtil.writeQualifiedAttribute(ATTRIBUTE_MULTIINSTANCE_COLLECTION, multiInstanceObject.getInputDataItem(), xtw);
        }
        if (StringUtils.isNotEmpty(multiInstanceObject.getElementVariable())) {
          BpmnXMLUtil.writeQualifiedAttribute(ATTRIBUTE_MULTIINSTANCE_VARIABLE, multiInstanceObject.getElementVariable(), xtw);
        }
        if (StringUtils.isNotEmpty(multiInstanceObject.getLoopCardinality())) {
          xtw.writeStartElement(ELEMENT_MULTIINSTANCE_CARDINALITY);
          xtw.writeCharacters(multiInstanceObject.getLoopCardinality());
          xtw.writeEndElement();
        }
        if (StringUtils.isNotEmpty(multiInstanceObject.getCompletionCondition())) {
          xtw.writeStartElement(ELEMENT_MULTIINSTANCE_CONDITION);
          xtw.writeCharacters(multiInstanceObject.getCompletionCondition());
          xtw.writeEndElement();
        }
        xtw.writeEndElement();
      }
    }
  }
}
