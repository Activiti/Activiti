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

package org.activiti.spring.process;

import java.io.InputStream;

import org.activiti.engine.RepositoryService;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessExtensionResourceReaderIT {

    @MockBean
    private RepositoryService repositoryService;

    @Autowired
    private ProcessExtensionResourceReader reader;

    @Test
    public void shouldReadExtensionFromJsonFile() throws Exception{
        try(InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("processes/initial-vars-extensions.json")) {
            ProcessExtensionModel processExtensionModel = reader.read(inputStream);
            assertThat(processExtensionModel).isNotNull();
            assertThat(processExtensionModel.getId()).isEqualTo("initialVarsProcess");
            assertThat(processExtensionModel.getExtensions("Process_initialVarsProcess").getProperties()).containsKey("d440ff7b-0ac8-4a97-b163-51a6ec49faa1");
        }
    }
}
