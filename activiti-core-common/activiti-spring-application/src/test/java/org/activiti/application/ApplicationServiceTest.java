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
package org.activiti.application;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceTest {

    @InjectMocks
    private ApplicationService applicationService;

    @Mock
    private ApplicationDiscovery applicationDiscovery;

    @Mock
    private ApplicationReader applicationReader;

    @Test
    public void shouldLoadApplications() throws Exception {
        //given
        Resource applicationResource = mock(Resource.class);
        given(applicationResource.getInputStream()).willReturn(mock(InputStream.class));

        given(applicationDiscovery.discoverApplications()).willReturn(singletonList(applicationResource));

        ApplicationContent applicationContent = new ApplicationContent();
        given(applicationReader.read(applicationResource.getInputStream())).willReturn(applicationContent);

        //when
        List<ApplicationContent> applicationContents = applicationService.loadApplications();

        //then
        assertThat(applicationContents).containsExactly(applicationContent);
    }

    @Test
    public void shouldThrowApplicationLoadExceptionWhenIOExceptionOccurs() throws Exception {
        //given
        Resource applicationResource = mock(Resource.class);
        IOException ioException = new IOException();
        given(applicationResource.getInputStream()).willThrow(ioException);

        given(applicationDiscovery.discoverApplications()).willReturn(singletonList(applicationResource));

        //when
        Throwable thrown = catchThrowable(() -> applicationService.loadApplications());

        //then
        assertThat(thrown)
            .isInstanceOf(ApplicationLoadException.class)
            .hasCause(ioException);
    }
}
