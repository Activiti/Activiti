/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.junit.Test;

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
        listenerExecuted = false;
        delegateExecuted = false;
        message = null;

        receivedEvents.clear();

        runtimeService.addEventListener(myListener,
                                        ActivitiEventType.ACTIVITY_MESSAGE_SENT);
    }

    @After
    public void tearDown() {
        runtimeService.removeEventListener(myListener);
    }

    @Test
    public void testMyThrowMessageDelegateFactory() {
        assertThat(StandaloneProcessEngineConfiguration.class.cast(processEngine.getProcessEngineConfiguration())
                                                             .getActivityBehaviorFactory())
                                                             .as("should provide custom throw message delegate factory")
                                                             .extracting("throwMessageDelegateFactory")
                                                             .isInstanceOf(MyThrowMessageDelegateFactory.class);
    }

    @Deployment
    public void testIntermediateThrowMessageEvent() throws Exception {
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("testIntermediateThrowMessageEvent");
      assertProcessEnded(pi.getProcessInstanceId());
      assertThat(listenerExecuted).isTrue();

      assertThat(receivedEvents).hasSize(1);

      ActivitiMessageEvent event = (ActivitiMessageEvent) receivedEvents.get(0);

      assertThat(event.getActivityId()).isEqualTo("messageThrow");
      assertThat(event.getActivityType()).isEqualTo("throwEvent");
      assertThat(event.getActivityName()).isEqualTo("Throw Message");
      assertThat(event.getBehaviorClass()).isEqualTo(IntermediateThrowMessageEventActivityBehavior.class.getName());
      assertThat(event.getMessageName()).isEqualTo("bpmnMessage");
      assertThat(event.getMessageData()).isNull();
      assertThat(event.getProcessDefinitionId()).isEqualTo(pi.getProcessDefinitionId());
      assertThat(event.getProcessInstanceId()).isEqualTo(pi.getId());
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_MESSAGE_SENT);
      assertThat(event.getExecutionId()).isNotNull();
    }

    @Deployment
    public void testIntermediateThrowMessageEventJavaDelegate() throws Exception {
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("testIntermediateThrowMessageEventJavaDelegate");
      assertProcessEnded(pi.getProcessInstanceId());
      assertThat(message.getName()).isEqualTo("bpmnMessage");
      assertThat(delegateExecuted).isTrue();

      assertThat(receivedEvents).hasSize(1);

      ActivitiMessageEvent event = (ActivitiMessageEvent) receivedEvents.get(0);

      assertThat(event.getActivityId()).isEqualTo("messageThrow");
      assertThat(event.getActivityType()).isEqualTo("throwEvent");
      assertThat(event.getActivityName()).isEqualTo("Throw Message");
      assertThat(event.getBehaviorClass()).isEqualTo(IntermediateThrowMessageEventActivityBehavior.class.getName());
      assertThat(event.getMessageName()).isEqualTo("bpmnMessage");
      assertThat(event.getMessageData()).isNotNull();
      assertThat(event.getProcessDefinitionId()).isEqualTo(pi.getProcessDefinitionId());
      assertThat(event.getProcessInstanceId()).isEqualTo(pi.getId());
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_MESSAGE_SENT);
      assertThat(event.getExecutionId()).isNotNull();
    }

    @Deployment
    public void testThrowMessageEndEvent() throws Exception {
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("testThrowMessageEndEvent");
      assertProcessEnded(pi.getProcessInstanceId());
      assertThat(listenerExecuted).isTrue();
      assertThat(receivedEvents).hasSize(1);

      ActivitiMessageEvent event = (ActivitiMessageEvent) receivedEvents.get(0);

      assertThat(event.getActivityId()).isEqualTo("theEnd");
      assertThat(event.getActivityType()).isEqualTo("endEvent");
      assertThat(event.getActivityName()).isEqualTo("Throw Message");
      assertThat(event.getBehaviorClass()).isEqualTo(ThrowMessageEndEventActivityBehavior.class.getName());
      assertThat(event.getMessageName()).isEqualTo("endMessage");
      assertThat(event.getMessageData()).isNull();
      assertThat(event.getProcessDefinitionId()).isEqualTo(pi.getProcessDefinitionId());
      assertThat(event.getProcessInstanceId()).isEqualTo(pi.getId());
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_MESSAGE_SENT);
      assertThat(event.getExecutionId()).isNotNull();
    }

    @Deployment
    public void testThrowMessageEndEventJavaDelegate() throws Exception {
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("testThrowMessageEndEventJavaDelegate");
      assertProcessEnded(pi.getProcessInstanceId());
      assertThat(message.getName()).isEqualTo("endMessage");
      assertThat(delegateExecuted).isTrue();
      assertThat(receivedEvents).hasSize(1);

      ActivitiMessageEvent event = (ActivitiMessageEvent) receivedEvents.get(0);

      assertThat(event.getActivityId()).isEqualTo("theEnd");
      assertThat(event.getActivityType()).isEqualTo("endEvent");
      assertThat(event.getActivityName()).isEqualTo("Throw Message");
      assertThat(event.getBehaviorClass()).isEqualTo(ThrowMessageEndEventActivityBehavior.class.getName());
      assertThat(event.getMessageName()).isEqualTo("endMessage");
      assertThat(event.getMessageData()).isNotNull();
      assertThat(event.getProcessDefinitionId()).isEqualTo(pi.getProcessDefinitionId());
      assertThat(event.getProcessInstanceId()).isEqualTo(pi.getId());
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_MESSAGE_SENT);
      assertThat(event.getExecutionId()).isNotNull();

    }

    @Deployment
    public void testIntermediateThrowMessageEventExpression() throws Exception {
      ProcessInstance pi = runtimeService.createProcessInstanceBuilder()
                                         .processDefinitionKey("testIntermediateThrowMessageEventExpression")
                                         .businessKey("foo")
                                         .start();

      assertProcessEnded(pi.getProcessInstanceId());
      assertThat(message.getName()).isEqualTo("bpmnMessage-foo");
      assertThat(delegateExecuted).isTrue();
      assertThat(receivedEvents).hasSize(1);

      ActivitiMessageEvent event = (ActivitiMessageEvent) receivedEvents.get(0);

      assertThat(event.getActivityId()).isEqualTo("messageThrow");
      assertThat(event.getActivityType()).isEqualTo("throwEvent");
      assertThat(event.getActivityName()).isEqualTo("Throw Message");
      assertThat(event.getBehaviorClass()).isEqualTo(IntermediateThrowMessageEventActivityBehavior.class.getName());
      assertThat(event.getMessageName()).isEqualTo("bpmnMessage-foo");
      assertThat(event.getMessageData()).isNotNull();
      assertThat(event.getMessageBusinessKey()).isEqualTo("foo");
      assertThat(event.getProcessDefinitionId()).isEqualTo(pi.getProcessDefinitionId());
      assertThat(event.getProcessInstanceId()).isEqualTo(pi.getId());
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_MESSAGE_SENT);
      assertThat(event.getExecutionId()).isNotNull();
    }

    @Deployment
    public void testThrowMessageEndEventExpression() throws Exception {
      ProcessInstance pi = runtimeService.createProcessInstanceBuilder()
                                         .processDefinitionKey("testThrowMessageEndEventExpression")
                                         .businessKey("bar")
                                         .start();

      assertProcessEnded(pi.getProcessInstanceId());
      assertThat(message.getName()).isEqualTo("endMessage-bar");
      assertThat(delegateExecuted);

      assertThat(receivedEvents.size() > 0);

      ActivitiMessageEvent event = (ActivitiMessageEvent) receivedEvents.get(0);

      assertThat(event.getActivityId()).isEqualTo("theEnd");
      assertThat(event.getActivityType()).isEqualTo("endEvent");
      assertThat(event.getActivityName()).isEqualTo("Throw Message");
      assertThat(event.getBehaviorClass()).isEqualTo(ThrowMessageEndEventActivityBehavior.class.getName());
      assertThat(event.getMessageName()).isEqualTo("endMessage-bar");
      assertThat(event.getMessageData()).isNotNull();
      assertThat(event.getMessageBusinessKey()).isEqualTo("bar");
      assertThat(event.getProcessDefinitionId()).isEqualTo(pi.getProcessDefinitionId());
      assertThat(event.getProcessInstanceId()).isEqualTo(pi.getId());
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_MESSAGE_SENT);
      assertThat(event.getExecutionId()).isNotNull();

    }

    @Deployment
    public void testIntermediateThrowMessageEventFieldExtensions() throws Exception {
      ProcessInstance pi = runtimeService.createProcessInstanceBuilder()
                                         .processDefinitionKey("process")
                                         .variable("foo", "bar")
                                         .businessKey("customerId")
                                         .start();

      assertProcessEnded(pi.getProcessInstanceId());
      assertThat(message.getName()).isEqualTo("bpmnMessage");
      assertThat(delegateExecuted);

      assertThat(receivedEvents.size() > 0);

      ActivitiMessageEvent event = (ActivitiMessageEvent) receivedEvents.get(0);

      assertThat(event.getActivityId()).isEqualTo("messageThrow");
      assertThat(event.getActivityType()).isEqualTo("throwEvent");
      assertThat(event.getActivityName()).isEqualTo("Throw Message");
      assertThat(event.getBehaviorClass()).isEqualTo(IntermediateThrowMessageEventActivityBehavior.class.getName());
      assertThat(event.getMessageName()).isEqualTo("bpmnMessage");
      assertThat(event.getMessageBusinessKey()).isEqualTo("customerId");
      assertThat(event.getMessageData()).as("should map payload from field extensions")
                                        .isInstanceOf(Map.class)
                                        .extracting("foo", "businessKey", "key", "bar")
                                        .containsExactly("bar", "customerId", "value", null);
      assertThat(event.getProcessDefinitionId()).isEqualTo(pi.getProcessDefinitionId());
      assertThat(event.getProcessInstanceId()).isEqualTo(pi.getId());
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_MESSAGE_SENT);
      assertThat(event.getExecutionId()).isNotNull();
    }

    @Deployment
    public void testIntermediateThrowMessageEventDelegateExpression() throws Exception {
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

      // when
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

      // then
      assertProcessEnded(pi.getProcessInstanceId());

      assertThat(delegateExecuted).as("should execute delegate expression")
                                  .isTrue();

      assertThat(message).isNotNull();
      assertThat(message.getName()).isEqualTo("endMessage");

    }

    @Deployment
    public void testIntermediateThrowMessageEventCorrelationKeyExpression() throws Exception {
      ProcessInstance pi = runtimeService.createProcessInstanceBuilder()
                                         .variable("foo", "bar")
                                         .processDefinitionKey("process")
                                         .start();

      assertProcessEnded(pi.getProcessInstanceId());
      assertThat(listenerExecuted).isTrue();

      assertThat(message.getCorrelationKey().isPresent()).isTrue();
      assertThat(message.getCorrelationKey().get()).isEqualTo("bar");

      assertThat(receivedEvents).hasSize(1);

      ActivitiMessageEvent event = (ActivitiMessageEvent) receivedEvents.get(0);

      assertThat(event.getActivityId()).isEqualTo("messageThrow");
      assertThat(event.getActivityType()).isEqualTo("throwEvent");
      assertThat(event.getActivityName()).isEqualTo("Throw Message");
      assertThat(event.getBehaviorClass()).isEqualTo(IntermediateThrowMessageEventActivityBehavior.class.getName());
      assertThat(event.getMessageName()).isEqualTo("bpmnMessage");
      assertThat(event.getMessageCorrelationKey()).isEqualTo("bar");
      assertThat(event.getMessageData()).isNull();
      assertThat(event.getMessageBusinessKey()).isNull();
      assertThat(event.getProcessDefinitionId()).isEqualTo(pi.getProcessDefinitionId());
      assertThat(event.getProcessInstanceId()).isEqualTo(pi.getId());
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_MESSAGE_SENT);
      assertThat(event.getExecutionId()).isNotNull();
    }

    @Deployment
    public void testThrowMessageEndEventCorrelationKeyExpression() throws Exception {
      ProcessInstance pi = runtimeService.createProcessInstanceBuilder()
                                         .variable("foo", "bar")
                                         .processDefinitionKey("process")
                                         .start();

      assertProcessEnded(pi.getProcessInstanceId());
      assertThat(listenerExecuted).isTrue();

      assertThat(message.getCorrelationKey().isPresent()).isTrue();
      assertThat(message.getCorrelationKey().get()).isEqualTo("bar");

      assertThat(receivedEvents).hasSize(1);

      ActivitiMessageEvent event = (ActivitiMessageEvent) receivedEvents.get(0);

      assertThat(event.getActivityId()).isEqualTo("theEnd");
      assertThat(event.getActivityType()).isEqualTo("endEvent");
      assertThat(event.getActivityName()).isEqualTo("Throw Message");
      assertThat(event.getBehaviorClass()).isEqualTo(ThrowMessageEndEventActivityBehavior.class.getName());
      assertThat(event.getMessageName()).isEqualTo("endMessage");
      assertThat(event.getMessageData()).isNull();
      assertThat(event.getMessageCorrelationKey()).isEqualTo("bar");
      assertThat(event.getProcessDefinitionId()).isEqualTo(pi.getProcessDefinitionId());
      assertThat(event.getProcessInstanceId()).isEqualTo(pi.getId());
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_MESSAGE_SENT);
      assertThat(event.getExecutionId()).isNotNull();
    }
}
