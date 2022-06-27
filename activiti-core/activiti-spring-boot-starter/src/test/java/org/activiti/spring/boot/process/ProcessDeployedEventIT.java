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
package org.activiti.spring.boot.process;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.spring.boot.process.listener.DeployedProcessesListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ProcessDeployedEventIT {

    private static final String CATEGORIZE_PROCESS = "categorizeProcess";
    private static final String CATEGORIZE_HUMAN_PROCESS = "categorizeHumanProcess";
    private static final String ONE_STEP_PROCESS = "OneStepProcess";

    @Autowired
    private DeployedProcessesListener listener;

    @Test
    public void shouldTriggerProcessDeployedEvents() {
        //when
        List<ProcessDefinition> deployedProcesses = listener.getDeployedProcesses();

        //then
        assertThat(deployedProcesses)
                .extracting(ProcessDefinition::getKey)
                .contains(CATEGORIZE_PROCESS,
                          CATEGORIZE_HUMAN_PROCESS,
                          ONE_STEP_PROCESS);
        assertThat(listener.getProcessModelContents().get(CATEGORIZE_PROCESS))
                .isNotEmpty()
                .isXmlEqualToContentOf(new File("src/test/resources/processes/categorize-image.bpmn20.xml"));
    }

}
