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
package org.activiti.runtime.api.impl;

import java.util.Map;
import org.activiti.engine.impl.bpmn.behavior.MappingExecutionContext;
import org.activiti.engine.impl.bpmn.behavior.VariablesCalculator;
import org.activiti.engine.impl.bpmn.helper.ErrorPayloadMappingProvider;

/**
 * Implementation of {@link ErrorPayloadMappingProvider} that resolves output mappings
 * from the process extensions JSON using the existing {@link VariablesCalculator} infrastructure.
 *
 * <p>When an error boundary event or error event subprocess catches an error, this provider
 * maps the error data (errorCode, errorName, errorId) to process variables according to
 * the output mappings configured in the extensions JSON for the catching event element.</p>
 */
public class ErrorVariablesMappingProvider implements ErrorPayloadMappingProvider {

    private final VariablesCalculator variablesCalculator;

    public ErrorVariablesMappingProvider(VariablesCalculator variablesCalculator) {
        this.variablesCalculator = variablesCalculator;
    }

    @Override
    public Map<String, Object> apply(Map<String, Object> errorPayload, String processDefinitionId, String activityId) {
        MappingExecutionContext context = MappingExecutionContext.buildMappingExecutionContext(
            processDefinitionId,
            activityId
        );

        return variablesCalculator.calculateOutPutVariables(context, errorPayload);
    }
}
