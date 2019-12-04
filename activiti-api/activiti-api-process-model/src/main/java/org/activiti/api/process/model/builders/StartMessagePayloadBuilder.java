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
package org.activiti.api.process.model.builders;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.activiti.api.process.model.payloads.StartMessagePayload;

public class StartMessagePayloadBuilder {

    private String name;
    private String businessKey;
    private Map<String, Object> variables;

    public static StartMessagePayloadBuilder from(StartMessagePayload messagePayload) {
        Objects.requireNonNull(messagePayload, "messagePayload must not be null");

        return new StartMessagePayloadBuilder().withName(messagePayload.getName())
                                               .withBusinessKey(messagePayload.getBusinessKey())
                                               .withVariables(messagePayload.getVariables());
    }

    public static StartMessagePayloadBuilder start(String name) {
        return new StartMessagePayloadBuilder().withName(name);
    }

    public StartMessagePayloadBuilder withName(String name) {
        Objects.requireNonNull(name, "name must not be null");

        this.name = name;

        return this;
    }

    public StartMessagePayloadBuilder withVariables(Map<String, Object> variables) {
        this.variables = variables;

        return this;
    }

    public StartMessagePayloadBuilder withVariable(String name,
                                                   Object value) {
        if (this.variables == null) {
            this.variables = new LinkedHashMap<>();
        }
        this.variables.put(name,
                           value);
        return this;
    }

    public StartMessagePayloadBuilder withBusinessKey(String businessKey) {
        this.businessKey = businessKey;

        return this;
    }

    public StartMessagePayload build() {
        return new StartMessagePayload(name,
                                       businessKey,
                                       this.variables);
    }
}
