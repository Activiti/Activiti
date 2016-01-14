package org.activiti.bpmn.converter.util;

import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.converter.child.ActivitiEventListenerParser;
import org.activiti.bpmn.converter.child.ActivitiFailedjobRetryParser;
import org.activiti.bpmn.converter.child.ActivitiMapExceptionParser;
import org.activiti.bpmn.converter.child.BaseChildElementParser;
import org.activiti.bpmn.converter.child.CancelEventDefinitionParser;
import org.activiti.bpmn.converter.child.CompensateEventDefinitionParser;
import org.activiti.bpmn.converter.child.ConditionExpressionParser;
import org.activiti.bpmn.converter.child.DataInputAssociationParser;
import org.activiti.bpmn.converter.child.DataOutputAssociationParser;
import org.activiti.bpmn.converter.child.DataStateParser;
import org.activiti.bpmn.converter.child.DocumentationParser;
import org.activiti.bpmn.converter.child.ErrorEventDefinitionParser;
import org.activiti.bpmn.converter.child.ExecutionListenerParser;
import org.activiti.bpmn.converter.child.FieldExtensionParser;
import org.activiti.bpmn.converter.child.FlowNodeRefParser;
import org.activiti.bpmn.converter.child.FormPropertyParser;
import org.activiti.bpmn.converter.child.IOSpecificationParser;
import org.activiti.bpmn.converter.child.MessageEventDefinitionParser;
import org.activiti.bpmn.converter.child.MultiInstanceParser;
import org.activiti.bpmn.converter.child.SignalEventDefinitionParser;
import org.activiti.bpmn.converter.child.TaskListenerParser;
import org.activiti.bpmn.converter.child.TerminateEventDefinitionParser;
import org.activiti.bpmn.converter.child.TimeCycleParser;
import org.activiti.bpmn.converter.child.TimeDateParser;
import org.activiti.bpmn.converter.child.TimeDurationParser;
import org.activiti.bpmn.converter.child.TimerEventDefinitionParser;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionAttribute;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.GraphicInfo;
import org.apache.commons.lang3.StringUtils;

public class BpmnXMLUtil implements BpmnXMLConstants {
  
  private static Map<String, BaseChildElementParser> genericChildParserMap = new HashMap<String, BaseChildElementParser>();
  
  static {
    addGenericParser(new ActivitiEventListenerParser());
    addGenericParser(new CancelEventDefinitionParser());
    addGenericParser(new CompensateEventDefinitionParser());
    addGenericParser(new ConditionExpressionParser());
    addGenericParser(new DataInputAssociationParser());
    addGenericParser(new DataOutputAssociationParser());
    addGenericParser(new DataStateParser());
    addGenericParser(new DocumentationParser());
    addGenericParser(new ErrorEventDefinitionParser());
    addGenericParser(new ExecutionListenerParser());
    addGenericParser(new FieldExtensionParser());
    addGenericParser(new FormPropertyParser());
    addGenericParser(new IOSpecificationParser());
    addGenericParser(new MessageEventDefinitionParser());
    addGenericParser(new MultiInstanceParser());
    addGenericParser(new SignalEventDefinitionParser());
    addGenericParser(new TaskListenerParser());
    addGenericParser(new TerminateEventDefinitionParser());
    addGenericParser(new TimerEventDefinitionParser());
    addGenericParser(new TimeDateParser());
    addGenericParser(new TimeCycleParser());
    addGenericParser(new TimeDurationParser());
    addGenericParser(new FlowNodeRefParser());
    addGenericParser(new ActivitiFailedjobRetryParser());
    addGenericParser(new ActivitiMapExceptionParser());
  }
  
  private static void addGenericParser(BaseChildElementParser parser) {
    genericChildParserMap.put(parser.getElementName(), parser);
  }

  public static void addXMLLocation(BaseElement element, XMLStreamReader xtr) {
    Location location = xtr.getLocation();
    element.setXmlRowNumber(location.getLineNumber());
    element.setXmlColumnNumber(location.getColumnNumber());
  }
  
  public static void addXMLLocation(GraphicInfo graphicInfo, XMLStreamReader xtr) {
    Location location = xtr.getLocation();
    graphicInfo.setXmlRowNumber(location.getLineNumber());
    graphicInfo.setXmlColumnNumber(location.getColumnNumber());
  }
  
  public static void parseChildElements(String elementName, BaseElement parentElement, XMLStreamReader xtr, BpmnModel model) throws Exception {
    parseChildElements(elementName, parentElement, xtr, null, model); 
  }
  
