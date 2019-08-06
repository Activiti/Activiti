package org.activiti.engine.test.bpmn.event.message;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiMessageEvent;
import org.activiti.engine.impl.bpmn.behavior.IntermediateThrowMessageEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ThrowMessageEndEventActivityBehavior;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.impl.delegate.ThrowMessage;
import org.activiti.engine.impl.delegate.ThrowMessageDelegate;
import org.activiti.engine.impl.delegate.ThrowMessageDelegateFactory;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.junit.After;
import org.junit.Before;

public class MessageThrowEventTest extends ResourceActivitiTestCase {

    private static boolean listenerExecuted;
    private static boolean delegateExecuted;
    private static ThrowMessage message;
    
    private static List<ActivitiEvent> receivedEvents = new LinkedList<>();
    
    public static class MyThrowMessageDelegateFactory implements ThrowMessageDelegateFactory {
        
    }

    public static class MyExecutionListener implements ExecutionListener {
      public void notify(DelegateExecution execution) {
        listenerExecuted = true;
      }
    }

    public static class MyJavaDelegate implements ThrowMessageDelegate {

        @Override
        public boolean send(DelegateExecution execution, ThrowMessage message) {
            delegateExecuted = true;
            MessageThrowEventTest.message = message;
            
            return true;
        }
      }
    
    private ActivitiEventListener myListener = new ActivitiEventListener() {  
        @Override
        public void onEvent(ActivitiEvent event) {
            receivedEvents.add(event);   
        }

        @Override
        public boolean isFailOnException() {
            return false;
        }            
    };

    public MessageThrowEventTest() {
        super("/org/activiti/engine/test/bpmn/event/message/MessageThrowEventTest.activiti.cfg.xml");
      }
    

    @Before
    public void setUp() {
        receivedEvents.clear();

        runtimeService.addEventListener(myListener, 
                                        ActivitiEventType.ACTIVITY_MESSAGE_SENT);
    }
    
    @After
    public void tearDown() {
        runtimeService.removeEventListener(myListener);
    }
    
    public void testMyThrowMessageDelegateFactory() {
        assertThat(StandaloneProcessEngineConfiguration.class.cast(processEngine.getProcessEngineConfiguration())
                                                             .getActivityBehaviorFactory())
                                                             .as("should provide custom throw message delegate factory")
                                                             .extracting("throwMessageDelegateFactory")
                                                             .allSatisfy(MyThrowMessageDelegateFactory.class::isInstance);
    }
    
    @Deployment
    public void testIntermediateThrowMessageEvent() throws Exception {
      listenerExecuted = false;
      
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("testIntermediateThrowMessageEvent");
      assertProcessEnded(pi.getProcessInstanceId());
      assertTrue(listenerExecuted);
      
      assertTrue(receivedEvents.size() > 0);
      
      ActivitiMessageEvent event = (ActivitiMessageEvent) receivedEvents.get(0);
      
      assertTrue(event.getActivityId().equals("messageThrow"));
      assertTrue(event.getActivityType().equals("throwEvent"));
      assertTrue(event.getActivityName().equals("Throw Message"));
      assertTrue(event.getBehaviorClass().equals(IntermediateThrowMessageEventActivityBehavior.class.getName()));
      assertTrue(event.getMessageName().equals("bpmnMessage"));
      assertTrue(event.getMessageData() == null);
      assertTrue(event.getProcessDefinitionId().equals(pi.getProcessDefinitionId()));
      assertTrue(event.getProcessInstanceId().equals(pi.getId()));
      assertTrue(event.getType().equals(ActivitiEventType.ACTIVITY_MESSAGE_SENT));
      assertTrue(event.getExecutionId() != null);      
    }    
   
    @Deployment
    public void testIntermediateThrowMessageEventJavaDelegate() throws Exception {
      delegateExecuted = false;
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("testIntermediateThrowMessageEventJavaDelegate");
      assertProcessEnded(pi.getProcessInstanceId());
      assertTrue(message.getName().equals("bpmnMessage"));
      assertTrue(delegateExecuted);

      assertTrue(receivedEvents.size() > 0);
      
      ActivitiMessageEvent event = (ActivitiMessageEvent) receivedEvents.get(0);
      
      assertTrue(event.getActivityId().equals("messageThrow"));
      assertTrue(event.getActivityType().equals("throwEvent"));
      assertTrue(event.getActivityName().equals("Throw Message"));
      assertTrue(event.getBehaviorClass().equals(IntermediateThrowMessageEventActivityBehavior.class.getName()));
      assertTrue(event.getMessageName().equals("bpmnMessage"));
      assertTrue(event.getMessageData() != null);
      assertTrue(event.getProcessDefinitionId().equals(pi.getProcessDefinitionId()));
      assertTrue(event.getProcessInstanceId().equals(pi.getId()));
      assertTrue(event.getType().equals(ActivitiEventType.ACTIVITY_MESSAGE_SENT));
      assertTrue(event.getExecutionId() != null);
    }

