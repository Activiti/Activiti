package org.activiti.engine.test.bpmn.multiinstance;

import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.delegate.ActivityExecution;

/**
 * @author Andreas Karnahl
 */
public class TestSampleServiceTask extends AbstractBpmnActivityBehavior {
  @Override
  public void execute(ActivityExecution execution) {
    System.out.println("###: execution: " + execution.getId() + "; " + execution.getVariable("value") + "; " + getMultiInstanceActivityBehavior());
    leave(execution);
  }
}
