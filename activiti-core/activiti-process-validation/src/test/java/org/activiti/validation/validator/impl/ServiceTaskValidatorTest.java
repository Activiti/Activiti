/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.validation.validator.impl;

import java.util.ArrayList;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.validation.ValidationError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceTaskValidatorTest {

    private ServiceTaskValidator validator = new ServiceTaskValidator();

    @Test
    public void executeValidationShouldRiseErrorsForEmptyServiceTask() {
        //given
        Process process = new Process();
        process.addFlowElement(new ServiceTask());
        BpmnModel bpmnModel = new BpmnModel();
        ArrayList<ValidationError> errors = new ArrayList<>();

        //when
        validator.executeValidation(bpmnModel, process,
                                    errors);

        //then
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getProblem()).isEqualTo("activiti-servicetask-missing-implementation");
        assertThat(errors.get(0).getDefaultDescription())
                .isEqualTo(
                        "One of the attributes 'implementation', 'class', 'delegateExpression', 'type', 'operation', or 'expression' is mandatory on serviceTask."
                );
    }

    @Test
    public void executeValidationShouldNotRiseErrorsForServiceTasksSettingOnlyTheImplementation() {
        //given
        Process process = new Process();
        ServiceTask serviceTask = new ServiceTask();
        serviceTask.setImplementation("myImpl");
        process.addFlowElement(serviceTask);
        BpmnModel bpmnModel = new BpmnModel();
        ArrayList<ValidationError> errors = new ArrayList<>();

        //when
        validator.executeValidation(bpmnModel, process,
                                    errors);

        //then
        assertThat(errors)
                .as("No error is expected: the default behavior will be used")
                .isEmpty();
    }

}
