package org.activiti.engine.impl.util;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.repository.ProcessDefinition;

public class ProcessDefinitionRetriever {

    private String tenantId;
    private DeploymentManager deploymentCache;

    public ProcessDefinitionRetriever(String tenantId, DeploymentManager deploymentCache){
        this.tenantId = tenantId;
        this.deploymentCache = deploymentCache;
    }

    public ProcessDefinition getProcessDefinition(String processDefinitionId, String processDefinitionKey) {
        if (processDefinitionId == null && processDefinitionKey == null) {
            throw new ActivitiIllegalArgumentException("processDefinitionKey and processDefinitionId are null");
        }

        ProcessDefinition processDefinition = this.getProcessDefinitionByProcessDefinitionId(processDefinitionId, deploymentCache);
        if(processDefinition == null) {
            processDefinition = (processDefinitionKey != null && hasNoTenant(tenantId)) ?
                this.getProcessDefinitionByProcessDefinitionKey(processDefinitionKey, deploymentCache):
                this.getProcessDefinitionByProcessDefinitionKeyAndTenantId(processDefinitionKey, tenantId, deploymentCache);
            if (processDefinition == null) {
                throw new ActivitiObjectNotFoundException("No process definition found for key '" + processDefinitionKey + "' for tenant identifier " + tenantId, ProcessDefinition.class);
            }
        }

        return processDefinition;
    }

    private ProcessDefinition getProcessDefinitionByProcessDefinitionId(String processDefinitionId, DeploymentManager deploymentCache){
        ProcessDefinition processDefinition = null;
        if (processDefinitionId != null) {
            processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
        }
        return processDefinition;
    }

    private ProcessDefinition getProcessDefinitionByProcessDefinitionKey(String processDefinitionKey, DeploymentManager deploymentCache) {
        ProcessDefinition processDefinition = null;
        processDefinition = deploymentCache.findDeployedLatestProcessDefinitionByKey(processDefinitionKey);
        if (processDefinition == null) {
            throw new ActivitiObjectNotFoundException("No process definition found for key '" + processDefinitionKey + "'", ProcessDefinition.class);
        }
        return processDefinition;
    }

    private ProcessDefinition getProcessDefinitionByProcessDefinitionKeyAndTenantId(String processDefinitionKey, String tenantId, DeploymentManager deploymentCache) {
        ProcessDefinition processDefinition = null;
        if (processDefinitionKey != null && tenantId != null && !ProcessEngineConfiguration.NO_TENANT_ID.equals(tenantId)) {
            processDefinition = deploymentCache.findDeployedLatestProcessDefinitionByKeyAndTenantId(processDefinitionKey, tenantId);
        }
        return processDefinition;
    }

    private boolean hasNoTenant(String tenantId){
        return tenantId == null || ProcessEngineConfiguration.NO_TENANT_ID.equals(tenantId);
    }

}
