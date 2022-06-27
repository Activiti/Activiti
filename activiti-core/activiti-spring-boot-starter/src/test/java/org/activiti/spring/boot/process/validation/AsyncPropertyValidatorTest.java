/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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
package org.activiti.spring.boot.process.validation;

import java.util.ArrayList;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.impl.util.io.InputStreamSource;
import org.activiti.validation.ValidationError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class AsyncPropertyValidatorTest {

    private AsyncPropertyValidator asyncPropertyValidator = new AsyncPropertyValidator();

    @Test
    public void shouldCheckAsyncPropertyWhenAsyncExecutorIsDisabled() {
        //given
        BpmnXMLConverter converter = new BpmnXMLConverter();
        BpmnModel bpmnModel = converter.convertToBpmnModel(
                new InputStreamSource(
                        ClassLoader.getSystemResourceAsStream("processes-validation/async-property-process.bpmn")),
                true,
                false);


        //when
        ArrayList<ValidationError> validationErrors = new ArrayList<>();
        asyncPropertyValidator.validate(bpmnModel,
                                        validationErrors);

        //then
        assertThat(validationErrors)
                .extracting(ValidationError::getProblem,
                            ValidationError::getProcessDefinitionId,
                            ValidationError::getActivityId)
                .contains(tuple("activiti-flow-element-async-not-available",
                                "async-property-root-process",
                                "usertask1"),
                          tuple("activiti-flow-element-async-not-available",
                                "async-property-root-process",
                                "usertask2"),
                          tuple("activiti-flow-element-async-not-available",
                                "async-property-root-process",
                                "usertask3"),
                          tuple("activiti-signal-async-not-available",
                                "async-property-root-process",
                                "signalintermediatethrowevent1"),
                          tuple("activiti-event-timer-async-not-available",
                                "async-property-root-process",
                                "boundarytimer1"),
                          tuple("activiti-event-timer-async-not-available",
                                "async-property-root-process",
                                "timerintermediatecatchevent1"),
                          tuple("activiti-event-timer-async-not-available",
                                "async-property-pool-process",
                                "timerstartevent1"));
    }
}
