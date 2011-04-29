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
package org.activiti.migration.service;

import java.util.List;
import java.util.Map;

import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.node.EndState;
import org.jbpm.graph.node.StartState;
import org.jbpm.graph.node.State;
import org.jbpm.graph.node.TaskNode;
import org.jbpm.taskmgmt.def.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * @author Joram Barrez
 */
public interface ProcessConversionService {
  
  // BPMN 2.0 XML constants
  String BPMN20_ROOT_TAG = "definitions";
  String PROCESS_TAG = "process";
  String DOCUMENTATION_TAG = "documentation";
  String START_EVENT_TAG = "startEvent";
  String END_EVENT_TAG = "endEvent";
  String TASK_TAG = "task";
  String RECEIVE_TASK_TAG = "receiveTask";
  String USER_TASK_TAG = "userTask";
  String HUMAN_PERFORMER_TAG = "humanPerformer";
  String RESOURCE_ASSIGNMENT_EXPRESSION_TAG = "resourceAssignmentExpression";
  String FORMAL_EXPRESSION_TAG = "formalExpression";
  String CANIDATE_USERS_ATTRIBUTE = "activiti:candidateUsers";
  String SEQUENCE_FLOW_TAG = "sequenceFlow";
  
  // Other constants
  String XMLNS = "http://www.w3.org/2000/xmlns/";
  String XMLNS_ACTIVITI = "xmlns:activiti";
  String TARGET_NAMESPACE = "targetNamespace";
  String TARGET_NAMESPACE_VALUE = "migratedProcess";
  
  Map<String, Document> convertAllProcessDefinitions();
  
  Document convertProcessDefinition(ProcessDefinition processDefinition);
  
  Element convertStartState(Document processDefinitionDocument, StartState startState);
  
  Element convertState(Document processDefinitionDocument, State state);
  
  Element convertEndState(Document processDefinitionDocument, EndState endState);
  
  Element convertTaskNode(Document processDefinitionDocument, TaskNode tasknode);
  
  Element convertTask(Document processDefinitionDocument, Task task);
  
  List<Element> convertTransitions(Document processDefinitionDocument, List<Transition> transitions);
  
  Element convertTransition(Document processDefinitionDocument, Transition transition);
  
  Document createEmptyBpmn20Document(String processName, int version);
  
  void close();
  
}
