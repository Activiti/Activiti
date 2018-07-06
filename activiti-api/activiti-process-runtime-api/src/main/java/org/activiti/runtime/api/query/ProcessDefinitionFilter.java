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

package org.activiti.runtime.api.query;

import java.util.Collections;
import java.util.Set;

public class ProcessDefinitionFilter {

    public static String ID = "id";

    public static String PROCESS_DEFINITION_ID = "processDefinitionId";

    private Set<String> processDefinitionKeys;

    private ProcessDefinitionFilter(Set<String> processDefinitionKeys) {
        this.processDefinitionKeys = processDefinitionKeys;
    }

    public static ProcessDefinitionFilter filterOnKeys(Set<String> processDefinitionKeys) {
        return new ProcessDefinitionFilter(processDefinitionKeys);
    }

    public static ProcessDefinitionFilter filterOnKey(String processDefinitionKey) {
        return new ProcessDefinitionFilter(Collections.singleton(processDefinitionKey));
    }

    public static ProcessDefinitionFilter unfiltered() {
        return new ProcessDefinitionFilter(Collections.emptySet());
    }

    public Set<String> getProcessDefinitionKeys() {
        return processDefinitionKeys;
    }

    public boolean hasProcessDefinitionKeys() {
        return processDefinitionKeys != null && !processDefinitionKeys.isEmpty();
    }

}
