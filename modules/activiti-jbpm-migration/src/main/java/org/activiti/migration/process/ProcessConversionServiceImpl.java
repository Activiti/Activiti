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
package org.activiti.migration.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.migration.dao.Jbpm3Dao;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.node.EndState;
import org.jbpm.graph.node.StartState;
import org.jbpm.graph.node.State;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * @author Joram Barrez
 */
public class ProcessConversionServiceImpl implements ProcessConversionService {
  
  protected static final Logger LOGGER = Logger.getLogger(ProcessConversionServiceImpl.class.getName());

  protected Jbpm3Dao jbpm3Dao;
  
  public ProcessConversionServiceImpl(Jbpm3Dao jbpm3Dao) {
    this.jbpm3Dao = jbpm3Dao;
  }
  
  public Map<String, Document> convertAllProcessDefinitions() {
    Map<String, Document> processDefinitionMap = new HashMap<String, Document>();
    List<ProcessDefinition> processDefinitions = jbpm3Dao.getAllProcessDefinitions();
    for (ProcessDefinition processDefinition : processDefinitions) {
      if (LOGGER.isLoggable(Level.INFO)) {
        LOGGER.info("Converting process definition '" + processDefinition.getName() + "'");
      }
      Document processDefinitionDocument = convertProcessDefinition(processDefinition);
      processDefinitionMap.put(processDefinition.getName(), processDefinitionDocument);
    }
    return processDefinitionMap;
  }
  
  public Document convertProcessDefinition(ProcessDefinition processDefinition) {
    Document processDefinitionXml = createEmptyBpmn20Document(processDefinition.getName());
    Element processElement = getProcessElement(processDefinitionXml);
    
    for (Node node : jbpm3Dao.getNodes(processDefinition)) {
      Element element = null;
      if (node instanceof StartState) {
        element = convertStartState(processDefinitionXml, (StartState) node);
      } else if (node instanceof EndState) {
        element = convertEndState(processDefinitionXml, (EndState) node);
      } else if (node instanceof State) {
        element = convertState(processDefinitionXml, (State) node);
      } else {
        // if node is not yet implemented, we convert it to a simple passthrough
        element = convertToPassThroughTask(processDefinitionXml, node);
      }
      
      processElement.appendChild(element);
      
      List<Transition> outgoingTransitions = jbpm3Dao.getOutgoingTransitions(node);
      List<Element> sequenceFlowElements = convertTransitions(processDefinitionXml, outgoingTransitions);
      for (Element sequenceFlowElement : sequenceFlowElements) {
        processElement.appendChild(sequenceFlowElement);
      }
    }
    
   
    
    return processDefinitionXml;
  }
  
  public Element convertStartState(Document processDefinitionDocument, StartState startState) {
    Element startElement = processDefinitionDocument.createElement(START_EVENT_TAG);
    startElement.setAttribute("id", startState.getName());
    return startElement;
  }
  
  public Element convertState(Document processDefinitionDocument, State state) {
    Element stateElement = processDefinitionDocument.createElement(RECEIVE_TASK_TAG);
    stateElement.setAttribute("id", state.getName());
    return stateElement;
  }
  
  public Element convertEndState(Document processDefinitionDocument, EndState endState) {
    Element endElement = processDefinitionDocument.createElement(END_EVENT_TAG);
    endElement.setAttribute("id", endState.getName());
    return endElement;
  }
  
  public List<Element> convertTransitions(Document processDefinitionDocument, List<Transition> transitions) {
    List<Element> sequenceFlowElements = new ArrayList<Element>();
    for (Transition transition : transitions) {
      sequenceFlowElements.add(convertTransition(processDefinitionDocument, transition));
    }
    return sequenceFlowElements;
  }
  
  public Element convertTransition(Document processDefinitionDocument, Transition transition) {
    Element sequenceFlowElement = processDefinitionDocument.createElement(SEQUENCE_FLOW_TAG);
    if (transition.getName() != null) {
      sequenceFlowElement.setAttribute("id", transition.getName());
    }
    sequenceFlowElement.setAttribute("sourceRef", transition.getFrom().getName());
    sequenceFlowElement.setAttribute("targetRef", transition.getTo().getName());
    return sequenceFlowElement;
  }
  
  public Document createEmptyBpmn20Document(String processName) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder documentBuilder = factory.newDocumentBuilder();
      Document document = documentBuilder.newDocument();
      
      // 'definitions' root
      Element root = document.createElementNS(BpmnParser.BPMN20_NS, BPMN20_ROOT_TAG);
      root.setAttributeNS(XMLNS, XMLNS_ACTIVITI, BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS);
      root.setAttribute(TARGET_NAMESPACE, TARGET_NAMESPACE_VALUE);
      document.appendChild(root);
      
      // 'process' element
      Element processElement = document.createElement(PROCESS_TAG);
      processElement.setAttribute("id", processName);
      processElement.setAttribute("name", processName);
      root.appendChild(processElement);
      
      return document;
    } catch (ParserConfigurationException e) {
      throw new ActivitiException("Could not create empty BPMN 2.0 xml document", e);
    }

  }
  
  public void close() {
    jbpm3Dao.close();
  }
  
  protected Element convertToPassThroughTask(Document processDefinitionDocument, Node node) {
    Element taskElement = processDefinitionDocument.createElement(TASK_TAG);
    taskElement.setAttribute("id", node.getName());
    return taskElement;
  }
  
  protected Element getProcessElement(Document processDefinitionDocument) {
    NodeList nodeList = processDefinitionDocument.getElementsByTagName(PROCESS_TAG);
    if (nodeList.getLength() == 0) {
      throw new ActivitiException("No process tag found in DOM document. Something is surely wrong.");
    }
    if (nodeList.getLength() > 1) {
      throw new ActivitiException("Multiple tags " + PROCESS_TAG + " found. Something is surely wrong.");
    }
    return (Element) nodeList.item(0);
  }
  
  // Getters and setters ///////////////////////////////////
  
  public Jbpm3Dao getJbpm3Dao() {
    return jbpm3Dao;
  }
  public void setJbpm3Dao(Jbpm3Dao jbpm3Dao) {
    this.jbpm3Dao = jbpm3Dao;
  }

}
