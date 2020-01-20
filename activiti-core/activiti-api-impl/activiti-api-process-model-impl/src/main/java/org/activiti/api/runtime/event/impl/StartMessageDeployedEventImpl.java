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

package org.activiti.api.runtime.event.impl;

import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.StartMessageDeploymentDefinition;
import org.activiti.api.process.model.events.MessageDefinitionEvent;
import org.activiti.api.process.model.events.StartMessageDeployedEvent;

public class StartMessageDeployedEventImpl extends RuntimeEventImpl<StartMessageDeploymentDefinition, MessageDefinitionEvent.MessageDefinitionEvents> 
                                           implements StartMessageDeployedEvent {

    private StartMessageDeployedEventImpl(Builder builder) {
        this(builder.entity);
    }

    StartMessageDeployedEventImpl() {
    }

    public StartMessageDeployedEventImpl(StartMessageDeploymentDefinition startMessageEventSubscription) {
        super(startMessageEventSubscription);
        
        ProcessDefinition processDefinition = startMessageEventSubscription.getProcessDefinition();
        
        setProcessDefinitionId(processDefinition.getId());
        setProcessDefinitionKey(processDefinition.getKey());
        setProcessDefinitionVersion(processDefinition.getVersion());
    }

    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Creates a builder to build {@link StartMessageDeployedEventImpl} and initialize it with the given object.
     * @param startMessageDeployedEventImpl to initialize the builder with
     * @return created builder
     */
    public static Builder builderFrom(StartMessageDeployedEventImpl startMessageDeployedEventImpl) {
        return new Builder(startMessageDeployedEventImpl);
    }

    /**
     * Builder to build {@link StartMessageDeployedEventImpl}.
     */
    public static final class Builder {

        private StartMessageDeploymentDefinition entity;

        public Builder() {
        }

        private Builder(StartMessageDeployedEventImpl startMessageDeployedEventImpl) {
            this.entity = startMessageDeployedEventImpl.getEntity();
        }

        /**
        * Builder method for entity parameter.
        * @param entity field to set
        * @return builder
        */
        public Builder withEntity(StartMessageDeploymentDefinition entity) {
            this.entity = entity;
            return this;
        }

        /**
        * Builder method of the builder.
        * @return built class
        */
        public StartMessageDeployedEventImpl build() {
            return new StartMessageDeployedEventImpl(this);
        }
    }


}
