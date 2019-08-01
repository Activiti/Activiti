package org.activiti.engine.test.bpmn.event.message;

import org.activiti.bpmn.model.Message;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.bpmn.behavior.ThrowMessageJavaDelegate;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

public class MessageThrowEventTest extends PluggableActivitiTestCase {

    private static boolean listenerExecuted;
    private static boolean delegateExecuted;
    private static Message message;

    public static class MyExecutionListener implements ExecutionListener {
      public void notify(DelegateExecution execution) {
        listenerExecuted = true;
      }
    }

    public static class MyJavaDelegate implements ThrowMessageJavaDelegate {

        @Override
        public Object execute(DelegateExecution execution, Message message) {
            delegateExecuted = true;
            MessageThrowEventTest.message = message;
            
            return null;
        }
      }
    
    @Deployment
    public void testIntermediateThrowMessageEvent() throws Exception {
      listenerExecuted = false;
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("testIntermediateThrowMessageEvent");
      assertProcessEnded(pi.getProcessInstanceId());
      assertTrue(listenerExecuted);
    }    
   
    @Deployment
    public void testIntermediateThrowMessageEventJavaDelegate() throws Exception {
      delegateExecuted = false;
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("testIntermediateThrowMessageEventJavaDelegate");
      assertProcessEnded(pi.getProcessInstanceId());
      assertTrue(message.getName().equals("bpmnMessage"));
      assertTrue(delegateExecuted);
    }

    @Deployment
    public void testThrowMessageEndEvent() throws Exception {
      listenerExecuted = false;
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("testThrowMessageEndEvent");
      assertProcessEnded(pi.getProcessInstanceId());
      assertTrue(listenerExecuted);
    }
    
    @Deployment
    public void testThrowMessageEndEventJavaDelegate() throws Exception {
      delegateExecuted = false;
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("testThrowMessageEndEventJavaDelegate");
      assertProcessEnded(pi.getProcessInstanceId());
      assertTrue(message.getName().equals("endMessage"));
      assertTrue(delegateExecuted);
    }         
    
}
