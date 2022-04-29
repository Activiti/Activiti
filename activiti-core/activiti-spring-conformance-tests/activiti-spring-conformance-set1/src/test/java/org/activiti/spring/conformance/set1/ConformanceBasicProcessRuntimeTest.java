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
package org.activiti.spring.conformance.set1;

import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessDefinitionMeta;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.conf.ProcessRuntimeConfiguration;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.runtime.shared.events.VariableEventListener;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.spring.conformance.util.security.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ConformanceBasicProcessRuntimeTest {


    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Test
    public void shouldGetConfiguration() {
        securityUtil.logInAs("user1");
        //when
        ProcessRuntimeConfiguration configuration = processRuntime.configuration();
        //then
        assertThat(configuration).isNotNull();
        //when
        List<ProcessRuntimeEventListener<?>> processRuntimeEventListeners = configuration.processEventListeners();
        List<VariableEventListener<?>> variableEventListeners = configuration.variableEventListeners();
        //then
        assertThat(processRuntimeEventListeners).hasSize(11);
        assertThat(variableEventListeners).hasSize(3);

    }

    @Test
    public void shouldProcessDefinitions() {
        securityUtil.logInAs("user1");

        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0, 50));

        List<ProcessDefinition> processDefinitions = processDefinitionPage.getContent();
        assertThat(processDefinitions).extracting(ProcessDefinition::getName).containsOnly(
                "ServiceTask with Implementation",
                "ServiceTask with Implementation Modify Variable"

        );

    }

    @Test
    public void shouldProcessDefinitionsMetaData() {
        securityUtil.logInAs("user1");

        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0, 50));

        List<ProcessDefinition> processDefinitions = processDefinitionPage.getContent();
        assertThat(processDefinitions).extracting(ProcessDefinition::getName).containsOnly(
                "ServiceTask with Implementation",
                "ServiceTask with Implementation Modify Variable"
        );

    }




}
