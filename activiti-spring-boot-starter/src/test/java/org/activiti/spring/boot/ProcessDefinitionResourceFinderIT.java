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

package org.activiti.spring.boot;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessDefinitionResourceFinderIT {

    @Autowired
    private ProcessDefinitionResourceFinder resourceFinder;

    @SpyBean
    private ActivitiProperties activitiProperties;

    @Test
    public void shouldReturnAllTheProcessDefinitionFilesOnSpecifiedFolder() throws Exception {
        //when
        List<Resource> resources = resourceFinder.discoverProcessDefinitionResources();

        //then
        assertThat(resources)
                .extracting(Resource::getFilename)
                .contains("categorize-human.bpmn20.xml",
                          "categorize-image.bpmn20.xml",
                          "gw.bpmn20.xml",
                          "waiter.bpmn20.xml",
                          "categorize-human-long-path.bpmn20.xml");//coming from folder long/path/for/processes
    }

    @Test
    public void shouldReturnEmptyListWhenNoProcessDefIsFoundOnSpecifiedFolder() throws Exception {
        //given
        given(activitiProperties.getProcessDefinitionLocationPrefix()).willReturn("classpath:**/processes-empty/");

        //when
        List<Resource> resources = resourceFinder.discoverProcessDefinitionResources();

        //then
        assertThat(resources).isEmpty();
    }

    @Test
    public void shouldReturnEmptyListWhenProcessDefinitionsFolderIsNotFound() throws Exception {
        //given
        given(activitiProperties.getProcessDefinitionLocationPrefix()).willReturn("classpath:**/does-not-exist/");

        //when
        List<Resource> resources = resourceFinder.discoverProcessDefinitionResources();

        //then
        assertThat(resources).isEmpty();
    }
}