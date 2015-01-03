package org.activiti.engine.impl.agenda;

import java.util.Collection;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.JobEntityManager;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

/**
 * @author Joram Barrez
 */
public class DestroyScopeOperation extends AbstractOperation {

	public DestroyScopeOperation(Agenda agenda, ActivityExecution execution) {
		super(agenda, execution);
	}

	@Override
	public void run() {

		FlowElement currentFlowElement = execution.getCurrentFlowElement();

		// Find the actual scope that needs to be destroyed.
		// This could be the incoming execution, or the first parent execution
		// where isScope = true

		// Find parent scope execution
		CommandContext commandContext = Context.getCommandContext();
		ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
		ExecutionEntity executionEntity = (ExecutionEntity) execution; 
		ExecutionEntity parentScopeExecution = null;

		if (execution.isScope()) {
			parentScopeExecution = executionEntity;
		} else {
			ExecutionEntity currentlyExaminedExecution = executionEntityManager.findExecutionById(execution.getParentId());
			while (currentlyExaminedExecution != null && parentScopeExecution == null) {
				if (currentlyExaminedExecution.isScope()) {
					parentScopeExecution = currentlyExaminedExecution;
				} else {
					currentlyExaminedExecution = executionEntityManager.findExecutionById(executionEntity.getParentId());
				}
			}
		}

		if (parentScopeExecution == null) {
			throw new ActivitiException("Programmatic error: no parent scope execution found for boundary event");
		}

		// Delete all child executions
		Collection<ExecutionEntity> childExecutions = executionEntityManager.findChildExecutionsByParentExecutionId(parentScopeExecution.getId());
		for (ExecutionEntity childExcecution : childExecutions) {
			deleteExecution(commandContext, childExcecution);
		}

		// Delete all scope tasks
		TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();
		Collection<TaskEntity> tasksForExecution = taskEntityManager.findTasksByExecutionId(parentScopeExecution.getId());
		for (TaskEntity taskEntity : tasksForExecution) {
			taskEntityManager.delete(taskEntity);
		}

		// Delete all scope jobs
		JobEntityManager jobEntityManager = commandContext.getJobEntityManager();
		Collection<JobEntity> jobsForExecution = jobEntityManager.findJobsByExecutionId(parentScopeExecution.getId());
		for (JobEntity job : jobsForExecution) {
			jobEntityManager.delete(job);
		}

		parentScopeExecution.setCurrentFlowElement(currentFlowElement);
	}

}
