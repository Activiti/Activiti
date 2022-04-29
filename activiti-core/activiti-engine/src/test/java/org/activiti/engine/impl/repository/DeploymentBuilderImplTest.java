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
package org.activiti.engine.impl.repository;

import java.io.IOException;
import java.io.InputStream;

import org.activiti.engine.ActivitiException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DeploymentBuilderImplTest {

    @Spy
    @InjectMocks
    private DeploymentBuilderImpl deploymentBuilder;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private Resource resource;

    @Test
    public void addInputStreamShouldAddZipInputStreamWhenItsAZipLike() {
        //given
        doReturn(deploymentBuilder).when(deploymentBuilder).addZipInputStream(any());

        //when
        deploymentBuilder.addInputStream("my.bar",
                                         resource);

        //then
        verify(deploymentBuilder).addZipInputStream(any());
    }

    @Test
    public void addInputStreamShouldAddNormalImportStreamWhenITsNotAZipLike() throws Exception {
        //given
        String resourceName = "any.xml";
        InputStream inputStream = mock(InputStream.class);
        given(resource.getInputStream()).willReturn(inputStream);

        doReturn(deploymentBuilder).when(deploymentBuilder).addInputStream(resourceName,
                                                                           inputStream);

        //when
        deploymentBuilder.addInputStream(resourceName,
                                         resource);

        //then
        verify(deploymentBuilder).addInputStream(resourceName,
                                                 inputStream);
    }

    @Test
    public void addInputStreamShouldThrowActivitiExceptionWhenIOExceptionIsThrown() throws Exception {
        //given
        given(resource.getInputStream()).willThrow(new IOException());

        //when
        Throwable thrown = catchThrowable(() -> deploymentBuilder.addInputStream("any.xml", resource));

        //then
        assertThat(thrown)
                .isInstanceOf(ActivitiException.class)
                .hasMessageContaining("Couldn't auto deploy resource");
    }
}
