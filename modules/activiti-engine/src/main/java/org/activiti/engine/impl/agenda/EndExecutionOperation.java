package org.activiti.engine.impl.agenda;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CompensateEventDefinition;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.Transaction;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.activiti.engine.impl.bpmn.helper.ScopeUtil;
import org.activiti.engine.impl.delegate.ActivityExecution;
import org.activiti.engine.impl.delegate.SubProcessActivityBehavior;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 */
public class EndExecutionOperation extends AbstractOperation {

  private static final Logger logger = LoggerFactory.getLogger(EndExecutionOperation.class);

  public EndExecutionOperation(CommandContext commandContext, ActivityExecution execution) {
    super(commandContext, execution);
  }

  @Override
  public void run() {

    ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
    ExecutionEntity executionEntity = (ExecutionEntity) execution;

    // Find parent execution. If not found, it's the process instance and other logic needs to happen
    ExecutionEntity parentExecution = null;
    if (executionEntity.getParentId() != null) {
      parentExecution = executionEntityManager.get(executionEntity.getParentId());
    }
    
    if (parentExecution != null) {

      // If the execution is a scope, all the child executions must be deleted first.
      if (executionEntity.isScope()) {
        executionEntityManager.deleteChildExecutions(executionEntity);
      }

      // Delete current execution
      logger.debug("Ending execution {}", execution.getId());
      executionEntityManager.deleteExecutionAndRelatedData(executionEntity);

      logger.debug("Parent execution found. Continuing process using execution {}", parentExecution.getId());

      SubProcess subProcess = null;
      if (executionEntity.getCurrentFlowElement() instanceof EndEvent) {
        EndEvent endEvent = (EndEvent) executionEntity.getCurrentFlowElement();
        subProcess = endEvent.getSubProcess();

        if (!parentExecution.getId().equals(parentExecution.getProcessInstanceId()) && subProcess != null && subProcess.getLoopCharacteristics() != null
            && subProcess.getBehavior() instanceof MultiInstanceActivityBehavior) {

          List<ExecutionEntity> activeChildExecutions = getActiveChildExecutionsForExecution(executionEntityManager, parentExecution.getId());
          boolean containsOtherChildExecutions = false;
          for (ExecutionEntity activeExecution : activeChildExecutions) {
            if (activeExecution.getId().equals(executionEntity.getId()) == false) {
              containsOtherChildExecutions = true;
            }
          }
          
          if (containsOtherChildExecutions == false) {
            MultiInstanceActivityBehavior multiInstanceBehavior = (MultiInstanceActivityBehavior) subProcess.getBehavior();
            parentExecution.setCurrentFlowElement(subProcess);
            multiInstanceBehavior.leave(parentExecution);
          }
          return;
        }
      }

      // If there are no more active child executions, the process can be continued
      // If not (eg an embedded subprocess still has active elements, we cannot continue)
      if (getNumberOfActiveChildExecutionsForExecution(executionEntityManager, parentExecution.getId()) == 0
          || isAllEventScopeExecutions(executionEntityManager, parentExecution)) {
        
        if (subProcess != null) {
          parentExecution.setCurrentFlowElement(subProcess);
          boolean hasCompensation = false;
          if (subProcess instanceof Transaction) {
            hasCompensation = true;
          } else {
            for (FlowElement subElement : subProcess.getFlowElements()) {
              if (subElement instanceof Activity) {
                Activity subActivity = (Activity) subElement;
                if (CollectionUtils.isNotEmpty(subActivity.getBoundaryEvents())) {
                  for (BoundaryEvent boundaryEvent : subActivity.getBoundaryEvents()) {
                    if (CollectionUtils.isNotEmpty(boundaryEvent.getEventDefinitions()) && 
                        boundaryEvent.getEventDefinitions().get(0) instanceof CompensateEventDefinition) {
                      
                      hasCompensation = true;
                      break;
                    }
                  }
                }
              }
            }
          }
          
          if (hasCompensation) {
            ScopeUtil.createCopyOfSubProcessExecutionForCompensation(parentExecution, parentExecution.getParent());
          }
          
        } else {
          if (executionEntity.getCurrentFlowElement() instanceof Activity) {
            Activity activity = (Activity) executionEntity.getCurrentFlowElement();
            if (activity.isForCompensation()) {
              return;
            }
          }
          
          if (!(parentExecution.getCurrentFlowElement() instanceof SubProcess)) {
            parentExecution.setCurrentFlowElement(executionEntity.getCurrentFlowElement());
          }
        }
        
        agenda.planTakeOutgoingSequenceFlowsOperation(parentExecution, true);
        
      }

    } else {

      String processInstanceId = executionEntity.getId(); // No parent execution == process instance id
      logger.debug("No parent execution found. Verifying if process instance {} can be stopped.", processInstanceId);

      ExecutionEntity superExecution = executionEntity.getSuperExecution();
      SubProcessActivityBehavior subProcessActivityBehavior = null;

      // copy variables before destroying the ended sub process instance (call activity)
      if (superExecution != null) {
        FlowNode superExecutionElement = (FlowNode) superExecution.getCurrentFlowElement();
        subProcessActivityBehavior = (SubProcessActivityBehavior) superExecutionElement.getBehavior();
        try {
          subProcessActivityBehavior.completing(superExecution, executionEntity);
        } catch (RuntimeException e) {
          logger.error("Error while completing sub process of execution {}", executionEntity, e);
          throw e;
        } catch (Exception e) {
          logger.error("Error while completing sub process of execution {}", executionEntity, e);
          throw new ActivitiException("Error while completing sub process of execution " + executionEntity, e);
        }
      }

      // TODO: optimisation can be made by keeping the nr of active executions directly on the process instance in db

      int activeExecutions = getNumberOfActiveChildExecutionsForProcessInstance(executionEntityManager, processInstanceId);
      if (activeExecutions == 0) {
        logger.debug("No active executions found. Ending process instance {} ", processInstanceId);
        executionEntityManager.deleteProcessInstanceExecutionEntity(processInstanceId, execution.getCurrentFlowElement() != null ? execution.getCurrentFlowElement().getId() : null, "FINISHED");
      } else {
        logger.debug("Active executions found. Process instance {} will not be ended.", processInstanceId);
      }

      Process process = getProcess(executionEntity.getProcessDefinitionId());
      
      // Execute execution listeners for process end.
      if (CollectionUtils.isNotEmpty(process.getExecutionListeners())) { 
        executeExecutionListeners(process, executionEntity, ExecutionListener.EVENTNAME_END, false);
      }
      
      // and trigger execution afterwards
      if (superExecution != null) {
        superExecution.setSubProcessInstance(null);
        try {
          subProcessActivityBehavior.completed(superExecution);
        } catch (RuntimeException e) {
          logger.error("Error while completing sub process of execution {}", executionEntity, e);
          throw e;
        } catch (Exception e) {
          logger.error("Error while completing sub process of execution {}", executionEntity, e);
          throw new ActivitiException("Error while completing sub process of execution " + executionEntity, e);
        }

      }
    }
  }