    @Deployment
    public void testThrowMessageEndEvent() throws Exception {
      listenerExecuted = false;
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("testThrowMessageEndEvent");
      assertProcessEnded(pi.getProcessInstanceId());
      assertTrue(listenerExecuted);
      
      assertTrue(receivedEvents.size() > 0);
      
      ActivitiMessageEvent event = (ActivitiMessageEvent) receivedEvents.get(0);
      
      assertTrue(event.getActivityId().equals("theEnd"));
      assertTrue(event.getActivityType().equals("endEvent"));
      assertTrue(event.getActivityName().equals("Throw Message"));
      assertTrue(event.getBehaviorClass().equals(ThrowMessageEndEventActivityBehavior.class.getName()));
      assertTrue(event.getMessageName().equals("endMessage"));
      assertTrue(event.getMessageData() == null);
      assertTrue(event.getProcessDefinitionId().equals(pi.getProcessDefinitionId()));
      assertTrue(event.getProcessInstanceId().equals(pi.getId()));
      assertTrue(event.getType().equals(ActivitiEventType.ACTIVITY_MESSAGE_SENT));
      assertTrue(event.getExecutionId() != null);      
    }
    
    @Deployment
    public void testThrowMessageEndEventJavaDelegate() throws Exception {
      delegateExecuted = false;
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("testThrowMessageEndEventJavaDelegate");
      assertProcessEnded(pi.getProcessInstanceId());
      assertTrue(message.getName().equals("endMessage"));
      assertTrue(delegateExecuted);
      
      assertTrue(receivedEvents.size() > 0);
      
      ActivitiMessageEvent event = (ActivitiMessageEvent) receivedEvents.get(0);
      
      assertTrue(event.getActivityId().equals("theEnd"));
      assertTrue(event.getActivityType().equals("endEvent"));
      assertTrue(event.getActivityName().equals("Throw Message"));
      assertTrue(event.getBehaviorClass().equals(ThrowMessageEndEventActivityBehavior.class.getName()));
      assertTrue(event.getMessageName().equals("endMessage"));
      assertTrue(event.getMessageData() != null);
      assertTrue(event.getProcessDefinitionId().equals(pi.getProcessDefinitionId()));
      assertTrue(event.getProcessInstanceId().equals(pi.getId()));
      assertTrue(event.getType().equals(ActivitiEventType.ACTIVITY_MESSAGE_SENT));
      assertTrue(event.getExecutionId() != null);
      
    }         

    @Deployment
    public void testIntermediateThrowMessageEventExpression() throws Exception {
      delegateExecuted = false;
      ProcessInstance pi = runtimeService.createProcessInstanceBuilder()
                                         .processDefinitionKey("testIntermediateThrowMessageEventExpression")
                                         .businessKey("foo")
                                         .start();
      
      assertProcessEnded(pi.getProcessInstanceId());
      assertTrue(message.getName().equals("bpmnMessage-foo"));
      assertTrue(delegateExecuted);
      
      assertTrue(receivedEvents.size() > 0);
      
      ActivitiMessageEvent event = (ActivitiMessageEvent) receivedEvents.get(0);
      
      assertTrue(event.getActivityId().equals("messageThrow"));
      assertTrue(event.getActivityType().equals("throwEvent"));
      assertTrue(event.getActivityName().equals("Throw Message"));
      assertTrue(event.getBehaviorClass().equals(IntermediateThrowMessageEventActivityBehavior.class.getName()));
      assertTrue(event.getMessageName().equals("bpmnMessage-foo"));
      assertTrue(event.getMessageData() != null);
      assertTrue(event.getProcessDefinitionId().equals(pi.getProcessDefinitionId()));
      assertTrue(event.getProcessInstanceId().equals(pi.getId()));
      assertTrue(event.getType().equals(ActivitiEventType.ACTIVITY_MESSAGE_SENT));
      assertTrue(event.getExecutionId() != null);      
    }
    
