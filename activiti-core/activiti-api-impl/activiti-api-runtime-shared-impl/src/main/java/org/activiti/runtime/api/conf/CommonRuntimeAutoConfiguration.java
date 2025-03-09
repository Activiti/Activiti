/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

package org.activiti.runtime.api.conf;

import static java.util.Collections.emptyList;

import java.util.List;
import org.activiti.api.model.shared.event.VariableCreatedEvent;
import org.activiti.api.model.shared.event.VariableUpdatedEvent;
import org.activiti.api.runtime.shared.events.VariableEventListener;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.runtime.api.event.impl.ToVariableCreatedConverter;
import org.activiti.runtime.api.event.impl.ToVariableUpdatedConverter;
import org.activiti.runtime.api.event.internal.VariableCreatedListenerDelegate;
import org.activiti.runtime.api.event.internal.VariableEventFilter;
import org.activiti.runtime.api.event.internal.VariableUpdatedListenerDelegate;
import org.activiti.runtime.api.impl.VariableNameValidator;
import org.activiti.runtime.api.model.impl.APIVariableInstanceConverter;
import org.activiti.spring.process.ProcessExtensionService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CommonRuntimeAutoConfiguration {

    @Bean
    public APIVariableInstanceConverter apiVariableInstanceConverter() {
        return new APIVariableInstanceConverter();
    }

    @Bean
    public VariableEventFilter variableEventFilter() {
        return new VariableEventFilter();
    }

    @Bean
    public InitializingBean registerVariableCreatedListenerDelegate(RuntimeService runtimeService,
        @Autowired(required = false) List<VariableEventListener<VariableCreatedEvent>> listeners,
        VariableEventFilter variableEventFilter, ProcessExtensionService processExtensionService) {
        return () -> runtimeService.addEventListener(
            new VariableCreatedListenerDelegate(getInitializedListeners(listeners),
                new ToVariableCreatedConverter(processExtensionService),
                variableEventFilter), ActivitiEventType.VARIABLE_CREATED);
    }

    private <T> List<T> getInitializedListeners(List<T> eventListeners) {
        return eventListeners != null ? eventListeners : emptyList();
    }

    @Bean
    public InitializingBean registerVariableUpdatedListenerDelegate(RuntimeService runtimeService,
        @Autowired(required = false) List<VariableEventListener<VariableUpdatedEvent>> listeners,
        VariableEventFilter variableEventFilter, ProcessExtensionService processExtensionService) {
        return () -> runtimeService.addEventListener(
            new VariableUpdatedListenerDelegate(getInitializedListeners(listeners),
                new ToVariableUpdatedConverter(processExtensionService),
                variableEventFilter), ActivitiEventType.VARIABLE_UPDATED);
    }

    @Bean
    public VariableNameValidator variableNameValidator() {
        return new VariableNameValidator();
    }

}
