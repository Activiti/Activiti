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
import org.activiti.engine.delegate.Expression;
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
import org.activiti.engine.impl.bpmn.parser.CompensateEventDefinition;
import org.activiti.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.bpmn.parser.factory.AbstractBehaviorFactory;
import org.activiti.engine.impl.bpmn.parser.factory.ActivityBehaviorFactory;
import org.activiti.engine.impl.el.FixedValue;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.impl.test.NoOpServiceTask;

/**
 * @author Joram Barrez
 */
public class TestActivityBehaviorFactory extends AbstractBehaviorFactory implements ActivityBehaviorFactory {
	
	/** 
	 * The ActivityBehaviorFactory that is constructed when the process engine was created
	 * This class delegates to this instance, unless some mocking has been defined. 
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

	public void setWrappedActivityBehaviorFactory(
			ActivityBehaviorFactory wrappedActivityBehaviorFactory) {
		this.wrappedActivityBehaviorFactory = wrappedActivityBehaviorFactory;
	}

	@Override
	public NoneStartEventActivityBehavior createNoneStartEventActivityBehavior(StartEvent startEvent) {
		return wrappedActivityBehaviorFactory.createNoneStartEventActivityBehavior(startEvent);
	}

	@Override
	public EventSubProcessStartEventActivityBehavior createEventSubProcessStartEventActivityBehavior(StartEvent startEvent, String activityId) {
		return wrappedActivityBehaviorFactory.createEventSubProcessStartEventActivityBehavior(startEvent, activityId);
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
	public UserTaskActivityBehavior createUserTaskActivityBehavior(
			UserTask userTask, TaskDefinition taskDefinition) {
		return wrappedActivityBehaviorFactory.createUserTaskActivityBehavior(userTask, taskDefinition);
	}

	@Override
	public ClassDelegate createClassDelegateServiceTask(ServiceTask serviceTask) {
		
		if (allServiceTasksNoOp 
				|| noOpServiceTaskIds.contains(serviceTask.getId())
				|| noOpServiceTaskClassNames.contains(serviceTask.getImplementation())) {
			
			return createNoOpServiceTask(serviceTask);
			
		} else if (serviceTask.getImplementation() != null && mockedClassDelegatesMapping.containsKey(serviceTask.getImplementation())) {
				
			return new ClassDelegate(mockedClassDelegatesMapping.get(serviceTask.getImplementation()),
					createFieldDeclarations(serviceTask.getFieldExtensions()));
			
		}
		
		return wrappedActivityBehaviorFactory.createClassDelegateServiceTask(serviceTask);
	}

	private ClassDelegate createNoOpServiceTask(ServiceTask serviceTask) {
	  List<FieldDeclaration> fieldDeclarations = new ArrayList<FieldDeclaration>();
	  fieldDeclarations.add(new FieldDeclaration("name", 
	  		Expression.class.getName(), new FixedValue(serviceTask.getImplementation())));
	  return new ClassDelegate(NoOpServiceTask.class, fieldDeclarations);
  }

	@Override
	public ServiceTaskDelegateExpressionActivityBehavior createServiceTaskDelegateExpressionActivityBehavior(ServiceTask serviceTask) {
		return wrappedActivityBehaviorFactory.createServiceTaskDelegateExpressionActivityBehavior(serviceTask);
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
	public ActivityBehavior createMuleActivityBehavior(ServiceTask serviceTask,BpmnModel bpmnModel) {
		return wrappedActivityBehaviorFactory.createMuleActivityBehavior(serviceTask, bpmnModel);
	}

	@Override
	public ActivityBehavior createMuleActivityBehavior(SendTask sendTask,BpmnModel bpmnModel) {
		return wrappedActivityBehaviorFactory.createMuleActivityBehavior(sendTask, bpmnModel);
	}

	@Override
	public ActivityBehavior createCamelActivityBehavior(ServiceTask serviceTask, BpmnModel bpmnModel) {
		return wrappedActivityBehaviorFactory.createCamelActivityBehavior(serviceTask, bpmnModel);
	}

	@Override
	public ActivityBehavior createCamelActivityBehavior(SendTask sendTask,BpmnModel bpmnModel) {
		return wrappedActivityBehaviorFactory.createCamelActivityBehavior(sendTask, bpmnModel);
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
	public SequentialMultiInstanceBehavior createSequentialMultiInstanceBehavior(
			ActivityImpl activity, AbstractBpmnActivityBehavior innerActivityBehavior) {
		return wrappedActivityBehaviorFactory.createSequentialMultiInstanceBehavior(activity, innerActivityBehavior);
	}

	@Override
	public ParallelMultiInstanceBehavior createParallelMultiInstanceBehavior(
			ActivityImpl activity, AbstractBpmnActivityBehavior innerActivityBehavior) {
		return wrappedActivityBehaviorFactory.createParallelMultiInstanceBehavior(activity, innerActivityBehavior);
	}

	@Override
	public SubProcessActivityBehavior createSubprocActivityBehavior(SubProcess subProcess) {
		return wrappedActivityBehaviorFactory.createSubprocActivityBehavior(subProcess);
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
	public IntermediateCatchEventActivityBehavior createIntermediateCatchEventActivityBehavior(
			IntermediateCatchEvent intermediateCatchEvent) {
		return wrappedActivityBehaviorFactory.createIntermediateCatchEventActivityBehavior(intermediateCatchEvent);
	}

	@Override
	public IntermediateThrowNoneEventActivityBehavior createIntermediateThrowNoneEventActivityBehavior(ThrowEvent throwEvent) {
		return wrappedActivityBehaviorFactory.createIntermediateThrowNoneEventActivityBehavior(throwEvent);
	}

	@Override
	public IntermediateThrowSignalEventActivityBehavior createIntermediateThrowSignalEventActivityBehavior(
			ThrowEvent throwEvent, Signal signal,
			EventSubscriptionDeclaration eventSubscriptionDeclaration) {
		return wrappedActivityBehaviorFactory.createIntermediateThrowSignalEventActivityBehavior(
				throwEvent, signal, eventSubscriptionDeclaration);
	}

	@Override
	public IntermediateThrowCompensationEventActivityBehavior createIntermediateThrowCompensationEventActivityBehavior(
			ThrowEvent throwEvent, CompensateEventDefinition compensateEventDefinition) {
		return wrappedActivityBehaviorFactory.createIntermediateThrowCompensationEventActivityBehavior(throwEvent, compensateEventDefinition);
	}

	@Override
	public NoneEndEventActivityBehavior createNoneEndEventActivityBehavior(EndEvent endEvent) {
		return wrappedActivityBehaviorFactory.createNoneEndEventActivityBehavior(endEvent);
	}

	@Override
	public ErrorEndEventActivityBehavior createErrorEndEventActivityBehavior(
			EndEvent endEvent, ErrorEventDefinition errorEventDefinition) {
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
	public BoundaryEventActivityBehavior createBoundaryEventActivityBehavior(
			BoundaryEvent boundaryEvent, boolean interrupting,
			ActivityImpl activity) {
		return wrappedActivityBehaviorFactory.createBoundaryEventActivityBehavior(boundaryEvent, interrupting, activity);
	}

	@Override
	public CancelBoundaryEventActivityBehavior createCancelBoundaryEventActivityBehavior(
			CancelEventDefinition cancelEventDefinition) {
		return wrappedActivityBehaviorFactory.createCancelBoundaryEventActivityBehavior(cancelEventDefinition);
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
