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

package org.activiti.runtime.api.model.impl;

import org.activiti.engine.repository.ProcessDefinition;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mock process definition builder.
 */
public class MockProcessDefinitionBuilder {

    private ProcessDefinition processDefinition;

    private MockProcessDefinitionBuilder(Class<? extends ProcessDefinition> processDefinitionClass) {
        processDefinition = mock(processDefinitionClass);
    }

    public static MockProcessDefinitionBuilder processDefinitionBuilderBuilder() {
        return new MockProcessDefinitionBuilder(ProcessDefinition.class);
    }

    public MockProcessDefinitionBuilder withId(String id) {
        when(processDefinition.getId()).thenReturn(id);
        return this;
    }

    public MockProcessDefinitionBuilder withKey(String key) {
        when(processDefinition.getKey()).thenReturn(key);
        return this;
    }

    public MockProcessDefinitionBuilder withName(String name) {
        when(processDefinition.getName()).thenReturn(name);
        return this;
    }

    public MockProcessDefinitionBuilder withDescription(String description) {
        when(processDefinition.getDescription()).thenReturn(description);
        return this;
    }

    public MockProcessDefinitionBuilder withVersion(int version) {
        when(processDefinition.getVersion()).thenReturn(version);
        return this;
    }

    public MockProcessDefinitionBuilder withAppVersion(Integer appVersion) {
        when(processDefinition.getAppVersion()).thenReturn(appVersion);
        return this;
    }

    public ProcessDefinition build() {
        return processDefinition;
    }
}
