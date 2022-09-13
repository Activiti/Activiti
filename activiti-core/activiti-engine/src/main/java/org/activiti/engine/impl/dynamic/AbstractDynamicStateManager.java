package org.activiti.engine.impl.dynamic;

import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.history.HistoryManager;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.entity.*;
import org.activiti.engine.impl.runtime.ChangeActivityStateBuilderImpl;
import org.activiti.engine.impl.runtime.MoveActivityIdContainer;
import org.activiti.engine.impl.runtime.MoveExecutionIdContainer;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.impl.util.CommandContextUtil;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author LoveMyOrange
 */
public abstract class AbstractDynamicStateManager {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    //-- Move container preparation section start
    public List<MoveExecutionEntityContainer> resolveMoveExecutionEntityContainers(ChangeActivityStateBuilderImpl changeActivityStateBuilder, Optional<String> migrateToProcessDefinitionId, Map<String, Object> variables, CommandContext commandContext) {
        List<MoveExecutionEntityContainer> moveExecutionEntityContainerList = new ArrayList<>();
        if (changeActivityStateBuilder.getMoveExecutionIdList().size() > 0) {
            for (MoveExecutionIdContainer executionContainer : changeActivityStateBuilder.getMoveExecutionIdList()) {
                //Executions belonging to the same parent should move together - i.e multipleExecution to single activity
                Map<String, List<ExecutionEntity>> executionsByParent = new HashMap<>();
                for (String executionId : executionContainer.getExecutionIds()) {
                    ExecutionEntity execution = resolveActiveExecution(executionId, commandContext);
                    List<ExecutionEntity> executionEntities = executionsByParent.computeIfAbsent(execution.getParentId(), k -> new ArrayList<>());
                    executionEntities.add(execution);
                }
                executionsByParent.values().forEach(executions -> {
                    MoveExecutionEntityContainer moveExecutionEntityContainer = new MoveExecutionEntityContainer(executions, executionContainer.getMoveToActivityIds());
                    executionContainer.getNewAssigneeId().ifPresent(moveExecutionEntityContainer::setNewAssigneeId);
                    moveExecutionEntityContainerList.add(moveExecutionEntityContainer);
                });
            }
        }

        if (changeActivityStateBuilder.getMoveActivityIdList().size() > 0) {
            for (MoveActivityIdContainer activityContainer : changeActivityStateBuilder.getMoveActivityIdList()) {
                Map<String, List<ExecutionEntity>> activitiesExecutionsByMultiInstanceParentId = new HashMap<>();
                List<ExecutionEntity> activitiesExecutionsNotInMultiInstanceParent = new ArrayList<>();

                for (String activityId : activityContainer.getActivityIds()) {
                    List<ExecutionEntity> activityExecutions = resolveActiveExecutions(changeActivityStateBuilder.getProcessInstanceId(), activityId, commandContext);
                    if (!activityExecutions.isEmpty()) {

                        // check for a multi instance root execution
                        ExecutionEntity miExecution = null;
                        boolean isInsideMultiInstance = false;
                        for (ExecutionEntity possibleMIExecution : activityExecutions) {
                            if (possibleMIExecution.isMultiInstanceRoot()) {
                                miExecution = possibleMIExecution;
                                isInsideMultiInstance = true;
                                break;
                            }

                            if (isExecutionInsideMultiInstance(possibleMIExecution)) {
                                isInsideMultiInstance = true;
                            }
                        }

                        //If inside a multiInstance, we create one container for each execution
                        if (isInsideMultiInstance) {

                            //We group by the parentId (executions belonging to the same parent execution instance
                            // i.e. gateways nested in MultiInstance subProcesses, need to be in the same move container)
                            Stream<ExecutionEntity> executionEntitiesStream = activityExecutions.stream();
                            if (miExecution != null) {
                                executionEntitiesStream = executionEntitiesStream.filter(ExecutionEntity::isMultiInstanceRoot);
                            }

                            executionEntitiesStream.forEach(childExecution -> {
                                String parentId = childExecution.isMultiInstanceRoot() ? childExecution.getId() : childExecution.getParentId();
                                List<ExecutionEntity> executionEntities = activitiesExecutionsByMultiInstanceParentId.computeIfAbsent(parentId, k -> new ArrayList<>());
                                executionEntities.add(childExecution);
                            });

                        } else {
                            ExecutionEntity execution = activityExecutions.iterator().next();
                            activitiesExecutionsNotInMultiInstanceParent.add(execution);
                        }
                    }
                }

                //Create a move container for each execution group (executionList)
                Stream.concat(activitiesExecutionsByMultiInstanceParentId.values().stream(), Stream.of(activitiesExecutionsNotInMultiInstanceParent))
                    .filter(executions -> executions != null && !executions.isEmpty())
                    .forEach(executions -> moveExecutionEntityContainerList.add(createMoveExecutionEntityContainer(activityContainer, executions, commandContext)));
            }
        }

        return moveExecutionEntityContainerList;
    }

    protected ExecutionEntity resolveActiveExecution(String executionId, CommandContext commandContext) {
        ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager(commandContext);
        ExecutionEntity execution = executionEntityManager.findById(executionId);

        if (execution == null) {
            throw new ActivitiException("Execution could not be found with id " + executionId);
        }

        return execution;
    }

    protected List<ExecutionEntity> resolveActiveExecutions(String processInstanceId, String activityId, CommandContext commandContext) {
        ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager(commandContext);
        ExecutionEntity processExecution = executionEntityManager.findById(processInstanceId);

        if (processExecution == null) {
            throw new ActivitiException("Execution could not be found with id " + processInstanceId);
        }

        if (!processExecution.isProcessInstanceType()) {
            throw new ActivitiException("Execution is not a process instance type execution for id " + processInstanceId);
        }

        List<ExecutionEntity> childExecutions = executionEntityManager.findChildExecutionsByProcessInstanceId(processExecution.getId());

        List<ExecutionEntity> executions = childExecutions.stream()
            .filter(e -> e.getCurrentActivityId() != null)
            .filter(e -> e.getCurrentActivityId().equals(activityId))
            .collect(Collectors.toList());

        if (executions.isEmpty()) {
            throw new ActivitiException("Active execution could not be found with activity id " + activityId);
        }

        return executions;
    }

