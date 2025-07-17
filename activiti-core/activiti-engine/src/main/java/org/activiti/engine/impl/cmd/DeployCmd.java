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

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.impl.repository.DeploymentBuilderImpl;
import org.activiti.engine.repository.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;


public class DeployCmd<T> implements Command<Deployment>, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployCmd.class);
    private static final long serialVersionUID = 1L;
    protected DeploymentBuilderImpl deploymentBuilder;

    public DeployCmd(DeploymentBuilderImpl deploymentBuilder) {
        this.deploymentBuilder = deploymentBuilder;
    }

    public Deployment execute(CommandContext commandContext) {
        return executeDeploy(commandContext);
    }

    protected Deployment executeDeploy(CommandContext commandContext) {

        DeploymentEntity newDeployment = setUpNewDeploymentFromContext(commandContext);

        if (deploymentBuilder.isDuplicateFilterEnabled()) {

            List<Deployment> existingDeployments = new ArrayList<>();

            if (newDeployment.getTenantId() == null ||
                ProcessEngineConfiguration.NO_TENANT_ID.equals(newDeployment.getTenantId())) {

                DeploymentEntity latestDeployment = getLatestDeployment(commandContext, newDeployment);

                if (latestDeployment != null) {
                    existingDeployments.add(latestDeployment);
                }

            } else {
                List<Deployment> deploymentList = commandContext
                    .getProcessEngineConfiguration()
                    .getRepositoryService()
                    .createDeploymentQuery()
                    .deploymentName(newDeployment.getName())
                    .deploymentTenantId(newDeployment.getTenantId())
                    .orderByDeploymentId()
                    .desc()
                    .list();

                if (!deploymentList.isEmpty()) {
                    existingDeployments.addAll(deploymentList);
                }
            }

            if (!existingDeployments.isEmpty()) {

                DeploymentEntity existingDeployment = (DeploymentEntity) existingDeployments.get(0);

                if (deploymentsDiffer(newDeployment, existingDeployment)) {
                    applyUpgradeLogic(newDeployment, existingDeployment);
                } else {
                    LOGGER.info("An existing deployment of version {} matching the current one was found, no need to deploy again.",
                        existingDeployment.getVersion());
                    return existingDeployment;
                }
            }
        }

        persistDeploymentInDatabase(commandContext, newDeployment);

        Map<String, Object> deploymentSettings = new HashMap<>();
        deploymentSettings.put(DeploymentSettings.IS_BPMN20_XSD_VALIDATION_ENABLED, deploymentBuilder.isBpmn20XsdValidationEnabled());
        deploymentSettings.put(DeploymentSettings.IS_PROCESS_VALIDATION_ENABLED, deploymentBuilder.isProcessValidationEnabled());

        LOGGER.info("Launching new deployment with version: " + newDeployment.getVersion());
        commandContext.getProcessEngineConfiguration().getDeploymentManager().deploy(newDeployment, deploymentSettings);

        if (deploymentBuilder.getProcessDefinitionsActivationDate() != null) {
            scheduleProcessDefinitionActivation(commandContext, newDeployment);
        }

        if (commandContext.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
            commandContext.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_INITIALIZED, newDeployment));
        }

        return newDeployment;
    }

    private static void persistDeploymentInDatabase(CommandContext commandContext, DeploymentEntity newDeployment) {
        commandContext.getDeploymentEntityManager().insert(newDeployment);

        if (commandContext.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
            commandContext.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, newDeployment));
        }
    }

    private DeploymentEntity getLatestDeployment(CommandContext commandContext, DeploymentEntity newDeployment) {

        DeploymentEntity latestDeployment = commandContext
            .getDeploymentEntityManager()
            .findLatestDeploymentByName(newDeployment.getName());

        if (latestDeployment != null) {
            latestDeployment = checkForRollback(commandContext, latestDeployment);
        }

        return latestDeployment;
    }

    private DeploymentEntity checkForRollback(CommandContext commandContext, DeploymentEntity latestDeployment) {

        if (commandContext.getProcessEngineConfiguration().isRollbackDeployment() &&
            latestDeployment.getVersion() > deploymentBuilder.getEnforcedAppVersion()) {

            LOGGER.info("Rollback detected: Previous rolled back deployment will be deleted");
            DeleteDeploymentCmd deleteDeploymentCmd = new DeleteDeploymentCmd(latestDeployment.getId(), false);
            deleteDeploymentCmd.execute(commandContext);

            return getDeploymentEntityForCurrentEnforcedAppVersion(commandContext);
        } else {
            return latestDeployment;
        }

    }

    private DeploymentEntity getDeploymentEntityForCurrentEnforcedAppVersion(CommandContext commandContext) {
        return commandContext
            .getDeploymentEntityManager()
            .findDeploymentByVersion(deploymentBuilder.getEnforcedAppVersion());
    }

    private DeploymentEntity setUpNewDeploymentFromContext(CommandContext commandContext) {
        DeploymentEntity deployment = deploymentBuilder.getDeployment();
        deployment.setDeploymentTime(commandContext.getProcessEngineConfiguration().getClock().getCurrentTime());
        setProjectReleaseVersion(deployment);
        deployment.setVersion(1);
        deployment.setNew(true);
        return deployment;
    }

    private void setProjectReleaseVersion(DeploymentEntity deployment) {
        if (deploymentBuilder.hasProjectManifestSet()) {
            deployment.setProjectReleaseVersion(deploymentBuilder.getProjectManifest().getVersion());
        }
    }

    private void applyUpgradeLogic(DeploymentEntity deployment,
                                   DeploymentEntity existingDeployment) {
        if (deploymentBuilder.hasEnforcedAppVersion()) {
            deployment.setVersion(deploymentBuilder.getEnforcedAppVersion());
        } else if (deploymentBuilder.hasProjectManifestSet()) {
            deployment.setVersion(existingDeployment.getVersion() + 1);
        }
    }

    protected boolean deploymentsDiffer(DeploymentEntity deployment,
                                        DeploymentEntity saved) {
        if (deploymentBuilder.hasEnforcedAppVersion()) {
            return deploymentsDifferWhenEnforcedAppVersionIsSet(saved);
        } else if (deploymentBuilder.hasProjectManifestSet()) {
            return deploymentsDifferWhenProjectManifestIsSet(deployment, saved);
        } else {
            return deploymentsDifferDefault(deployment, saved);
        }
    }

    private boolean deploymentsDifferWhenEnforcedAppVersionIsSet(DeploymentEntity saved) {
        return !deploymentBuilder.getEnforcedAppVersion().equals(saved.getVersion());
    }

    private boolean deploymentsDifferWhenProjectManifestIsSet(DeploymentEntity deployment,
                                                              DeploymentEntity saved) {
        return !deployment.getProjectReleaseVersion().equals(saved.getProjectReleaseVersion());
    }

    private boolean deploymentsDifferDefault(DeploymentEntity deployment, DeploymentEntity saved) {
        if (deployment.getResources() == null || saved.getResources() == null) {
            return true;
        }
        Map<String, ResourceEntity> resources = deployment.getResources();
        Map<String, ResourceEntity> savedResources = saved.getResources();

        for (String resourceName : resources.keySet()) {
            ResourceEntity savedResource = savedResources.get(resourceName);

            if (savedResource == null) {
                return true;
            }

            if (!savedResource.isGenerated()) {
                ResourceEntity resource = resources.get(resourceName);

                byte[] bytes = resource.getBytes();
                byte[] savedBytes = savedResource.getBytes();
                if (!Arrays.equals(bytes, savedBytes)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void scheduleProcessDefinitionActivation(CommandContext commandContext, DeploymentEntity deployment) {
        for (ProcessDefinitionEntity processDefinitionEntity : deployment.getDeployedArtifacts(ProcessDefinitionEntity.class)) {

            // If activation date is set, we first suspend all the process
            // definition
            SuspendProcessDefinitionCmd suspendProcessDefinitionCmd = new SuspendProcessDefinitionCmd(processDefinitionEntity, false, null, deployment.getTenantId());
            suspendProcessDefinitionCmd.execute(commandContext);

            // And we schedule an activation at the provided date
            ActivateProcessDefinitionCmd activateProcessDefinitionCmd = new ActivateProcessDefinitionCmd(processDefinitionEntity, false, deploymentBuilder.getProcessDefinitionsActivationDate(),
                deployment.getTenantId());
            activateProcessDefinitionCmd.execute(commandContext);
        }
    }

}
