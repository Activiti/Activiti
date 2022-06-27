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


package org.activiti.engine.impl.bpmn.behavior;

import java.util.List;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.history.DeleteReason;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;


public class TerminateEndEventActivityBehavior extends FlowNodeActivityBehavior {

  private static final long serialVersionUID = 1L;

  protected boolean terminateAll;
  protected boolean terminateMultiInstance;

  public TerminateEndEventActivityBehavior() {

  }

  @Override
  public void execute(DelegateExecution execution) {

    CommandContext commandContext = Context.getCommandContext();
    ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();

    if (terminateAll) {
      terminateAllBehaviour(execution, commandContext, executionEntityManager);
    } else if (terminateMultiInstance) {
      terminateMultiInstanceRoot(execution, commandContext, executionEntityManager);
    } else {
      defaultTerminateEndEventBehaviour(execution, commandContext, executionEntityManager);
    }
  }

  protected void terminateAllBehaviour(DelegateExecution execution, CommandContext commandContext, ExecutionEntityManager executionEntityManager) {
    ExecutionEntity rootExecutionEntity = executionEntityManager.findByRootProcessInstanceId(execution.getRootProcessInstanceId());
    String deleteReason = createDeleteReason(execution.getCurrentActivityId());
    deleteExecutionEntities(executionEntityManager, rootExecutionEntity, deleteReason);
    endAllHistoricActivities(rootExecutionEntity.getId(), deleteReason);
    commandContext.getHistoryManager().recordProcessInstanceEnd(rootExecutionEntity.getId(),
        deleteReason, execution.getCurrentActivityId());
  }

  protected void defaultTerminateEndEventBehaviour(DelegateExecution execution, CommandContext commandContext,
      ExecutionEntityManager executionEntityManager) {

    ExecutionEntity scopeExecutionEntity = executionEntityManager.findFirstScope((ExecutionEntity) execution);
    sendProcessInstanceCancelledEvent(scopeExecutionEntity, execution.getCurrentFlowElement());

    // If the scope is the process instance, we can just terminate it all
    // Special treatment is needed when the terminated activity is a subprocess (embedded/callactivity/..)
    // The subprocess is destroyed, but the execution calling it, continues further on.
    // In case of a multi-instance subprocess, only one instance is terminated, the other instances continue to exist.

    String deleteReason = createDeleteReason(execution.getCurrentActivityId());

    if (scopeExecutionEntity.isProcessInstanceType() && scopeExecutionEntity.getSuperExecutionId() == null) {

      endAllHistoricActivities(scopeExecutionEntity.getId(), deleteReason);
      deleteExecutionEntities(executionEntityManager, scopeExecutionEntity, deleteReason);
      commandContext.getHistoryManager().recordProcessInstanceEnd(scopeExecutionEntity.getId(), deleteReason, execution.getCurrentActivityId());

    } else if (scopeExecutionEntity.getCurrentFlowElement() != null
        && scopeExecutionEntity.getCurrentFlowElement() instanceof SubProcess) { // SubProcess

      SubProcess subProcess = (SubProcess) scopeExecutionEntity.getCurrentFlowElement();

      scopeExecutionEntity.setDeleteReason(deleteReason);
      if (subProcess.hasMultiInstanceLoopCharacteristics()) {

        Context.getAgenda().planDestroyScopeOperation(scopeExecutionEntity);
        MultiInstanceActivityBehavior multiInstanceBehavior = (MultiInstanceActivityBehavior) subProcess.getBehavior();
        multiInstanceBehavior.leave(scopeExecutionEntity);

      } else {
        Context.getAgenda().planDestroyScopeOperation(scopeExecutionEntity);
        ExecutionEntity outgoingFlowExecution = executionEntityManager.createChildExecution(scopeExecutionEntity.getParent());
        outgoingFlowExecution.setCurrentFlowElement(scopeExecutionEntity.getCurrentFlowElement());
        Context.getAgenda().planTakeOutgoingSequenceFlowsOperation(outgoingFlowExecution, true);
      }

    } else if (scopeExecutionEntity.getParentId() == null
        && scopeExecutionEntity.getSuperExecutionId() != null) { // CallActivity

      ExecutionEntity callActivityExecution = scopeExecutionEntity.getSuperExecution();
      CallActivity callActivity = (CallActivity) callActivityExecution.getCurrentFlowElement();

      if (callActivity.hasMultiInstanceLoopCharacteristics()) {

        MultiInstanceActivityBehavior multiInstanceBehavior = (MultiInstanceActivityBehavior) callActivity.getBehavior();
        multiInstanceBehavior.leave(callActivityExecution);
        executionEntityManager.deleteProcessInstanceExecutionEntity(scopeExecutionEntity.getId(), execution.getCurrentFlowElement().getId(), "terminate end event", false, false);

      } else {

        executionEntityManager.deleteProcessInstanceExecutionEntity(scopeExecutionEntity.getId(), execution.getCurrentFlowElement().getId(), "terminate end event", false, false);
        ExecutionEntity superExecutionEntity = executionEntityManager.findById(scopeExecutionEntity.getSuperExecutionId());
        Context.getAgenda().planTakeOutgoingSequenceFlowsOperation(superExecutionEntity, true);

      }

    }
  }

