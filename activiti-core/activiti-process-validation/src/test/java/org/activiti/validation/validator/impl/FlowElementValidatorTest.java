/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.validation.validator.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;
import org.activiti.validation.ProcessValidatorImpl;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.ValidatorSet;
import org.activiti.validation.validator.ValidatorSetNames;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class FlowElementValidatorTest {

  ValidatorSet validatorSet = new ValidatorSet(ValidatorSetNames.ACTIVITI_EXECUTABLE_PROCESS);
  ProcessValidatorImpl validator = new ProcessValidatorImpl();

  @BeforeEach
  void setUp() {
    validatorSet.addValidator(new FlowElementValidator());
    validator.addValidatorSet(validatorSet);
  }

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
        BpmnModel bpmnModel = new BpmnModel();
        bpmnModel.addProcess(process);

        var errors = validator.validate(bpmnModel);

        assertThat(errors).hasSize(1).first()
            .extracting(ValidationError::getProblem, ValidationError::getDefaultDescription)
            .containsExactly("activiti-multi-instance-missing-collection",
              "Either loopCardinality or loopDataInputRef/activiti:collection must been set");
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
        BpmnModel bpmnModel = new BpmnModel();
        bpmnModel.addProcess(process);

        var errors = validator.validate(bpmnModel);

        assertThat(errors).isEmpty();
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
        BpmnModel bpmnModel = new BpmnModel();
        bpmnModel.addProcess(process);

        var errors = validator.validate(bpmnModel);

        assertThat(errors).isEmpty();
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
        BpmnModel bpmnModel = new BpmnModel();
        bpmnModel.addProcess(process);

        var errors = validator.validate(bpmnModel);

      assertThat(errors).hasSize(1).first()
          .extracting(ValidationError::getProblem, ValidationError::getDefaultDescription)
          .containsExactly("activiti-multi-instance-missing-collection",
              "Either loopCardinality or loopDataInputRef/activiti:collection must been set");
    }
}
