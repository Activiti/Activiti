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
package org.activiti.engine.impl.bpmn.deployer;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BpmnDeployerTest {

    @InjectMocks
    private BpmnDeployer bpmnDeployer;

    @Mock
    private BpmnDeploymentHelper bpmnDeploymentHelper;

    @Test
    public void makeProcessDefinitionsConsistentWithPersistedVersions_should_setAppVersion() {
        //given
        ParsedDeployment parsedDeployment = mock(ParsedDeployment.class);
        ProcessDefinitionEntityImpl parsedProcessDefinition = new ProcessDefinitionEntityImpl();

        given(parsedDeployment.getAllProcessDefinitions()).willReturn(singletonList(parsedProcessDefinition));

        ProcessDefinitionEntityImpl persistedProcessDefinition = new ProcessDefinitionEntityImpl();
        persistedProcessDefinition.setId("procId");
        persistedProcessDefinition.setVersion(1);
        persistedProcessDefinition.setAppVersion(2);
        given(bpmnDeploymentHelper.getPersistedInstanceOfProcessDefinition(parsedProcessDefinition))
            .willReturn(persistedProcessDefinition);

        //when
        bpmnDeployer.makeProcessDefinitionsConsistentWithPersistedVersions(
            parsedDeployment);

        //then
        assertThat(parsedProcessDefinition.getId()).isEqualTo(persistedProcessDefinition.getId());
        assertThat(parsedProcessDefinition.getVersion()).isEqualTo(persistedProcessDefinition.getVersion());
        assertThat(parsedProcessDefinition.getAppVersion()).isEqualTo(persistedProcessDefinition.getAppVersion());
        assertThat(parsedProcessDefinition.getSuspensionState()).isEqualTo(persistedProcessDefinition.getSuspensionState());

    }

}
