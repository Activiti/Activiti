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

package org.activiti.api.task.conf.impl;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.TaskCandidateGroup;
import org.activiti.api.task.model.TaskCandidateUser;
import org.activiti.api.task.model.impl.TaskCandidateGroupImpl;
import org.activiti.api.task.model.impl.TaskCandidateUserImpl;
import org.activiti.api.task.model.impl.TaskImpl;
import org.activiti.api.task.model.payloads.ClaimTaskPayload;
import org.activiti.api.task.model.payloads.CompleteTaskPayload;
import org.activiti.api.task.model.payloads.CreateTaskPayload;
import org.activiti.api.task.model.payloads.DeleteTaskPayload;
import org.activiti.api.task.model.payloads.GetTaskVariablesPayload;
import org.activiti.api.task.model.payloads.GetTasksPayload;
import org.activiti.api.task.model.payloads.ReleaseTaskPayload;
import org.activiti.api.task.model.payloads.SaveTaskPayload;
import org.activiti.api.task.model.payloads.UpdateTaskPayload;
import org.activiti.api.task.model.results.TaskResult;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfigureBefore({JacksonAutoConfiguration.class})
@Configuration
public class TaskModelAutoConfiguration {

    //this bean will be automatically injected inside boot's ObjectMapper
    @Bean
    public Module customizeTaskModelObjectMapper() {
        SimpleModule module = new SimpleModule("mapTaskRuntimeInterfaces",
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
        resolver.addMapping(Task.class,
                            TaskImpl.class);
        resolver.addMapping(TaskCandidateUser.class,
                            TaskCandidateUserImpl.class);
        resolver.addMapping(TaskCandidateGroup.class,
                            TaskCandidateGroupImpl.class);

        module.registerSubtypes(new NamedType(TaskResult.class,
                                              TaskResult.class.getSimpleName()));

        module.registerSubtypes(new NamedType(ClaimTaskPayload.class,
                                              ClaimTaskPayload.class.getSimpleName()));

        module.registerSubtypes(new NamedType(CompleteTaskPayload.class,
                                              CompleteTaskPayload.class.getSimpleName()));

        module.registerSubtypes(new NamedType(SaveTaskPayload.class,
                                              SaveTaskPayload.class.getSimpleName()));
        
        module.registerSubtypes(new NamedType(CreateTaskPayload.class,
                                              CreateTaskPayload.class.getSimpleName()));

        module.registerSubtypes(new NamedType(DeleteTaskPayload.class,
                                              DeleteTaskPayload.class.getSimpleName()));

        module.registerSubtypes(new NamedType(GetTasksPayload.class,
                                              GetTasksPayload.class.getSimpleName()));

        module.registerSubtypes(new NamedType(GetTaskVariablesPayload.class,
                                              GetTaskVariablesPayload.class.getSimpleName()));

        module.registerSubtypes(new NamedType(ReleaseTaskPayload.class,
                                              ReleaseTaskPayload.class.getSimpleName()));

        module.registerSubtypes(new NamedType(UpdateTaskPayload.class,
                                              UpdateTaskPayload.class.getSimpleName()));

        module.setAbstractTypes(resolver);

        return module;
    }
}
