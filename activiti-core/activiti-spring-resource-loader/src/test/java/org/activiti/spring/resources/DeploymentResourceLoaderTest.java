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
package org.activiti.spring.resources;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.util.IoUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class DeploymentResourceLoaderTest {

    @Test
    public void shouldLoadOnlyResourcesSelectdedByReader() throws IOException {
        //given
        List<String> names = new ArrayList<>();
        names.add("classpath:file-selected.txt");
        names.add("classpath:file-unselected.txt");
        Resource resource = new ClassPathResource("file-selected.txt");

        RepositoryService service = Mockito.mock(RepositoryService.class);
        Mockito.when(service.getDeploymentResourceNames("123456"))
                .thenReturn(names);
        Mockito.when(service.getResourceAsStream("123456", "classpath:file-selected.txt"))
                .thenReturn(resource.getInputStream());

        DeploymentResourceLoader deploymentResourceLoader = new DeploymentResourceLoader<String>();
        deploymentResourceLoader.setRepositoryService(service);

        ResourceReader selectorReader = new ResourceReader<String>() {
            @Override
            public Predicate<String> getResourceNameSelector() {
                return resourceName -> resourceName.endsWith("-selected.txt");

            }

            @Override
            public String read(InputStream inputStream) throws IOException {
                return new String(IoUtil.readInputStream(inputStream, "the stream"));
            }
        };

        //when
        List<String> loaded = deploymentResourceLoader.loadResourcesForDeployment("123456", selectorReader);

        //then
        assertThat(loaded)
                .hasSize(1)
                .contains("a selected resource"+System.getProperty("line.separator"));

    }
}