    protected MoveExecutionEntityContainer createMoveExecutionEntityContainer(MoveActivityIdContainer activityContainer, List<ExecutionEntity> executions, CommandContext commandContext) {
        MoveExecutionEntityContainer moveExecutionEntityContainer = new MoveExecutionEntityContainer(executions, activityContainer.getMoveToActivityIds());
        activityContainer.getNewAssigneeId().ifPresent(moveExecutionEntityContainer::setNewAssigneeId);

        if (activityContainer.isMoveToParentProcess()) {
            ExecutionEntity processInstanceExecution = executions.get(0).getProcessInstance();
            ExecutionEntity superExecution = processInstanceExecution.getSuperExecution();
            if (superExecution == null) {
                throw new ActivitiException("No parent process found for execution with activity id " + executions.get(0).getCurrentActivityId());
            }

            moveExecutionEntityContainer.setMoveToParentProcess(true);
            moveExecutionEntityContainer.setSuperExecution(superExecution);

        } else if (activityContainer.isMoveToSubProcessInstance()) {
            moveExecutionEntityContainer.setMoveToSubProcessInstance(true);
            moveExecutionEntityContainer.setCallActivityId(activityContainer.getCallActivityId());
            moveExecutionEntityContainer.setCallActivitySubProcessVersion(activityContainer.getCallActivitySubProcessVersion());
        }
        return moveExecutionEntityContainer;
    }

    protected void prepareMoveExecutionEntityContainer(MoveExecutionEntityContainer moveExecutionContainer, Optional<String> migrateToProcessDefinitionId, CommandContext commandContext) {
        ExpressionManager expressionManager = CommandContextUtil.getProcessEngineConfiguration(commandContext).getExpressionManager();

        Optional<BpmnModel> bpmnModelToMigrateTo = migrateToProcessDefinitionId.map(ProcessDefinitionUtil::getBpmnModel);
        boolean canContainerDirectMigrate = (moveExecutionContainer.getMoveToActivityIds().size() == 1) && (moveExecutionContainer.getExecutions().size() == 1);
        for (String activityId : moveExecutionContainer.getMoveToActivityIds()) {
            FlowElement currentFlowElement;
            FlowElement newFlowElement;
            String currentActivityId;
            if (moveExecutionContainer.isMoveToParentProcess()) {
                String parentProcessDefinitionId = moveExecutionContainer.getSuperExecution().getProcessDefinitionId();
                BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(parentProcessDefinitionId);
                BpmnModel modelOfCallActivity = ProcessDefinitionUtil.getBpmnModel(moveExecutionContainer.getExecutions().get(0).getProcessDefinitionId());
                currentActivityId = moveExecutionContainer.getExecutions().get(0).getCurrentActivityId();
                currentFlowElement = resolveFlowElementFromBpmnModel(modelOfCallActivity, currentActivityId);
                newFlowElement = resolveFlowElementFromBpmnModel(bpmnModelToMigrateTo.orElse(bpmnModel), activityId);
                canContainerDirectMigrate = false;

            } else if (moveExecutionContainer.isMoveToSubProcessInstance()) {
                //The subProcess model is defined in the callActivity of the current processDefinition or the migrateProcessDefinition if defined
                ExecutionEntity firstExecution = moveExecutionContainer.getExecutions().get(0);
                BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(firstExecution.getProcessDefinitionId());
                currentActivityId = firstExecution.getCurrentActivityId();
                currentFlowElement = resolveFlowElementFromBpmnModel(bpmnModel, currentActivityId);

                String processDefinitionIdOfCallActivity = migrateToProcessDefinitionId.orElse(firstExecution.getProcessDefinitionId());
                CallActivity callActivity = (CallActivity) resolveFlowElementFromBpmnModel(bpmnModelToMigrateTo.orElse(bpmnModel), moveExecutionContainer.getCallActivityId());

                moveExecutionContainer.setCallActivity(callActivity);
                ProcessDefinition callActivityProcessDefinition = ProcessDefinitionUtil.getProcessDefinition(processDefinitionIdOfCallActivity);
                String tenantId = callActivityProcessDefinition.getTenantId();
                Integer calledProcessVersion = moveExecutionContainer.getCallActivitySubProcessVersion();
                String calledProcessDefKey = callActivity.getCalledElement();
                if (isExpression(calledProcessDefKey)) {
                    try {
                        calledProcessDefKey = expressionManager.createExpression(calledProcessDefKey).getValue(firstExecution.getProcessInstance()).toString();
                    } catch (ActivitiException e) {
                        throw new ActivitiException("Cannot resolve calledElement expression '" + calledProcessDefKey + "' of callActivity '" + callActivity.getId() + "'", e);
                    }
                }
                moveExecutionContainer.setSubProcessDefKey(calledProcessDefKey);
                ProcessDefinition subProcessDefinition = resolveProcessDefinition(calledProcessDefKey, calledProcessVersion, tenantId, commandContext);
                BpmnModel subProcessModel = ProcessDefinitionUtil.getBpmnModel(subProcessDefinition.getId());
                moveExecutionContainer.setSubProcessDefinition(subProcessDefinition);
                moveExecutionContainer.setSubProcessModel(subProcessModel);

                newFlowElement = resolveFlowElementFromBpmnModel(subProcessModel, activityId);
                canContainerDirectMigrate = false;

            } else {
                // Get first execution to get process definition id
                ExecutionEntity firstExecution = moveExecutionContainer.getExecutions().get(0);
                BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(firstExecution.getProcessDefinitionId());
                currentActivityId = firstExecution.getCurrentActivityId();
                currentFlowElement = resolveFlowElementFromBpmnModel(bpmnModel, currentActivityId);
                newFlowElement = resolveFlowElementFromBpmnModel(bpmnModelToMigrateTo.orElse(bpmnModel), activityId);
            }

            moveExecutionContainer.addMoveToFlowElement(activityId, currentFlowElement, newFlowElement);
            canContainerDirectMigrate = canContainerDirectMigrate && isDirectFlowElementExecutionMigration(currentFlowElement, newFlowElement);
        }

        moveExecutionContainer.setDirectExecutionMigration(canContainerDirectMigrate && migrateToProcessDefinitionId.isPresent());
    }

    protected FlowElement resolveFlowElementFromBpmnModel(BpmnModel bpmnModel, String activityId) {
        FlowElement flowElement = bpmnModel.getFlowElement(activityId);
        if (flowElement == null) {
            throw new ActivitiException("Cannot find activity '" + activityId + "' in process definition with id '" + bpmnModel.getMainProcess().getId() + "'");
        }
        return flowElement;
    }
    //-- Move container preparation section end

