package org.activiti.osgi.blueprint.bean;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

public class ActivityBehaviourBean implements ActivityBehavior {

  private static final long serialVersionUID = 1L;

  @Override
  public void execute(DelegateExecution execution) {
    execution.setVariable("visitedActivityBehaviour", true);
    Context.getAgenda().planTakeOutgoingSequenceFlowsOperation((ExecutionEntity) execution, true);
  }
}
