/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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
package org.activiti.api.runtime.conf.impl;

import tools.jackson.core.Version;
import tools.jackson.databind.jsontype.NamedType;
import tools.jackson.databind.module.SimpleAbstractTypeResolver;
import tools.jackson.databind.module.SimpleModule;
import org.activiti.api.model.shared.EmptyResult;
import org.activiti.api.model.shared.Payload;
import org.activiti.api.model.shared.Result;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.runtime.model.impl.VariableInstanceImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import tools.jackson.databind.JacksonModule;

@AutoConfiguration
@PropertySource("classpath:conf/rest-jackson-configuration.properties") //load default jackson configuration
public class CommonModelAutoConfiguration {

    //this bean will be automatically injected inside boot's jsonMapper
    @Bean
    public JacksonModule customizeCommonModelObjectMapper() {
        SimpleModule module = new SimpleModule("mapCommonModelInterfaces", Version.unknownVersion());
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();

        resolver.addMapping(VariableInstance.class, VariableInstanceImpl.class);

        module.setAbstractTypes(resolver);

        module.setMixInAnnotation(Payload.class, PayloadMixIn.class);
        module.setMixInAnnotation(Result.class, ResultMixIn.class);

        module.registerSubtypes(new NamedType(EmptyResult.class, EmptyResult.class.getSimpleName()));

        return module;
    }
}
