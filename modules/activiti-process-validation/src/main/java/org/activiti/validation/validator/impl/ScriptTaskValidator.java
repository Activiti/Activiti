package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.ScriptTask;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;
import org.apache.commons.lang3.StringUtils;

/**
 * @author jbarrez
 */
public class ScriptTaskValidator extends ProcessLevelValidator {

	@Override
	protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
		List<ScriptTask> scriptTasks = process.findFlowElementsOfType(ScriptTask.class);
		for (ScriptTask scriptTask : scriptTasks) {
		  if (StringUtils.isEmpty(scriptTask.getScript())) {
		  	addError(errors, Problems.SCRIPT_TASK_MISSING_SCRIPT, process, scriptTask, "No script provided for script task");
	    }
		}
	}

}
