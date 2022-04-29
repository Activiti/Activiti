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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.activiti.api.process.model.payloads.MessageEventPayload;

public class MessageEventPayloadBuilder {

    private String name;
    private String correlationKey;
    private String businessKey;
    private Map<String, Object> variables;

    public static MessageEventPayloadBuilder from(MessageEventPayload messagePayload) {
        Objects.requireNonNull(messagePayload, "messagePayload must not be null");

        return new MessageEventPayloadBuilder().withName(messagePayload.getName())
                                               .withBusinessKey(messagePayload.getBusinessKey())
                                               .withCorrelationKey(messagePayload.getCorrelationKey())
                                               .withVariables(messagePayload.getVariables());
    }

    public static MessageEventPayloadBuilder messageEvent(String name) {
        return new MessageEventPayloadBuilder().withName(name);
    }

    public MessageEventPayloadBuilder withName(String name) {
        Objects.requireNonNull(name, "name must not be null");

        this.name = name;

        return this;
    }

    public MessageEventPayloadBuilder withVariables(Map<String, Object> variables) {
        this.variables = variables;

        return this;
    }

    public MessageEventPayloadBuilder withVariable(String name,
                                                     Object value) {
        if (this.variables == null) {
            this.variables = new LinkedHashMap<>();
        }
        this.variables.put(name,
                           value);
        return this;
    }

    public MessageEventPayloadBuilder withCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;

        return this;
    }

    public MessageEventPayloadBuilder withBusinessKey(String businessKey) {
        this.businessKey = businessKey;

        return this;
    }


    public MessageEventPayload build() {
        return new MessageEventPayload(name,
                                       correlationKey,
                                       businessKey,
                                       variables);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MessageEventPayloadBuilder [name=");
        builder.append(name);
        builder.append(", correlationKey=");
        builder.append(correlationKey);
        builder.append(", businessKey=");
        builder.append(businessKey);
        builder.append(", variables=");
        builder.append(variables);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(correlationKey, name, variables);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MessageEventPayloadBuilder other = (MessageEventPayloadBuilder) obj;
        return Objects.equals(correlationKey, other.correlationKey)
                && Objects.equals(businessKey, other.businessKey)
                && Objects.equals(name, other.name)
                && Objects.equals(variables, other.variables);
    }
}