  protected int getNumberOfActiveChildExecutionsForProcessInstance(ExecutionEntityManager executionEntityManager, String processInstanceId) {
    Collection<ExecutionEntity> executions = executionEntityManager.findChildExecutionsByProcessInstanceId(processInstanceId);
    int activeExecutions = 0;
    for (ExecutionEntity execution : executions) {
      if (execution.isActive() && !processInstanceId.equals(execution.getId())) {
        activeExecutions++;
      }
    }
    return activeExecutions;
  }
  
  protected int getNumberOfActiveChildExecutionsForExecution(ExecutionEntityManager executionEntityManager, String executionId) {
    List<ExecutionEntity> executions = executionEntityManager.findChildExecutionsByParentExecutionId(executionId);
    int activeExecutions = 0;
    
    // Filter out the boundary events
    // TODO: discuss: should this be in the db?
    for (ExecutionEntity activeExecution : executions) {
      if (!(activeExecution.getCurrentFlowElement() instanceof BoundaryEvent)) {
        activeExecutions++;
      }
    }
    
    return activeExecutions;
  }
  
  protected List<ExecutionEntity> getActiveChildExecutionsForExecution(ExecutionEntityManager executionEntityManager, String executionId) {
    List<ExecutionEntity> activeChildExecutions = new ArrayList<ExecutionEntity>();
    List<ExecutionEntity> executions = executionEntityManager.findChildExecutionsByParentExecutionId(executionId);
    
    for (ExecutionEntity activeExecution : executions) {
      if (!(activeExecution.getCurrentFlowElement() instanceof BoundaryEvent)) {
        activeChildExecutions.add(activeExecution);
      }
    }
    
    return activeChildExecutions;
  }
  
  protected boolean isAllEventScopeExecutions(ExecutionEntityManager executionEntityManager, ExecutionEntity parentExecution) {
    boolean allEventScopeExecutions = true;
    List<ExecutionEntity> executions = executionEntityManager.findChildExecutionsByParentExecutionId(parentExecution.getId());
    for (ExecutionEntity childExecution : executions) {
      if (childExecution.isEventScope()) {
        executionEntityManager.deleteExecutionAndRelatedData(childExecution);
      } else {
        allEventScopeExecutions = false;
        break;
      }
    }
    return allEventScopeExecutions;
  }

  protected Process getProcess(String processDefinitionId) {
    BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(processDefinitionId);
    return bpmnModel.getMainProcess();
  }
}
