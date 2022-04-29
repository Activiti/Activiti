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
package org.activiti.spring.boot;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.model.Extension;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessExtensionServiceIT {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ProcessExtensionService processExtensionService;


    @Test
    public void canReadExtension() throws IOException {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("Process_initialVarsProcess")
                .singleResult();

        boolean hasExtensionsFor = processExtensionService.hasExtensionsFor(processDefinition);

        assertThat(hasExtensionsFor).isTrue();

        Extension extensions = processExtensionService.getExtensionsFor(processDefinition);

        assertThat(extensions).isNotNull();
        assertThat(extensions.getProperties()).containsKey("d440ff7b-0ac8-4a97-b163-51a6ec49faa1");
    }
}
