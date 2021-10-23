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
package org.activiti.engine.compatibility;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.model.MapExceptionEntry;
import org.activiti.engine.OldActivitiCompatibilityHandler;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionCacheEntry;
import org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.impl.repository.DeploymentBuilderImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Clock;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface Activiti5CompatibilityHandler extends OldActivitiCompatibilityHandler {

    public static final String ACTIVITI_5_ENGINE_TAG = "activiti-5";

    ProcessDefinition getProcessDefinitionByKey(String processDefinitionKey);

    ObjectNode getProcessDefinitionInfo(String processDefinitionId);

    ProcessDefinitionCacheEntry resolveProcessDefinition(ProcessDefinition processDefinition);

    boolean isProcessDefinitionSuspended(String processDefinitionId);

    void addCandidateStarter(String processDefinitionId, String userId, String groupId);

    void deleteCandidateStarter(String processDefinitionId, String userId, String groupId);

    void suspendProcessDefinition(String processDefinitionId, String processDefinitionKey, boolean suspendProcessInstances, Date suspensionDate, String tenantId);

    void activateProcessDefinition(String processDefinitionId, String processDefinitionKey, boolean activateProcessInstances, Date activationDate, String tenantId);

    void setProcessDefinitionCategory(String processDefinitionId, String category);

    Object getExecutionVariable(String executionId, String variableName, boolean isLocal);

    VariableInstance getExecutionVariableInstance(String executionId, String variableName, boolean isLocal);

    Map<String, VariableInstance> getExecutionVariableInstances(String executionId, Collection<String> variableNames, boolean isLocal);

    void removeExecutionVariables(String executionId, Collection<String> variableNames, boolean isLocal);

    void addIdentityLinkForProcessInstance(String processInstanceId, String userId, String groupId, String identityLinkType);

    void deleteIdentityLinkForProcessInstance(String processInstanceId, String userId, String groupId, String identityLinkType);

    void claimTask(String taskId, String userId);

    void setTaskVariables(String taskId, Map<String, ? extends Object> variables, boolean isLocal);

    void removeTaskVariables(String taskId, Collection<String> variableNames, boolean isLocal);

    void setTaskDueDate(String taskId, Date dueDate);

    void setTaskPriority(String taskId, int priority);

    void deleteHistoricTask(String taskId);

    void addIdentityLink(String taskId, String identityId, int identityIdType, String identityType);

    void deleteIdentityLink(String taskId, String userId, String groupId, String identityLinkType);

    Comment addComment(String taskId, String processInstanceId, String type, String message);

    void saveAttachment(Attachment attachment);

    void deleteAttachment(String attachmentId);

    void handleFailedJob(Job job, Throwable exception);

    void deleteJob(String jobId);

    void leaveExecution(DelegateExecution execution);

    boolean mapException(Exception camelException, DelegateExecution execution, List<MapExceptionEntry> mapExceptions);

    Object getScriptingEngineValue(String payloadExpressionValue, String languageValue, DelegateExecution execution);

    void throwErrorEvent(ActivitiEvent event);

    Object getRawProcessEngine();

    Object getRawProcessConfiguration();

    Object getRawCommandExecutor();

    Object getCamelContextObject(String camelContextValue);
}
