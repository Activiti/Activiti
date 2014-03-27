package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.Task;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;

public abstract class ExternalInvocationTaskValidator extends ProcessLevelValidator {
	
	protected void validateFieldDeclarationsForEmail(org.activiti.bpmn.model.Process process, Task task, List<FieldExtension> fieldExtensions, List<ValidationError> errors) {
    boolean toDefined = false;
    boolean textOrHtmlDefined = false;
    
    for (FieldExtension fieldExtension : fieldExtensions) {
      if (fieldExtension.getFieldName().equals("to")) {
        toDefined = true;
      }
      if (fieldExtension.getFieldName().equals("html")) {
        textOrHtmlDefined = true;
      }
			if (fieldExtension.getFieldName().equals("htmlVar")) {
				textOrHtmlDefined = true;
			}
      if (fieldExtension.getFieldName().equals("text")) {
        textOrHtmlDefined = true;
      }
			if (fieldExtension.getFieldName().equals("textVar")) {
				textOrHtmlDefined = true;
			}
    }

    if (!toDefined) {
    	addError(errors, Problems.MAIL_TASK_NO_RECIPIENT, process, task, "No recipient is defined on the mail activity");
    }
    if (!textOrHtmlDefined) {
    	addError(errors, Problems.MAIL_TASK_NO_CONTENT, process, task, "Text, html, textVar or htmlVar field should be provided");
    }
  }

  protected void validateFieldDeclarationsForShell(org.activiti.bpmn.model.Process process, Task task, List<FieldExtension> fieldExtensions, List<ValidationError> errors) {
    boolean shellCommandDefined = false;

    for (FieldExtension fieldExtension : fieldExtensions) {
      String fieldName = fieldExtension.getFieldName();
      String fieldValue = fieldExtension.getStringValue();

      shellCommandDefined |= fieldName.equals("command");

      if ((fieldName.equals("wait") 
      		|| fieldName.equals("redirectError") 
      		|| fieldName.equals("cleanEnv")) 
      			&& !fieldValue.toLowerCase().equals("true")
            && !fieldValue.toLowerCase().equals("false")) {
      	addError(errors, Problems.SHELL_TASK_INVALID_PARAM, process, task, "Undefined parameter value for shell field");
      }

    }

    if (!shellCommandDefined) {
    	addError(errors, Problems.SHELL_TASK_NO_COMMAND, process, task, "No shell command is defined on the shell activity");
    }
  }

}
