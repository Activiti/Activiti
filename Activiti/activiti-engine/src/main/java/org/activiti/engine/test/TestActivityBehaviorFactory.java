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
package org.activiti.engine.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BusinessRuleTask;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.CancelEventDefinition;
import org.activiti.bpmn.model.CompensateEventDefinition;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.EventGateway;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.InclusiveGateway;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.ManualTask;
import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.ReceiveTask;
import org.activiti.bpmn.model.ScriptTask;
import org.activiti.bpmn.model.SendTask;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.Task;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.bpmn.model.Transaction;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.AdhocSubProcessActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.BoundaryCancelEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.BoundaryCompensateEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.BoundaryMessageEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.BoundarySignalEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.BoundaryTimerEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.CancelEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ErrorEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.EventBasedGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.EventSubProcessErrorStartEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.EventSubProcessMessageStartEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ExclusiveGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.InclusiveGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateCatchEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateCatchMessageEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateCatchSignalEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateCatchTimerEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateThrowCompensationEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateThrowMessageEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateThrowNoneEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateThrowSignalEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.MailActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ManualTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.NoneEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.NoneStartEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ParallelGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.ReceiveTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ScriptTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.ServiceTaskDelegateExpressionActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ServiceTaskExpressionActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ShellActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.SubProcessActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.TaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.TerminateEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ThrowMessageEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.TransactionActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.WebServiceActivityBehavior;
import org.activiti.engine.impl.bpmn.helper.ClassDelegate;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.bpmn.parser.factory.AbstractBehaviorFactory;
import org.activiti.engine.impl.bpmn.parser.factory.ActivityBehaviorFactory;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.el.FixedValue;
import org.activiti.engine.impl.test.NoOpServiceTask;

public class TestActivityBehaviorFactory extends AbstractBehaviorFactory implements ActivityBehaviorFactory {

  /**
   * The ActivityBehaviorFactory that is constructed when the process engine was created This class delegates to this instance, unless some mocking has been defined.
   */
  protected ActivityBehaviorFactory wrappedActivityBehaviorFactory;

  protected boolean allServiceTasksNoOp;
  protected Map<String, String> mockedClassDelegatesMapping = new HashMap<String, String>();
  protected Set<String> noOpServiceTaskIds = new HashSet<String>();
  protected Set<String> noOpServiceTaskClassNames = new HashSet<String>();

  public TestActivityBehaviorFactory() {

  }

  public TestActivityBehaviorFactory(ActivityBehaviorFactory wrappedActivityBehaviorFactory) {
    this.wrappedActivityBehaviorFactory = wrappedActivityBehaviorFactory;
  }

  public ActivityBehaviorFactory getWrappedActivityBehaviorFactory() {
    return wrappedActivityBehaviorFactory;
  }

  public void setWrappedActivityBehaviorFactory(ActivityBehaviorFactory wrappedActivityBehaviorFactory) {
    this.wrappedActivityBehaviorFactory = wrappedActivityBehaviorFactory;
  }

  @Override
  public NoneStartEventActivityBehavior createNoneStartEventActivityBehavior(StartEvent startEvent) {
    return wrappedActivityBehaviorFactory.createNoneStartEventActivityBehavior(startEvent);
  }

  @Override
  public TaskActivityBehavior createTaskActivityBehavior(Task task) {
    return wrappedActivityBehaviorFactory.createTaskActivityBehavior(task);
  }

  @Override
  public ManualTaskActivityBehavior createManualTaskActivityBehavior(ManualTask manualTask) {
    return wrappedActivityBehaviorFactory.createManualTaskActivityBehavior(manualTask);
  }

  @Override
  public ReceiveTaskActivityBehavior createReceiveTaskActivityBehavior(ReceiveTask receiveTask) {
    return wrappedActivityBehaviorFactory.createReceiveTaskActivityBehavior(receiveTask);
  }
  
  @Override
  public UserTaskActivityBehavior createUserTaskActivityBehavior(UserTask userTask) {
    return wrappedActivityBehaviorFactory.createUserTaskActivityBehavior(userTask);
  }

  @Override
  public ClassDelegate createClassDelegateServiceTask(ServiceTask serviceTask) {

    if (allServiceTasksNoOp || noOpServiceTaskIds.contains(serviceTask.getId()) || noOpServiceTaskClassNames.contains(serviceTask.getImplementation())) {

      return createNoOpServiceTask(serviceTask);

    } else if (serviceTask.getImplementation() != null && mockedClassDelegatesMapping.containsKey(serviceTask.getImplementation())) {

      return new ClassDelegate(mockedClassDelegatesMapping.get(serviceTask.getImplementation()), createFieldDeclarations(serviceTask.getFieldExtensions()));

    }

    return wrappedActivityBehaviorFactory.createClassDelegateServiceTask(serviceTask);
  }

