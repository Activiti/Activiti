/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.client.model;

import java.util.Map;

public class CompleteTaskInfo {

    private Map<String, Object> inputVariables;

    public Map<String, Object> getInputVariables() {
        return inputVariables;
    }

    public void setInputVariables(Map<String, Object> inputVariables) {
        this.inputVariables = inputVariables;
    }
}