  public static void parseChildElements(String elementName, BaseElement parentElement, XMLStreamReader xtr, 
      Map<String, BaseChildElementParser> childParsers, BpmnModel model) throws Exception {
    
    Map<String, BaseChildElementParser> localParserMap =
        new HashMap<String, BaseChildElementParser>(genericChildParserMap);
    if (childParsers != null) {
      localParserMap.putAll(childParsers);
    }

    boolean inExtensionElements = false;
    boolean readyWithChildElements = false;
    while (readyWithChildElements == false && xtr.hasNext()) {
      xtr.next();
      if (xtr.isStartElement()) {
        if (ELEMENT_EXTENSIONS.equals(xtr.getLocalName())) {
          inExtensionElements = true;
        } else if (localParserMap.containsKey(xtr.getLocalName())) {
          BaseChildElementParser childParser = localParserMap.get(xtr.getLocalName());
          //if we're into an extension element but the current element is not accepted by this parentElement then is read as a custom extension element
          if (inExtensionElements && !childParser.accepts(parentElement)) {
            ExtensionElement extensionElement = BpmnXMLUtil.parseExtensionElement(xtr);
            parentElement.addExtensionElement(extensionElement);
            continue;
          }
          localParserMap.get(xtr.getLocalName()).parseChildElement(xtr, parentElement, model);
        } else if (inExtensionElements) {
          ExtensionElement extensionElement = BpmnXMLUtil.parseExtensionElement(xtr);
          parentElement.addExtensionElement(extensionElement);
        }

      } else if (xtr.isEndElement()) {
        if (ELEMENT_EXTENSIONS.equals(xtr.getLocalName())) {
          inExtensionElements = false;
        } else if (elementName.equalsIgnoreCase(xtr.getLocalName())) {
          readyWithChildElements = true;
        }
      }
    }
  }
  
  public static ExtensionElement parseExtensionElement(XMLStreamReader xtr) throws Exception {
    ExtensionElement extensionElement = new ExtensionElement();
    extensionElement.setName(xtr.getLocalName());
    if (StringUtils.isNotEmpty(xtr.getNamespaceURI())) {
      extensionElement.setNamespace(xtr.getNamespaceURI());
    }
    if (StringUtils.isNotEmpty(xtr.getPrefix())) {
      extensionElement.setNamespacePrefix(xtr.getPrefix());
    }
    
    for (int i = 0; i < xtr.getAttributeCount(); i++) {
      ExtensionAttribute extensionAttribute = new ExtensionAttribute();
      extensionAttribute.setName(xtr.getAttributeLocalName(i));
      extensionAttribute.setValue(xtr.getAttributeValue(i));
      if (StringUtils.isNotEmpty(xtr.getAttributeNamespace(i))) {
        extensionAttribute.setNamespace(xtr.getAttributeNamespace(i));
      }
      if (StringUtils.isNotEmpty(xtr.getAttributePrefix(i))) {
        extensionAttribute.setNamespacePrefix(xtr.getAttributePrefix(i));
      }
      extensionElement.addAttribute(extensionAttribute);
    }
    
    boolean readyWithExtensionElement = false;
    while (readyWithExtensionElement == false && xtr.hasNext()) {
      xtr.next();
      if (xtr.isCharacters() || XMLStreamReader.CDATA == xtr.getEventType()) {
        if (StringUtils.isNotEmpty(xtr.getText().trim())) {
          extensionElement.setElementText(xtr.getText().trim());
        }
      } else if (xtr.isStartElement()) {
        ExtensionElement childExtensionElement = parseExtensionElement(xtr);
        extensionElement.addChildElement(childExtensionElement);
      } else if (xtr.isEndElement() && extensionElement.getName().equalsIgnoreCase(xtr.getLocalName())) {
        readyWithExtensionElement = true;
      }
    }
    return extensionElement;
  }
  
  public static void writeDefaultAttribute(String attributeName, String value, XMLStreamWriter xtw) throws Exception {
    if (StringUtils.isNotEmpty(value) && "null".equalsIgnoreCase(value) == false) {
      xtw.writeAttribute(attributeName, value);
    }
  }
  
  public static void writeQualifiedAttribute(String attributeName, String value, XMLStreamWriter xtw) throws Exception {
    if (StringUtils.isNotEmpty(value)) {
      xtw.writeAttribute(ACTIVITI_EXTENSIONS_PREFIX, ACTIVITI_EXTENSIONS_NAMESPACE, attributeName, value);
    }
  }
  
