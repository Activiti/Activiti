/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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
package org.activiti.spring.boot.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessRuntimeMultipleInstancesIT {

    private static final String PROCESS_MULTIPLE_INSTANCE_1 =
        "Process_1HN1Cx_u";
    private static final String PROCESS_MULTIPLE_INSTANCE_2 = "Process_0j9xfcp";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @AfterEach
    public void cleanUp() {
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void should_executeMultipleInstances() {
        securityUtil.logInAs("user");

        ProcessInstance processInstance1 = processRuntime.start(
            ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(PROCESS_MULTIPLE_INSTANCE_1)
                .build()
        );
        assertThat(processInstance1).isNotNull();

        List<VariableInstance> processInstance1Vars = processRuntime.variables(
            ProcessPayloadBuilder
                .variables()
                .withProcessInstanceId(processInstance1.getId())
                .build()
        );
        assertThat(processInstance1Vars)
            .extracting(VariableInstance::getName, VariableInstance::getValue)
            .containsOnly(tuple("name", "Kermit"));

        ProcessInstance processInstance2 = processRuntime.start(
            ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(PROCESS_MULTIPLE_INSTANCE_2)
                .build()
        );
        assertThat(processInstance1).isNotNull();

        List<VariableInstance> processInstance2Vars = processRuntime.variables(
            ProcessPayloadBuilder
                .variables()
                .withProcessInstanceId(processInstance2.getId())
                .build()
        );
        assertThat(processInstance2Vars)
            .extracting(VariableInstance::getName, VariableInstance::getValue)
            .containsOnly(tuple("lastName", "The Frog"));
    }
}
