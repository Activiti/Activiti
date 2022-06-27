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
package org.activiti.api.process.model.builders;

import java.util.HashMap;
import java.util.Map;

import org.activiti.api.process.model.payloads.SignalPayload;

public class SignalPayloadBuilder {

    private String name;
    private Map<String, Object> variables;

    public SignalPayloadBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public SignalPayloadBuilder withVariables(Map<String, Object> variables) {
        this.variables = variables;
        return this;
    }

    public SignalPayloadBuilder withVariable(String name,
                                             Object value) {
        if (this.variables == null) {
            this.variables = new HashMap<>();
        }
        this.variables.put(name,
                           value);
        return this;
    }

    public SignalPayload build() {
        return new SignalPayload(name,
                                 this.variables);
    }
}
