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
package org.activiti.engine.test.bpmn.event.signal;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.repository.ProcessDefinition;
/**
 * @author Fritsche
 */
public class StartProcessInstanceDelegate implements JavaDelegate {


	public void execute(DelegateExecution execution) {
		ProcessDefinition processDefinition = Context.getProcessEngineConfiguration()
				.getDeploymentManager()
				.findDeployedLatestProcessDefinitionByKey("subProcessTask");

		Process subProcess = ProcessDefinitionUtil.getProcess(processDefinition.getId());
		FlowElement initialFlowElement = subProcess.getInitialFlowElement();
		ExecutionEntity executionEntity = (ExecutionEntity) execution;

		ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();
		ExecutionEntity subProcessInstance = executionEntityManager.createSubprocessInstance(processDefinition,
				executionEntity,null);

		Context.getCommandContext().getHistoryManager()
		.recordSubProcessInstanceStart(executionEntity, subProcessInstance, initialFlowElement);

		ExecutionEntity subProcessInitialExecution = executionEntityManager.createChildExecution(subProcessInstance);
		subProcessInitialExecution.setCurrentFlowElement(initialFlowElement);

		Context.getAgenda().planContinueProcessOperation(subProcessInitialExecution);

		Context.getProcessEngineConfiguration().getEventDispatcher()
		.dispatchEvent(ActivitiEventBuilder.createProcessStartedEvent(subProcessInitialExecution,null,false));

	}

}