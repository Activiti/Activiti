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
package org.activiti.api.process.model.events;

import org.activiti.api.process.model.ProcessInstance;

public interface ExtendedProcessRuntimeEvent<T extends ProcessInstance> extends ProcessRuntimeEvent<T> {

    /**
     * @return the id of the process instance of the nested process that starts the current process instance, or null if
     *         the current process instance is not started into a nested process.
     */
    String getNestedProcessInstanceId();

    /**
     * @return the id of the process definition of the nested process that starts the current process instance, or null
     *         if the current process instance is not started into a nested process.
     */
    String getNestedProcessDefinitionId();

}