  private ClassDelegate createNoOpServiceTask(ServiceTask serviceTask) {
    List<FieldDeclaration> fieldDeclarations = new ArrayList<FieldDeclaration>();
    fieldDeclarations.add(new FieldDeclaration("name", Expression.class.getName(), new FixedValue(serviceTask.getImplementation())));
    return new ClassDelegate(NoOpServiceTask.class, fieldDeclarations);
  }

  @Override
  public ServiceTaskDelegateExpressionActivityBehavior createServiceTaskDelegateExpressionActivityBehavior(ServiceTask serviceTask) {
    return wrappedActivityBehaviorFactory.createServiceTaskDelegateExpressionActivityBehavior(serviceTask);
  }

  @Override
  public ActivityBehavior createDefaultServiceTaskBehavior(ServiceTask serviceTask) {
    return wrappedActivityBehaviorFactory.createDefaultServiceTaskBehavior(serviceTask);
  }

  @Override
  public ServiceTaskExpressionActivityBehavior createServiceTaskExpressionActivityBehavior(ServiceTask serviceTask) {
    return wrappedActivityBehaviorFactory.createServiceTaskExpressionActivityBehavior(serviceTask);
  }

  @Override
  public WebServiceActivityBehavior createWebServiceActivityBehavior(ServiceTask serviceTask) {
    return wrappedActivityBehaviorFactory.createWebServiceActivityBehavior(serviceTask);
  }

  @Override
  public WebServiceActivityBehavior createWebServiceActivityBehavior(SendTask sendTask) {
    return wrappedActivityBehaviorFactory.createWebServiceActivityBehavior(sendTask);
  }

  @Override
  public MailActivityBehavior createMailActivityBehavior(ServiceTask serviceTask) {
    return wrappedActivityBehaviorFactory.createMailActivityBehavior(serviceTask);
  }

  @Override
  public MailActivityBehavior createMailActivityBehavior(SendTask sendTask) {
    return wrappedActivityBehaviorFactory.createMailActivityBehavior(sendTask);
  }

  @Override
  public ActivityBehavior createMuleActivityBehavior(ServiceTask serviceTask) {
    return wrappedActivityBehaviorFactory.createMuleActivityBehavior(serviceTask);
  }

  @Override
  public ActivityBehavior createMuleActivityBehavior(SendTask sendTask) {
    return wrappedActivityBehaviorFactory.createMuleActivityBehavior(sendTask);
  }

  @Override
  public ActivityBehavior createCamelActivityBehavior(ServiceTask serviceTask) {
    return wrappedActivityBehaviorFactory.createCamelActivityBehavior(serviceTask);
  }

  @Override
  public ActivityBehavior createCamelActivityBehavior(SendTask sendTask) {
    return wrappedActivityBehaviorFactory.createCamelActivityBehavior(sendTask);
  }

  @Override
  public ShellActivityBehavior createShellActivityBehavior(ServiceTask serviceTask) {
    return wrappedActivityBehaviorFactory.createShellActivityBehavior(serviceTask);
  }

  @Override
  public ActivityBehavior createBusinessRuleTaskActivityBehavior(BusinessRuleTask businessRuleTask) {
    return wrappedActivityBehaviorFactory.createBusinessRuleTaskActivityBehavior(businessRuleTask);
  }

  @Override
  public ScriptTaskActivityBehavior createScriptTaskActivityBehavior(ScriptTask scriptTask) {
    return wrappedActivityBehaviorFactory.createScriptTaskActivityBehavior(scriptTask);
  }

  @Override
  public ExclusiveGatewayActivityBehavior createExclusiveGatewayActivityBehavior(ExclusiveGateway exclusiveGateway) {
    return wrappedActivityBehaviorFactory.createExclusiveGatewayActivityBehavior(exclusiveGateway);
  }

  @Override
  public ParallelGatewayActivityBehavior createParallelGatewayActivityBehavior(ParallelGateway parallelGateway) {
    return wrappedActivityBehaviorFactory.createParallelGatewayActivityBehavior(parallelGateway);
  }

  @Override
  public InclusiveGatewayActivityBehavior createInclusiveGatewayActivityBehavior(InclusiveGateway inclusiveGateway) {
    return wrappedActivityBehaviorFactory.createInclusiveGatewayActivityBehavior(inclusiveGateway);
  }

  @Override
  public EventBasedGatewayActivityBehavior createEventBasedGatewayActivityBehavior(EventGateway eventGateway) {
    return wrappedActivityBehaviorFactory.createEventBasedGatewayActivityBehavior(eventGateway);
  }

