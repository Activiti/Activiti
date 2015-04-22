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
package org.activiti.engine.impl.agenda;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntityManager;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;

/**
 * @author Joram Barrez
 */
public abstract class AbstractOperation implements Runnable {

	protected CommandContext commandContext;
    protected Agenda agenda;
    protected ActivityExecution execution;

    public AbstractOperation() {

    }

    public AbstractOperation(CommandContext commandContext, ActivityExecution execution) {
    	this.commandContext = commandContext;
        this.execution = execution;
        this.agenda = commandContext.getAgenda();
    }

    /**
     * Helper method to match the activityId of an execution with a FlowElement
     * of the process definition referenced by the execution.
     */
    protected FlowElement findCurrentFlowElement(final ActivityExecution execution) {
        String processDefinitionId = execution.getProcessDefinitionId();
        org.activiti.bpmn.model.Process process = ProcessDefinitionUtil.getProcess(processDefinitionId);
        String activityId = execution.getCurrentActivityId();
        FlowElement currentFlowElement = process.getFlowElement(activityId, true);
        execution.setCurrentFlowElement(currentFlowElement);
        return currentFlowElement;
    }

    protected void deleteExecution(CommandContext commandContext, ExecutionEntity executionEntity) {
        OperationUtil.deleteDataRelatedToExecution(commandContext, executionEntity);
        commandContext.getExecutionEntityManager().delete(executionEntity); // TODO: what about delete reason?
    }

    protected void deleteProcessInstanceExecutionEntity(CommandContext commandContext, ExecutionEntityManager executionEntityManager, String processInstanceId) {

        IdentityLinkEntityManager identityLinkEntityManager = commandContext.getIdentityLinkEntityManager();
        List<IdentityLinkEntity> identityLinkEntities = identityLinkEntityManager.findIdentityLinksByProcessInstanceId(processInstanceId);
        for (IdentityLinkEntity identityLinkEntity : identityLinkEntities) {
            identityLinkEntityManager.delete(identityLinkEntity);
        }

        ExecutionEntity processInstanceEntity = executionEntityManager.findExecutionById(processInstanceId);
        deleteExecution(commandContext, processInstanceEntity);
        
        // TODO: what about delete reason?
        Context.getCommandContext().getHistoryManager()
        	.recordProcessInstanceEnd(processInstanceId, "finished", execution.getCurrentFlowElement() != null ? execution.getCurrentFlowElement().getId() : null);
    }

    protected void deleteChildExecutions(CommandContext commandContext, ExecutionEntity executionEntity) {

        // The children of an execution for a tree. For correct deletions
        // (taking care of foreign keys between child-parent)
        // the leafs of this tree must be deleted first before the parents
        // elements.

        // Gather all children
        List<ExecutionEntity> childExecutionEntities = new ArrayList<ExecutionEntity>();
        LinkedList<ExecutionEntity> uncheckedExecutions = new LinkedList<ExecutionEntity>(executionEntity.getExecutions());
        while (!uncheckedExecutions.isEmpty()) {
            ExecutionEntity currentExecutionentity = uncheckedExecutions.pop();
            childExecutionEntities.add(currentExecutionentity);
            uncheckedExecutions.addAll(currentExecutionentity.getExecutions());
        }

        // Delete them (reverse order : leafs of the tree first)
        for (int i = childExecutionEntities.size() - 1; i >= 0; i--) {
            ExecutionEntity childExecutionEntity = childExecutionEntities.get(i);
            if (childExecutionEntity.isActive() && !childExecutionEntity.isEnded()) {
                OperationUtil.deleteDataRelatedToExecution(commandContext, childExecutionEntity);
                commandContext.getExecutionEntityManager().delete(childExecutionEntity);
            }
        }

    }
    
    public CommandContext getCommandContext() {
		return commandContext;
	}

	public void setCommandContext(CommandContext commandContext) {
		this.commandContext = commandContext;
	}

	public Agenda getAgenda() {
        return agenda;
    }

    public void setAgenda(Agenda agenda) {
        this.agenda = agenda;
    }

    public ActivityExecution getExecution() {
        return execution;
    }

    public void setExecution(ActivityExecution execution) {
        this.execution = execution;
    }

}