    protected void doMoveExecutionState(ProcessInstanceChangeState processInstanceChangeState, CommandContext commandContext) {

        ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager(commandContext);
        Map<String, List<ExecutionEntity>> activeEmbeddedSubProcesses = resolveActiveEmbeddedSubProcesses(processInstanceChangeState.getProcessInstanceId(), commandContext);
        processInstanceChangeState.setProcessInstanceActiveEmbeddedExecutions(activeEmbeddedSubProcesses);

        //Set the processInstance variables first so they are available during te change state
        ExecutionEntity processInstanceExecution = executionEntityManager.findById(processInstanceChangeState.getProcessInstanceId());
        processInstanceExecution.setVariables(processInstanceChangeState.getProcessInstanceVariables());

        for (MoveExecutionEntityContainer moveExecutionContainer : processInstanceChangeState.getMoveExecutionEntityContainers()) {
            prepareMoveExecutionEntityContainer(moveExecutionContainer, processInstanceChangeState.getProcessDefinitionToMigrateTo().map(ProcessDefinition::getId), commandContext);
            // Action the moves (changeState)
            if (moveExecutionContainer.isMoveToParentProcess()) {
                String callActivityInstanceId = moveExecutionContainer.getExecutions().get(0).getProcessInstanceId();
                String deleteReason = "";
                if (StringUtils.isNotBlank(processInstanceChangeState.getJumpReason())) {
                    deleteReason = processInstanceChangeState.getJumpReason();
                } else {
                    deleteReason = "Change activity to parent process activity ids: " + printFlowElementIds(moveExecutionContainer.getMoveToFlowElements());
                }
                safeDeleteSubProcessInstance(callActivityInstanceId, moveExecutionContainer.getExecutions(), deleteReason, commandContext);
            }

            List<ExecutionEntity> executionsToMove;
            if (moveExecutionContainer.isMoveToParentProcess()) {
                executionsToMove = Collections.singletonList(moveExecutionContainer.getSuperExecution());
            } else {
                executionsToMove = moveExecutionContainer.getExecutions();
            }

            Collection<MoveExecutionEntityContainer.FlowElementMoveEntry> moveToFlowElements;
            if (moveExecutionContainer.isMoveToSubProcessInstance()) {
                moveToFlowElements = Collections.singletonList(new MoveExecutionEntityContainer.FlowElementMoveEntry(moveExecutionContainer.getCallActivity(), moveExecutionContainer.getCallActivity()));
            } else {
                moveToFlowElements = moveExecutionContainer.getMoveToFlowElements();
            }

            String flowElementIdsLine = printFlowElementIds(moveToFlowElements);
            Collection<String> executionIdsNotToDelete = new HashSet<>();
            for (ExecutionEntity execution : executionsToMove) {
                executionIdsNotToDelete.add(execution.getId());

                executionEntityManager.deleteChildExecutions(execution, "Change parent activity to " + flowElementIdsLine/*, true*/);
                if (!moveExecutionContainer.isDirectExecutionMigration()) {
                    String deleteReason = "";
                    String jumpReason = processInstanceChangeState.getJumpReason();
                    if (StringUtils.isNotBlank(jumpReason)) {
                        deleteReason = jumpReason;
                    } else {
                        deleteReason = "Change activity to " + flowElementIdsLine;
                    }
                    executionEntityManager.deleteExecutionAndRelatedData(execution, deleteReason/* ,  true*/);
                }

                // Make sure we are not moving the root execution
                if (execution.getParentId() == null) {
                    throw new ActivitiException("Execution has no parent execution " + execution.getParentId());
                }

                // Delete the parent executions for each current execution when the move to activity id has the same subProcess scope
                ExecutionEntity continueParentExecution;
                if (processInstanceChangeState.getProcessDefinitionToMigrateTo().isPresent()) {
                    continueParentExecution = deleteDirectParentExecutions(execution.getParentId(), moveToFlowElements, executionIdsNotToDelete, commandContext, processInstanceChangeState.getJumpReason());
                } else {
                    continueParentExecution = deleteParentExecutions(execution.getParentId(), moveToFlowElements, executionIdsNotToDelete, commandContext, processInstanceChangeState.getJumpReason());
                }
                moveExecutionContainer.addContinueParentExecution(execution.getId(), continueParentExecution);
            }

            List<ExecutionEntity> newChildExecutions = createEmbeddedSubProcessAndExecutions(moveToFlowElements, executionsToMove, moveExecutionContainer, processInstanceChangeState, commandContext);

            if (moveExecutionContainer.isMoveToSubProcessInstance()) {
                CallActivity callActivity = moveExecutionContainer.getCallActivity();
                Process subProcess = moveExecutionContainer.getSubProcessModel().getProcessById(moveExecutionContainer.getSubProcessDefKey());
                ExecutionEntity callActivityInstanceExecution = createCallActivityInstance(callActivity, moveExecutionContainer.getSubProcessDefinition(), newChildExecutions.get(0), subProcess.getInitialFlowElement().getId(), commandContext);
                List<ExecutionEntity> moveExecutions = moveExecutionContainer.getExecutions();
                MoveExecutionEntityContainer subProcessMoveExecutionEntityContainer = new MoveExecutionEntityContainer(moveExecutions, moveExecutionContainer.getMoveToActivityIds());
                subProcessMoveExecutionEntityContainer.setNewAssigneeId(moveExecutionContainer.getNewAssigneeId());
                moveExecutions.forEach(executionEntity -> subProcessMoveExecutionEntityContainer.addContinueParentExecution(executionEntity.getId(), callActivityInstanceExecution));
                newChildExecutions = createEmbeddedSubProcessAndExecutions(moveExecutionContainer.getMoveToFlowElements(), moveExecutions, subProcessMoveExecutionEntityContainer, new ProcessInstanceChangeState(), commandContext);
            }

            if (!processInstanceChangeState.getLocalVariables().isEmpty()) {
                Map<String, Map<String, Object>> localVariables = processInstanceChangeState.getLocalVariables();
                Iterator<ExecutionEntity> newChildExecutionsIterator = newChildExecutions.iterator();
                while (newChildExecutionsIterator.hasNext()) {
                    //With changeState Api we can set local variables to the parents of moved executions (i.e. subProcesses created during the move)
                    //Thus we traverse up in the hierarchy from the newly created executions
                    ExecutionEntity execution = newChildExecutionsIterator.next();
                    while (execution != null) {
                        if (execution.getActivityId() != null && localVariables.containsKey(execution.getActivityId())) {
                            if (execution.isScope() || execution.getCurrentFlowElement() instanceof UserTask) {
                                execution.setVariablesLocal(localVariables.get(execution.getActivityId()));
                            } else {
                                ExecutionEntity scopedExecutionCandidate = execution;
                                while (scopedExecutionCandidate.getParent() != null) {
                                    ExecutionEntity parentExecution = scopedExecutionCandidate.getParent();
                                    if (parentExecution.isScope()) {
                                        parentExecution.setVariablesLocal(localVariables.get(execution.getActivityId()));
                                        break;
                                    }

                                    scopedExecutionCandidate = scopedExecutionCandidate.getParent();
                                }
                            }
                        }
                        execution = execution.getParent();
                    }
                }
            }

            if (!moveExecutionContainer.isDirectExecutionMigration()) {
                for (ExecutionEntity newChildExecution : newChildExecutions) {
                    if (moveExecutionContainer.getNewAssigneeId() != null && moveExecutionContainer.hasNewExecutionId(newChildExecution.getId())) {
//                        MigrationContext migrationContext = new MigrationContext();
//                        migrationContext.setAssignee(moveExecutionContainer.getNewAssigneeId());
//                        CommandContextUtil.getAgenda(commandContext).planContinueProcessOperation();
//                        CommandContextUtil.getAgenda(commandContext).planContinueProcessWithMigrationContextOperation(newChildExecution, migrationContext);
                        CommandContextUtil.getAgenda(commandContext).planContinueProcessOperation(newChildExecution);
                    } else {

                        CommandContextUtil.getAgenda(commandContext).planContinueProcessOperation(newChildExecution);
                    }
                }
            }
        }

        processPendingEventSubProcessesStartEvents(processInstanceChangeState, commandContext);
    }

