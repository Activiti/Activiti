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

    // remobve processType="none"
    xml = xml.replaceAll("processType=\"none\"", "");
    xml = xml.replaceAll("processType=\"private\"", "");
    xml = xml.replaceAll("processType=\"public\"", "");
    xml = xml.replaceAll("processType=\"non-executable\"", "");
    xml = xml.replaceAll("processType=\"executable\"", "");

    // add namespace (yeah, pretty hacky, I know)
    xml = xml.replaceAll("<definitions ", "<definitions xmlns:activiti=\"http://activiti.org/bpmn\" ");

    // remove old BPMN-DI (yeah, even more hacky ;-))
    xml = xml.substring(0, xml.indexOf("<bpmndi:processDiagram") - 1) + xml.substring(xml.indexOf("</bpmndi:processDiagram>") + 25);
    
    return xml;
  }
}
