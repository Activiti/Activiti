package org.activiti.engine.test.regression;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Error;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.validation.ProcessValidator;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.ValidatorSet;

/**
 * Sample Process Validator for Activiti Test case.
 */
public class ActivitiTestCaseProcessValidator implements ProcessValidator {

  @Override
  public List<ValidationError> validate(BpmnModel bpmnModel) {
    CustomParseValidator customParseValidator = new CustomParseValidator();

    for (Process process : bpmnModel.getProcesses()) {
      customParseValidator.executeParse(bpmnModel, process);
    }
    return bpmnModel.getErrors().values().stream()
           .map(bpmnError -> {
             ValidationError error = new ValidationError();
             error.setValidatorSetName("Manual BPMN parse validator");
             error.setProblem(bpmnError.getId());
             error.setActivityId(bpmnError.getId());
             return error;
           }).collect(Collectors.toList());
  }

  @Override
  public List<ValidatorSet> getValidatorSets() {
    return null;
  }

  class CustomParseValidator {
    protected void executeParse(BpmnModel bpmnModel, Process element) {
      for (FlowElement flowElement : element.getFlowElements()) {
        if (!ServiceTask.class.isAssignableFrom(flowElement.getClass())) {
          continue;
        }
        ServiceTask serviceTask = (ServiceTask) flowElement;
        validateAsyncAttribute(serviceTask, bpmnModel, flowElement);
      }
    }

    void validateAsyncAttribute(ServiceTask serviceTask, BpmnModel bpmnModel, FlowElement flowElement) {
      if (!serviceTask.isAsynchronous()) {
        bpmnModel.addError("Please set value of 'activiti:async'" + "attribute as true for task:" + serviceTask.getName(),
                           "error-" + serviceTask.getName(),
                           flowElement.getId());
      }
    }
  }
}