    protected void processPendingEventSubProcessesStartEvents(ProcessInstanceChangeState processInstanceChangeState, CommandContext commandContext) {
        for (Map.Entry<? extends StartEvent, ExecutionEntity> pendingStartEventEntry : processInstanceChangeState.getPendingEventSubProcessesStartEvents().entrySet()) {
            StartEvent startEvent = pendingStartEventEntry.getKey();
            if (!processInstanceChangeState.getCreatedEmbeddedSubProcesses().containsKey(startEvent.getSubProcess().getId())) {
                throw new ActivitiException("The current version does not support it, please wait");
            }
        }
    }


    protected abstract Map<String, List<ExecutionEntity>> resolveActiveEmbeddedSubProcesses(String processInstanceId, CommandContext commandContext);

    protected abstract boolean isDirectFlowElementExecutionMigration(FlowElement currentFlowElement, FlowElement newFlowElement);

    protected void safeDeleteSubProcessInstance(String processInstanceId, List<ExecutionEntity> executionsPool, String deleteReason, CommandContext commandContext) {
        ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager(commandContext);

        //Confirm that all the subProcessExecutions are in the executionsPool
        List<ExecutionEntity> subProcessExecutions = executionEntityManager.findChildExecutionsByProcessInstanceId(processInstanceId);
        HashSet<String> executionIdsToMove = executionsPool.stream().map(ExecutionEntity::getId).collect(Collectors.toCollection(HashSet::new));
        Optional<ExecutionEntity> notIncludedExecution = subProcessExecutions.stream().filter(e -> !executionIdsToMove.contains(e.getId())).findAny();
        if (notIncludedExecution.isPresent()) {
            throw new ActivitiException("Execution of sub process instance is not moved " + notIncludedExecution.get().getId());
        }

        // delete the sub process instance
        executionEntityManager.deleteProcessInstance(processInstanceId, deleteReason, true);
    }

    protected ExecutionEntity deleteParentExecutions(String parentExecutionId, Collection<MoveExecutionEntityContainer.FlowElementMoveEntry> moveToFlowElements, CommandContext commandContext, String jumpReason) {
        return deleteParentExecutions(parentExecutionId, moveToFlowElements, null, commandContext, jumpReason);
    }

    protected ExecutionEntity deleteParentExecutions(String parentExecutionId, Collection<MoveExecutionEntityContainer.FlowElementMoveEntry> moveToFlowElements, Collection<String> executionIdsNotToDelete, CommandContext commandContext, String jumpReason) {
        ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager(commandContext);

        ExecutionEntity parentExecution = executionEntityManager.findById(parentExecutionId);
        if (parentExecution != null && parentExecution.getCurrentFlowElement() instanceof SubProcess) {
            SubProcess parentSubProcess = (SubProcess) parentExecution.getCurrentFlowElement();
            if (!isSubProcessAncestorOfAnyNewFlowElements(parentSubProcess.getId(), moveToFlowElements)) {
                ExecutionEntity toDeleteParentExecution = resolveParentExecutionToDelete(parentExecution, moveToFlowElements);
                ExecutionEntity finalDeleteExecution = null;
                if (toDeleteParentExecution != null) {
                    finalDeleteExecution = toDeleteParentExecution;
                } else {
                    finalDeleteExecution = parentExecution;
                }

                parentExecution = finalDeleteExecution.getParent();

                String flowElementIdsLine = printFlowElementIds(moveToFlowElements);

                String deleteReason = "";
                if (StringUtils.isNotBlank(jumpReason)) {
                    deleteReason = jumpReason;
                } else {
                    deleteReason = "Change activity to " + flowElementIdsLine;
                }

                executionEntityManager.deleteChildExecutions(finalDeleteExecution, deleteReason/*, true*/);
                executionEntityManager.deleteExecutionAndRelatedData(finalDeleteExecution, deleteReason/*, true*/);
            }
        }

        return parentExecution;
    }

    protected ExecutionEntity deleteDirectParentExecutions(String parentExecutionId, Collection<MoveExecutionEntityContainer.FlowElementMoveEntry> moveToFlowElements, Collection<String> executionIdsNotToDelete, CommandContext commandContext, String jumpReason) {
        ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager(commandContext);

        ExecutionEntity parentExecution = executionEntityManager.findById(parentExecutionId);
        if (parentExecution.getCurrentFlowElement() instanceof SubProcess) {
            SubProcess parentSubProcess = (SubProcess) parentExecution.getCurrentFlowElement();
            if (!isSubProcessContainerOfAnyFlowElement(parentSubProcess.getId(), moveToFlowElements)) {
                ExecutionEntity toDeleteParentExecution = resolveParentExecutionToDelete(parentExecution, moveToFlowElements);
                ExecutionEntity finalDeleteExecution = null;
                if (toDeleteParentExecution != null) {
                    finalDeleteExecution = toDeleteParentExecution;
                } else {
                    finalDeleteExecution = parentExecution;
                }

                parentExecution = finalDeleteExecution.getParent();
                String flowElementIdsLine = printFlowElementIds(moveToFlowElements);
                String deleteReason = "";
                if (StringUtils.isNotBlank(jumpReason)) {
                    deleteReason = jumpReason;
                } else {
                    deleteReason = "Change activity to " + flowElementIdsLine;
                }
                executionEntityManager.deleteChildExecutions(finalDeleteExecution, deleteReason/*, true*/);
                executionEntityManager.deleteExecutionAndRelatedData(finalDeleteExecution, deleteReason/*, true*/);
            }
        }

        return parentExecution;
    }

    protected boolean isSubProcessContainerOfAnyFlowElement(String subProcessId, Collection<MoveExecutionEntityContainer.FlowElementMoveEntry> moveToFlowElements) {
        Optional<SubProcess> isUsed = moveToFlowElements.stream()
            .map(MoveExecutionEntityContainer.FlowElementMoveEntry::getNewFlowElement)
            .map(FlowElement::getSubProcess)
            .filter(Objects::nonNull)
            .filter(elementSubProcess -> elementSubProcess.getId().equals(subProcessId))
            .findAny();

        return isUsed.isPresent();
    }

