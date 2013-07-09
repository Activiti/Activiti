package org.activiti.bpmn.converter.child;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.Assignment;
import org.activiti.bpmn.model.DataAssociation;
import org.apache.commons.lang3.StringUtils;

public class DataAssociationParser implements BpmnXMLConstants {
  
  protected static final Logger LOGGER = Logger.getLogger(DataAssociationParser.class.getName());

  public static void parseDataAssociation(DataAssociation dataAssociation, String elementName, XMLStreamReader xtr) {
    boolean readyWithDataAssociation = false;
    Assignment assignment = null;
    try {
      while (readyWithDataAssociation == false && xtr.hasNext()) {
        xtr.next();
        if (xtr.isStartElement() && ELEMENT_SOURCE_REF.equals(xtr.getLocalName())) {
          String sourceRef = xtr.getElementText();
          if (StringUtils.isNotEmpty(sourceRef)) {
            dataAssociation.setSourceRef(sourceRef.trim());
          }

        } else if (xtr.isStartElement() && ELEMENT_TARGET_REF.equals(xtr.getLocalName())) {
          String targetRef = xtr.getElementText();
          if (StringUtils.isNotEmpty(targetRef)) {
            dataAssociation.setTargetRef(targetRef.trim());
          }
          
        } else if (xtr.isStartElement() && ELEMENT_TRANSFORMATION.equals(xtr.getLocalName())) {
          String transformation = xtr.getElementText();
          if (StringUtils.isNotEmpty(transformation)) {
            dataAssociation.setTransformation(transformation.trim());
          }
          
        } else if (xtr.isStartElement() && ELEMENT_ASSIGNMENT.equals(xtr.getLocalName())) {
          assignment = new Assignment();
          BpmnXMLUtil.addXMLLocation(assignment, xtr);
          
        } else if (xtr.isStartElement() && ELEMENT_FROM.equals(xtr.getLocalName())) {
          String from = xtr.getElementText();
          if (assignment != null && StringUtils.isNotEmpty(from)) {
            assignment.setFrom(from.trim());
          }
          
        } else if (xtr.isStartElement() && ELEMENT_TO.equals(xtr.getLocalName())) {
          String to = xtr.getElementText();
          if (assignment != null && StringUtils.isNotEmpty(to)) {
            assignment.setTo(to.trim());
          }
          
        } else if (xtr.isEndElement() && ELEMENT_ASSIGNMENT.equals(xtr.getLocalName())) {
          if (StringUtils.isNotEmpty(assignment.getFrom()) && StringUtils.isNotEmpty(assignment.getTo())) {
            dataAssociation.getAssignments().add(assignment);
          }
          
        } else if (xtr.isEndElement() && elementName.equals(xtr.getLocalName())) {
          readyWithDataAssociation = true;
        }
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Error parsing data association child elements", e);
    }
  }
}
