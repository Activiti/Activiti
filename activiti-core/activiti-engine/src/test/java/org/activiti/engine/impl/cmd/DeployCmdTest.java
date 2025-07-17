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
package org.activiti.engine.impl.cmd;

import org.activiti.engine.delegate.event.impl.ActivitiEventDispatcherImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.DeploymentEntityImpl;
import org.activiti.engine.impl.persistence.entity.DeploymentEntityManager;
import org.activiti.engine.impl.repository.DeploymentBuilderImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.Clock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DeployCmdTest {

    private static final int ENFORCED_DEPLOYMENT_VERSION = 7;

    @Mock
    private CommandContext commandContext;

    @Mock
    private ProcessEngineConfigurationImpl processEngineConfiguration;

    @Mock
    private DeploymentManager deploymentManager;

    @Mock
    private ActivitiEventDispatcherImpl activitiEventDispatcher;

    @Mock
    private Clock clock;

    @Mock
    private DeploymentEntityManager deploymentEntityManager;

    @Mock
    private DeploymentBuilderImpl deploymentBuilder;

    @InjectMocks
    private DeployCmd deployCmd;

    @Before
    public void setUp() throws Exception {
        given(commandContext.getProcessEngineConfiguration()).willReturn(processEngineConfiguration);
        given(commandContext.getDeploymentEntityManager()).willReturn(deploymentEntityManager);
        given(processEngineConfiguration.getClock()).willReturn(clock);
        given(processEngineConfiguration.getEventDispatcher()).willReturn(activitiEventDispatcher);
        given(processEngineConfiguration.getDeploymentManager()).willReturn(deploymentManager);
        given(deploymentBuilder.getDeployment()).willReturn(new DeploymentEntityImpl());
        given(deploymentBuilder.isDuplicateFilterEnabled()).willReturn(true);
    }

    @Test
    public void should_returnAndPersistAndDeployNewDeployment_when_latestIsNotFound() {

        Deployment deployment = deployCmd.executeDeploy(commandContext);

        assertThat(((DeploymentEntity)deployment).isNew()).isTrue();
        assertThat((deployment).getVersion()).isEqualTo(1);

        verify(deploymentEntityManager).insert((DeploymentEntity) deployment);
        verify(deploymentManager).deploy((DeploymentEntity) deployment, buildDeploymentSettings());
    }

    @Test
    public void should_returnLatestDeploymentWithoutPersistingOrDeployingAgain_when_latestDeploymentFound() {

        DeploymentEntityImpl existingDeployment = buildExistingDeployment();

        given(deploymentEntityManager.findLatestDeploymentByName(any())).willReturn(existingDeployment);
        given(deploymentBuilder.getEnforcedAppVersion()).willReturn(ENFORCED_DEPLOYMENT_VERSION);
        given(deploymentBuilder.hasEnforcedAppVersion()).willReturn(true);

        Deployment deployment = deployCmd.executeDeploy(commandContext);

        assertThat(((DeploymentEntity)deployment).isNew()).isFalse();
        assertThat((deployment).getVersion()).isEqualTo(ENFORCED_DEPLOYMENT_VERSION);

        verify(deploymentEntityManager, never()).insert((DeploymentEntity) deployment);
        verify(deploymentManager, never()).deploy((DeploymentEntity) deployment, buildDeploymentSettings());
    }

    @Test
    public void should_returnLatestDeploymentWithoutPersistingOrDeployingAgain_when_latestDeploymentFoundDuringRollback() {

        DeploymentEntityImpl rolledBackDeployment = buildRolledBackDeployment();

        given(deploymentEntityManager.findLatestDeploymentByName(any())).willReturn(rolledBackDeployment);
        given(deploymentBuilder.getEnforcedAppVersion()).willReturn(ENFORCED_DEPLOYMENT_VERSION);
        given(deploymentBuilder.hasEnforcedAppVersion()).willReturn(true);
        given(processEngineConfiguration.isRollbackDeployment()).willReturn(true);

        DeploymentEntityImpl existingDeployment = buildExistingDeployment();

        given(deploymentEntityManager.findDeploymentByVersion(ENFORCED_DEPLOYMENT_VERSION)).willReturn(existingDeployment);

        Deployment deployment = deployCmd.executeDeploy(commandContext);

        assertThat(deployment.getName()).isEqualTo(existingDeployment.getName());
        assertThat(((DeploymentEntity)deployment).isNew()).isFalse();
        assertThat((deployment).getVersion()).isEqualTo(ENFORCED_DEPLOYMENT_VERSION);

        verify(deploymentEntityManager).findDeploymentByVersion(ENFORCED_DEPLOYMENT_VERSION);

        verify(deploymentEntityManager, never()).insert((DeploymentEntity) deployment);
        verify(deploymentManager, never()).deploy((DeploymentEntity) deployment, buildDeploymentSettings());
    }

    private static DeploymentEntityImpl buildExistingDeployment() {
        DeploymentEntityImpl existingDeployment = new DeploymentEntityImpl();
        existingDeployment.setId("existing-deployment-id");
        existingDeployment.setNew(false);
        existingDeployment.setVersion(ENFORCED_DEPLOYMENT_VERSION);
        return existingDeployment;
    }

    private static DeploymentEntityImpl buildRolledBackDeployment() {
        DeploymentEntityImpl rolledBackDeployment = new DeploymentEntityImpl();
        rolledBackDeployment.setId("rolled-back-deployment-id");
        rolledBackDeployment.setNew(false);
        rolledBackDeployment.setVersion(ENFORCED_DEPLOYMENT_VERSION + 1);
        return rolledBackDeployment;
    }

    private Map<String, Object> buildDeploymentSettings() {
        Map<String, Object> deploymentSettings = new HashMap<>();
        deploymentSettings.put(DeploymentSettings.IS_BPMN20_XSD_VALIDATION_ENABLED, deploymentBuilder.isBpmn20XsdValidationEnabled());
        deploymentSettings.put(DeploymentSettings.IS_PROCESS_VALIDATION_ENABLED, deploymentBuilder.isProcessValidationEnabled());
        return deploymentSettings;
    }

}
