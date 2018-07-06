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

package org.activiti.runtime.api.model.builder.impl;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.activiti.runtime.api.model.builder.SignalPayload;

public class SignalPayloadImpl implements SignalPayload {

    private final RuntimeService runtimeService;

    private String name;
    private Map<String, Object> variables = new HashMap<>();

    public SignalPayloadImpl(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @Override
    public SignalPayload name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public SignalPayload variable(String key,
                                  Object value) {
        variables.put(key,
                      value);
        return this;
    }

    @Override
    public SignalPayload variables(Map<String, Object> variables) {
        if (variables != null) {
            this.variables.putAll(variables);
        }
        return this;
    }

    @Override
    public Void doIt() {
        runtimeService.signalEventReceived(name,
                                           variables);
        return null;
    }
}
