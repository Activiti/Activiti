package org.activiti.engine.impl.cmd;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.validation.ProcessValidator;
import org.activiti.validation.ValidationError;

/**

 */
public class ValidateBpmnModelCmd implements Command<List<ValidationError>> {

  protected BpmnModel bpmnModel;

  public ValidateBpmnModelCmd(BpmnModel bpmnModel) {
    this.bpmnModel = bpmnModel;
  }

  @Override
  public List<ValidationError> execute(CommandContext commandContext) {
    ProcessValidator processValidator = commandContext.getProcessEngineConfiguration().getProcessValidator();
    if (processValidator == null) {
      throw new ActivitiException("No process validator defined");
    }

    return processValidator.validate(bpmnModel);
  }

}
