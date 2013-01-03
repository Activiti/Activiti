package org.activiti.engine.impl.bpmn.parser;

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
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.util.xml.Element;
import org.activiti.engine.impl.variable.VariableDeclaration;

/**
 * Abstract base class for implementing a {@link BpmnParseListener} without being forced to implement
 * all methods provided, which makes the implementation more robust to future changes.
 * 
 * @author ruecker
 * @author Joram Barrez
 */
public class AbstractBpmnParseListener implements BpmnParseListener {

  public void parseProcess(Process process, ProcessDefinitionEntity processDefinition) {
  }

  public void parseStartEvent(StartEvent startEvent, ScopeImpl scope, ActivityImpl startEventActivity) {
  }

  public void parseExclusiveGateway(ExclusiveGateway exclusiveGateway, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseInclusiveGateway(InclusiveGateway inclusiveGateway, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseParallelGateway(ParallelGateway parallelGateway, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseScriptTask(ScriptTask scriptTask, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseServiceTask(ServiceTask serviceTask, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseBusinessRuleTask(BusinessRuleTask businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseTask(Task task, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseManualTask(ManualTask manualTask, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseUserTask(UserTask userTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseEndEvent(EndEvent endEventElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseBoundaryTimerEventDefinition(TimerEventDefinition timerEventDefinition, boolean interrupting, ActivityImpl timerActivity) {
  }

  public void parseBoundaryErrorEventDefinition(ErrorEventDefinition errorEventDefinition, boolean interrupting, ActivityImpl activity,
          ActivityImpl nestedErrorEventActivity) {
  }

  public void parseSubProcess(SubProcess subProcess, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseCallActivity(CallActivity callActivity, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration, ActivityImpl activity) {
  }

  public void parseSequenceFlow(SequenceFlow sequenceFlow, ScopeImpl scopeElement, TransitionImpl transition) {
  }

  public void parseSendTask(SendTask sendTask, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseMultiInstanceLoopCharacteristics(Activity modelActivity, MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics,
          ActivityImpl activity) {
  }

  public void parseIntermediateTimerEventDefinition(TimerEventDefinition timerEventDefinition, ActivityImpl timerActivity) {
  }

  public void parseReceiveTask(ReceiveTask receiveTask, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseIntermediateSignalCatchEventDefinition(SignalEventDefinition signalEventDefinition, ActivityImpl signalActivity) {
  }

  public void parseIntermediateMessageCatchEventDefinition(MessageEventDefinition messageEventDefinition, ActivityImpl nestedActivity) {
  }

  public void parseBoundarySignalEventDefinition(SignalEventDefinition signalEventDefinition, boolean interrupting, ActivityImpl signalActivity) {
  }

  public void parseEventBasedGateway(EventGateway eventBasedGateway, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseTransaction(Transaction transaction, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseCompensateEventDefinition(Element compensateEventDefinition, ActivityImpl compensationActivity) {
  }

  public void parseIntermediateThrowEvent(ThrowEvent intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseIntermediateCatchEvent(IntermediateCatchEvent intermediateCatchEvent, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseBoundaryEvent(BoundaryEvent boundaryEvent, ScopeImpl scopeElement, ActivityImpl nestedActivity) {
  }

  public void parseBoundaryMessageEventDefinition(MessageEventDefinition messageEventDefinition, boolean interrupting, ActivityImpl messageActivity) {
  }

}
