/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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
package org.activiti.api.process.model;

import java.io.Serializable;

public class SetVariablesTaskError implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String executionId;
    private final String errorMessage;

    public SetVariablesTaskError(String executionId, String errorMessage) {
        this.executionId = executionId;
        this.errorMessage = errorMessage;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return (
            "SetVariablesTaskError{" +
            "executionId='" +
            executionId +
            '\'' +
            ", errorMessage='" +
            errorMessage +
            '\'' +
            '}'
        );
    }
}