    @Deployment
    public void testThrowMessageEndEventExpression() throws Exception {
      delegateExecuted = false;
      ProcessInstance pi = runtimeService.createProcessInstanceBuilder()
                                         .processDefinitionKey("testThrowMessageEndEventExpression")
                                         .businessKey("bar")
                                         .start();
      
      assertProcessEnded(pi.getProcessInstanceId());
      assertTrue(message.getName().equals("endMessage-bar"));
      assertTrue(delegateExecuted);
      
      assertTrue(receivedEvents.size() > 0);
      
      ActivitiMessageEvent event = (ActivitiMessageEvent) receivedEvents.get(0);
      
      assertTrue(event.getActivityId().equals("theEnd"));
      assertTrue(event.getActivityType().equals("endEvent"));
      assertTrue(event.getActivityName().equals("Throw Message"));
      assertTrue(event.getBehaviorClass().equals(ThrowMessageEndEventActivityBehavior.class.getName()));
      assertTrue(event.getMessageName().equals("endMessage-bar"));
      assertTrue(event.getMessageData() != null);
      assertTrue(event.getProcessDefinitionId().equals(pi.getProcessDefinitionId()));
      assertTrue(event.getProcessInstanceId().equals(pi.getId()));
      assertTrue(event.getType().equals(ActivitiEventType.ACTIVITY_MESSAGE_SENT));
      assertTrue(event.getExecutionId() != null);      
      
    }
    
    @Deployment
    public void testIntermediateThrowMessageEventFieldExtensions() throws Exception {
      delegateExecuted = false;
      ProcessInstance pi = runtimeService.createProcessInstanceBuilder()
                                         .processDefinitionKey("process")
                                         .variable("foo", "bar")
                                         .businessKey("customerId")
                                         .start();
      
      assertProcessEnded(pi.getProcessInstanceId());
      assertTrue(message.getName().equals("bpmnMessage"));
      assertTrue(delegateExecuted);

      assertTrue(receivedEvents.size() > 0);
      
      ActivitiMessageEvent event = (ActivitiMessageEvent) receivedEvents.get(0);
      
      assertTrue(event.getActivityId().equals("messageThrow"));
      assertTrue(event.getActivityType().equals("throwEvent"));
      assertTrue(event.getActivityName().equals("Throw Message"));
      assertTrue(event.getBehaviorClass().equals(IntermediateThrowMessageEventActivityBehavior.class.getName()));
      assertTrue(event.getMessageName().equals("bpmnMessage"));
      assertThat(event.getMessageData()).as("should map payload from field extensions")
                                        .isInstanceOf(Map.class)
                                        .extracting("foo", "businessKey", "key", "bar")
                                        .containsExactly("bar", "customerId", "value", null);
      assertTrue(event.getProcessDefinitionId().equals(pi.getProcessDefinitionId()));
      assertTrue(event.getProcessInstanceId().equals(pi.getId()));
      assertTrue(event.getType().equals(ActivitiEventType.ACTIVITY_MESSAGE_SENT));
      assertTrue(event.getExecutionId() != null);
    }
    
    @Deployment
    public void testIntermediateThrowMessageEventDelegateExpression() throws Exception {
      delegateExecuted = false;
      ProcessInstance pi = runtimeService.createProcessInstanceBuilder()
                                         .processDefinitionKey("process")
                                         .variable("foo", "bar")
                                         .businessKey("customerId")
                                         .start();
      
      assertProcessEnded(pi.getProcessInstanceId());
      assertThat(delegateExecuted).as("should execute delegate expression")
                                  .isTrue();
      
      assertThat(message).isNotNull();
      assertThat(message.getName()).isEqualTo("bpmnMessage");
    }    

    @Deployment
    public void testThrowMessageEndEventDelegateExpression() throws Exception {
      // given
      delegateExecuted = false;
      message = null;
      
      // when
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");
      
      // then
      assertProcessEnded(pi.getProcessInstanceId());

      assertThat(delegateExecuted).as("should execute delegate expression")
                                  .isTrue();

      assertThat(message).isNotNull();
      assertThat(message.getName()).isEqualTo("endMessage");      
      
    }
   
}
