package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.UserTask;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;

/**
 * @author jbarrez
 */
public class UserTaskValidator extends ProcessLevelValidator {

	@Override
	protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
		List<UserTask> userTasks = process.findFlowElementsOfType(UserTask.class);
		for (UserTask userTask : userTasks) {
			if (userTask.getTaskListeners() != null) {
				for (ActivitiListener listener : userTask.getTaskListeners()) {
					if (listener.getImplementation() == null || listener.getImplementationType() == null) {
						addError(errors, Problems.USER_TASK_LISTENER_IMPLEMENTATION_MISSING, process, userTask, 
								"Element 'class' or 'expression' is mandatory on executionListener");
					}
				}
			}
		}
	}

}
