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

package org.activiti.application;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

public class ApplicationDiscoveryTest {

    private ApplicationDiscovery applicationDiscovery;

    @Mock
    private ResourcePatternResolver resourceLoader;

    @BeforeEach
    public void setUp() {
        initMocks(this);
        applicationDiscovery = new ApplicationDiscovery(resourceLoader, "classpath:/applications/");
    }

    @Test
    public void discoverApplicationsShouldReturnResourcesFoundByResourceLoader() throws Exception {
        //given
        givenExistingResourceFolder();

        Resource applicationResource = mock(Resource.class);
        given(resourceLoader.getResources(anyString())).willReturn(new Resource[]{applicationResource});

        //when
        List<Resource> resources = applicationDiscovery.discoverApplications();

        //then
        assertThat(resources).containsExactly(applicationResource);
    }

    @Test
    public void discoverApplicationsShouldThrowApplicationLoadExceptionWhenIOExceptionOccurs() throws IOException {
        //given
        givenExistingResourceFolder();

        IOException ioException = new IOException();
        given(resourceLoader.getResources(anyString())).willThrow(ioException);

        //when
        Throwable thrown = catchThrowable(() -> applicationDiscovery.discoverApplications());

        //then
        assertThat(thrown)
                .isInstanceOf(ApplicationLoadException.class)
                .hasCause(ioException);
    }

    private void givenExistingResourceFolder() {
        Resource folderResource = mock(Resource.class);
        given(folderResource.exists()).willReturn(true);
        given(resourceLoader.getResource(anyString())).willReturn(folderResource);
    }
}
