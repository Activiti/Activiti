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
package org.activiti.api.runtime.model.impl;

import java.util.Date;
import java.util.Objects;

import org.activiti.api.process.model.MessageSubscription;

public class MessageSubscriptionImpl implements MessageSubscription {

    private String id;
    private String eventName;
    private String executionId;
    private String processInstanceId;
    private String processDefinitionId;
    private String businessKey;
    private String configuration;
    private String activityId;
    private Date created;

    private MessageSubscriptionImpl(Builder builder) {
        this.id = builder.id;
        this.eventName = builder.eventName;
        this.executionId = builder.executionId;
        this.processInstanceId = builder.processInstanceId;
        this.processDefinitionId = builder.processDefinitionId;
        this.businessKey = builder.businessKey;
        this.configuration = builder.configuration;
        this.activityId = builder.activityId;
        this.created = builder.created;
    }

    MessageSubscriptionImpl() {
    }

    public String getId() {
        return id;
    }

    public String getEventName() {
        return eventName;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public String getConfiguration() {
        return configuration;
    }

    public String getActivityId() {
        return activityId;
    }

    public Date getCreated() {
        return created;
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId,
                            configuration,
                            created,
                            eventName,
                            executionId,
                            id,
                            processDefinitionId,
                            processInstanceId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MessageSubscriptionImpl other = (MessageSubscriptionImpl) obj;
        return Objects.equals(activityId, other.activityId) &&
                Objects.equals(configuration, other.configuration) &&
                Objects.equals(created, other.created) &&
                Objects.equals(eventName, other.eventName) &&
                Objects.equals(executionId, other.executionId) &&
                Objects.equals(id, other.id) &&
                Objects.equals(processDefinitionId, other.processDefinitionId) &&
                Objects.equals(processInstanceId, other.processInstanceId);
    }

    @Override
    public String toString() {
        StringBuilder builder2 = new StringBuilder();
        builder2.append("MessageEventSubscriptionImpl [id=")
                .append(id)
                .append(", eventName=")
                .append(eventName)
                .append(", executionId=")
                .append(executionId)
                .append(", processInstanceId=")
                .append(processInstanceId)
                .append(", processDefinitionId=")
                .append(processDefinitionId)
                .append(", configuration=")
                .append(configuration)
                .append(", activityId=")
                .append(activityId)
                .append(", created=")
                .append(created)
                .append("]");
        return builder2.toString();
    }


    public String getBusinessKey() {
        return businessKey;
    }


    /**
     * Creates a builder to build {@link MessageSubscriptionImpl}.
     * @return created builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder to build {@link MessageSubscriptionImpl} and initialize it with the given object.
     * @param messageEventSubscriptionImpl to initialize the builder with
     * @return created builder
     */
    public static Builder builderFrom(MessageSubscriptionImpl messageEventSubscriptionImpl) {
        return new Builder(messageEventSubscriptionImpl);
    }


    /**
     * Builder to build {@link MessageSubscriptionImpl}.
     */
    public static final class Builder {

        private String id;
        private String eventName;
        private String executionId;
        private String processInstanceId;
        private String processDefinitionId;
        private String businessKey;
        private String configuration;
        private String activityId;
        private Date created;

        public Builder() {
        }

        private Builder(MessageSubscriptionImpl messageEventSubscriptionImpl) {
            this.id = messageEventSubscriptionImpl.id;
            this.eventName = messageEventSubscriptionImpl.eventName;
            this.executionId = messageEventSubscriptionImpl.executionId;
            this.processInstanceId = messageEventSubscriptionImpl.processInstanceId;
            this.processDefinitionId = messageEventSubscriptionImpl.processDefinitionId;
            this.businessKey = messageEventSubscriptionImpl.businessKey;
            this.configuration = messageEventSubscriptionImpl.configuration;
            this.activityId = messageEventSubscriptionImpl.activityId;
            this.created = messageEventSubscriptionImpl.created;
        }

        /**
        * Builder method for id parameter.
        * @param id field to set
        * @return builder
        */
        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        /**
        * Builder method for eventName parameter.
        * @param eventName field to set
        * @return builder
        */
        public Builder withEventName(String eventName) {
            this.eventName = eventName;
            return this;
        }

        /**
        * Builder method for executionId parameter.
        * @param executionId field to set
        * @return builder
        */
        public Builder withExecutionId(String executionId) {
            this.executionId = executionId;
            return this;
        }

        /**
        * Builder method for processInstanceId parameter.
        * @param processInstanceId field to set
        * @return builder
        */
        public Builder withProcessInstanceId(String processInstanceId) {
            this.processInstanceId = processInstanceId;
            return this;
        }

        /**
        * Builder method for processDefinitionId parameter.
        * @param processDefinitionId field to set
        * @return builder
        */
        public Builder withProcessDefinitionId(String processDefinitionId) {
            this.processDefinitionId = processDefinitionId;
            return this;
        }

        /**
        * Builder method for businessKey parameter.
        * @param businessKey field to set
        * @return builder
        */
        public Builder withBusinessKey(String businessKey) {
            this.businessKey = businessKey;
            return this;
        }

        /**
        * Builder method for configuration parameter.
        * @param configuration field to set
        * @return builder
        */
        public Builder withConfiguration(String configuration) {
            this.configuration = configuration;
            return this;
        }

        /**
        * Builder method for activityId parameter.
        * @param activityId field to set
        * @return builder
        */
        public Builder withActivityId(String activityId) {
            this.activityId = activityId;
            return this;
        }

        /**
        * Builder method for created parameter.
        * @param created field to set
        * @return builder
        */
        public Builder withCreated(Date created) {
            this.created = created;
            return this;
        }

        /**
        * Builder method of the builder.
        * @return built class
        */
        public MessageSubscriptionImpl build() {
            return new MessageSubscriptionImpl(this);
        }
    }
}
