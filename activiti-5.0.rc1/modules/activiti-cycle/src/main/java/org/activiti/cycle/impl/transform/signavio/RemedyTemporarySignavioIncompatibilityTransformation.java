package org.activiti.cycle.impl.transform.signavio;

/**
 * Temporary transformation to adjust BPMN 2.0 XML to the version Activiti
 * understands (there are some minor incompatibilities due to changes in the
 * final BPMN 2.0 spec. Should be temporary, that's why we did it very hacky.
 * 
 * @author ruecker
 */
public class RemedyTemporarySignavioIncompatibilityTransformation {

  public String transformBpmn20Xml(String xml) {
    
    // remove "resourceRef"
    xml = xml.replaceAll("resourceRef=\"\"", "");
    
    // Change upper/lower case problem <exclusiveGateway gatewayDirection="Diverging"
    xml = xml.replaceAll("gatewayDirection=\"diverging\"", "gatewayDirection=\"Diverging\"");
    
    return xml;
  }
}
