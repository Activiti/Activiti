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

package org.activiti.engine.impl.history.handler;

import java.util.List;

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
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.BpmnParseListener;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.impl.util.xml.Element;
import org.activiti.engine.impl.variable.VariableDeclaration;

/**
 * Implements writing the history, but not all logic is contained here, see {@link ProcessDefinitionEntity} as well!
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Falko Menge
 * @author Bernd Ruecker (camunda)
 * @author Christian Lipphardt (camunda)
 */
public class HistoryParseListener implements BpmnParseListener {

  protected static final StartEventEndHandler START_EVENT_END_HANDLER = new StartEventEndHandler();

  protected static final ActivityInstanceEndHandler ACTIVITI_INSTANCE_END_LISTENER = new ActivityInstanceEndHandler();

  protected static final ActivityInstanceStartHandler ACTIVITY_INSTANCE_START_LISTENER = new ActivityInstanceStartHandler();

  protected static final UserTaskAssignmentHandler USER_TASK_ASSIGNMENT_HANDLER = new UserTaskAssignmentHandler();
  
  protected static final ProcessInstanceEndHandler PROCESS_INSTANCE_END_HANDLER = new ProcessInstanceEndHandler();

  protected static final UserTaskIdHandler USER_TASK_ID_HANDLER = new UserTaskIdHandler();

  public void parseProcess(Process processElement, ProcessDefinitionEntity processDefinition) {
    processDefinition.addExecutionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, PROCESS_INSTANCE_END_HANDLER);
  }

  public void parseExclusiveGateway(ExclusiveGateway exclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseInclusiveGateway(InclusiveGateway inclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseCallActivity(CallActivity callActivityElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseManualTask(ManualTask manualTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseReceiveTask(ReceiveTask receiveTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseScriptTask(ScriptTask scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseTask(Task taskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseUserTask(UserTask userTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
    
    TaskDefinition taskDefinition = ((UserTaskActivityBehavior) activity.getActivityBehavior()).getTaskDefinition();
    taskDefinition.addTaskListener(TaskListener.EVENTNAME_ASSIGNMENT, USER_TASK_ASSIGNMENT_HANDLER);
    taskDefinition.addTaskListener(TaskListener.EVENTNAME_CREATE, USER_TASK_ID_HANDLER);
  }

  public void parseServiceTask(ServiceTask serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseBusinessRuleTask(BusinessRuleTask businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseSubProcess(SubProcess subProcessElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseStartEvent(StartEvent startEventElement, ScopeImpl scope, ActivityImpl activity) {
    activity.addExecutionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, START_EVENT_END_HANDLER);
  }

  public void parseSendTask(SendTask sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseEndEvent(EndEvent endEventElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseParallelGateway(ParallelGateway parallelGwElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);    
  }

  public void parseBoundaryTimerEventDefinition(TimerEventDefinition timerEventDefinition, boolean interrupting, ActivityImpl timerActivity) {
  }

  public void parseBoundaryErrorEventDefinition(ErrorEventDefinition errorEventDefinition, boolean interrupting, ActivityImpl activity, ActivityImpl nestedErrorEventActivity) {
  }

  public void parseIntermediateTimerEventDefinition(TimerEventDefinition timerEventDefinition, ActivityImpl timerActivity) {
  }

  public void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration, ActivityImpl activity) {
  }

  public void parseSequenceFlow(SequenceFlow sequenceFlowElement, ScopeImpl scopeElement, TransitionImpl transition) {
  }

  public void parseRootElement(Element rootElement, List<ProcessDefinitionEntity> processDefinitions) {
  }
  
  public void parseBoundarySignalEventDefinition(SignalEventDefinition signalEventDefinition, boolean interrupting, ActivityImpl signalActivity) {
  }
  
  public void parseEventBasedGateway(EventGateway eventBasedGwElement, ScopeImpl scope, ActivityImpl activity) {
    // TODO: Shall we add audit logging here as well? 
  }
  
  public void parseMultiInstanceLoopCharacteristics(Activity activityElement, 
          MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristicsElement, ActivityImpl activity) {
    // Remove any history parse listeners already attached: the Multi instance behavior will
    // call them for every instance that will be created
  }

  // helper methods ///////////////////////////////////////////////////////////

  protected void addActivityHandlers(ActivityImpl activity) {
      activity.addExecutionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, ACTIVITY_INSTANCE_START_LISTENER, 0);
      activity.addExecutionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, ACTIVITI_INSTANCE_END_LISTENER);
  }

  public void parseIntermediateSignalCatchEventDefinition(SignalEventDefinition signalEventDefinition, ActivityImpl signalActivity) {
  }

  public void parseTransaction(Transaction transaction, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseCompensateEventDefinition(Element compensateEventDefinition, ActivityImpl compensationActivity) {
  }

  public void parseIntermediateThrowEvent(ThrowEvent intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseIntermediateCatchEvent(IntermediateCatchEvent intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseBoundaryEvent(BoundaryEvent boundaryEventElement, ScopeImpl scopeElement, ActivityImpl activity) {
    // TODO: Add to audit logging? Discuss
  }
  
  public void parseIntermediateMessageCatchEventDefinition(MessageEventDefinition messageEventDefinition, ActivityImpl nestedActivity) {
  }

  public void parseBoundaryMessageEventDefinition(MessageEventDefinition element, boolean interrupting, ActivityImpl messageActivity) {
  }

}
