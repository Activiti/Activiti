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

package org.conf.activiti.runtime;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.activiti.runtime.api.event.BPMNActivityEvent;
import org.activiti.runtime.api.event.IntegrationEvent;
import org.activiti.runtime.api.event.ProcessRuntimeEvent;
import org.activiti.runtime.api.event.SequenceFlowEvent;
import org.activiti.runtime.api.event.impl.CloudBPMNActivityCancelledEventImpl;
import org.activiti.runtime.api.event.impl.CloudBPMNActivityCompletedEventImpl;
import org.activiti.runtime.api.event.impl.CloudBPMNActivityStartedEventImpl;
import org.activiti.runtime.api.event.impl.CloudIntegrationRequestedImpl;
import org.activiti.runtime.api.event.impl.CloudIntegrationResultReceivedImpl;
import org.activiti.runtime.api.event.impl.CloudProcessCancelledEventImpl;
import org.activiti.runtime.api.event.impl.CloudProcessCompletedEventImpl;
import org.activiti.runtime.api.event.impl.CloudProcessCreatedEventImpl;
import org.activiti.runtime.api.event.impl.CloudProcessResumedEventImpl;
import org.activiti.runtime.api.event.impl.CloudProcessStartedEventImpl;
import org.activiti.runtime.api.event.impl.CloudProcessSuspendedEventImpl;
import org.activiti.runtime.api.event.impl.CloudSequenceFlowTakenImpl;
import org.activiti.runtime.api.model.CloudProcessDefinition;
import org.activiti.runtime.api.model.CloudProcessInstance;
import org.activiti.runtime.api.model.IntegrationRequest;
import org.activiti.runtime.api.model.IntegrationResult;
import org.activiti.runtime.api.model.impl.CloudProcessDefinitionImpl;
import org.activiti.runtime.api.model.impl.CloudProcessInstanceImpl;
import org.activiti.runtime.api.model.impl.IntegrationRequestImpl;
import org.activiti.runtime.api.model.impl.IntegrationResultImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudProcessModelAutoConfiguration {

    //this bean will be automatically injected inside boot's ObjectMapper
    @Bean
    public Module customizeCloudProcessModelObjectMapper() {
        SimpleModule module = new SimpleModule("mapProcessRuntimeEvents",
                                               Version.unknownVersion());
        module.registerSubtypes(new NamedType(CloudBPMNActivityStartedEventImpl.class,
                                              BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED.name()));
        module.registerSubtypes(new NamedType(CloudBPMNActivityCompletedEventImpl.class,
                                              BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED.name()));
        module.registerSubtypes(new NamedType(CloudBPMNActivityCancelledEventImpl.class,
                                              BPMNActivityEvent.ActivityEvents.ACTIVITY_CANCELLED.name()));
        module.registerSubtypes(new NamedType(CloudProcessStartedEventImpl.class,
                                              ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED.name()));
        module.registerSubtypes(new NamedType(CloudProcessCreatedEventImpl.class,
                                              ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED.name()));
        module.registerSubtypes(new NamedType(CloudProcessCompletedEventImpl.class,
                                              ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED.name()));
        module.registerSubtypes(new NamedType(CloudProcessSuspendedEventImpl.class,
                                              ProcessRuntimeEvent.ProcessEvents.PROCESS_SUSPENDED.name()));
        module.registerSubtypes(new NamedType(CloudProcessResumedEventImpl.class,
                                              ProcessRuntimeEvent.ProcessEvents.PROCESS_RESUMED.name()));
        module.registerSubtypes(new NamedType(CloudProcessCancelledEventImpl.class,
                                              ProcessRuntimeEvent.ProcessEvents.PROCESS_CANCELLED.name()));
        module.registerSubtypes(new NamedType(CloudSequenceFlowTakenImpl.class,
                                              SequenceFlowEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN.name()));

        module.registerSubtypes(new NamedType(CloudIntegrationRequestedImpl.class,
                                              IntegrationEvent.IntegrationEvents.INTEGRATION_REQUESTED.name()));
        module.registerSubtypes(new NamedType(CloudIntegrationResultReceivedImpl.class,
                                              IntegrationEvent.IntegrationEvents.INTEGRATION_RESULT_RECEIVED.name()));

        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver() {
            //this is a workaround for https://github.com/FasterXML/jackson-databind/issues/2019
            //once version 2.9.6 is related we can remove this @override method
            @Override
            public JavaType resolveAbstractType(DeserializationConfig config,
                                                BeanDescription typeDesc) {
                return findTypeMapping(config,
                                       typeDesc.getType());
            }
        };

        resolver.addMapping(IntegrationRequest.class, IntegrationRequestImpl.class);
        resolver.addMapping(IntegrationResult.class, IntegrationResultImpl.class);

        resolver.addMapping(CloudProcessDefinition.class,
                            CloudProcessDefinitionImpl.class);
        resolver.addMapping(CloudProcessInstance.class,
                            CloudProcessInstanceImpl.class);

        module.setAbstractTypes(resolver);

        return module;
    }

}
