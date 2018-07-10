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
import org.activiti.runtime.api.cmd.ProcessCommands;
import org.activiti.runtime.api.cmd.RuntimeCommands;
import org.activiti.runtime.api.cmd.impl.ResumeProcessImpl;
import org.activiti.runtime.api.cmd.impl.SendSignalImpl;
import org.activiti.runtime.api.cmd.impl.StartProcessImpl;
import org.activiti.runtime.api.cmd.impl.SuspendProcessImpl;
import org.activiti.runtime.api.cmd.result.impl.ResumeProcessResultImpl;
import org.activiti.runtime.api.cmd.result.impl.SendSignalResultImpl;
import org.activiti.runtime.api.cmd.result.impl.StartProcessResultImpl;
import org.activiti.runtime.api.cmd.result.impl.SuspendProcessResultImpl;
import org.activiti.runtime.api.model.BPMNActivity;
import org.activiti.runtime.api.model.IntegrationContext;
import org.activiti.runtime.api.model.ProcessDefinition;
import org.activiti.runtime.api.model.ProcessInstance;
import org.activiti.runtime.api.model.SequenceFlow;
import org.activiti.runtime.api.model.impl.BPMNActivityImpl;
import org.activiti.runtime.api.model.impl.IntegrationContextImpl;
import org.activiti.runtime.api.model.impl.ProcessDefinitionImpl;
import org.activiti.runtime.api.model.impl.ProcessInstanceImpl;
import org.activiti.runtime.api.model.impl.SequenceFlowImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProcessModelAutoConfiguration {

    //this bean will be automatically injected inside boot's ObjectMapper
    @Bean
    public Module customizeProcessModelObjectMapper() {
        SimpleModule module = new SimpleModule("mapProcessModelInterfaces",
                                               Version.unknownVersion());
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

        resolver.addMapping(BPMNActivity.class,
                            BPMNActivityImpl.class);
        resolver.addMapping(ProcessInstance.class,
                            ProcessInstanceImpl.class);
        resolver.addMapping(ProcessDefinition.class,
                            ProcessDefinitionImpl.class);
        resolver.addMapping(SequenceFlow.class,
                            SequenceFlowImpl.class);

        resolver.addMapping(IntegrationContext.class,
                            IntegrationContextImpl.class);

        module.registerSubtypes(new NamedType(StartProcessImpl.class,
                                              ProcessCommands.START_PROCESS.name()));
        module.registerSubtypes(new NamedType(StartProcessResultImpl.class,
                                              ProcessCommands.START_PROCESS.name()));

        module.registerSubtypes(new NamedType(SuspendProcessImpl.class,
                                              ProcessCommands.SUSPEND_PROCESS.name()));
        module.registerSubtypes(new NamedType(SuspendProcessResultImpl.class,
                                              ProcessCommands.SUSPEND_PROCESS.name()));

        module.registerSubtypes(new NamedType(ResumeProcessImpl.class,
                                              ProcessCommands.RESUME_PROCESS.name()));
        module.registerSubtypes(new NamedType(ResumeProcessResultImpl.class,
                                              ProcessCommands.RESUME_PROCESS.name()));

        module.registerSubtypes(new NamedType(SendSignalImpl.class,
                                              RuntimeCommands.SEND_SIGNAL.name()));
        module.registerSubtypes(new NamedType(SendSignalResultImpl.class,
                                              RuntimeCommands.SEND_SIGNAL.name()));

        module.setAbstractTypes(resolver);
        return module;
    }
}
