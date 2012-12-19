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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.converter.child.BaseChildElementParser;
import org.activiti.bpmn.converter.child.ConditionExpressionParser;
import org.activiti.bpmn.converter.child.DocumentationParser;
import org.activiti.bpmn.converter.child.ExecutionListenerParser;
import org.activiti.bpmn.converter.child.FieldExtensionParser;
import org.activiti.bpmn.converter.child.FormPropertyParser;
import org.activiti.bpmn.converter.child.MessageEventDefinitionParser;
import org.activiti.bpmn.converter.child.MultiInstanceParser;
import org.activiti.bpmn.converter.child.SignalEventDefinitionParser;
import org.activiti.bpmn.converter.child.TaskListenerParser;
import org.activiti.bpmn.converter.child.TimerEventDefinitionParser;
import org.activiti.bpmn.converter.util.ActivitiListenerUtil;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.Artifact;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.Gateway;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.bpmn.model.UserTask;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tijs Rademakers
 */
public abstract class BaseBpmnXMLConverter implements BpmnXMLConstants {

  protected static final Logger LOGGER = LoggerFactory.getLogger(BaseBpmnXMLConverter.class);
  
  protected BpmnModel model;
  protected Process activeProcess;
  protected Map<String, BaseChildElementParser> childElementParsers = new HashMap<String, BaseChildElementParser>();
  private static Map<String, BaseChildElementParser> genericChildParserMap = new HashMap<String, BaseChildElementParser>();
  
  protected boolean didWriteExtensionStartElement = false;
  
  static {
    addGenericParser(new ConditionExpressionParser());
    addGenericParser(new DocumentationParser());
    addGenericParser(new ErrorEventDefinitionParser());
    addGenericParser(new ExecutionListenerParser());
    addGenericParser(new FieldExtensionParser());
    addGenericParser(new FormPropertyParser());
    addGenericParser(new MessageEventDefinitionParser());
    addGenericParser(new MultiInstanceParser());
    addGenericParser(new SignalEventDefinitionParser());
    addGenericParser(new TaskListenerParser());
    addGenericParser(new TimerEventDefinitionParser());
  }
  
  private static void addGenericParser(BaseChildElementParser parser) {
    genericChildParserMap.put(parser.getElementName(), parser);
  }
  
  public void convertToBpmnModel(XMLStreamReader xtr, BpmnModel model, Process activeProcess, 
      List<SubProcess> activeSubProcessList) throws Exception {
    
    this.model = model;
    this.activeProcess = activeProcess;
    
    String elementId = xtr.getAttributeValue(null, ATTRIBUTE_ID);
    String elementName = xtr.getAttributeValue(null, ATTRIBUTE_NAME);
    boolean async = parseAsync(xtr);
    boolean notExclusive = parseNotExclusive(xtr);
    String defaultFlow = xtr.getAttributeValue(null, "default");
    
    BaseElement parsedElement = convertXMLToElement(xtr);
    
    if (parsedElement instanceof Artifact) {
      Artifact currentArtifact = (Artifact) parsedElement;
      currentArtifact.setId(elementId);

      if (isInSubProcess(activeSubProcessList)) {
        final SubProcess currentSubProcess = activeSubProcessList.get(activeSubProcessList.size() - 2);
        currentSubProcess.addArtifact(currentArtifact);

      } else {
        this.activeProcess.addArtifact(currentArtifact);
      }
    }
    
    if(parsedElement instanceof FlowElement) {
      
      FlowElement currentFlowElement = (FlowElement) parsedElement;
      currentFlowElement.setId(elementId);
      currentFlowElement.setName(elementName);
      
      if(currentFlowElement instanceof Activity) {
        
        Activity activity = (Activity) currentFlowElement;
        activity.setAsynchronous(async);
        activity.setNotExclusive(notExclusive);
        if(StringUtils.isNotEmpty(defaultFlow)) {
          activity.setDefaultFlow(defaultFlow);
        }
      }
      
      if(currentFlowElement instanceof Gateway) {
        if(StringUtils.isNotEmpty(defaultFlow)) {
          ((Gateway) currentFlowElement).setDefaultFlow(defaultFlow);
        }
      }
      
      if (activeSubProcessList.size() > 0) {
        activeSubProcessList.get(activeSubProcessList.size() - 1).addFlowElement(currentFlowElement);
      } else {
        this.activeProcess.addFlowElement(currentFlowElement);
      }
    }
  }
  
