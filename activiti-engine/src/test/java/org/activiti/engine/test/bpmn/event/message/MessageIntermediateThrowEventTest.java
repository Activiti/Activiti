package org.activiti.engine.test.bpmn.event.message;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

public class MessageIntermediateThrowEventTest extends PluggableActivitiTestCase {

    private static boolean listenerExecuted;

    public static class MyExecutionListener implements ExecutionListener {
      public void notify(DelegateExecution execution) {
        listenerExecuted = true;
      }
    }

    @Deployment
    public void testThrowMessageEvent() throws Exception {
      assertFalse(listenerExecuted);
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("testThrowMessageEvent");
      assertProcessEnded(pi.getProcessInstanceId());
      assertTrue(listenerExecuted);
    }    
}
