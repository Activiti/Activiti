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
package org.activiti.engine.impl.bpmn.parser.factory;

import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.BusinessRuleTask;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.CancelEventDefinition;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.EventGateway;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.InclusiveGateway;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.ManualTask;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.ReceiveTask;
import org.activiti.bpmn.model.ScriptTask;
import org.activiti.bpmn.model.SendTask;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.Task;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.bpmn.model.Transaction;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.CancelBoundaryEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.CancelEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ErrorEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.EventBasedGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.EventSubProcessStartEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ExclusiveGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.InclusiveGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateCatchEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateThrowCompensationEventActivityBehavior;
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
import org.activiti.engine.impl.bpmn.behavior.TransactionActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.WebServiceActivityBehavior;
import org.activiti.engine.impl.bpmn.helper.ClassDelegate;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.bpmn.parser.CompensateEventDefinition;
import org.activiti.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;

/**
 * Factory class used by the {@link BpmnParser} and {@link BpmnParse} to instantiate
 * the behaviour classes. For example when parsing an exclusive gateway, this factory
 * will be requested to create a new {@link ActivityBehavior} that will be set on the 
 * {@link ActivityImpl} of that step of the process and will implement the spec-compliant
 * behavior of the exclusive gateway.
 * 
 * You can provide your own implementation of this class. This way, you can give
 * different execution semantics to a standard bpmn xml construct. Eg. you could
 * tweak the exclusive gateway to do something completely different if you would want that.
 * Creating your own {@link ActivityBehaviorFactory} is only advisable if you
 * want to change the default behavior of any BPMN default construct.
 * And even then, think twice, because it won't be spec compliant bpmn anymore.
 * 
 * Note that you can always express any custom step as a service task with a class delegation.
 * 
 * The easiest and advisable way to implement your own {@link ActivityBehaviorFactory} 
 * is to extend the {@link DefaultActivityBehaviorFactory} class and override 
 * the method specific to the {@link ActivityBehavior} you want to change. 
 * 
 * An instance of this interface can be injected in the {@link ProcessEngineConfigurationImpl}
 * and its subclasses. 
 * 
 * @author Joram Barrez
 */
public interface ActivityBehaviorFactory {

  public abstract NoneStartEventActivityBehavior createNoneStartEventActivityBehavior(StartEvent startEvent);

  public abstract EventSubProcessStartEventActivityBehavior createEventSubProcessStartEventActivityBehavior(StartEvent startEvent, String activityId);

  public abstract TaskActivityBehavior createTaskActivityBehavior(Task task);

  public abstract ManualTaskActivityBehavior createManualTaskActivityBehavior(ManualTask manualTask);

  public abstract ReceiveTaskActivityBehavior createReceiveTaskActivityBehavior(ReceiveTask receiveTask);

  public abstract UserTaskActivityBehavior createUserTaskActivityBehavior(UserTask userTask, TaskDefinition taskDefinition);

  public abstract ClassDelegate createClassDelegateServiceTask(ServiceTask serviceTask);

  public abstract ServiceTaskDelegateExpressionActivityBehavior createServiceTaskDelegateExpressionActivityBehavior(ServiceTask serviceTask);

  public abstract ServiceTaskExpressionActivityBehavior createServiceTaskExpressionActivityBehavior(ServiceTask serviceTask);

  public abstract WebServiceActivityBehavior createWebServiceActivityBehavior(ServiceTask serviceTask);

  public abstract WebServiceActivityBehavior createWebServiceActivityBehavior(SendTask sendTask);

  public abstract MailActivityBehavior createMailActivityBehavior(ServiceTask serviceTask);

  public abstract MailActivityBehavior createMailActivityBehavior(SendTask sendTask);

  // We do not want a hard dependency on the Mule module, hence we return ActivityBehavior and instantiate 
  // the delegate instance using a string instead of the Class itself.
  public abstract ActivityBehavior createMuleActivityBehavior(ServiceTask serviceTask, BpmnModel bpmnModel);

  public abstract ActivityBehavior createMuleActivityBehavior(SendTask sendTask, BpmnModel bpmnModel);
  
  public abstract ActivityBehavior createCamelActivityBehavior(ServiceTask serviceTask, BpmnModel bpmnModel);
 
  public abstract ActivityBehavior createCamelActivityBehavior(SendTask sendTask, BpmnModel bpmnModel);

  public abstract ShellActivityBehavior createShellActivityBehavior(ServiceTask serviceTask);

  public abstract ActivityBehavior createBusinessRuleTaskActivityBehavior(BusinessRuleTask businessRuleTask);

  public abstract ScriptTaskActivityBehavior createScriptTaskActivityBehavior(ScriptTask scriptTask);

  public abstract ExclusiveGatewayActivityBehavior createExclusiveGatewayActivityBehavior(ExclusiveGateway exclusiveGateway);

  public abstract ParallelGatewayActivityBehavior createParallelGatewayActivityBehavior(ParallelGateway parallelGateway);

  public abstract InclusiveGatewayActivityBehavior createInclusiveGatewayActivityBehavior(InclusiveGateway inclusiveGateway);

  public abstract EventBasedGatewayActivityBehavior createEventBasedGatewayActivityBehavior(EventGateway eventGateway);

  public abstract SequentialMultiInstanceBehavior createSequentialMultiInstanceBehavior(ActivityImpl activity, AbstractBpmnActivityBehavior innerActivityBehavior);

  public abstract ParallelMultiInstanceBehavior createParallelMultiInstanceBehavior(ActivityImpl activity, AbstractBpmnActivityBehavior innerActivityBehavior);

  public abstract SubProcessActivityBehavior createSubprocActivityBehavior(SubProcess subProcess);

  public abstract CallActivityBehavior createCallActivityBehavior(CallActivity callActivity);

  public abstract TransactionActivityBehavior createTransactionActivityBehavior(Transaction transaction);

  public abstract IntermediateCatchEventActivityBehavior createIntermediateCatchEventActivityBehavior(IntermediateCatchEvent intermediateCatchEvent);

  public abstract IntermediateThrowNoneEventActivityBehavior createIntermediateThrowNoneEventActivityBehavior(ThrowEvent throwEvent);

  public abstract IntermediateThrowSignalEventActivityBehavior createIntermediateThrowSignalEventActivityBehavior(ThrowEvent throwEvent, Signal signal, EventSubscriptionDeclaration eventSubscriptionDeclaration);

  public abstract IntermediateThrowCompensationEventActivityBehavior createIntermediateThrowCompensationEventActivityBehavior(ThrowEvent throwEvent, CompensateEventDefinition compensateEventDefinition);

  public abstract NoneEndEventActivityBehavior createNoneEndEventActivityBehavior(EndEvent endEvent);

  public abstract ErrorEndEventActivityBehavior createErrorEndEventActivityBehavior(EndEvent endEvent, ErrorEventDefinition errorEventDefinition);

  public abstract CancelEndEventActivityBehavior createCancelEndEventActivityBehavior(EndEvent endEvent);

  public abstract TerminateEndEventActivityBehavior createTerminateEndEventActivityBehavior(EndEvent endEvent);

  public abstract BoundaryEventActivityBehavior createBoundaryEventActivityBehavior(BoundaryEvent boundaryEvent, boolean interrupting, ActivityImpl activity);

  public abstract CancelBoundaryEventActivityBehavior createCancelBoundaryEventActivityBehavior(CancelEventDefinition cancelEventDefinition);

}