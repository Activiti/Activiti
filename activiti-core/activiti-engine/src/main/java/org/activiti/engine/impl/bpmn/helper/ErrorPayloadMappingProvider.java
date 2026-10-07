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
package org.activiti.engine.impl.bpmn.helper;

import java.util.Collections;
import java.util.Map;

/**
 * Provider that maps error data (error code, error name) to process variables
 * when an error boundary event or error event subprocess catches an error.
 *
 * <p>The default implementation is a no-op that returns an empty map,
 * preserving existing behavior. When the extensions-based mapping provider
 * is active, it resolves output mappings from the process extensions JSON
 * and returns the mapped variables to be set on the catching execution.</p>
 */
public interface ErrorPayloadMappingProvider {
    /**
     * Apply output mappings to the error payload and return the variables to set.
     *
     * @param errorPayload map containing error data keys: "errorCode", "errorName", "errorId"
     * @param processDefinitionId the process definition ID of the catching process
     * @param activityId the activity ID of the catching error event (boundary event or start event)
     * @return map of variable names to values to set on the catching execution, or empty map if no mappings
     */
    default Map<String, Object> apply(Map<String, Object> errorPayload, String processDefinitionId, String activityId) {
        return Collections.emptyMap();
    }
}