    protected ExecutionEntity resolveParentExecutionToDelete(ExecutionEntity execution, Collection<MoveExecutionEntityContainer.FlowElementMoveEntry> moveToFlowElements) {
        ExecutionEntity parentExecution = execution.getParent();

        if (parentExecution.isProcessInstanceType()) {
            return null;
        }

        if (!isSubProcessContainerOfAnyFlowElement(parentExecution.getActivityId(), moveToFlowElements)) {
            ExecutionEntity subProcessParentExecution = resolveParentExecutionToDelete(parentExecution, moveToFlowElements);
            if (subProcessParentExecution != null) {
                return subProcessParentExecution;
            } else {
                return parentExecution;
            }
        }

        return null;
    }

    protected List<ExecutionEntity> createEmbeddedSubProcessAndExecutions(Collection<MoveExecutionEntityContainer.FlowElementMoveEntry> moveToFlowElements, List<ExecutionEntity> movingExecutions,
                                                                          MoveExecutionEntityContainer moveExecutionEntityContainer, ProcessInstanceChangeState processInstanceChangeState, CommandContext commandContext) {

        ProcessEngineConfigurationImpl processEngineConfiguration = CommandContextUtil.getProcessEngineConfiguration(commandContext);
        ExecutionEntityManager executionEntityManager = processEngineConfiguration.getExecutionEntityManager();

        // Resolve the sub process elements that need to be created for each move to flow element
        HashMap<String, SubProcess> subProcessesToCreate = new HashMap<>();
        for (MoveExecutionEntityContainer.FlowElementMoveEntry flowElementMoveEntry : moveToFlowElements) {
            FlowElement newFlowElement = flowElementMoveEntry.getNewFlowElement();
            SubProcess subProcess = newFlowElement.getSubProcess();
            //If the new flowElement is the StartEvent of and EventSubProcess, we skip the subProcess creation, the startEvent is contained in a level above
            if (isEventSubProcessStart(newFlowElement)) {
                subProcess = subProcess.getSubProcess();
            }
            while (subProcess != null) {
                if (!processInstanceChangeState.getProcessInstanceActiveEmbeddedExecutions().containsKey(subProcess.getId()) && !isSubProcessAncestorOfAnyExecution(subProcess.getId(), movingExecutions)) {
                    subProcessesToCreate.put(subProcess.getId(), subProcess);
                }
                subProcess = subProcess.getSubProcess();
            }
        }

        // The default parent execution is retrieved from the match with the first source execution
        ExecutionEntity defaultContinueParentExecution = moveExecutionEntityContainer.getContinueParentExecution(movingExecutions.get(0).getId());
        Set<String> movingExecutionIds = movingExecutions.stream().map(ExecutionEntity::getId).collect(Collectors.toSet());

        //Build the subProcess hierarchy
        for (SubProcess subProcess : subProcessesToCreate.values()) {
            if (!processInstanceChangeState.getCreatedEmbeddedSubProcesses().containsKey(subProcess.getId())) {
                ExecutionEntity embeddedSubProcess = createEmbeddedSubProcessHierarchy(subProcess, defaultContinueParentExecution, subProcessesToCreate, movingExecutionIds, processInstanceChangeState, commandContext);
                processInstanceChangeState.addCreatedEmbeddedSubProcess(subProcess.getId(), embeddedSubProcess);
            }
        }

        //Adds the execution (leaf) to the subProcess
        List<ExecutionEntity> newChildExecutions = new ArrayList<>();
        for (MoveExecutionEntityContainer.FlowElementMoveEntry flowElementMoveEntry : moveToFlowElements) {
            FlowElement newFlowElement = flowElementMoveEntry.getNewFlowElement();
            ExecutionEntity parentExecution;
            if (newFlowElement.getSubProcess() != null && processInstanceChangeState.getCreatedEmbeddedSubProcesses().containsKey(newFlowElement.getSubProcess().getId())) {
                parentExecution = processInstanceChangeState.getCreatedEmbeddedSubProcesses().get(newFlowElement.getSubProcess().getId());
            } else {
                parentExecution = defaultContinueParentExecution;
            }

            if (isEventSubProcessStart(newFlowElement)) {
                //EventSubProcessStarts are created later if the eventSubProcess was not created already during another move
                processInstanceChangeState.addPendingEventSubProcessStartEvent((StartEvent) newFlowElement, parentExecution);
            } else {
                ExecutionEntity newChildExecution;
                if (moveExecutionEntityContainer.isDirectExecutionMigration() && isDirectFlowElementExecutionMigration(flowElementMoveEntry.originalFlowElement, flowElementMoveEntry.newFlowElement)) {
                    newChildExecution = migrateExecutionEntity(parentExecution, movingExecutions.get(0), newFlowElement, commandContext);
                } else {
                    newChildExecution = executionEntityManager.createChildExecution(parentExecution);
                    newChildExecution.setCurrentFlowElement(newFlowElement);
                    moveExecutionEntityContainer.addNewExecutionId(newChildExecution.getId());
                }

                if (newChildExecution != null) {
                    if (moveExecutionEntityContainer.getNewAssigneeId() != null && newFlowElement instanceof UserTask &&
                        !moveExecutionEntityContainer.hasNewExecutionId(newChildExecution.getId())) {

                        handleUserTaskNewAssignee(newChildExecution, moveExecutionEntityContainer.getNewAssigneeId(), commandContext);
                    }

                    if (newFlowElement instanceof CallActivity) {
                        processEngineConfiguration.getHistoryManager().recordActivityStart(newChildExecution);

                        ActivitiEventDispatcher eventDispatcher = processEngineConfiguration.getEventDispatcher();
                        if (eventDispatcher != null && eventDispatcher.isEnabled()) {
                            eventDispatcher.dispatchEvent(
                                ActivitiEventBuilder.createActivityEvent(ActivitiEventType.ACTIVITY_STARTED, newFlowElement.getId(), newFlowElement.getName(), newChildExecution.getId(),
                                    newChildExecution.getProcessInstanceId(), newChildExecution.getProcessDefinitionId(), newFlowElement));
                        }
                    }

                    newChildExecutions.add(newChildExecution);
                }

                // Parallel gateway joins needs each incoming execution to enter the gateway naturally as it checks the number of executions to be able to progress/continue
                // If we have multiple executions going into a gateway, usually into a gateway join using xxxToSingleActivityId
                if (newFlowElement instanceof Gateway) {
                    //Skip one that was already added
                    movingExecutions.stream().skip(1).forEach(e -> {
                        ExecutionEntity childExecution = executionEntityManager.createChildExecution(defaultContinueParentExecution);
                        childExecution.setCurrentFlowElement(newFlowElement);
                        newChildExecutions.add(childExecution);
                    });
                }
            }
        }

        return newChildExecutions;
    }

