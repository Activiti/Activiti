package org.activiti.engine.impl.dynamic;


import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.engine.dynamic.DynamicStateManager;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.runtime.ChangeActivityStateBuilderImpl;
import org.activiti.engine.impl.util.CommandContextUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author LoveMyOrange
 */
public class DefaultDynamicStateManager extends AbstractDynamicStateManager implements DynamicStateManager {

    @Override
    public void moveExecutionState(ChangeActivityStateBuilderImpl changeActivityStateBuilder, CommandContext commandContext) {
        List<MoveExecutionEntityContainer> moveExecutionEntityContainerList = resolveMoveExecutionEntityContainers(changeActivityStateBuilder, Optional.empty(), changeActivityStateBuilder.getProcessInstanceVariables(), commandContext);
        List<ExecutionEntity> executions = moveExecutionEntityContainerList.iterator().next().getExecutions();
        String processInstanceId = executions.iterator().next().getProcessInstanceId();

        ProcessInstanceChangeState processInstanceChangeState = new ProcessInstanceChangeState()
            .setProcessInstanceId(processInstanceId)
            .setMoveExecutionEntityContainers(moveExecutionEntityContainerList)
            .setLocalVariables(changeActivityStateBuilder.getLocalVariables())
            .setProcessInstanceVariables(changeActivityStateBuilder.getProcessInstanceVariables())
            .setJumpReason(changeActivityStateBuilder.getJumpReason());


        doMoveExecutionState(processInstanceChangeState, commandContext);
    }

    @Override
    protected Map<String, List<ExecutionEntity>> resolveActiveEmbeddedSubProcesses(String processInstanceId, CommandContext commandContext) {
        ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager(commandContext);
        List<ExecutionEntity> childExecutions = executionEntityManager.findChildExecutionsByProcessInstanceId(processInstanceId);

        Map<String, List<ExecutionEntity>> activeSubProcessesByActivityId = childExecutions.stream()
            .filter(ExecutionEntity::isActive)
            .filter(executionEntity -> executionEntity.getCurrentFlowElement() instanceof SubProcess)
            .collect(Collectors.groupingBy(ExecutionEntity::getActivityId));
        return activeSubProcessesByActivityId;
    }

    @Override
    protected boolean isDirectFlowElementExecutionMigration(FlowElement currentFlowElement, FlowElement newFlowElement) {
        return false;
    }

}