  public static boolean writeExtensionElements(BaseElement baseElement, boolean didWriteExtensionStartElement, XMLStreamWriter xtw) throws Exception {
    return didWriteExtensionStartElement = writeExtensionElements(baseElement, didWriteExtensionStartElement, null, xtw);
  }
 
  public static boolean writeExtensionElements(BaseElement baseElement, boolean didWriteExtensionStartElement, Map<String, String> namespaceMap, XMLStreamWriter xtw) throws Exception {
    if (!baseElement.getExtensionElements().isEmpty()) {
      if (didWriteExtensionStartElement == false) {
        xtw.writeStartElement(ELEMENT_EXTENSIONS);
        didWriteExtensionStartElement = true;
      }
      
      if (namespaceMap == null) {
        namespaceMap = new HashMap<String, String>();
      }
      
      for (List<ExtensionElement> extensionElements : baseElement.getExtensionElements().values()) {
        for (ExtensionElement extensionElement : extensionElements) {
          writeExtensionElement(extensionElement, namespaceMap, xtw);
        }
      }
    }
    return didWriteExtensionStartElement;
  }
  
  protected static void writeExtensionElement(ExtensionElement extensionElement, Map<String, String> namespaceMap, XMLStreamWriter xtw) throws Exception {
    if (StringUtils.isNotEmpty(extensionElement.getName())) {
      Map<String, String> localNamespaceMap = new HashMap<String, String>();
      if (StringUtils.isNotEmpty(extensionElement.getNamespace())) {
        if (StringUtils.isNotEmpty(extensionElement.getNamespacePrefix())) {
          xtw.writeStartElement(extensionElement.getNamespacePrefix(), extensionElement.getName(), extensionElement.getNamespace());
          
          if (namespaceMap.containsKey(extensionElement.getNamespacePrefix()) == false ||
              namespaceMap.get(extensionElement.getNamespacePrefix()).equals(extensionElement.getNamespace()) == false) {
            
            xtw.writeNamespace(extensionElement.getNamespacePrefix(), extensionElement.getNamespace());
            namespaceMap.put(extensionElement.getNamespacePrefix(), extensionElement.getNamespace());
            localNamespaceMap.put(extensionElement.getNamespacePrefix(), extensionElement.getNamespace());
          }
        } else {
          xtw.writeStartElement(extensionElement.getNamespace(), extensionElement.getName());
        }
      } else {
        xtw.writeStartElement(extensionElement.getName());
      }
      
      for (List<ExtensionAttribute> attributes : extensionElement.getAttributes().values()) {
        for (ExtensionAttribute attribute : attributes) {
          if (StringUtils.isNotEmpty(attribute.getName()) && attribute.getValue() != null) {
            if (StringUtils.isNotEmpty(attribute.getNamespace())) {
              if (StringUtils.isNotEmpty(attribute.getNamespacePrefix())) {
                
                if (namespaceMap.containsKey(attribute.getNamespacePrefix()) == false ||
                    namespaceMap.get(attribute.getNamespacePrefix()).equals(attribute.getNamespace()) == false) {
                  
                  xtw.writeNamespace(attribute.getNamespacePrefix(), attribute.getNamespace());
                  namespaceMap.put(attribute.getNamespacePrefix(), attribute.getNamespace());
                }
                
                xtw.writeAttribute(attribute.getNamespacePrefix(), attribute.getNamespace(), attribute.getName(), attribute.getValue());
              } else {
                xtw.writeAttribute(attribute.getNamespace(), attribute.getName(), attribute.getValue());
              }
            } else {
              xtw.writeAttribute(attribute.getName(), attribute.getValue());
            }
          }
        }
      }
      
      if (extensionElement.getElementText() != null) {
        xtw.writeCData(extensionElement.getElementText());
      } else {
        for (List<ExtensionElement> childElements : extensionElement.getChildElements().values()) {
          for (ExtensionElement childElement : childElements) {
            writeExtensionElement(childElement, namespaceMap, xtw);
          }
        }
      }
      
      for (String prefix : localNamespaceMap.keySet()) {
        namespaceMap.remove(prefix);
      }
      
      xtw.writeEndElement();
    }
  }
  
