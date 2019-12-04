/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.spring.boot.process;

import java.util.Map;

import org.activiti.api.process.runtime.connector.Connector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

@Configuration
public class ConnectorsConfiguration {

    @Bean
    public Connector connectorWithoutDefinition(){
        return  integrationContext -> {
            Map<String, Object> inBoundVariables = integrationContext.getInBoundVariables();
            assertThat(inBoundVariables).containsKeys("age",
                                                      "name",
                                                      "nameAsStaticValue");
            assertThat(inBoundVariables.get("age")).isEqualTo(20);
            assertThat(inBoundVariables.get("name")).isEqualTo("inName");
            assertThat(inBoundVariables.get("nameAsStaticValue")).isEqualTo("Paul");

            integrationContext.addOutBoundVariable("age", (Integer)inBoundVariables.get("age") + 1);
            integrationContext.addOutBoundVariable("name", inBoundVariables.get("nameAsStaticValue"));
            return integrationContext;
        };
    }

}
