package org.activiti.engine.impl.agenda;

import java.util.ArrayList;
import java.util.Collection;

import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.impl.delegate.InactiveActivityBehavior;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 */
public class ExecuteInactiveBehaviorsOperation extends AbstractOperation {

  private static final Logger logger = LoggerFactory.getLogger(ExecuteInactiveBehaviorsOperation.class);

  protected Collection<ExecutionEntity> involvedExecutions;

  public ExecuteInactiveBehaviorsOperation(CommandContext commandContext) {
    super(commandContext, null);
    this.involvedExecutions = agenda.getCommandContext().getInvolvedExecutions();
  }

  @Override
  public void run() {

    /**
     * Algorithm: for each execution that is involved in this command context, 1) Verify if its process definitions has any InactiveActivityBehavior instances. 2) If so, verify if there are any
     * executions inactive in those 3) Execute the inactivated behavior
     */

    for (ExecutionEntity executionEntity : involvedExecutions) {

      Process process = ProcessDefinitionUtil.getProcess(executionEntity.getProcessDefinitionId());
      Collection<String> flowNodeIdsWithInactivatedBehavior = new ArrayList<String>();
      for (FlowNode flowNode : process.findFlowElementsOfType(FlowNode.class)) {
        if (flowNode.getBehavior() instanceof InactiveActivityBehavior) {
          flowNodeIdsWithInactivatedBehavior.add(flowNode.getId());
        }
      }

      // Only check if the process actually has inactivated functionality
      // TODO: this information could be cached

      if (flowNodeIdsWithInactivatedBehavior.size() > 0) {
        Collection<ExecutionEntity> inactiveExecutions = commandContext.getExecutionEntityManager().getInactiveExecutionsForProcessInstance(executionEntity.getProcessInstanceId());
        for (ExecutionEntity inactiveExecution : inactiveExecutions) {
          if (!inactiveExecution.isActive() && flowNodeIdsWithInactivatedBehavior.contains(inactiveExecution.getActivityId())
              && !commandContext.getDbSqlSession().isPersistentObjectDeleted(inactiveExecution)) {

            FlowNode flowNode = (FlowNode) process.getFlowElement(inactiveExecution.getActivityId(), true);
            InactiveActivityBehavior inactiveActivityBehavior = ((InactiveActivityBehavior) flowNode.getBehavior());
            logger.debug("Found InactiveActivityBehavior instance of class {} that can be executed on activity '{}'", inactiveActivityBehavior.getClass(), flowNode.getId());
            inactiveActivityBehavior.executeInactive(inactiveExecution);
            ;

          }
        }
      }

    }
  }

}
