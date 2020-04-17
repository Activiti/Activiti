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

package org.activiti.spring.resources;

import java.util.List;

import org.activiti.spring.resources.conf.ResourceFinderAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ResourceFinderAutoConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ResourceFinderIT {

    @Autowired
    private ResourceFinder resourceFinder;

    @Test
    public void shouldReturnEmptyListWhenLocationDoesNotExist() throws Exception {
        //given
        DummyResourceFinderDescriptor finderDescriptor = new DummyResourceFinderDescriptor("classpath:**/not-exists/",
                                                                                                   "**.txt");

        //when
        List<Resource> foundResources = resourceFinder.discoverResources(finderDescriptor);

        //then
        assertThat(foundResources).isEmpty();
    }

    @Test
    public void shouldReturnEmptyListWhenLocationExitsButNoFileMatches() throws Exception {
        //given
        DummyResourceFinderDescriptor finderDescriptor = new DummyResourceFinderDescriptor("classpath:/no-matching-resources",
                                                                                                   "**.txt");

        //when
        List<Resource> foundResources = resourceFinder.discoverResources(finderDescriptor);

        //then
        assertThat(foundResources).isEmpty();
    }

    @Test
    public void shouldReturnMatchingFiles() throws Exception {
        //given
        DummyResourceFinderDescriptor finderDescriptor = new DummyResourceFinderDescriptor("classpath:/matching-resources/",
                                                                                           "**.json",
                                                                                           "**.txt");

        //when
        List<Resource> foundResources = resourceFinder.discoverResources(finderDescriptor);

        //then
        assertThat(foundResources)
                .extracting(resource -> resource.getFilename())
                .containsOnly("matching.json",
                              "matching.txt");
    }
}
