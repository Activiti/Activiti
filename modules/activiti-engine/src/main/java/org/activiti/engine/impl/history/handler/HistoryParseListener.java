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

import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.BpmnParseListener;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
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

  protected static final UserTaskIdHandler USER_TASK_ID_HANDLER = new UserTaskIdHandler();

  // The history level set in the Activiti configuration
  protected int historyLevel;

  public HistoryParseListener(int historyLevel) {
    this.historyLevel = historyLevel;
  }

  public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
    if (activityHistoryEnabled(processDefinition, historyLevel)) {
      processDefinition.addExecutionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, new ProcessInstanceEndHandler());
    }
  }

  public void parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseInclusiveGateway(Element inclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseReceiveTask(Element receiveTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);

    if (activityHistoryEnabled(scope, historyLevel)) {
      TaskDefinition taskDefinition = ((UserTaskActivityBehavior) activity.getActivityBehavior()).getTaskDefinition();
      taskDefinition.addTaskListener(TaskListener.EVENTNAME_ASSIGNMENT, USER_TASK_ASSIGNMENT_HANDLER);
      taskDefinition.addTaskListener(TaskListener.EVENTNAME_CREATE, USER_TASK_ID_HANDLER);
    }
  }

  public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl activity) {
    if (activityHistoryEnabled(activity, historyLevel)) {
      activity.addExecutionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, START_EVENT_END_HANDLER);
    }
  }

  public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseParallelGateway(Element parallelGwElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);    
  }

  public void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting, ActivityImpl timerActivity) {
  }

  public void parseBoundaryErrorEventDefinition(Element errorEventDefinition, boolean interrupting, ActivityImpl activity, ActivityImpl nestedErrorEventActivity) {
  }

  public void parseIntermediateTimerEventDefinition(Element timerEventDefinition, ActivityImpl timerActivity) {
  }

  public void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration, ActivityImpl activity) {
  }

  public void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scopeElement, TransitionImpl transition) {
  }

  public void parseRootElement(Element rootElement, List<ProcessDefinitionEntity> processDefinitions) {
  }
  
  public void parseBoundarySignalEventDefinition(Element signalEventDefinition, boolean interrupting, ActivityImpl signalActivity) {
  }
  
  public void parseEventBasedGateway(Element eventBasedGwElement, ScopeImpl scope, ActivityImpl activity) {
    // TODO: Shall we add audit logging here as well? 
  }
  
  public void parseMultiInstanceLoopCharacteristics(Element activityElement, 
          Element multiInstanceLoopCharacteristicsElement, ActivityImpl activity) {
    // Remove any history parse listeners already attached: the Multi instance behavior will
    // call them for every instance that will be created
  }

  // helper methods ///////////////////////////////////////////////////////////

  protected void addActivityHandlers(ActivityImpl activity) {
    if (activityHistoryEnabled(activity, historyLevel)) {
      activity.addExecutionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, ACTIVITY_INSTANCE_START_LISTENER, 0);
      activity.addExecutionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, ACTIVITI_INSTANCE_END_LISTENER);
    }
  }

  public static boolean fullHistoryEnabled(int historyLevel) {
    return historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_FULL;
  }

  public static boolean auditHistoryEnabled(ScopeImpl scopeElement, int historyLevel) {
    return historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT;
  }

  public static boolean variableHistoryEnabled(ScopeImpl scopeElement, int historyLevel) {
    return historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY;
  }
  
  public static boolean activityHistoryEnabled(ScopeImpl scopeElement, int historyLevel) {
    return historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY;
  }
  
  public void parseIntermediateSignalCatchEventDefinition(Element signalEventDefinition, ActivityImpl signalActivity) {
  }

  public void parseTransaction(Element transactionElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseCompensateEventDefinition(Element compensateEventDefinition, ActivityImpl compensationActivity) {
  }

  public void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  public void parseBoundaryEvent(Element boundaryEventElement, ScopeImpl scopeElement, ActivityImpl activity) {
    // TODO: Add to audit logging? Discuss
  }
  
  public void parseIntermediateMessageCatchEventDefinition(Element messageEventDefinition, ActivityImpl nestedActivity) {
  }

  public void parseBoundaryMessageEventDefinition(Element element, boolean interrupting, ActivityImpl messageActivity) {
  }

}
