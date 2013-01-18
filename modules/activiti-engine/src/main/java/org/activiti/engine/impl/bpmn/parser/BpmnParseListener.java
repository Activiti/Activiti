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

package org.activiti.engine.impl.bpmn.parser;

import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BusinessRuleTask;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.EventGateway;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.InclusiveGateway;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.ManualTask;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.ReceiveTask;
import org.activiti.bpmn.model.ScriptTask;
import org.activiti.bpmn.model.SendTask;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.Task;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.bpmn.model.Transaction;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.util.xml.Element;
import org.activiti.engine.impl.variable.VariableDeclaration;

/**
 * Listener which can be registered within the engine to receive events during parsing (and
 * maybe influence ist). Instead of implementing this interface you might consider to extend 
 * the {@link AbstractBpmnParseListener}, which contains an empty implementation for all methods
 * and makes your implementation easier and more robust to future changes.
 * 
 * @author Tom Baeyens
 * @author Falko Menge
 * @author Joram Barrez
 */
public interface BpmnParseListener {

  void parseProcess(Process process, ProcessDefinitionEntity processDefinition);
  void parseStartEvent(StartEvent startEvent, ScopeImpl scope, ActivityImpl startEventActivity);
  void parseExclusiveGateway(ExclusiveGateway exclusiveGateway, ScopeImpl scope, ActivityImpl activity);
  void parseInclusiveGateway(InclusiveGateway inclusiveGateway, ScopeImpl scope, ActivityImpl activity);
  void parseParallelGateway(ParallelGateway parallelGateway, ScopeImpl scope, ActivityImpl activity);
  void parseScriptTask(ScriptTask scriptTask, ScopeImpl scope, ActivityImpl activity);
  void parseServiceTask(ServiceTask serviceTask, ScopeImpl scope, ActivityImpl activity);
  void parseBusinessRuleTask(BusinessRuleTask businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity);
  void parseTask(Task task, ScopeImpl scope, ActivityImpl activity);
  void parseManualTask(ManualTask manualTask, ScopeImpl scope, ActivityImpl activity);
  void parseUserTask(UserTask userTaskElement, ScopeImpl scope, ActivityImpl activity);
  void parseEndEvent(EndEvent endEventElement, ScopeImpl scope, ActivityImpl activity);
  void parseBoundaryTimerEventDefinition(TimerEventDefinition timerEventDefinition, boolean interrupting, ActivityImpl timerActivity);
  void parseBoundaryErrorEventDefinition(ErrorEventDefinition errorEventDefinition, boolean interrupting, ActivityImpl activity, ActivityImpl nestedErrorEventActivity);
  void parseSubProcess(SubProcess subProcess, ScopeImpl scope, ActivityImpl activity);
  void parseCallActivity(CallActivity callActivity, ScopeImpl scope, ActivityImpl activity);
  void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration, ActivityImpl activity);
  void parseSequenceFlow(SequenceFlow sequenceFlow, ScopeImpl scopeElement, TransitionImpl transition);
  void parseSendTask(SendTask sendTask, ScopeImpl scope, ActivityImpl activity);
  void parseMultiInstanceLoopCharacteristics(org.activiti.bpmn.model.Activity modelActivity, MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics, ActivityImpl activity);
  void parseIntermediateTimerEventDefinition(TimerEventDefinition timerEventDefinition, ActivityImpl timerActivity);
  void parseReceiveTask(ReceiveTask receiveTask, ScopeImpl scope, ActivityImpl activity);
  void parseIntermediateSignalCatchEventDefinition(SignalEventDefinition signalEventDefinition, ActivityImpl signalActivity);
  void parseIntermediateMessageCatchEventDefinition(MessageEventDefinition messageEventDefinition, ActivityImpl nestedActivity);
  void parseBoundarySignalEventDefinition(SignalEventDefinition signalEventDefinition, boolean interrupting, ActivityImpl signalActivity);
  void parseEventBasedGateway(EventGateway eventBasedGateway, ScopeImpl scope, ActivityImpl activity);
  void parseTransaction(Transaction transaction, ScopeImpl scope, ActivityImpl activity);
  void parseCompensateEventDefinition(Element compensateEventDefinition, ActivityImpl compensationActivity);
  void parseIntermediateThrowEvent(ThrowEvent intermediateEventElement, ScopeImpl scope, ActivityImpl activity);
  void parseIntermediateCatchEvent(IntermediateCatchEvent intermediateCatchEvent, ScopeImpl scope, ActivityImpl activity);
  void parseBoundaryEvent(BoundaryEvent boundaryEvent, ScopeImpl scopeElement, ActivityImpl nestedActivity);
  void parseBoundaryMessageEventDefinition(MessageEventDefinition messageEventDefinition, boolean interrupting, ActivityImpl messageActivity);

}
