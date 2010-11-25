package org.activiti.cycle.impl.transform.signavio;

/**
 * Temporary transformation to adjust BPMN 2.0 XML to the version Activiti
 * understands (there are some minor incompatibilities due to changes in the
 * final BPMN 2.0 spec. Should be temporary, that's why we did it very very
 * hacky.
 * 
 * @author ruecker
 */
public class RemedyTemporarySignavioIncompatibilityTransformation {

  public String transformBpmn20Xml(String xml, String processName) {
    // set process id and name
    processName = ExchangeSignavioUuidWithNameTransformation.adjustNamesForEngine(processName);
    xml = setAttributeText(xml, "process", "id", processName);
    if (!existAttribute(xml, "process", "name")) {
      xml = addAttribute(xml, "process", "name", processName);
    }
    
    return transformBpmn20Xml(xml);
  }

  public String transformBpmn20Xml(String xml) {
    // Change upper/lower case problem <exclusiveGateway gatewayDirection="Diverging"
    xml = exchangeAttributeText(xml, "gatewayDirection", "diverging", "Diverging");
    xml = exchangeAttributeText(xml, "gatewayDirection", "converging", "Converging");
    xml = exchangeAttributeText(xml, "gatewayDirection", "mixed", "Mixed");

    xml = removeAttribute(xml, "processType");

    // add namespace (yeah, pretty hacky, I know)
    xml = addAttribute(xml, "definitions", "xmlns:activiti", "http://activiti.org/bpmn");

    // remove BPMN-DI
    xml = removeElement(xml, "bpmndi:BPMNDiagram");
    
    // remove Signavio extensions
    xml = removeElement(xml, "signavio:signavioMetaData");

    // removed currently unused elements
    xml = removeElement(xml, "laneSet");
    xml = removeElement(xml, "messageEventDefinition");
    
    // removed unused default attributes
    xml = removeAttribute(xml, "resourceRef");
    xml = removeAttribute(xml, "completionQuantity");
    xml = removeAttribute(xml, "startQuantity");
    xml = removeAttribute(xml, "implementation");
    xml = removeAttribute(xml, "completionQuantity");
    xml = removeAttribute(xml, "startQuantity");
    xml = removeAttribute(xml, "isForCompensation");
    xml = removeAttribute(xml, "isInterrupting");
    xml = removeAttribute(xml, "isClosed");
        
    xml = removeAttribute(xml, "typeLanguage");
    xml = removeAttribute(xml, "expressionLanguage");
    xml = removeAttribute(xml, "xmlns:bpmndi");
   
    return xml;
  }

  // public static void main(String[] args) {
  // System.out.println(new
  // RemedyTemporarySignavioIncompatibilityTransformation().removeAttribute("<process test=\"hallo\" />",
  // "process", "test"));
  // System.out.println(new
  // RemedyTemporarySignavioIncompatibilityTransformation().setAttributeText("<process test=\"hallo\" />",
  // "process", "test", "bernd"));
  // System.out
  // .println(new
  // RemedyTemporarySignavioIncompatibilityTransformation().exchangeAttributeText("<process test=\"hallo\" />",
  // "test", "hallo", "bernd"));
  // System.out.println(new
  // RemedyTemporarySignavioIncompatibilityTransformation().addAttribute("<process test=\"hallo\" />",
  // "process", "new", "bernd"));
  // System.out.println(new
  // RemedyTemporarySignavioIncompatibilityTransformation().removeElement("<process><test /></process>",
  // "test"));
  // System.out.println(new
  // RemedyTemporarySignavioIncompatibilityTransformation().removeElement("<process><test><hallo /></test>></process>",
  // "test"));
  // }

  private String removeAttribute(String xml, String attributeName) {
    int startIndex = xml.indexOf(" " + attributeName + "=\"");
    while (startIndex != -1) {
      int endIndex = xml.indexOf("\"", startIndex + attributeName.length() + 3);
      xml = xml.substring(0, startIndex) + xml.substring(endIndex + 1);

      startIndex = xml.indexOf(" " + attributeName + "=\"");
    }
    return xml;
  }

  private String setAttributeText(String xml, String element, String attributeName, String newValue) {
    int elementStartIndex = xml.indexOf("<" + element);
    if (elementStartIndex == -1)
      return xml;
    int startIndex = xml.indexOf(attributeName + "=\"", elementStartIndex) + attributeName.length() + 2;
    if (startIndex == -1) {
      return xml;
    }
    int endIndex = xml.indexOf("\"", startIndex);
    
    return xml.substring(0, startIndex) + newValue + xml.substring(endIndex);
  }

  private boolean existAttribute(String xml, String elementName, String attributeName) {
    int elementStartIndex = xml.indexOf("<" + elementName);
    if (elementStartIndex == -1)
      return false;
    int elementEndIndex = xml.indexOf(">", elementStartIndex);
    xml = xml.substring(0, elementEndIndex);
    int startIndex = xml.indexOf(attributeName + "=\"", elementStartIndex);
    return (startIndex > -1);
  }

  private String exchangeAttributeText(String xml, String attributeName, String oldValue, String newValue) {
    return xml.replaceAll(attributeName + "=\"" + oldValue + "\"", attributeName + "=\"" + newValue + "\"");
  }

  private String addAttribute(String xml, String elementName, String attributeName, String value) {
    int index = xml.indexOf("<" + elementName + " ") + elementName.length() + 2;
    if (index == 0)
      return xml;
    return xml.substring(0, index) + attributeName + "=\"" + value + "\" " + xml.substring(index);
  }

  private String removeElement(String xml, String elementName) {
    int startIndex = xml.indexOf("<" + elementName);
    while (startIndex != -1) {
      int endIndex = xml.indexOf("</" + elementName + ">");
      if (endIndex != -1) {
        endIndex = endIndex + elementName.length() + 3;
      } else {
        endIndex = xml.indexOf("/>", startIndex) + 2;
      }

      String lastPartOfString = xml.substring(endIndex);
      xml = xml.substring(0, startIndex) + lastPartOfString;

      startIndex = xml.indexOf("<" + elementName);
    }
    return xml;
  }

}