  public void convertToXML(XMLStreamWriter xtw, BaseElement baseElement) throws Exception {
    xtw.writeStartElement(getXMLElementName());
    didWriteExtensionStartElement = false;
    writeDefaultAttribute(ATTRIBUTE_ID, baseElement.getId(), xtw);
    if (baseElement instanceof FlowElement) {
      writeDefaultAttribute(ATTRIBUTE_NAME, ((FlowElement) baseElement).getName(), xtw);
    }
    
    if (baseElement instanceof Activity) {
      Activity activity = (Activity) baseElement;
      if (activity.isAsynchronous()) {
        writeQualifiedAttribute(ATTRIBUTE_ACTIVITY_ASYNCHRONOUS, ATTRIBUTE_VALUE_TRUE, xtw);
      }
      if (activity.isNotExclusive()) {
        writeQualifiedAttribute(ATTRIBUTE_ACTIVITY_EXCLUSIVE, ATTRIBUTE_VALUE_FALSE, xtw);
      }
      writeDefaultAttribute(ATTRIBUTE_ACTIVITY_DEFAULT, activity.getDefaultFlow(), xtw);
    }
    
    writeAdditionalAttributes(baseElement, xtw);
    
    if (baseElement instanceof FlowElement) {
      FlowElement flowElement = (FlowElement) baseElement;
      if (StringUtils.isNotEmpty(flowElement.getDocumentation())) {
  
        xtw.writeStartElement(ELEMENT_DOCUMENTATION);
        xtw.writeCharacters(flowElement.getDocumentation());
        xtw.writeEndElement();
      }
    }
    writeAdditionalChildElements(baseElement, xtw);
    
    didWriteExtensionStartElement = writeListeners(baseElement, xtw);
    
    if (didWriteExtensionStartElement) {
      xtw.writeEndElement();
    }
    
    if (baseElement instanceof Activity) {
      Activity activity = (Activity) baseElement;
      if (activity.getLoopCharacteristics() != null) {
        MultiInstanceLoopCharacteristics multiInstanceObject = activity.getLoopCharacteristics();
        if (StringUtils.isNotEmpty(multiInstanceObject.getLoopCardinality()) ||
            StringUtils.isNotEmpty(multiInstanceObject.getInputDataItem()) ||
            StringUtils.isNotEmpty(multiInstanceObject.getCompletionCondition())) {
          
          xtw.writeStartElement(ELEMENT_MULTIINSTANCE);
          writeDefaultAttribute(ATTRIBUTE_MULTIINSTANCE_SEQUENTIAL, String.valueOf(multiInstanceObject.isSequential()).toLowerCase(), xtw);
          if (StringUtils.isNotEmpty(multiInstanceObject.getInputDataItem())) {
            writeQualifiedAttribute(ATTRIBUTE_MULTIINSTANCE_COLLECTION, multiInstanceObject.getInputDataItem(), xtw);
          }
          if (StringUtils.isNotEmpty(multiInstanceObject.getElementVariable())) {
            writeQualifiedAttribute(ATTRIBUTE_MULTIINSTANCE_VARIABLE, multiInstanceObject.getElementVariable(), xtw);
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
    
    xtw.writeEndElement();
  }
  
  protected abstract BaseElement convertXMLToElement(XMLStreamReader xtr) throws Exception;
  
  protected abstract String getXMLElementName();
  
  protected abstract void writeAdditionalAttributes(BaseElement element, XMLStreamWriter xtw) throws Exception;
  
  protected abstract void writeAdditionalChildElements(BaseElement element, XMLStreamWriter xtw) throws Exception;
  
  // To BpmnModel converter convenience methods
  
  protected void parseChildElements(String elementName, BaseElement parentElement, XMLStreamReader xtr) {
    Map<String, BaseChildElementParser> childParsers = new HashMap<String, BaseChildElementParser>();
    childParsers.putAll(genericChildParserMap);
    if (childElementParsers != null) {
      childParsers.putAll(childElementParsers);
    }
    
    boolean readyWithChildElements = false;
    try {
      while (readyWithChildElements == false && xtr.hasNext()) {
        xtr.next();
        if (xtr.isStartElement()) {
          if (childParsers.containsKey(xtr.getLocalName())) {
            childParsers.get(xtr.getLocalName()).parseChildElement(xtr, parentElement);
          }

        } else if (xtr.isEndElement() && elementName.equalsIgnoreCase(xtr.getLocalName())) {
          readyWithChildElements = true;
        }
      }
    } catch (Exception e) {
      LOGGER.warn("Error parsing child elements for {}", elementName, e);
    }
  }
  
  private boolean parseAsync(XMLStreamReader xtr) {
    boolean async = false;
    String asyncString = xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_ACTIVITY_ASYNCHRONOUS);
    if (ATTRIBUTE_VALUE_TRUE.equalsIgnoreCase(asyncString)) {
      async = true;
    }
    return async;
  }
  
  private boolean parseNotExclusive(XMLStreamReader xtr) {
    boolean notExclusive = false;
    String exclusiveString = xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_ACTIVITY_EXCLUSIVE);
    if (ATTRIBUTE_VALUE_FALSE.equalsIgnoreCase(exclusiveString)) {
      notExclusive = true;
    }
    return notExclusive;
  }
  
  protected List<String> parseDelimitedList(String expression) {
    List<String> resultList = new ArrayList<String>();
    if (StringUtils.isNotEmpty(expression)) {
      String[] expressionList = null;
      if (expression.contains(",")) {
        expressionList = expression.split(",");
      } else {
        expressionList = new String[] { expression };
      }
      for (String strExpression : expressionList) {
        resultList.add(strExpression);
      }
    }
    return resultList;
  }
  
  private boolean isInSubProcess(List<SubProcess> subProcessList) {
    if(subProcessList.size() > 1) {
      return true;
    } else {
      return false;
    }
  }
  
  // To XML converter convenience methods
  
  protected String convertToDelimitedString(List<String> stringList) {
    StringBuilder resultString = new StringBuilder();
    for (String result : stringList) {
      if (resultString.length() > 0) {
        resultString.append(",");
      }
      resultString.append(result);
    }
    return resultString.toString();
  }
  
  protected void writeFormProperties(FlowElement flowElement, XMLStreamWriter xtw) throws Exception {
    
    List<FormProperty> propertyList = null;
    if (flowElement instanceof UserTask) {
      propertyList = ((UserTask) flowElement).getFormProperties();
    } else if (flowElement instanceof StartEvent) {
      propertyList = ((StartEvent) flowElement).getFormProperties();
    }
    
    if (propertyList != null) {
    
      for (FormProperty property : propertyList) {
        
        if (StringUtils.isNotEmpty(property.getId())) {
          
          if (didWriteExtensionStartElement == false) { 
            xtw.writeStartElement(ELEMENT_EXTENSIONS);
            didWriteExtensionStartElement = true;
          }
          
          xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, ELEMENT_FORMPROPERTY, ACTIVITI_EXTENSIONS_NAMESPACE);
          writeDefaultAttribute(ATTRIBUTE_FORM_ID, property.getId(), xtw);
          
          writeDefaultAttribute(ATTRIBUTE_FORM_NAME, property.getName(), xtw);
          writeDefaultAttribute(ATTRIBUTE_FORM_TYPE, property.getType(), xtw);
          writeDefaultAttribute(ATTRIBUTE_FORM_EXPRESSION, property.getExpression(), xtw);
          writeDefaultAttribute(ATTRIBUTE_FORM_VARIABLE, property.getVariable(), xtw);
          
          xtw.writeEndElement();
        }
      }
    }
  }
  
  protected boolean writeListeners(BaseElement element, XMLStreamWriter xtw) throws Exception {
    return ActivitiListenerUtil.writeListeners(element, didWriteExtensionStartElement, xtw);
  }
  
  protected void writeEventDefinitions(List<EventDefinition> eventDefinitions, XMLStreamWriter xtw) throws Exception {
    for (EventDefinition eventDefinition : eventDefinitions) {
      if (eventDefinition instanceof TimerEventDefinition) {
        writeTimerDefinition((TimerEventDefinition) eventDefinition, xtw);
      } else if (eventDefinition instanceof SignalEventDefinition) {
        writeSignalDefinition((SignalEventDefinition) eventDefinition, xtw);
      } else if (eventDefinition instanceof MessageEventDefinition) {
        writeMessageDefinition((MessageEventDefinition) eventDefinition, xtw);
      } else if (eventDefinition instanceof ErrorEventDefinition) {
        writeErrorDefinition((ErrorEventDefinition) eventDefinition, xtw);
      }
    }
  }
  
  protected void writeTimerDefinition(TimerEventDefinition timerDefinition, XMLStreamWriter xtw) throws Exception {
    xtw.writeStartElement(ELEMENT_EVENT_TIMERDEFINITION);
    
    if (StringUtils.isNotEmpty(timerDefinition.getTimeDate())) {
      xtw.writeStartElement(ATTRIBUTE_TIMER_DATE);
      xtw.writeCharacters(timerDefinition.getTimeDate());
      xtw.writeEndElement();
      
    } else if (StringUtils.isNotEmpty(timerDefinition.getTimeCycle())) {
      xtw.writeStartElement(ATTRIBUTE_TIMER_CYCLE);
      xtw.writeCharacters(timerDefinition.getTimeCycle());
      xtw.writeEndElement();
      
    } else if (StringUtils.isNotEmpty(timerDefinition.getTimeDuration())) {
      xtw.writeStartElement(ATTRIBUTE_TIMER_DURATION);
      xtw.writeCharacters(timerDefinition.getTimeDuration());
      xtw.writeEndElement();
    }
    
    xtw.writeEndElement();
  }
  
  protected void writeSignalDefinition(SignalEventDefinition signalDefinition, XMLStreamWriter xtw) throws Exception {
    xtw.writeStartElement(ELEMENT_EVENT_SIGNALDEFINITION);
    writeDefaultAttribute(ATTRIBUTE_SIGNAL_REF, signalDefinition.getSignalRef(), xtw); 
    xtw.writeEndElement();
  }
  
  protected void writeMessageDefinition(MessageEventDefinition messageDefinition, XMLStreamWriter xtw) throws Exception {
    xtw.writeStartElement(ELEMENT_EVENT_MESSAGEDEFINITION);
    writeDefaultAttribute(ATTRIBUTE_MESSAGE_REF, messageDefinition.getMessageRef(), xtw); 
    xtw.writeEndElement();
  }
  
  protected void writeErrorDefinition(ErrorEventDefinition errorDefinition, XMLStreamWriter xtw) throws Exception {
    xtw.writeStartElement(ELEMENT_EVENT_ERRORDEFINITION);
    writeDefaultAttribute(ATTRIBUTE_ERROR_REF, errorDefinition.getErrorCode(), xtw); 
    xtw.writeEndElement();
  }
  
  protected void writeDefaultAttribute(String attributeName, String value, XMLStreamWriter xtw) throws Exception {
    BpmnXMLUtil.writeDefaultAttribute(attributeName, value, xtw);
  }
  
  protected void writeQualifiedAttribute(String attributeName, String value, XMLStreamWriter xtw) throws Exception {
    BpmnXMLUtil.writeQualifiedAttribute(attributeName, value, xtw);
  }
}
