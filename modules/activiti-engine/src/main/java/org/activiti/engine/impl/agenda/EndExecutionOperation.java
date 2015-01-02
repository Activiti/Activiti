package org.activiti.engine.impl.agenda;

import java.util.Collection;
import java.util.List;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntityManager;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.JobEntityManager;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntityManager;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 */
public class EndExecutionOperation extends AbstractOperation {
	
	private static final Logger logger = LoggerFactory.getLogger(EndExecutionOperation.class);

	public EndExecutionOperation(Agenda agenda, ActivityExecution execution) {
		super(agenda, execution);
	}

	@Override
	public void run() {
		
		CommandContext commandContext = Context.getCommandContext();
		ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
		ExecutionEntity executionEntity = (ExecutionEntity) execution; // TODO: don't like cast here ...
		executionEntity.setEnded(true);

		// Get variables related to execution and delete them
		VariableInstanceEntityManager variableInstanceEntityManager = commandContext.getVariableInstanceEntityManager();
		Collection<VariableInstanceEntity> executionVariables = variableInstanceEntityManager.findVariableInstancesByExecutionId(execution.getId());
		for (VariableInstanceEntity variableInstanceEntity : executionVariables) {
			variableInstanceEntityManager.delete(variableInstanceEntity);
		}
		
		ExecutionEntity parentExecution = null;
//		if (executionEntity.isScope()) {
//			parentExecution = executionEntity.getParent();
//		}
		if (executionEntity.getParentId() != null) {
			parentExecution = executionEntityManager.get(executionEntity.getParentId());
		}

		if (parentExecution != null) {
			parentExecution.setActive(true);
		}

		// TODO: optimise by keeping boolean on executions!

		// Delete current user tasks
		TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();
		Collection<TaskEntity> tasksForExecution = taskEntityManager.findTasksByExecutionId(execution.getId());
		for (TaskEntity taskEntity : tasksForExecution) {
			taskEntityManager.delete(taskEntity);
		}

		// Delete jobs
		JobEntityManager jobEntityManager = commandContext.getJobEntityManager();
		Collection<JobEntity> jobsForExecution = jobEntityManager.findJobsByExecutionId(execution.getId());
		for (JobEntity job : jobsForExecution) {
			jobEntityManager.delete(job);
		}

		if (parentExecution != null) {
			
			// Delete current execution
			logger.debug("Ending execution {}", execution.getId());
			executionEntityManager.delete(executionEntity);
			
			logger.debug("Parent execution found. Continuing process using execution {}", parentExecution.getId());
			parentExecution.setCurrentFlowElement(executionEntity.getCurrentFlowElement());
			agenda.planTakeOutgoingSequenceFlowsOperation(parentExecution, true);
			
		} else {
			
			String processInstanceId = executionEntity.getId(); // No parent execution == process instance id
			logger.debug("No parent execution found. Verifying if process instance {} can be stopped.", processInstanceId);
			
			// TODO: optimisation can be made by keeping the nr of active
			// executions directly on the process instance in db

			// TODO: verify how many executions are still active in the process
			// instance, and stop the process instance otherwise
			Collection<ExecutionEntity> executions = executionEntityManager.findChildExecutionsByProcessInstanceId(processInstanceId);
			int activeExecutions = 0;
			for (ExecutionEntity execution : executions) {
				if (execution.isActive()) {
					activeExecutions++;
				}
			}
			
			// Delete identity links
			IdentityLinkEntityManager identityLinkEntityManager = commandContext.getIdentityLinkEntityManager();
			List<IdentityLinkEntity> identityLinkEntities = identityLinkEntityManager.findIdentityLinksByProcessInstanceId(processInstanceId);
			for (IdentityLinkEntity identityLinkEntity : identityLinkEntities) {
				identityLinkEntityManager.delete(identityLinkEntity);
			}

			if (activeExecutions == 0) {
				logger.debug("No active executions found. Ending process instance {} ", processInstanceId);
				ExecutionEntity processInstanceEntity = executionEntityManager.findExecutionById(processInstanceId);
				executionEntityManager.delete(processInstanceEntity); // TODO: what about delete reason?
			} else {
				logger.debug("Active executions found. Process instance {} will not be ended.", processInstanceId);
			}
		}
	}

}