    protected boolean isSubProcessAncestorOfAnyExecution(String subProcessId, List<ExecutionEntity> executions) {
        for (ExecutionEntity execution : executions) {
            FlowElement executionElement = execution.getCurrentFlowElement();

            if (isSubProcessAncestor(subProcessId, executionElement)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isSubProcessAncestorOfAnyNewFlowElements(String subProcessId, Collection<MoveExecutionEntityContainer.FlowElementMoveEntry> flowElements) {
        for (MoveExecutionEntityContainer.FlowElementMoveEntry flowElementMoveEntry : flowElements) {
            if (isSubProcessAncestor(subProcessId, flowElementMoveEntry.getNewFlowElement())) {
                return true;
            }
        }
        return false;
    }

    private boolean isSubProcessAncestor(String subProcessId, FlowElement flowElement) {
        while (flowElement.getSubProcess() != null) {
            String execElemSubProcessId = flowElement.getSubProcess().getId();
            if (execElemSubProcessId != null && execElemSubProcessId.equals(subProcessId)) {
                return true;
            }
            flowElement = flowElement.getSubProcess();
        }
        return false;
    }

    protected ExecutionEntity createEmbeddedSubProcessHierarchy(SubProcess subProcess, ExecutionEntity defaultParentExecution, Map<String, SubProcess> subProcessesToCreate, Set<String> movingExecutionIds, ProcessInstanceChangeState processInstanceChangeState, CommandContext commandContext) {
        ProcessEngineConfigurationImpl processEngineConfiguration = CommandContextUtil.getProcessEngineConfiguration(commandContext);
        if (processInstanceChangeState.getProcessInstanceActiveEmbeddedExecutions().containsKey(subProcess.getId())) {
            return processInstanceChangeState.getProcessInstanceActiveEmbeddedExecutions().get(subProcess.getId()).get(0);
        }

        if (processInstanceChangeState.getCreatedEmbeddedSubProcesses().containsKey(subProcess.getId())) {
            return processInstanceChangeState.getCreatedEmbeddedSubProcesses().get(subProcess.getId());
        }

        //Create the parent, if needed
        ExecutionEntity parentSubProcess = defaultParentExecution;
        if (subProcess.getSubProcess() != null) {
            parentSubProcess = createEmbeddedSubProcessHierarchy(subProcess.getSubProcess(), defaultParentExecution, subProcessesToCreate, movingExecutionIds, processInstanceChangeState, commandContext);
            processInstanceChangeState.getCreatedEmbeddedSubProcesses().put(subProcess.getSubProcess().getId(), parentSubProcess);
        }
        ExecutionEntityManager executionEntityManager = processEngineConfiguration.getExecutionEntityManager();

        ExecutionEntity subProcessExecution = executionEntityManager.createChildExecution(parentSubProcess);
        subProcessExecution.setCurrentFlowElement(subProcess);
        subProcessExecution.setScope(true);

        ActivitiEventDispatcher eventDispatcher = processEngineConfiguration.getEventDispatcher();
        if (eventDispatcher != null && eventDispatcher.isEnabled()) {
            eventDispatcher.dispatchEvent(
                ActivitiEventBuilder.createActivityEvent(ActivitiEventType.ACTIVITY_STARTED, subProcess.getId(), subProcess.getName(), subProcessExecution.getId(),
                    subProcessExecution.getProcessInstanceId(), subProcessExecution.getProcessDefinitionId(), subProcess));
        }

        subProcessExecution.setVariablesLocal(processDataObjects(subProcess.getDataObjects()));

        processEngineConfiguration.getHistoryManager().recordActivityStart(subProcessExecution);

        List<BoundaryEvent> boundaryEvents = subProcess.getBoundaryEvents();
        if (CollectionUtil.isNotEmpty(boundaryEvents)) {
            executeBoundaryEvents(boundaryEvents, subProcessExecution);
        }

        if (subProcess instanceof EventSubProcess) {
            processCreatedEventSubProcess((EventSubProcess) subProcess, subProcessExecution, movingExecutionIds, commandContext);
        }
        return subProcessExecution;
    }

    protected Map<String, Object> processDataObjects(Collection<ValuedDataObject> dataObjects) {
        Map<String, Object> variablesMap = new HashMap<>();
        // convert data objects to process variables
        if (dataObjects != null) {
            variablesMap = new HashMap<>(dataObjects.size());
            for (ValuedDataObject dataObject : dataObjects) {
                variablesMap.put(dataObject.getName(), dataObject.getValue());
            }
        }
        return variablesMap;
    }

    protected void executeBoundaryEvents(Collection<BoundaryEvent> boundaryEvents, ExecutionEntity execution) {

        // The parent execution becomes a scope, and a child execution is created for each of the boundary events
        for (BoundaryEvent boundaryEvent : boundaryEvents) {

            if (CollectionUtil.isEmpty(boundaryEvent.getEventDefinitions())
                || (boundaryEvent.getEventDefinitions().get(0) instanceof CompensateEventDefinition)) {
                continue;
            }

            // A Child execution of the current execution is created to represent the boundary event being active
            ExecutionEntity childExecutionEntity = CommandContextUtil.getExecutionEntityManager().createChildExecution(execution);
            childExecutionEntity.setParentId(execution.getId());
            childExecutionEntity.setCurrentFlowElement(boundaryEvent);
            childExecutionEntity.setScope(false);

            CommandContextUtil.getProcessEngineConfiguration().getHistoryManager().recordActivityStart(childExecutionEntity);

            ActivityBehavior boundaryEventBehavior = ((ActivityBehavior) boundaryEvent.getBehavior());
            LOGGER.debug("Executing boundary event activityBehavior {} with execution {}", boundaryEventBehavior.getClass(), childExecutionEntity.getId());
            boundaryEventBehavior.execute(childExecutionEntity);
        }
    }

    protected ExecutionEntity createCallActivityInstance(CallActivity callActivity, ProcessDefinition subProcessDefinition, ExecutionEntity parentExecution, String initialActivityId, CommandContext commandContext) {

        ProcessEngineConfigurationImpl processEngineConfiguration = CommandContextUtil.getProcessEngineConfiguration(commandContext);
        ExpressionManager expressionManager = processEngineConfiguration.getExpressionManager();
        ExecutionEntityManager executionEntityManager = processEngineConfiguration.getExecutionEntityManager();

        Process subProcess = ProcessDefinitionUtil.getProcess(subProcessDefinition.getId());
        if (subProcess == null) {
            throw new ActivitiException("Cannot start a sub process instance. Process model " + subProcessDefinition.getName() + " (id = " + subProcessDefinition.getId() + ") could not be found");
        }

        String businessKey = null;

        if (!StringUtils.isEmpty(callActivity.getBusinessKey())) {
            Expression expression = expressionManager.createExpression(callActivity.getBusinessKey());
            businessKey = expression.getValue(parentExecution).toString();

        } else if (callActivity.isInheritBusinessKey()) {
            ExecutionEntity processInstance = executionEntityManager.findById(parentExecution.getProcessInstanceId());
            businessKey = processInstance.getBusinessKey();
        }

        ExecutionEntity subProcessInstance = executionEntityManager.createSubprocessInstance(subProcessDefinition, parentExecution, businessKey);

        CommandContextUtil.getHistoryManager().recordSubProcessInstanceStart(parentExecution, subProcessInstance, callActivity);

        ActivitiEventDispatcher eventDispatcher = processEngineConfiguration.getEventDispatcher();
        if (eventDispatcher != null && eventDispatcher.isEnabled()) {
            CommandContextUtil.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(
                ActivitiEventType.PROCESS_STARTED, subProcessInstance));
        }

        // process template-defined data objects
        subProcessInstance.setVariables(processDataObjects(subProcess.getDataObjects()));

        Map<String, Object> variables = new HashMap<>();

        if (callActivity.isInheritVariables()) {
            Map<String, Object> executionVariables = parentExecution.getVariables();
            for (Map.Entry<String, Object> entry : executionVariables.entrySet()) {
                variables.put(entry.getKey(), entry.getValue());
            }
        }

        // copy process variables
        for (IOParameter ioParameter : callActivity.getInParameters()) {
            Object value;
            if (StringUtils.isNotEmpty(ioParameter.getSourceExpression())) {
                Expression expression = expressionManager.createExpression(ioParameter.getSourceExpression().trim());
                value = expression.getValue(parentExecution);

            } else {
                value = parentExecution.getVariable(ioParameter.getSource());
            }
            variables.put(ioParameter.getTarget(), value);
        }

        if (!variables.isEmpty()) {
            subProcessInstance.setVariables(variables);
        }

        if (eventDispatcher != null && eventDispatcher.isEnabled()) {
            eventDispatcher.dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_INITIALIZED, subProcessInstance));
        }

        return subProcessInstance;
    }

