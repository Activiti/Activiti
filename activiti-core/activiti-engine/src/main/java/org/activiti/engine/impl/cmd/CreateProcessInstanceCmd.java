package org.activiti.engine.impl.cmd;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.runtime.ProcessInstanceBuilderImpl;
import org.activiti.engine.impl.util.ProcessInstanceHelper;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;

import java.util.Map;

public class CreateProcessInstanceCmd implements Command<ProcessInstance> {

    private static final long serialVersionUID = 1L;
    protected String processDefinitionKey;
    protected String processDefinitionId;
    protected Map<String, Object> variables;
    protected Map<String, Object> transientVariables;
    protected String businessKey;
    protected String tenantId;
    protected String processInstanceName;
    protected ProcessInstanceHelper processInstanceHelper;

    public CreateProcessInstanceCmd(String processDefinitionKey, String processDefinitionId, String businessKey, Map<String, Object> variables) {
        this.processDefinitionKey = processDefinitionKey;
        this.processDefinitionId = processDefinitionId;
        this.businessKey = businessKey;
        this.variables = variables;
    }

    public CreateProcessInstanceCmd(String processDefinitionKey, String processDefinitionId, String businessKey, Map<String, Object> variables, String tenantId) {
        this(processDefinitionKey, processDefinitionId, businessKey, variables);
        this.tenantId = tenantId;
    }

    public CreateProcessInstanceCmd(ProcessInstanceBuilderImpl processInstanceBuilder) {
        this(processInstanceBuilder.getProcessDefinitionKey(),
            processInstanceBuilder.getProcessDefinitionId(),
            processInstanceBuilder.getBusinessKey(),
            processInstanceBuilder.getVariables(),
            processInstanceBuilder.getTenantId());
        this.processInstanceName = processInstanceBuilder.getProcessInstanceName();
        this.transientVariables = processInstanceBuilder.getTransientVariables();
    }

    public ProcessInstance execute(CommandContext commandContext) {

        if (processDefinitionId == null && processDefinitionKey == null) {
            throw new ActivitiIllegalArgumentException("processDefinitionKey and processDefinitionId are null");
        }

        DeploymentManager deploymentCache = commandContext.getProcessEngineConfiguration().getDeploymentManager();
        ProcessDefinition processDefinition = this.getProcessDefinitionByProcessDefinitionId(processDefinitionId, deploymentCache);
        if(processDefinition == null) {
            processDefinition = (processDefinitionKey != null && hasNoTenant(tenantId)) ?
                this.getProcessDefinitionByProcessDefinitionKey(processDefinitionKey, tenantId, deploymentCache):
                this.getProcessDefinitionByProcessDefinitionKeyAndTenantId(processDefinitionKey, tenantId, deploymentCache);
            if (processDefinition == null) {
                throw new ActivitiObjectNotFoundException("No process definition found for key '" + processDefinitionKey + "' for tenant identifier " + tenantId, ProcessDefinition.class);
            }
        }

        processInstanceHelper = commandContext.getProcessEngineConfiguration().getProcessInstanceHelper();
        return processInstanceHelper.createProcessInstance(processDefinition, businessKey, processInstanceName, variables, transientVariables);
    }

    private ProcessDefinition getProcessDefinitionByProcessDefinitionId(String processDefinitionId, DeploymentManager deploymentCache){
        ProcessDefinition processDefinition = null;
        if (processDefinitionId != null) {
            processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
        }
        return processDefinition;
    }

    private ProcessDefinition getProcessDefinitionByProcessDefinitionKey(String processDefinitionKey, String tenantId, DeploymentManager deploymentCache) {
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
