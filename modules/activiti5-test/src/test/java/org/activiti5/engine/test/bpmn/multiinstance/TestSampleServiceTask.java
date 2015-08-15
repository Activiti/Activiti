package org.activiti5.engine.test.bpmn.multiinstance;

import org.activiti5.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti5.engine.impl.pvm.delegate.ActivityExecution;

/**
 * @author Andreas Karnahl
 */
public class TestSampleServiceTask extends AbstractBpmnActivityBehavior {
    @Override
    public void execute(ActivityExecution execution) throws Exception {
        System.out.println("###: execution: " + execution.getId() + "; " + execution.getVariable("value") + "; " + getMultiInstanceActivityBehavior());
        leave(execution);
    }
}
