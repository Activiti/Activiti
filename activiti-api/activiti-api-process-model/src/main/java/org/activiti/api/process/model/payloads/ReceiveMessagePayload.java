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
package org.activiti.api.process.model.payloads;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.activiti.api.model.shared.Payload;

public class ReceiveMessagePayload implements Payload {

    private final String id;
    private String name;
    private String correlationKey;
    private Map<String, Object> variables = new LinkedHashMap<>();

    public ReceiveMessagePayload() {
        this.id = UUID.randomUUID().toString();
    }

    public ReceiveMessagePayload(String name,
                                 String correlationKey,
                                 Map<String, Object> variables) {
        this();

        Objects.requireNonNull(name, "name must not be null");

        this.name = name;
        this.correlationKey = correlationKey;
        this.variables = variables;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCorrelationKey() {
        return correlationKey;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, correlationKey, variables);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReceiveMessagePayload other = (ReceiveMessagePayload) obj;
        return Objects.equals(correlationKey, other.correlationKey)
                && Objects.equals(id, other.id)
                && Objects.equals(name, other.name)
                && Objects.equals(variables, other.variables);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ReceiveMessagePayload [id=");
        builder.append(id);
        builder.append(", name=");
        builder.append(name);
        builder.append(", correlationKey=");
        builder.append(correlationKey);
        builder.append(", variables=");
        builder.append(variables);
        builder.append("]");
        return builder.toString();
    }

}
