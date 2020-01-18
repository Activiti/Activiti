package org.activiti.engine.test.bpmn.multiinstance;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;

/**

 */
public class TestSampleServiceTask extends AbstractBpmnActivityBehavior {
  
  private static final long serialVersionUID = 1L;

  @Override
  public void execute(DelegateExecution execution) {
    System.out.println("###: execution: " + execution.getId() + "; " + execution.getVariable("value") + "; " + getMultiInstanceActivityBehavior());
    leave(execution);
  }
}
