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
package org.activiti.editor.language.bpmn.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamReader;

import org.activiti.editor.language.bpmn.export.ActivitiNamespaceConstants;
import org.activiti.editor.language.bpmn.model.Activity;
import org.activiti.editor.language.bpmn.model.Artifact;
import org.activiti.editor.language.bpmn.model.BaseElement;
import org.activiti.editor.language.bpmn.model.FlowElement;
import org.activiti.editor.language.bpmn.model.Gateway;
import org.activiti.editor.language.bpmn.model.Process;
import org.activiti.editor.language.bpmn.model.SubProcess;
import org.apache.commons.lang.StringUtils;

/**
 * @author Tijs Rademakers
 */
public abstract class BaseBpmnElementParser implements ActivitiNamespaceConstants {

  protected static final Logger LOGGER = Logger.getLogger(BaseBpmnElementParser.class.getName());
  
  protected XMLStreamReader xtr;
  protected BpmnModel model;
  protected List<SubProcess> activeSubProcessList;
  protected List<BoundaryEventModel> boundaryList;
  protected Map<String, BaseChildElementParser> childElementParsers = new HashMap<String, BaseChildElementParser>();
  private static Map<String, BaseChildElementParser> genericChildParserMap = new HashMap<String, BaseChildElementParser>();
  
  static {
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
  
  public void parse(XMLStreamReader xtr, BpmnModel model, Process activeProcess, 
      List<SubProcess> activeSubProcessList, List<BoundaryEventModel> boundaryList) throws Exception {
    
    this.xtr = xtr;
    this.model = model;
    this.activeSubProcessList = activeSubProcessList;
    this.boundaryList = boundaryList;
    
    String elementId = xtr.getAttributeValue(null, "id");
    String elementName = xtr.getAttributeValue(null, "name");
    boolean async = parseAsync();
    boolean notExclusive = parseNotExclusive();
    String defaultFlow = xtr.getAttributeValue(null, "default");
    
    BaseElement parsedElement = parseElement();
    
    if (parsedElement instanceof Artifact) {
      Artifact currentArtifact = (Artifact) parsedElement;
      currentArtifact.setId(elementId);

      if (isInSubProcess(activeSubProcessList)) {
        final SubProcess currentSubProcess = activeSubProcessList.get(activeSubProcessList.size() - 2);
        currentSubProcess.getArtifacts().add(currentArtifact);

      } else {
        activeProcess.getArtifacts().add(currentArtifact);
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
      
      if(currentFlowElement instanceof SubProcess) {
        if(isInSubProcess(activeSubProcessList)) {
          activeSubProcessList.get(activeSubProcessList.size() - 2).addFlowElement(currentFlowElement);
          
        } else {
          activeProcess.addFlowElement(currentFlowElement);
        }
        
      } else if (activeSubProcessList.size() > 0) {
        activeSubProcessList.get(activeSubProcessList.size() - 1).addFlowElement(currentFlowElement);
      } else {
        activeProcess.addFlowElement(currentFlowElement);
      }
    }
  }
  
  protected abstract BaseElement parseElement() throws Exception;
  
  public static String getElementName(){
    return null;
  }
  
  protected void parseChildElements(String elementName, BaseElement parentElement) {
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
      LOGGER.log(Level.WARNING, "Error parsing child elements for " + elementName, e);
    }
  }
  
  private boolean parseAsync() {
    boolean async = false;
    String asyncString = xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, "async");
    if ("true".equalsIgnoreCase(asyncString)) {
      async = true;
    }
    return async;
  }
  
  private boolean parseNotExclusive() {
    boolean notExclusive = false;
    String exclusiveString = xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, "exclusive");
    if ("false".equalsIgnoreCase(exclusiveString)) {
      notExclusive = true;
    }
    return notExclusive;
  }
  
  private boolean isInSubProcess(List<SubProcess> subProcessList) {
    if(subProcessList.size() > 1) {
      return true;
    } else {
      return false;
    }
  }
}
