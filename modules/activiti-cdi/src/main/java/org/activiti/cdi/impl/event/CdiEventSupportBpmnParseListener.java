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
package org.activiti.cdi.impl.event;

import org.activiti.bpmn.model.Activity;
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
import org.activiti.cdi.BusinessProcessEventType;
import org.activiti.cdi.impl.event.CdiExecutionListener;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.util.xml.Element;
import org.activiti.engine.impl.variable.VariableDeclaration;
import org.activiti.engine.parser.BpmnParseListener;

/**
 * {@link BpmnParseListener} registering the {@link CdiExecutionListener} for
 * distributing execution events using the cdi event infrastructure
 * 
 * @author Daniel Meyer
 */
public class CdiEventSupportBpmnParseListener implements BpmnParseListener {

  protected void addEndEventListener(ActivityImpl activity) {
    activity.addExecutionListener(ExecutionListener.EVENTNAME_END, new CdiExecutionListener(activity.getId(), BusinessProcessEventType.END_ACTIVITY));
  }

  protected void addStartEventListener(ActivityImpl activity) {
    activity.addExecutionListener(ExecutionListener.EVENTNAME_START, new CdiExecutionListener(activity.getId(), BusinessProcessEventType.START_ACTIVITY));
  }

  @Override
  public void parseProcess(Process processElement, ProcessDefinitionEntity processDefinition) {
  }

  @Override
  public void parseStartEvent(StartEvent startEventElement, ScopeImpl scope, ActivityImpl startEventActivity) {
    addStartEventListener(startEventActivity);
    addEndEventListener(startEventActivity);
  }

  @Override
  public void parseExclusiveGateway(ExclusiveGateway exclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseInclusiveGateway(InclusiveGateway inclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseParallelGateway(ParallelGateway parallelGwElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseScriptTask(ScriptTask scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseServiceTask(ServiceTask serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseBusinessRuleTask(BusinessRuleTask businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseTask(Task taskElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseManualTask(ManualTask manualTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseUserTask(UserTask userTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseEndEvent(EndEvent endEventElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseBoundaryTimerEventDefinition(TimerEventDefinition timerEventDefinition, boolean interrupting, ActivityImpl timerActivity) {
    addStartEventListener(timerActivity);
    addEndEventListener(timerActivity);
  }

  @Override
  public void parseBoundaryErrorEventDefinition(ErrorEventDefinition errorEventDefinition, boolean interrupting, ActivityImpl activity, ActivityImpl nestedErrorEventActivity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseSubProcess(SubProcess subProcessElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseCallActivity(CallActivity callActivityElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration, ActivityImpl activity) {
  }

  @Override
  public void parseSequenceFlow(SequenceFlow sequenceFlowElement, ScopeImpl scopeElement, TransitionImpl transition) {
    transition.addExecutionListener(new CdiExecutionListener(transition.getId()));
  }

  @Override
  public void parseSendTask(SendTask sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseMultiInstanceLoopCharacteristics(Activity activityElement, MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristicsElement, ActivityImpl activity) {
  }
  
  @Override
  public void parseIntermediateTimerEventDefinition(TimerEventDefinition timerEventDefinition, ActivityImpl timerActivity) {
    addStartEventListener(timerActivity);
    addEndEventListener(timerActivity);
  }

  @Override
  public void parseReceiveTask(ReceiveTask receiveTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseIntermediateSignalCatchEventDefinition(SignalEventDefinition signalEventDefinition, ActivityImpl signalActivity) {
    addStartEventListener(signalActivity);
    addEndEventListener(signalActivity);
  }

  @Override
  public void parseBoundarySignalEventDefinition(SignalEventDefinition signalEventDefinition, boolean interrupting, ActivityImpl signalActivity) {
    addStartEventListener(signalActivity);
    addEndEventListener(signalActivity);
  }

  @Override
  public void parseEventBasedGateway(EventGateway eventBasedGwElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseTransaction(Transaction transactionElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);
  }

  @Override
  public void parseCompensateEventDefinition(Element compensateEventDefinition, ActivityImpl compensationActivity) {

  }

  @Override
  public void parseIntermediateThrowEvent(ThrowEvent intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    addStartEventListener(activity);
    addEndEventListener(activity);    
  }

  @Override
  public void parseIntermediateCatchEvent(IntermediateCatchEvent intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
  }

  @Override
  public void parseBoundaryEvent(BoundaryEvent boundaryEventElement, ScopeImpl scopeElement, ActivityImpl nestedActivity) {
  }
  
  @Override
  public void parseIntermediateMessageCatchEventDefinition(MessageEventDefinition messageEventDefinition, ActivityImpl nestedActivity) {
  }

  @Override
  public void parseBoundaryMessageEventDefinition(MessageEventDefinition element, boolean interrupting, ActivityImpl messageActivity) {
  }

}
