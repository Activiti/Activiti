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
package org.activiti.runtime.api.impl;

import static org.activiti.engine.impl.util.CollectionUtil.map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.payloads.CreateTaskVariablePayload;
import org.activiti.api.task.model.payloads.UpdateTaskVariablePayload;
import org.activiti.common.util.DateFormatterProvider;
import org.junit.jupiter.api.Test;

public class TaskVariablesPayloadValidatorTest {

    private TaskVariablesPayloadValidator taskVariablesPayloadValidator = new TaskVariablesPayloadValidator(new DateFormatterProvider("yyyy-MM-dd[['T']HH:mm:ss[.SSS'Z']]"),
                                                                                                            new VariableNameValidator());

    @Test
    public void should_convertStringToDate_when_stringRepresentsADate() {
        //given
        Map<String, Object> payloadMap = map(
            "date", "1970-01-01",
            "dateTime", "1970-01-01T01:01:01.001Z",
            "notADate", "this is not a date",
            "int", 1,
            "boolean", true
        );

        //calculate number of milliseconds after 1970-01-01T00:00:00.000Z
        long time = Duration.ofHours(1).toMillis() + Duration.ofMinutes(1).toMillis() + Duration.ofSeconds(1).toMillis() + 1;

        //when
        Map<String, Object> handledDates = taskVariablesPayloadValidator.handlePayloadVariables(payloadMap);

        //then
        assertThat(handledDates).containsEntry("date", new Date(0));
        assertThat(handledDates).containsEntry("dateTime", new Date(time));
        assertThat(handledDates).containsEntry("notADate", "this is not a date");
        assertThat(handledDates).containsEntry("int", 1);
        assertThat(handledDates).containsEntry("boolean", true);
    }

    @Test
    public void should_convertStringToDate_when_stringRepresentsADateInCreateTaskVariablePayload() {
        //given
        CreateTaskVariablePayload payload = TaskPayloadBuilder.createVariable()
                .withVariable("date",
                              "1970-01-01")
                .build();

        //when
        CreateTaskVariablePayload handledVariablePayload = taskVariablesPayloadValidator.handleCreateTaskVariablePayload(payload);

        //then
        assertThat(handledVariablePayload.getValue()).isEqualTo(new Date(0));
    }

    @Test
    public void should_convertStringToDate_when_stringRepresentsADateWithTimeInCreateTaskVariablePayload() {
        //given
        CreateTaskVariablePayload payload = TaskPayloadBuilder.createVariable()
                .withVariable("date",
                              "1970-01-01T01:01:01.001Z")
                .build();
        //calculate number of milliseconds after 1970-01-01T00:00:00.000Z
        long time = Duration.ofHours(1).toMillis() + Duration.ofMinutes(1).toMillis() + Duration.ofSeconds(1).toMillis() + 1;

        //when
        CreateTaskVariablePayload handledVariablePayload = taskVariablesPayloadValidator.handleCreateTaskVariablePayload(payload);

        //then
        assertThat(handledVariablePayload.getValue()).isEqualTo(new Date(time));
    }

    @Test
    public void should_doNothing_when_stringIsNotADateInCreateTaskVariablePayload() {
        //given
        CreateTaskVariablePayload payload = TaskPayloadBuilder.createVariable()
                .withVariable("notADate",
                              "this is not a date")
                .build();

        //when
        CreateTaskVariablePayload handledVariablePayload = taskVariablesPayloadValidator.handleCreateTaskVariablePayload(payload);

        //then
        assertThat(handledVariablePayload.getValue()).isEqualTo("this is not a date");
    }

    @Test
    public void should_doNothing_when_itIsNotAStringInCreateTaskVariablePayload() {
        //given
        CreateTaskVariablePayload payload = TaskPayloadBuilder.createVariable()
                .withVariable("notAString",
                              10)
                .build();

        //when
        CreateTaskVariablePayload handledVariablePayload = taskVariablesPayloadValidator.handleCreateTaskVariablePayload(payload);

        //then
        assertThat(handledVariablePayload.getValue()).isEqualTo(10);
    }

    @Test
    public void should_convertStringToDate_when_stringRepresentsADateInUpdateTaskPayload() {
        //given
        UpdateTaskVariablePayload updateTaskVariablePayload = TaskPayloadBuilder.updateVariable().withVariable("date",
                                                                                          "1970-01-01").build();
        //when
        UpdateTaskVariablePayload handledDatePayload = taskVariablesPayloadValidator.handleUpdateTaskVariablePayload(updateTaskVariablePayload);

        //then
        assertThat(handledDatePayload.getValue()).isEqualTo(new Date(0));
    }

    @Test
    public void should_convertStringToDate_when_stringRepresentsADateWithTimeInUpdateTaskPayload() {
        //given
        UpdateTaskVariablePayload updateTaskVariablePayload = TaskPayloadBuilder
                .updateVariable()
                .withVariable("date",
                              "1970-01-01T01:01:01.001Z")
                .build();
        //calculate number of milliseconds after 1970-01-01T00:00:00.000Z
        long time = Duration.ofHours(1).toMillis() + Duration.ofMinutes(1).toMillis() + Duration.ofSeconds(1).toMillis() + 1;

        //when
        UpdateTaskVariablePayload handledDatePayload = taskVariablesPayloadValidator.handleUpdateTaskVariablePayload(updateTaskVariablePayload);

        //then
        assertThat(handledDatePayload.getValue()).isEqualTo(new Date(time));
    }

    @Test
    public void should_doNothing_when_stringIsNotADateInUpdateTaskPayload() {
        //given
        UpdateTaskVariablePayload updateTaskVariablePayload = TaskPayloadBuilder
                .updateVariable()
                .withVariable("notADate",
                              "this is not a date")
                .build();

        //when
        UpdateTaskVariablePayload handledDatePayload = taskVariablesPayloadValidator.handleUpdateTaskVariablePayload(updateTaskVariablePayload);

        //then
        assertThat(handledDatePayload.getValue()).isEqualTo("this is not a date");
    }

    @Test
    public void should_doNothing_when_itIsNotAStringInUpdateTaskPayload() {
        //given
        UpdateTaskVariablePayload updateTaskVariablePayload = TaskPayloadBuilder
                .updateVariable()
                .withVariable("notAString",
                              true)
                .build();

        //when
        UpdateTaskVariablePayload handledDatePayload = taskVariablesPayloadValidator.handleUpdateTaskVariablePayload(updateTaskVariablePayload);

        //then
        assertThat(handledDatePayload.getValue()).isEqualTo(true);
    }

    @Test
    public void should_returnErrorList_when_setVariableWithWrongCharactersInName() throws Exception {

        CreateTaskVariablePayload payload = TaskPayloadBuilder.createVariable()
                .withVariable("wrong-name", 10)
                .build();

        String expectedTypeErrorMessage = "wrong-name";

        Throwable throwable = catchThrowable(() -> taskVariablesPayloadValidator.handleCreateTaskVariablePayload(payload));

        assertThat(throwable)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(expectedTypeErrorMessage);
    }

}