  public static List<String> parseDelimitedList(String s) {
    List<String> result = new ArrayList<String>();
    if (StringUtils.isNotEmpty(s)) {

      StringCharacterIterator iterator = new StringCharacterIterator(s);
      char c = iterator.first();

      StringBuilder strb = new StringBuilder();
      boolean insideExpression = false;

      while (c != StringCharacterIterator.DONE) {
        if (c == '{' || c == '$') {
          insideExpression = true;
        } else if (c == '}') {
          insideExpression = false;
        } else if (c == ',' && !insideExpression) {
          result.add(strb.toString().trim());
          strb.delete(0, strb.length());
        }

        if (c != ',' || (insideExpression)) {
          strb.append(c);
        }

        c = iterator.next();
      }

      if (strb.length() > 0) {
        result.add(strb.toString().trim());
      }

    }
    return result;
  }
  
  public static String convertToDelimitedString(List<String> stringList) {
    StringBuilder resultString = new StringBuilder();
    
    if(stringList != null) {
    	for (String result : stringList) {
    		if (resultString.length() > 0) {
    			resultString.append(",");
    		}
    		resultString.append(result);
    	}
    }
    return resultString.toString();
  }

  /**
   * add all attributes from XML to element extensionAttributes (except blackListed).
   *
   * @param xtr
   * @param element
   * @param blackList
   */
  public static void addCustomAttributes(XMLStreamReader xtr, BaseElement element, List<ExtensionAttribute>... blackLists) {
    for (int i = 0; i < xtr.getAttributeCount(); i++) {
      ExtensionAttribute extensionAttribute = new ExtensionAttribute();
      extensionAttribute.setName(xtr.getAttributeLocalName(i));
      extensionAttribute.setValue(xtr.getAttributeValue(i));
      if (StringUtils.isNotEmpty(xtr.getAttributeNamespace(i))) {
        extensionAttribute.setNamespace(xtr.getAttributeNamespace(i));
      }
      if (StringUtils.isNotEmpty(xtr.getAttributePrefix(i))) {
        extensionAttribute.setNamespacePrefix(xtr.getAttributePrefix(i));
      }
      if (!isBlacklisted(extensionAttribute, blackLists)) {
        element.addAttribute(extensionAttribute);
      }
    }
  }

  public static void writeCustomAttributes(Collection<List<ExtensionAttribute>> attributes, XMLStreamWriter xtw, List<ExtensionAttribute>... blackLists) throws XMLStreamException {
    writeCustomAttributes(attributes, xtw, new LinkedHashMap<String, String>(), blackLists);
  }
  
  /**
   * write attributes to xtw (except blacklisted)
   * @param attributes
   * @param xtw
   * @param blackList
   */
  public static void writeCustomAttributes(Collection<List<ExtensionAttribute>> attributes, XMLStreamWriter xtw, Map<String, String> namespaceMap,
      List<ExtensionAttribute>... blackLists) throws XMLStreamException {
    
    for (List<ExtensionAttribute> attributeList : attributes) {
      if (attributeList != null && !attributeList.isEmpty()) {
        for (ExtensionAttribute attribute : attributeList) {
          if (!isBlacklisted(attribute, blackLists)) {
            if (attribute.getNamespacePrefix() == null) {
              if (attribute.getNamespace() == null)
                xtw.writeAttribute(attribute.getName(), attribute.getValue());
              else {
                xtw.writeAttribute(attribute.getNamespace(), attribute.getName(), attribute.getValue());
              }
            } else {
              if (!namespaceMap.containsKey(attribute.getNamespacePrefix())) {
                namespaceMap.put(attribute.getNamespacePrefix(), attribute.getNamespace());
                xtw.writeNamespace(attribute.getNamespacePrefix(), attribute.getNamespace());
              }
              xtw.writeAttribute(attribute.getNamespacePrefix(), attribute.getNamespace(),
                  attribute.getName(), attribute.getValue());
            }
          }
        }
      }
    }
  }

  public static boolean isBlacklisted(ExtensionAttribute attribute, List<ExtensionAttribute>... blackLists) {
    if (blackLists != null) {
      for (List<ExtensionAttribute> blackList : blackLists) {
        for (ExtensionAttribute blackAttribute : blackList) {
          if (blackAttribute.getName().equals(attribute.getName())) {
            if ( blackAttribute.getNamespace() != null && attribute.getNamespace() != null
                && blackAttribute.getNamespace().equals(attribute.getNamespace()))
              return true;
            if (blackAttribute.getNamespace() == null && attribute.getNamespace() == null)
              return true;
          }
        }
      }
    }
    return false;
  }
}
