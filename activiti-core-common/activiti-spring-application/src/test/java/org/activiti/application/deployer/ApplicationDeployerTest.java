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
package org.activiti.application.deployer;

import org.activiti.application.ApplicationContent;
import org.activiti.application.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class ApplicationDeployerTest {

    private ApplicationDeployer deployer;

    @Mock
    private ApplicationService applicationLoader;

    @Mock
    private ApplicationEntryDeployer firstDeployer;

    @Mock
    private ApplicationEntryDeployer secondDeployer;

    @BeforeEach
    public void setUp() {
        initMocks(this);
        deployer = new ApplicationDeployer(applicationLoader,
                                           asList(firstDeployer, secondDeployer));
    }

    @Test
    public void shouldDelegateDeployToEntryDeployers() {
        //given
        ApplicationContent firstApp = mock(ApplicationContent.class);
        ApplicationContent secondApp = mock(ApplicationContent.class);
        given(applicationLoader.loadApplications()).willReturn(asList(firstApp, secondApp));

        //when
        deployer.deploy();

        //then
        verify(firstDeployer).deployEntries(firstApp);
        verify(firstDeployer).deployEntries(secondApp);
        verify(secondDeployer).deployEntries(firstApp);
        verify(secondDeployer).deployEntries(secondApp);
    }
}
