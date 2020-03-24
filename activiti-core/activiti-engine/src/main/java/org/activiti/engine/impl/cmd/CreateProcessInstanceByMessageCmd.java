package org.activiti.engine.impl.cmd;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.activiti.engine.impl.runtime.ProcessInstanceBuilderImpl;
import org.activiti.engine.impl.util.ProcessInstanceHelper;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;

import java.util.Map;

public class CreateProcessInstanceByMessageCmd  implements Command<ProcessInstance> {

    protected String messageName;
    protected String businessKey;
    protected Map<String, Object> processVariables;
    protected Map<String, Object> transientVariables;
    protected String tenantId;

    public CreateProcessInstanceByMessageCmd(String messageName, String businessKey, Map<String, Object> processVariables, String tenantId) {
        this.messageName = messageName;
        this.businessKey = businessKey;
        this.processVariables = processVariables;
        this.tenantId = tenantId;
    }

    public CreateProcessInstanceByMessageCmd(ProcessInstanceBuilderImpl processInstanceBuilder) {
        this.messageName = processInstanceBuilder.getMessageName();
        this.businessKey = processInstanceBuilder.getBusinessKey();
        this.processVariables = processInstanceBuilder.getVariables();
        this.transientVariables = processInstanceBuilder.getTransientVariables();
        this.tenantId = processInstanceBuilder.getTenantId();
    }

    @Override
    public ProcessInstance execute(CommandContext commandContext) {

        if (messageName == null) {
            throw new ActivitiIllegalArgumentException("Cannot start process instance by message: message name is null");
        }

        MessageEventSubscriptionEntity messageEventSubscription = commandContext.getEventSubscriptionEntityManager().findMessageStartEventSubscriptionByName(messageName, tenantId);
        this.checkMessageSubscription(messageEventSubscription);
        ProcessDefinition processDefinition = this.getProjectDefinition(commandContext, messageEventSubscription);

        ProcessInstanceHelper processInstanceHelper = commandContext.getProcessEngineConfiguration().getProcessInstanceHelper();
        ProcessInstance processInstance = processInstanceHelper.createProcessInstance(processDefinition, businessKey, messageName, processVariables, transientVariables);

        return processInstance;
    }


    private void checkMessageSubscription(MessageEventSubscriptionEntity messageEventSubscription) {

        if (messageEventSubscription == null) {
            throw new ActivitiObjectNotFoundException("Cannot start process instance by message: no subscription to message with name '" + messageName + "' found.", MessageEventSubscriptionEntity.class);
        }

        String processDefinitionId = messageEventSubscription.getConfiguration();
        if (processDefinitionId == null) {
            throw new ActivitiException("Cannot start process instance by message: subscription to message with name '" + messageName + "' is not a message start event.");
        }
    }

    private ProcessDefinition getProjectDefinition(CommandContext commandContext, MessageEventSubscriptionEntity messageEventSubscription){
        String processDefinitionId = messageEventSubscription.getConfiguration();
        DeploymentManager deploymentCache = commandContext.getProcessEngineConfiguration().getDeploymentManager();
        ProcessDefinition processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
        if (processDefinition == null) {
            throw new ActivitiObjectNotFoundException("No process definition found for id '" + processDefinitionId + "'", ProcessDefinition.class);
        }
        return processDefinition;
    }
}
