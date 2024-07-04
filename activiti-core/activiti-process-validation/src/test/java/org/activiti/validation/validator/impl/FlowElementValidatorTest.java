package org.activiti.validation.validator.impl;

import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;
import org.activiti.validation.ValidationError;
import org.junit.jupiter.api.Test;
import org.activiti.bpmn.model.Process;
import java.util.ArrayList;
import java.util.List;



public class FlowElementValidatorTest {

    private FlowElementValidator flowElementValidator = new FlowElementValidator();

    @Test
    public void testExecuteValidationofProcessWithoutCardinality() {
        Process process = new Process();
        process.setId("myProcess");
        process.setName("My Process");
        UserTask userTask = new UserTask();
        userTask.setId("myTask");
        userTask.setName("My Task");
        MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics = new MultiInstanceLoopCharacteristics();
        multiInstanceLoopCharacteristics.setLoopCardinality("");
        multiInstanceLoopCharacteristics.setSequential(false);
        userTask.setLoopCharacteristics(multiInstanceLoopCharacteristics);
        process.addFlowElement(userTask);
        List<ValidationError> errors = new ArrayList<>();
        flowElementValidator.executeValidation(null, process, errors);

        assert(errors.size() == 1);
  }
    @Test
    public void testExecuteValidationofProcessWithCardinality() {
        Process process = new Process();
        process.setId("myProcess");
        process.setName("My Process");
        UserTask userTask = new UserTask();
        userTask.setId("myTask");
        userTask.setName("My Task");
        MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics = new MultiInstanceLoopCharacteristics();
        multiInstanceLoopCharacteristics.setLoopCardinality("1");
        multiInstanceLoopCharacteristics.setSequential(false);
        userTask.setLoopCharacteristics(multiInstanceLoopCharacteristics);
        process.addFlowElement(userTask);
        List<ValidationError> errors = new ArrayList<>();
        flowElementValidator.executeValidation(null, process, errors);

        assert(errors.size() == 0);
    }
    @Test
    public void testExecuteValidationofProcessHavingSubProcessWithCardinality() {
        Process process = new Process();
        process.setId("myProcess");
        process.setName("My Process");
        SubProcess subProcess = new SubProcess();
        subProcess.setId("mySubProcess");
        subProcess.setName("My Sub Process");
        UserTask userTask = new UserTask();
        userTask.setId("myTask");
        userTask.setName("My Task");
        subProcess.addFlowElement(userTask);
        MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics = new MultiInstanceLoopCharacteristics();
        multiInstanceLoopCharacteristics.setLoopCardinality("1");
        multiInstanceLoopCharacteristics.setSequential(false);
        subProcess.setLoopCharacteristics(multiInstanceLoopCharacteristics);
        process.addFlowElement(subProcess);
        List<ValidationError> errors = new ArrayList<>();
        flowElementValidator.executeValidation(null, process, errors);

        assert(errors.size() == 0);
    }
    @Test
    public void testExecuteValidationofProcessHavingSubProcessWithOutCardinality() {
        Process process = new Process();
        process.setId("myProcess");
        process.setName("My Process");
        SubProcess subProcess = new SubProcess();
        subProcess.setId("mySubProcess");
        subProcess.setName("My Sub Process");
        UserTask userTask = new UserTask();
        userTask.setId("myTask");
        userTask.setName("My Task");
        subProcess.addFlowElement(userTask);
        MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics = new MultiInstanceLoopCharacteristics();
        multiInstanceLoopCharacteristics.setLoopCardinality("");
        multiInstanceLoopCharacteristics.setSequential(false);
        userTask.setLoopCharacteristics(multiInstanceLoopCharacteristics);
        process.addFlowElement(subProcess);
        List<ValidationError> errors = new ArrayList<>();
        flowElementValidator.executeValidation(null, process, errors);

        assert(errors.size() == 1);
    }
}