    protected ExecutionEntity migrateExecutionEntity(ExecutionEntity parentExecutionEntity, ExecutionEntity childExecution, FlowElement newFlowElement, CommandContext commandContext) {
        ProcessEngineConfigurationImpl processEngineConfiguration = CommandContextUtil.getProcessEngineConfiguration(commandContext);
        TaskService taskService = processEngineConfiguration.getTaskService();

        // manage the bidirectional parent-child relation
        childExecution.setProcessInstanceId(parentExecutionEntity.getProcessInstanceId());
        childExecution.setProcessInstance(parentExecutionEntity.getProcessInstance());
        childExecution.setProcessDefinitionId(parentExecutionEntity.getProcessDefinitionId());
        ExecutionEntity oldParent = childExecution.getParent();
        if (oldParent != null && !oldParent.getId().equals(parentExecutionEntity.getId())) {
            oldParent.getExecutions().remove(childExecution);
        }
        childExecution.setParent(parentExecutionEntity);
        parentExecutionEntity.addChildExecution(childExecution);

        //Additional changes if the new activity Id doesn't match
        String oldActivityId = childExecution.getCurrentActivityId();
        if (!childExecution.getCurrentActivityId().equals(newFlowElement.getId())) {
            ExecutionEntityImpl childExecution1 = (ExecutionEntityImpl) childExecution;
            childExecution1.setCurrentFlowElement(newFlowElement);
        }

        // If we are moving a UserTask we need to update its processDefinition references
        if (newFlowElement instanceof UserTask) {
            TaskEntityImpl task = (TaskEntityImpl) taskService.createTaskQuery()
                .executionId(childExecution.getId()).singleResult();
            task.setProcessDefinitionId(childExecution.getProcessDefinitionId());
            task.setTaskDefinitionKey(newFlowElement.getId());
            task.setName(newFlowElement.getName());
            task.setProcessInstanceId(childExecution.getProcessInstanceId());

            //Sync history
            syncUserTaskExecutionActivityInstance(childExecution, oldActivityId, newFlowElement);

            updateActivity(processEngineConfiguration, childExecution, oldActivityId, newFlowElement, task, new Date());
        }

        // Boundary Events - only applies to Activities and up to this point we have a UserTask or ReceiveTask execution, both are Activities
        List<BoundaryEvent> boundaryEvents = ((Activity) newFlowElement).getBoundaryEvents();
        if (boundaryEvents != null && !boundaryEvents.isEmpty()) {
            List<ExecutionEntity> boundaryEventsExecutions = createBoundaryEvents(boundaryEvents, childExecution, commandContext);
            executeBoundaryEvents(boundaryEvents, boundaryEventsExecutions);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Child execution {} updated with parent {}", childExecution, parentExecutionEntity.getId());
        }
        return childExecution;
    }

    public void updateActivity(ProcessEngineConfigurationImpl processEngineConfiguration, ExecutionEntity childExecution, String oldActivityId, FlowElement newFlowElement, TaskEntity task, Date updateTime) {

        HistoryManager historyManager = processEngineConfiguration.getHistoryManager();
        if (historyManager.isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
            HistoricActivityInstanceEntityManager historicActivityInstanceEntityManager = CommandContextUtil.getHistoricActivityInstanceEntityManager();
            List<HistoricActivityInstanceEntity> historicActivityInstances = historicActivityInstanceEntityManager.findUnfinishedHistoricActivityInstancesByExecutionAndActivityId(childExecution.getId(), oldActivityId);
            for (HistoricActivityInstanceEntity historicActivityInstance : historicActivityInstances) {
                historicActivityInstance.setProcessDefinitionId(childExecution.getProcessDefinitionId());
                historicActivityInstance.setActivityId(childExecution.getActivityId());
                historicActivityInstance.setActivityName(newFlowElement.getName());
            }
        }

        if (historyManager.isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
            historyManager.recordTaskAssigneeChange(task.getId(), task.getAssignee());
        }

    }

    protected void syncUserTaskExecutionActivityInstance(ExecutionEntity childExecution, String oldActivityId,
                                                         FlowElement newFlowElement) {
        HistoricActivityInstanceEntityManager activityInstanceEntityManager = CommandContextUtil.getActivityInstanceEntityManager();
        List<HistoricActivityInstanceEntity> activityInstances = activityInstanceEntityManager.findUnfinishedHistoricActivityInstancesByExecutionAndActivityId(childExecution.getId(), oldActivityId);
        for (HistoricActivityInstanceEntity activityInstance : activityInstances) {
            activityInstance.setProcessDefinitionId(childExecution.getProcessDefinitionId());
            activityInstance.setActivityId(childExecution.getActivityId());
            activityInstance.setActivityName(newFlowElement.getName());
        }
    }

