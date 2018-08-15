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
import org.activiti.runtime.api.event.CloudRuntimeEvent;
import org.activiti.runtime.api.event.VariableEvent;
import org.activiti.runtime.api.event.impl.CloudVariableCreatedEventImpl;
import org.activiti.runtime.api.event.impl.CloudVariableDeletedEventImpl;
import org.activiti.runtime.api.event.impl.CloudVariableUpdatedEventImpl;
import org.activiti.runtime.api.model.CloudVariableInstance;
import org.activiti.runtime.api.model.impl.CloudVariableInstanceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudCommonModelAutoConfiguration {

    //this bean will be automatically injected inside boot's ObjectMapper
    @Bean
    public Module customizeCloudCommonModelObjectMapper() {
        SimpleModule module = new SimpleModule("mapMixCloudRuntimeEvents",
                                               Version.unknownVersion());

        module.registerSubtypes(new NamedType(CloudVariableCreatedEventImpl.class,
                                              VariableEvent.VariableEvents.VARIABLE_CREATED.name()));
        module.registerSubtypes(new NamedType(CloudVariableUpdatedEventImpl.class,
                                              VariableEvent.VariableEvents.VARIABLE_UPDATED.name()));
        module.registerSubtypes(new NamedType(CloudVariableDeletedEventImpl.class,
                                              VariableEvent.VariableEvents.VARIABLE_DELETED.name()));

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

        resolver.addMapping(CloudVariableInstance.class,
                            CloudVariableInstanceImpl.class);

        module.setAbstractTypes(resolver);

        module.setMixInAnnotation(CloudRuntimeEvent.class, CloudRuntimeMixIn.class);
        return module;
    }

}
