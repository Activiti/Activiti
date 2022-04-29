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

import java.time.DateTimeException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.activiti.api.task.model.payloads.CreateTaskVariablePayload;
import org.activiti.api.task.model.payloads.UpdateTaskVariablePayload;
import org.activiti.common.util.DateFormatterProvider;

public class TaskVariablesPayloadValidator {

    private final DateFormatterProvider dateFormatterProvider;
    private final VariableNameValidator variableNameValidator;

    public TaskVariablesPayloadValidator(DateFormatterProvider dateFormatterProvider,
                                         VariableNameValidator variableNameValidator) {
        this.dateFormatterProvider = dateFormatterProvider;
        this.variableNameValidator = variableNameValidator;
    }

    private Optional<Date> handleAsDate(String value) {
        try {
            return Optional.ofNullable(dateFormatterProvider.parse(value));
        } catch (DateTimeException e) {
            // ignore exception and return empty: it's not a date so let's keep initial value
            return Optional.empty();
        }
    }

    private void checkNotValidCharactersInVariableName(String name,
                                                        String errorMsg) {

        if (!variableNameValidator.validate(name)) {
            throw new IllegalStateException(errorMsg + (name != null ? name : "null" ));
        }
    }

    public CreateTaskVariablePayload handleCreateTaskVariablePayload(CreateTaskVariablePayload createTaskVariablePayload) {

        checkNotValidCharactersInVariableName(createTaskVariablePayload.getName(),
                                               "Variable has not a valid name: ");

        Object value = createTaskVariablePayload.getValue();
        if (value instanceof String) {
            handleAsDate((String)value).ifPresent(createTaskVariablePayload::setValue);
        }
        return createTaskVariablePayload;
    }

    public UpdateTaskVariablePayload handleUpdateTaskVariablePayload(UpdateTaskVariablePayload updateTaskVariablePayload) {

        checkNotValidCharactersInVariableName(updateTaskVariablePayload.getName(),
                                               "You cannot update a variable with not a valid name: ");

        Object value = updateTaskVariablePayload.getValue();

        if (value instanceof String) {
            handleAsDate((String) value).ifPresent(updateTaskVariablePayload::setValue);
        }
        return updateTaskVariablePayload;
    }

    public Map<String, Object> handlePayloadVariables(Map<String, Object> variables) {
        if (variables != null) {
            Set<String> mismatchedVars = variableNameValidator.validateVariables(variables);

            if (!mismatchedVars.isEmpty()) {
                throw new IllegalStateException("Variables have not valid names: " + String.join(", ",
                                                                                                 mismatchedVars));

            }
            handleStringVariablesAsDates(variables);
        }

        return variables;
    }

    private void handleStringVariablesAsDates(Map<String, Object> variables) {
        if (variables != null) {
            variables.entrySet()
            .stream()
            .filter(stringObjectEntry -> stringObjectEntry.getValue() instanceof String)
            .forEach(stringObjectEntry ->
                        handleAsDate((String) stringObjectEntry.getValue()).ifPresent(stringObjectEntry::setValue));
        }
    }

}
