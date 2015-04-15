package org.activiti.engine.impl.agenda;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Gateway;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.util.condition.ConditionUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 */
public class TakeOutgoingSequenceFlowsOperation extends AbstractOperation {

    private static final Logger logger = LoggerFactory.getLogger(TakeOutgoingSequenceFlowsOperation.class);

    protected boolean evaluateConditions;

    public TakeOutgoingSequenceFlowsOperation(CommandContext commandContext, ActivityExecution activityExecution, boolean evaluateConditions) {
        super(commandContext, activityExecution);
        this.evaluateConditions = evaluateConditions;
    }

    @Override
    public void run() {
        FlowElement currentFlowElement = execution.getCurrentFlowElement();

        if (currentFlowElement == null) {
            currentFlowElement = findCurrentFlowElement(execution);
        } else {
            execution.setCurrentActivityId(currentFlowElement.getId());
        }

        // If execution is a scope (and not the process instance), the scope
        // must first be destroyed before we can continue
        if (execution.getParentId() != null && execution.isScope()) {
            agenda.planDestroyScopeOperation(execution);
        }

        // No scope, can continue
        if (currentFlowElement instanceof FlowNode) {
            leaveFlowNode((FlowNode) currentFlowElement);
        } else if (currentFlowElement instanceof SequenceFlow) {
            // Nothing to do here. The operation wasn't really needed, so simply pass it through
            agenda.planContinueProcessOperation(execution);
        }
    }

    protected void leaveFlowNode(FlowNode flowNode) {

        logger.debug("Leaving flow node {} with id '{}' by following it's {} outgoing sequenceflow", flowNode.getClass(), flowNode.getId(), flowNode.getOutgoingFlows().size());

        // Get default sequence flow (if set)
        String defaultSequenceFlowId = null;
        if (flowNode instanceof Activity) {
        	defaultSequenceFlowId = ((Activity) flowNode).getDefaultFlow();
        } else if (flowNode instanceof Gateway) {
         	defaultSequenceFlowId = ((Gateway) flowNode).getDefaultFlow();
        }
        
        // Determine which sequence flows can be used for leaving
        List<SequenceFlow> outgoingSequenceFlow = new ArrayList<SequenceFlow>();
        for (SequenceFlow sequenceFlow : flowNode.getOutgoingFlows()) {
            if (!evaluateConditions 
            		|| (evaluateConditions 
            				&& ConditionUtil.hasTrueCondition(sequenceFlow, execution)
            				&& (defaultSequenceFlowId == null || !defaultSequenceFlowId.equals(sequenceFlow.getId()))
            			)
            	) {
                outgoingSequenceFlow.add(sequenceFlow);
            }
        }
        
        // Check if there is a default sequence flow
    	if (outgoingSequenceFlow.size() == 0 && evaluateConditions) { // The elements that set this to false also have no support for default sequence flow
            if (defaultSequenceFlowId != null) {
            	 for (SequenceFlow sequenceFlow : flowNode.getOutgoingFlows()) {
            		 if (defaultSequenceFlowId.equals(sequenceFlow.getId())) {
            			 outgoingSequenceFlow.add(sequenceFlow);
            			 break;
            		 }
            	 }
            }
    	}

        // No outgoing found. Ending the execution
        if (outgoingSequenceFlow.size() == 0) {
            logger.warn("No outgoing sequence flow found for flow node '{}'.", flowNode.getId());
            agenda.planEndExecutionOperation(execution);
            return;
        }

        // Leave, and reuse the incoming sequence flow, make executions for all the others (if applicable)

        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
        List<ExecutionEntity> outgoingExecutions = new ArrayList<ExecutionEntity>(flowNode.getOutgoingFlows().size());

        // Reuse existing one
        SequenceFlow sequenceFlow = outgoingSequenceFlow.get(0);
        execution.setCurrentFlowElement(sequenceFlow);
        execution.setActive(true);
//        execution.setScope(false);
        outgoingExecutions.add((ExecutionEntity) execution);

        // Executions for all the other one
        if (outgoingSequenceFlow.size() > 1) {
            for (int i = 1; i < outgoingSequenceFlow.size(); i++) {

                ExecutionEntity outgoingExecutionEntity = new ExecutionEntity();
                outgoingExecutionEntity.setProcessDefinitionId(execution.getProcessDefinitionId());
                outgoingExecutionEntity.setProcessInstanceId(execution.getProcessInstanceId());

                outgoingExecutionEntity.setScope(false);
                outgoingExecutionEntity.setActive(true);

                outgoingExecutionEntity.setParentId(execution.getParentId() != null ? execution.getParentId() : execution.getId());

                sequenceFlow = outgoingSequenceFlow.get(i);
                outgoingExecutionEntity.setCurrentFlowElement(sequenceFlow);

                executionEntityManager.insert(outgoingExecutionEntity);
                outgoingExecutions.add(outgoingExecutionEntity);
            }
        }

        // Leave (only done when all executions have been made, since some queries depend on this)
        for (ExecutionEntity outgoingExecution : outgoingExecutions) {
            agenda.planContinueProcessOperation(outgoingExecution);
        }
    }

}
