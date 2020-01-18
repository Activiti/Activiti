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
import org.activiti.runtime.api.model.payloads.DeleteProcessPayload;
import org.activiti.runtime.api.model.payloads.GetProcessDefinitionsPayload;
import org.activiti.runtime.api.model.payloads.GetProcessInstancesPayload;
import org.activiti.runtime.api.model.payloads.GetVariablesPayload;
import org.activiti.runtime.api.model.payloads.RemoveProcessVariablesPayload;
import org.activiti.runtime.api.model.payloads.ResumeProcessPayload;
import org.activiti.runtime.api.model.payloads.SetProcessVariablesPayload;
import org.activiti.runtime.api.model.payloads.SignalPayload;
import org.activiti.runtime.api.model.payloads.StartProcessPayload;
import org.activiti.runtime.api.model.payloads.SuspendProcessPayload;
import org.activiti.runtime.api.model.results.ProcessInstanceResult;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfigureBefore({JacksonAutoConfiguration.class})
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

        module.registerSubtypes(new NamedType(ProcessInstanceResult.class,
                                              ProcessInstanceResult.class.getSimpleName()));

        module.registerSubtypes(new NamedType(DeleteProcessPayload.class,
                                              DeleteProcessPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(GetProcessDefinitionsPayload.class,
                                              GetProcessDefinitionsPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(GetProcessInstancesPayload.class,
                                              GetProcessInstancesPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(GetVariablesPayload.class,
                                              GetVariablesPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(RemoveProcessVariablesPayload.class,
                                              RemoveProcessVariablesPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(SetProcessVariablesPayload.class,
                                              SetProcessVariablesPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(SignalPayload.class,
                                              SignalPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(StartProcessPayload.class,
                                              StartProcessPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(SuspendProcessPayload.class,
                                              SuspendProcessPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(ResumeProcessPayload.class,
                                              ResumeProcessPayload.class.getSimpleName()));

        module.setAbstractTypes(resolver);
        return module;
    }
}
