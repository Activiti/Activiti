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
package org.activiti.engine;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.repository.DeploymentBuilderImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Clock;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

public interface OldActivitiCompatibilityHandler {

    org.activiti.bpmn.model.Process getProcessDefinitionProcessObject(String processDefinitionId);

    BpmnModel getProcessDefinitionBpmnModel(String processDefinitionId);

    void deleteDeployment(String deploymentId, boolean cascade);

    ProcessDefinition getProcessDefinition(String processDefinitionId);

    Deployment deploy(DeploymentBuilderImpl deploymentBuilder);

    ProcessInstance startProcessInstance(String processDefinitionKey, String processDefinitionId, Map<String, Object> variables, String businessKey, String tenantId, String processInstanceName);

    ProcessInstance startProcessInstanceByMessage(String messageName, Map<String, Object> variables, String businessKey, String tenantId);

    Map<String, Object> getExecutionVariables(String executionId, Collection<String> variableNames, boolean isLocal);

    void setExecutionVariables(String executionId, Map<String, ? extends Object> variables, boolean isLocal);

    void deleteProcessInstance(String processInstanceId, String deleteReason);

    void deleteHistoricProcessInstance(String processInstanceId);

    void completeTask(TaskEntity taskEntity, Map<String, Object> variables, boolean localScope);

    void deleteTask(String taskId, String deleteReason, boolean cascade);

    void saveTask(TaskEntity task);

    void deleteComment(String commentId, String taskId, String processInstanceId);

    Attachment createAttachment(String attachmentType, String taskId, String processInstanceId, String attachmentName, String attachmentDescription, InputStream content, String url);

    void trigger(String executionId, Map<String, Object> processVariables);

    void messageEventReceived(String messageName, String executionId, Map<String, Object> processVariables, boolean async);

    void signalEventReceived(String signalName, String executionId, Map<String, Object> processVariables, boolean async, String tenantId);

    void signalEventReceived(SignalEventSubscriptionEntity signalEventSubscriptionEntity, Object payload, boolean async);

    void executeJob(Job job);

    void propagateError(BpmnError bpmnError, DelegateExecution execution);

    Map<String, Object> getVariables(ProcessInstance processInstance);

    void setClock(Clock clock);

    void resetClock();

    void executeJobWithLockAndRetry(Job job);

    void changeDeploymentTenantId(String deploymentId, String newTenantId);

    void activateProcessInstance(String processInstanceId);

    void suspendProcessInstance(String processInstanceId);

    void setDeploymentCategory(String deploymentId, String category);

    void updateBusinessKey(String processInstanceId, String businessKey);

}