  protected void endAllHistoricActivities(String processInstanceId, String deleteReason) {

    if (!Context.getProcessEngineConfiguration().getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      return;
    }

    List<HistoricActivityInstanceEntity> historicActivityInstances = Context.getCommandContext().getHistoricActivityInstanceEntityManager()
      .findUnfinishedHistoricActivityInstancesByProcessInstanceId(processInstanceId);

    for (HistoricActivityInstanceEntity historicActivityInstance : historicActivityInstances) {
      historicActivityInstance.markEnded(deleteReason);

      // Fire event
      ProcessEngineConfigurationImpl config = Context.getProcessEngineConfiguration();
      if (config != null && config.getEventDispatcher().isEnabled()) {
        config.getEventDispatcher().dispatchEvent(
            ActivitiEventBuilder.createEntityEvent(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_ENDED, historicActivityInstance));
      }
    }

  }

  protected void terminateMultiInstanceRoot(DelegateExecution execution, CommandContext commandContext,
      ExecutionEntityManager executionEntityManager) {

    // When terminateMultiInstance is 'true', we look for the multi instance root and delete it from there.
    ExecutionEntity miRootExecutionEntity = executionEntityManager.findFirstMultiInstanceRoot((ExecutionEntity) execution);
    if (miRootExecutionEntity != null) {

      // Create sibling execution to continue process instance execution before deletion
      ExecutionEntity siblingExecution = executionEntityManager.createChildExecution(miRootExecutionEntity.getParent());
      siblingExecution.setCurrentFlowElement(miRootExecutionEntity.getCurrentFlowElement());

      deleteExecutionEntities(executionEntityManager, miRootExecutionEntity, createDeleteReason(miRootExecutionEntity.getActivityId()));

      Context.getAgenda().planTakeOutgoingSequenceFlowsOperation(siblingExecution, true);
    } else {
      defaultTerminateEndEventBehaviour(execution, commandContext, executionEntityManager);
    }
  }

  protected void deleteExecutionEntities(ExecutionEntityManager executionEntityManager, ExecutionEntity rootExecutionEntity, String deleteReason) {

    List<ExecutionEntity> childExecutions = executionEntityManager.collectChildren(rootExecutionEntity);
    for (int i=childExecutions.size()-1; i>=0; i--) {
      executionEntityManager.cancelExecutionAndRelatedData(childExecutions.get(i), deleteReason);
    }
    executionEntityManager.cancelExecutionAndRelatedData(rootExecutionEntity, deleteReason);
  }

  protected void sendProcessInstanceCancelledEvent(DelegateExecution execution, FlowElement terminateEndEvent) {
      if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
          if (execution.isProcessInstanceType() && execution instanceof ExecutionEntity) {
              Context.getProcessEngineConfiguration().getEventDispatcher()
                  .dispatchEvent(
                      ActivitiEventBuilder.createProcessCancelledEvent(
                          ((ExecutionEntity) execution).getProcessInstance(),
                          createDeleteReason(terminateEndEvent.getId())));
          }
      }

    dispatchExecutionCancelled(execution, terminateEndEvent);
  }

  protected void dispatchExecutionCancelled(DelegateExecution execution, FlowElement terminateEndEvent) {

    ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();

    // subprocesses
    for (DelegateExecution subExecution : executionEntityManager.findChildExecutionsByParentExecutionId(execution.getId())) {
      dispatchExecutionCancelled(subExecution, terminateEndEvent);
    }

    // call activities
    ExecutionEntity subProcessInstance = Context.getCommandContext().getExecutionEntityManager().findSubProcessInstanceBySuperExecutionId(execution.getId());
    if (subProcessInstance != null) {
      dispatchExecutionCancelled(subProcessInstance, terminateEndEvent);
    }
  }


  public static String createDeleteReason(String activityId) {
      return activityId != null ?  DeleteReason.TERMINATE_END_EVENT + ": " + activityId : DeleteReason.TERMINATE_END_EVENT;
  }

  public boolean isTerminateAll() {
    return terminateAll;
  }

  public void setTerminateAll(boolean terminateAll) {
    this.terminateAll = terminateAll;
  }

  public boolean isTerminateMultiInstance() {
    return terminateMultiInstance;
  }

  public void setTerminateMultiInstance(boolean terminateMultiInstance) {
    this.terminateMultiInstance = terminateMultiInstance;
  }

}