  @Override
  public SequentialMultiInstanceBehavior createSequentialMultiInstanceBehavior(Activity activity, AbstractBpmnActivityBehavior innerActivityBehavior) {
    return wrappedActivityBehaviorFactory.createSequentialMultiInstanceBehavior(activity, innerActivityBehavior);
  }

  @Override
  public ParallelMultiInstanceBehavior createParallelMultiInstanceBehavior(Activity activity, AbstractBpmnActivityBehavior innerActivityBehavior) {
    return wrappedActivityBehaviorFactory.createParallelMultiInstanceBehavior(activity, innerActivityBehavior);
  }

  @Override
  public SubProcessActivityBehavior createSubprocessActivityBehavior(SubProcess subProcess) {
    return wrappedActivityBehaviorFactory.createSubprocessActivityBehavior(subProcess);
  }
  
  @Override
  public EventSubProcessErrorStartEventActivityBehavior createEventSubProcessErrorStartEventActivityBehavior(StartEvent startEvent) {
    return wrappedActivityBehaviorFactory.createEventSubProcessErrorStartEventActivityBehavior(startEvent);
  }
  
  @Override
  public EventSubProcessMessageStartEventActivityBehavior createEventSubProcessMessageStartEventActivityBehavior(StartEvent startEvent, MessageEventDefinition messageEventDefinition) {
    return wrappedActivityBehaviorFactory.createEventSubProcessMessageStartEventActivityBehavior(startEvent, messageEventDefinition);
  }
  
  @Override
  public AdhocSubProcessActivityBehavior createAdhocSubprocessActivityBehavior(SubProcess subProcess) {
    return wrappedActivityBehaviorFactory.createAdhocSubprocessActivityBehavior(subProcess);
  }

  @Override
  public CallActivityBehavior createCallActivityBehavior(CallActivity callActivity) {
    return wrappedActivityBehaviorFactory.createCallActivityBehavior(callActivity);
  }

  @Override
  public TransactionActivityBehavior createTransactionActivityBehavior(Transaction transaction) {
    return wrappedActivityBehaviorFactory.createTransactionActivityBehavior(transaction);
  }

  @Override
  public IntermediateCatchEventActivityBehavior createIntermediateCatchEventActivityBehavior(IntermediateCatchEvent intermediateCatchEvent) {
    return wrappedActivityBehaviorFactory.createIntermediateCatchEventActivityBehavior(intermediateCatchEvent);
  }

  @Override
  public IntermediateCatchMessageEventActivityBehavior createIntermediateCatchMessageEventActivityBehavior(IntermediateCatchEvent intermediateCatchEvent, MessageEventDefinition messageEventDefinition) {

    return wrappedActivityBehaviorFactory.createIntermediateCatchMessageEventActivityBehavior(intermediateCatchEvent, messageEventDefinition);
  }

  @Override
  public IntermediateCatchTimerEventActivityBehavior createIntermediateCatchTimerEventActivityBehavior(IntermediateCatchEvent intermediateCatchEvent, TimerEventDefinition timerEventDefinition) {
    return wrappedActivityBehaviorFactory.createIntermediateCatchTimerEventActivityBehavior(intermediateCatchEvent, timerEventDefinition);
  }

  @Override
  public IntermediateCatchSignalEventActivityBehavior createIntermediateCatchSignalEventActivityBehavior(IntermediateCatchEvent intermediateCatchEvent, SignalEventDefinition signalEventDefinition,
      Signal signal) {

    return wrappedActivityBehaviorFactory.createIntermediateCatchSignalEventActivityBehavior(intermediateCatchEvent, signalEventDefinition, signal);
  }

  @Override
  public IntermediateThrowNoneEventActivityBehavior createIntermediateThrowNoneEventActivityBehavior(ThrowEvent throwEvent) {
    return wrappedActivityBehaviorFactory.createIntermediateThrowNoneEventActivityBehavior(throwEvent);
  }

  @Override
  public IntermediateThrowSignalEventActivityBehavior createIntermediateThrowSignalEventActivityBehavior(ThrowEvent throwEvent, SignalEventDefinition signalEventDefinition, Signal signal) {

    return wrappedActivityBehaviorFactory.createIntermediateThrowSignalEventActivityBehavior(throwEvent, signalEventDefinition, signal);
  }

  @Override
  public IntermediateThrowCompensationEventActivityBehavior createIntermediateThrowCompensationEventActivityBehavior(ThrowEvent throwEvent, CompensateEventDefinition compensateEventDefinition) {
    return wrappedActivityBehaviorFactory.createIntermediateThrowCompensationEventActivityBehavior(throwEvent, compensateEventDefinition);
  }

  @Override
  public NoneEndEventActivityBehavior createNoneEndEventActivityBehavior(EndEvent endEvent) {
    return wrappedActivityBehaviorFactory.createNoneEndEventActivityBehavior(endEvent);
  }