    protected void handleUserTaskNewAssignee(ExecutionEntity taskExecution, String newAssigneeId, CommandContext commandContext) {
        ProcessEngineConfigurationImpl processEngineConfiguration = CommandContextUtil.getProcessEngineConfiguration(commandContext);
        TaskService taskService = processEngineConfiguration.getTaskService();
        TaskEntityImpl task = (TaskEntityImpl) taskService.createTaskQuery()
            .executionId(taskExecution.getId()).singleResult();
        if (task != null) {
            taskService.setAssignee(task.getId(), newAssigneeId);
        }
    }

    protected boolean isEventSubProcessStart(FlowElement flowElement) {
        return flowElement instanceof StartEvent && flowElement.getSubProcess() != null && flowElement.getSubProcess() instanceof EventSubProcess;
    }

    protected List<ExecutionEntity> createBoundaryEvents(List<BoundaryEvent> boundaryEvents, ExecutionEntity execution, CommandContext commandContext) {
        ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager(commandContext);

        List<ExecutionEntity> boundaryEventExecutions = new ArrayList<>(boundaryEvents.size());

        // The parent execution becomes a scope, and a child execution is created for each of the boundary events
        for (BoundaryEvent boundaryEvent : boundaryEvents) {

            if (CollectionUtil.isEmpty(boundaryEvent.getEventDefinitions())
                || (boundaryEvent.getEventDefinitions().get(0) instanceof CompensateEventDefinition)) {
                continue;
            }

            // A Child execution of the current execution is created to represent the boundary event being active
            ExecutionEntity childExecutionEntity = executionEntityManager.createChildExecution(execution);
            childExecutionEntity.setParentId(execution.getId());
            childExecutionEntity.setCurrentFlowElement(boundaryEvent);
            childExecutionEntity.setScope(false);
            boundaryEventExecutions.add(childExecutionEntity);
        }

        return boundaryEventExecutions;
    }

    protected void executeBoundaryEvents(List<BoundaryEvent> boundaryEvents, List<ExecutionEntity> boundaryEventExecutions) {
        if (!CollectionUtil.isEmpty(boundaryEventExecutions)) {
            Iterator<BoundaryEvent> boundaryEventsIterator = boundaryEvents.iterator();
            Iterator<ExecutionEntity> boundaryEventExecutionsIterator = boundaryEventExecutions.iterator();

            while (boundaryEventsIterator.hasNext() && boundaryEventExecutionsIterator.hasNext()) {
                BoundaryEvent boundaryEvent = boundaryEventsIterator.next();
                ExecutionEntity boundaryEventExecution = boundaryEventExecutionsIterator.next();
                ActivityBehavior boundaryEventBehavior = ((ActivityBehavior) boundaryEvent.getBehavior());
                LOGGER.debug("Executing boundary event activityBehavior {} with execution {}", boundaryEventBehavior.getClass(), boundaryEventExecution.getId());
                boundaryEventBehavior.execute(boundaryEventExecution);
            }
        }
    }

    protected boolean isExecutionInsideMultiInstance(ExecutionEntity execution) {
        return getFlowElementMultiInstanceParentId(execution.getCurrentFlowElement()).isPresent();
    }

    protected Optional<String> getFlowElementMultiInstanceParentId(FlowElement flowElement) {
        FlowElementsContainer parentContainer = flowElement.getParentContainer();
        while (parentContainer instanceof Activity) {
            if (isFlowElementMultiInstance((Activity) parentContainer)) {
                return Optional.of(((Activity) parentContainer).getId());
            }
            parentContainer = ((Activity) parentContainer).getParentContainer();
        }
        return Optional.empty();
    }

    protected boolean isFlowElementMultiInstance(FlowElement flowElement) {
        if (flowElement instanceof Activity) {
            return ((Activity) flowElement).getLoopCharacteristics() != null;
        }
        return false;
    }

    protected void processCreatedEventSubProcess(EventSubProcess eventSubProcess, ExecutionEntity eventSubProcessExecution, Set<String> movingExecutionIds, CommandContext commandContext) {
        // @todo如果后续使用到 EventSubProcess,则需要拓展
    }

    protected boolean isOnlyRemainingExecutionAtParentScope(ExecutionEntity executionEntity, Set<String> ignoreExecutionIds, CommandContext commandContext) {
        ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager(commandContext);
        List<ExecutionEntity> siblingExecutions = executionEntityManager.findChildExecutionsByParentExecutionId(executionEntity.getParentId());
        return siblingExecutions.stream()
            .filter(ExecutionEntity::isActive)
            .filter(execution -> !execution.getId().equals(executionEntity.getId()))
            .filter(execution -> !ignoreExecutionIds.contains(execution.getId()))
            .count() == 0;
    }

    protected boolean isExpression(String variableName) {
        return variableName.startsWith("${") || variableName.startsWith("#{");
    }

    protected ProcessDefinition resolveProcessDefinition(String processDefinitionKey, Integer processDefinitionVersion, String tenantId, CommandContext commandContext) {
        ProcessDefinitionEntityManager processDefinitionEntityManager = CommandContextUtil.getProcessDefinitionEntityManager(commandContext);
        ProcessDefinition processDefinition;
        if (processDefinitionVersion != null) {
            processDefinition = processDefinitionEntityManager.findProcessDefinitionByKeyAndVersionAndTenantId(processDefinitionKey, processDefinitionVersion, tenantId);
        } else {
            if (tenantId == null || ProcessEngineConfiguration.NO_TENANT_ID.equals(tenantId)) {
                processDefinition = processDefinitionEntityManager.findLatestProcessDefinitionByKey(processDefinitionKey);
            } else {
                processDefinition = processDefinitionEntityManager.findLatestProcessDefinitionByKeyAndTenantId(processDefinitionKey, tenantId);
            }
        }

        if (processDefinition == null) {
            DeploymentManager deploymentManager = CommandContextUtil.getProcessEngineConfiguration(commandContext).getDeploymentManager();
            if (tenantId == null || ProcessEngineConfiguration.NO_TENANT_ID.equals(tenantId)) {
                processDefinition = deploymentManager.findDeployedLatestProcessDefinitionByKey(processDefinitionKey);
            } else {
                processDefinition = deploymentManager.findDeployedLatestProcessDefinitionByKeyAndTenantId(processDefinitionKey, tenantId);
            }
        }
        return processDefinition;
    }

    private String printFlowElementIds(Collection<MoveExecutionEntityContainer.FlowElementMoveEntry> flowElements) {
        return flowElements.stream().map(MoveExecutionEntityContainer.FlowElementMoveEntry::getNewFlowElement).map(FlowElement::getId).collect(Collectors.joining(","));
    }
}

