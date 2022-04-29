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
package org.activiti.runtime.api.conf.impl;

import static java.util.Collections.unmodifiableList;

import org.activiti.api.process.runtime.conf.ProcessRuntimeConfiguration;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.runtime.shared.events.VariableEventListener;

import java.util.List;

public class ProcessRuntimeConfigurationImpl implements ProcessRuntimeConfiguration {

    private List<ProcessRuntimeEventListener<?>> processRuntimeEventListeners;
    private final List<VariableEventListener<?>> variableEventListeners;

    public ProcessRuntimeConfigurationImpl(List<ProcessRuntimeEventListener<?>> processRuntimeEventListeners,
                                           List<VariableEventListener<?>> variableEventListeners) {
        this.processRuntimeEventListeners = processRuntimeEventListeners;
        this.variableEventListeners = variableEventListeners;
    }

    @Override
    public List<ProcessRuntimeEventListener<?>> processEventListeners() {
        return unmodifiableList(processRuntimeEventListeners);
    }

    @Override
    public List<VariableEventListener<?>> variableEventListeners() {
        return unmodifiableList(variableEventListeners);
    }

}