  @Override
  public ErrorEndEventActivityBehavior createErrorEndEventActivityBehavior(EndEvent endEvent, ErrorEventDefinition errorEventDefinition) {
    return wrappedActivityBehaviorFactory.createErrorEndEventActivityBehavior(endEvent, errorEventDefinition);
  }

  @Override
  public CancelEndEventActivityBehavior createCancelEndEventActivityBehavior(EndEvent endEvent) {
    return wrappedActivityBehaviorFactory.createCancelEndEventActivityBehavior(endEvent);
  }

  @Override
  public TerminateEndEventActivityBehavior createTerminateEndEventActivityBehavior(EndEvent endEvent) {
    return wrappedActivityBehaviorFactory.createTerminateEndEventActivityBehavior(endEvent);
  }

  @Override
  public BoundaryEventActivityBehavior createBoundaryEventActivityBehavior(BoundaryEvent boundaryEvent, boolean interrupting) {
    return wrappedActivityBehaviorFactory.createBoundaryEventActivityBehavior(boundaryEvent, interrupting);
  }

  @Override
  public BoundaryCancelEventActivityBehavior createBoundaryCancelEventActivityBehavior(CancelEventDefinition cancelEventDefinition) {
    return wrappedActivityBehaviorFactory.createBoundaryCancelEventActivityBehavior(cancelEventDefinition);
  }

  @Override
  public BoundaryTimerEventActivityBehavior createBoundaryTimerEventActivityBehavior(BoundaryEvent boundaryEvent, TimerEventDefinition timerEventDefinition, boolean interrupting) {
    return wrappedActivityBehaviorFactory.createBoundaryTimerEventActivityBehavior(boundaryEvent, timerEventDefinition, interrupting);
  }

  @Override
  public BoundarySignalEventActivityBehavior createBoundarySignalEventActivityBehavior(BoundaryEvent boundaryEvent, SignalEventDefinition signalEventDefinition, Signal signal, boolean interrupting) {
    return wrappedActivityBehaviorFactory.createBoundarySignalEventActivityBehavior(boundaryEvent, signalEventDefinition, signal, interrupting);
  }

  @Override
  public BoundaryMessageEventActivityBehavior createBoundaryMessageEventActivityBehavior(BoundaryEvent boundaryEvent, MessageEventDefinition messageEventDefinition, boolean interrupting) {
    return wrappedActivityBehaviorFactory.createBoundaryMessageEventActivityBehavior(boundaryEvent, messageEventDefinition, interrupting);
  }
  
  @Override
  public BoundaryCompensateEventActivityBehavior createBoundaryCompensateEventActivityBehavior(BoundaryEvent boundaryEvent, CompensateEventDefinition compensateEventDefinition, boolean interrupting) {
    return wrappedActivityBehaviorFactory.createBoundaryCompensateEventActivityBehavior(boundaryEvent, compensateEventDefinition, interrupting);
  }

  @Override
  public IntermediateThrowMessageEventActivityBehavior createThrowMessageEventActivityBehavior(ThrowEvent throwEvent,
                                                                                               MessageEventDefinition messageEventDefinition,
                                                                                               Message message) {
      return wrappedActivityBehaviorFactory.createThrowMessageEventActivityBehavior(throwEvent, 
                                                                                    messageEventDefinition, 
                                                                                    message);
  }
  @Override
  public ThrowMessageEndEventActivityBehavior createThrowMessageEndEventActivityBehavior(EndEvent endEvent,
                                                                                         MessageEventDefinition messageEventDefinition,
                                                                                         Message message) {
      return wrappedActivityBehaviorFactory.createThrowMessageEndEventActivityBehavior(endEvent, 
                                                                                       messageEventDefinition, 
                                                                                       message);
  }
  
  // Mock support //////////////////////////////////////////////////////

  public void addClassDelegateMock(String originalClassFqn, Class<?> mockClass) {
    mockedClassDelegatesMapping.put(originalClassFqn, mockClass.getName());
  }

  public void addClassDelegateMock(String originalClassFqn, String mockedClassFqn) {
    mockedClassDelegatesMapping.put(originalClassFqn, mockedClassFqn);
  }

  public void addNoOpServiceTaskById(String id) {
    noOpServiceTaskIds.add(id);
  }

  public void addNoOpServiceTaskByClassName(String className) {
    noOpServiceTaskClassNames.add(className);
  }

  public void setAllServiceTasksNoOp() {
    allServiceTasksNoOp = true;
  }

  public void reset() {
    this.mockedClassDelegatesMapping.clear();

    this.noOpServiceTaskIds.clear();
    this.noOpServiceTaskClassNames.clear();

    allServiceTasksNoOp = false;
    NoOpServiceTask.reset();
  }

}
