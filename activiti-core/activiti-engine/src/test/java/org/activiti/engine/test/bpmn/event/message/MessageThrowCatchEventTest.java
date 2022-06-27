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
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiMessageEvent;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.TransactionListener;
import org.activiti.engine.impl.cfg.TransactionState;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ThrowMessage;
import org.activiti.engine.impl.delegate.ThrowMessageDelegate;
import org.activiti.engine.impl.delegate.ThrowMessageDelegateFactory;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class MessageThrowCatchEventTest extends ResourceActivitiTestCase {

    private static final String THROW_MESSAGE2 = "throwMessage";
    private static final String END_MESSAGE = "endMessage";
    private static final String START_MESSAGE = "startMessage";
    private static final String CATCH_MESSAGE = "catchMessage";
    private static final String THROW_MESSAGE = THROW_MESSAGE2;
    private static final String TEST_MESSAGE = "testMessage";
    private static List<ActivitiEvent> receivedEvents = new LinkedList<>();
    private static Map<SubscriptionKey, BlockingQueue<ThrowMessage>> messageQueueRegistry = new ConcurrentHashMap<>();
    private static Map<SubscriptionKey, Optional<String>> messageExecutionRegistry = new ConcurrentHashMap<>();
    private static CountDownLatch startCountDownLatch;
    private static AtomicReference<CountDownLatch> waitingCountDownLatchRef = new AtomicReference<>();

    private ActivitiEventListener catchMessageListener = new CatchMessageListener(waitingCountDownLatchRef);

    public MessageThrowCatchEventTest() {
        super("/org/activiti/engine/test/bpmn/event/message/MessageThrowCatchEventTest.activiti.cfg.xml");
    }

    public static class TestThrowMessageDelegateFactory implements ThrowMessageDelegateFactory {

        @Override
        public ThrowMessageDelegate create() {
            return new TestThrowMessageDelegate();
        }
    }

    public static class TestThrowMessageDelegate implements ThrowMessageDelegate {

        @Override
        public boolean send(DelegateExecution execution, ThrowMessage message) {

            Context.getTransactionContext()
                   .addTransactionListener(TransactionState.COMMITTED,
                                           new ThrowMessageTransactionListener(message));

            return true;
        }

        class ThrowMessageTransactionListener implements TransactionListener {
            private final ThrowMessage message;

            public ThrowMessageTransactionListener(ThrowMessage message) {
                this.message = message;
            }

            @Override
            public void execute(CommandContext commandContext) {
                SubscriptionKey key = new SubscriptionKey(message.getName(),
                                                          message.getCorrelationKey());

                Queue<ThrowMessage> queue = getThrowMessageQueue(key);

                queue.offer(message);
            }
        }
    }

    static class CatchMessageListener implements ActivitiEventListener {

        private final  AtomicReference<CountDownLatch> waitingCountDownLatchRef;

        public CatchMessageListener( AtomicReference<CountDownLatch> waitingCountDownLatchRef) {
            this.waitingCountDownLatchRef = waitingCountDownLatchRef;
        }

        @Override
        public void onEvent(ActivitiEvent event) {
            ActivitiMessageEvent message = ActivitiMessageEvent.class.cast(event);

            SubscriptionKey key = new SubscriptionKey(message.getMessageName(),
                                                      Optional.ofNullable(message.getMessageCorrelationKey()));

            String executionId = message.getExecutionId();

            BlockingQueue<ThrowMessage> messageQueue = registerSubscription(key,
                                                                            Optional.of(executionId));

            Context.getTransactionContext()
                   .addTransactionListener(TransactionState.COMMITTED,
                                           new HandleMessageTransactionListener(key,
                                                                                executionId,
                                                                                messageQueue));
        }

        @Override
        public boolean isFailOnException() {
            // TODO Auto-generated method stub
            return false;
        }

        class HandleMessageTransactionListener implements TransactionListener {

            private final String executionId;
            private final BlockingQueue<ThrowMessage> messageQueue;
            private final SubscriptionKey key;

            public HandleMessageTransactionListener(SubscriptionKey key,
                                                    String executionId,
                                                    BlockingQueue<ThrowMessage> messageQueue) {
                this.key = key;
                this.executionId = executionId;
                this.messageQueue = messageQueue;
            }

            @Override
            public void execute(CommandContext commandContext) {
                RuntimeService runtimeService = commandContext.getProcessEngineConfiguration()
                                                              .getRuntimeService();
                // TOOD: use reactor
                new Thread(() -> {
                    try {
                        ThrowMessage message = messageQueue.take();

                        Map<String, Object> payload = message.getPayload()
                                                             .orElse(null);

                        runtimeService.messageEventReceived(message.getName(),
                                                            executionId,
                                                            payload);

                        waitingCountDownLatchRef.get()
                                                .countDown();

                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    } finally {
                        removeSubscription(key);
                    }
                }).start();
             }
         }
    }

    private ActivitiEventListener spyMessageListener = new ActivitiEventListener() {

        @Override
        public void onEvent(ActivitiEvent event) {
            receivedEvents.add(event);
        }

        @Override
        public boolean isFailOnException() {
            return false;
        }
    };

    @Before
    public void setUp() throws Exception {
        super.setUp();

        receivedEvents.clear();
        messageQueueRegistry.clear();
        messageExecutionRegistry.clear();

        startCountDownLatch = new CountDownLatch(1);
        waitingCountDownLatchRef.set(new CountDownLatch(1));

        runtimeService.addEventListener(spyMessageListener,
                                        ActivitiEventType.ACTIVITY_MESSAGE_SENT,
                                        ActivitiEventType.ACTIVITY_MESSAGE_WAITING,
                                        ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);

        // Initialize hook for catch message subscriptions
        runtimeService.addEventListener(catchMessageListener,
                                        ActivitiEventType.ACTIVITY_MESSAGE_WAITING);

        // Initialize existing message event subscriptions, i.e. start and catch messages
        initExistingMessageSubscriptions();

    }

    @After
    public void tearDown() {
        runtimeService.removeEventListener(spyMessageListener);
        runtimeService.removeEventListener(catchMessageListener);
    }

    @Test
    public void testMyThrowMessageDelegateFactory() {
        assertThat(StandaloneProcessEngineConfiguration.class.cast(processEngine.getProcessEngineConfiguration())
                                                             .getActivityBehaviorFactory())
                                                             .as("should provide custom throw message delegate factory")
                                                             .extracting("throwMessageDelegateFactory")
                                                             .isInstanceOf(TestThrowMessageDelegateFactory.class);
    }

    @Deployment(resources = {
        "org/activiti/engine/test/bpmn/event/message/MessageThrowCatchEventTest.throwMessage.bpmn20.xml",
        "org/activiti/engine/test/bpmn/event/message/MessageThrowCatchEventTest.catchMessage.bpmn20.xml"
    })
    public void testThrowCatchIntermediateMessageEvent() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new CountDownMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
        // when
        ProcessInstance throwMsg = runtimeService.startProcessInstanceByKey(THROW_MESSAGE);
        ProcessInstance catchMsg = runtimeService.startProcessInstanceByKey(CATCH_MESSAGE);

        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        assertProcessEnded(throwMsg.getProcessInstanceId());
        assertProcessEnded(catchMsg.getProcessInstanceId());

        HistoricProcessInstance startMsg = historyService.createHistoricProcessInstanceQuery()
                                                         .processDefinitionKey(CATCH_MESSAGE)
                                                         .includeProcessVariables()
                                                         .singleResult();

        assertThat(startMsg.getProcessVariables()).containsEntry("foo", "bar");

        assertThat(receivedEvents).hasSize(3)
                                  .extracting("type",
                                              "messageName",
                                              "correlationKey")
                                  .contains(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, TEST_MESSAGE, null),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, TEST_MESSAGE, null),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, TEST_MESSAGE, null));

    }

    @Deployment(resources = {
            "org/activiti/engine/test/bpmn/event/message/MessageThrowCatchEventTest.throwMessage.bpmn20.xml",
            "org/activiti/engine/test/bpmn/event/message/MessageThrowCatchEventTest.startMessage.bpmn20.xml"
    })
    public void testThrowCatchStartMessageEvent() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new CountDownMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);

        assertThat(messageExecutionRegistry).containsExactly(entry(new SubscriptionKey(TEST_MESSAGE, Optional.empty()),
                                                                  Optional.empty()));
        // when
        ProcessInstance throwMsg = runtimeService.createProcessInstanceBuilder()
                                                 .businessKey("foobar")
                                                 .processDefinitionKey(THROW_MESSAGE)
                                                 .start();

        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        HistoricProcessInstance startMsg = historyService.createHistoricProcessInstanceQuery()
                                                         .processDefinitionKey(START_MESSAGE)
                                                         .includeProcessVariables()
                                                         .singleResult();

        assertThat(startMsg.getBusinessKey()).isEqualTo("foobar");
        assertThat(startMsg.getProcessVariables()).containsEntry("foo", "bar");

        assertProcessEnded(throwMsg.getId());
        assertProcessEnded(startMsg.getId());

        assertThat(receivedEvents).hasSize(2)
                                  .extracting("type",
                                              "messageName",
                                              "correlationKey")
                                  .contains(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, TEST_MESSAGE, null),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, TEST_MESSAGE, null));
    }

    @Deployment(resources = {
            "org/activiti/engine/test/bpmn/event/message/MessageThrowCatchEventTest.endMessage.bpmn20.xml",
            "org/activiti/engine/test/bpmn/event/message/MessageThrowCatchEventTest.startMessage.bpmn20.xml"
    })
    public void testThrowCatchEndMessageEvent() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new CountDownMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
        // when
        ProcessInstance throwMsg = runtimeService.createProcessInstanceBuilder()
                                                 .businessKey("foobar")
                                                 .processDefinitionKey(END_MESSAGE)
                                                 .start();

        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        HistoricProcessInstance startMsg = historyService.createHistoricProcessInstanceQuery()
                                                         .processDefinitionKey(START_MESSAGE)
                                                         .includeProcessVariables()
                                                         .singleResult();

        assertThat(startMsg.getBusinessKey()).isEqualTo("foobar");
        assertThat(startMsg.getProcessVariables()).containsEntry("foo", "bar");

        assertProcessEnded(throwMsg.getId());
        assertProcessEnded(startMsg.getId());

        assertThat(receivedEvents).hasSize(2)
                                  .extracting("type",
                                              "messageName",
                                              "correlationKey")
                                  .contains(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, TEST_MESSAGE, null),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, TEST_MESSAGE, null));
    }

    @Deployment
    public void testIntermediateThrowCatchMessage() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new CountDownMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
        // when
        ProcessInstance throwMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(THROW_MESSAGE2)
                                                     .start();

        ProcessInstance catchMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(CATCH_MESSAGE)
                                                     .start();
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        assertProcessEnded(throwMessage.getId());
        assertProcessEnded(catchMessage.getId());

        assertThat(receivedEvents).hasSize(3)
                                  .extracting("type",
                                              "messageName",
                                              "correlationKey")
                                  .contains(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, TEST_MESSAGE, null),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, TEST_MESSAGE, null),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, TEST_MESSAGE, null));
    }

    @Deployment
    public void testIntermediateThrowCatchMessageCorrelationKey() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new CountDownMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
        // when
        ProcessInstance throwMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(THROW_MESSAGE2)
                                                     .variable("correlationId", 1)
                                                     .start();

        ProcessInstance catchMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(CATCH_MESSAGE)
                                                     .variable("correlationId", 1)
                                                     .start();
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        assertProcessEnded(throwMessage.getId());
        assertProcessEnded(catchMessage.getId());

        assertThat(receivedEvents).hasSize(3)
                                  .extracting("type",
                                              "messageName",
                                              "correlationKey")
                                  .contains(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, TEST_MESSAGE, "1"),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, TEST_MESSAGE, "1"),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, TEST_MESSAGE, "1"));
    }

    @Deployment(resources = "org/activiti/engine/test/bpmn/event/message/MessageThrowCatchEventTest.testIntermediateThrowCatchMessageCorrelationKey.bpmn20.xml")
    public void testIntermediateThrowCatchMessageNonMatchingCorrelationKey() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new CountDownMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
        // when
        ProcessInstance throwMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(THROW_MESSAGE2)
                                                     .variable("correlationId", 1)
                                                     .start();

        ProcessInstance catchMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(CATCH_MESSAGE)
                                                     .variable("correlationId", 2)
                                                     .start();
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isFalse();

        assertProcessEnded(throwMessage.getId());
        assertThat(processEngine.getRuntimeService()
                                .createProcessInstanceQuery()
                                .processInstanceId(catchMessage.getId())
                                .singleResult()).isNotNull();

        assertThat(receivedEvents).hasSize(2)
                                  .extracting("type",
                                              "messageName",
                                              "correlationKey")
                                  .containsExactly(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, TEST_MESSAGE, "1"),
                                                   tuple(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, TEST_MESSAGE, "2"));
    }


    @Deployment
    public void testEndThrowStartMessage() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new CountDownMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
        // when
        ProcessInstance throwMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(THROW_MESSAGE2)
                                                     .businessKey("businessKey")
                                                     .start();
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        assertProcessEnded(throwMessage.getId());
        assertThat(processEngine.getRuntimeService()
                                .createProcessInstanceQuery()
                                .processDefinitionKey(CATCH_MESSAGE)
                                .singleResult())
                                .isNotNull();

        assertThat(receivedEvents).hasSize(2)
                                  .extracting("type",
                                              "messageName",
                                              "businessKey",
                                              "correlationKey")
                                  .contains(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, TEST_MESSAGE, "businessKey", null),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, TEST_MESSAGE, "businessKey", null));
    }

    @Deployment
    public void testIntermediateThrowStartMessage() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new CountDownMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
        // when
        ProcessInstance throwMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(THROW_MESSAGE2)
                                                     .businessKey("businessKey")
                                                     .start();
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        assertProcessEnded(throwMessage.getId());
        assertThat(processEngine.getRuntimeService()
                                .createProcessInstanceQuery()
                                .processDefinitionKey(CATCH_MESSAGE)
                                .singleResult())
                                .isNotNull();

        assertThat(receivedEvents).hasSize(2)
                                  .extracting("type",
                                              "messageName",
                                              "businessKey",
                                              "correlationKey")
                                  .contains(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, TEST_MESSAGE, "businessKey", null),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, TEST_MESSAGE, "businessKey", null));
    }

    @Deployment
    public void testIntermediateThrowCatchMessageEventGateway() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new CountDownMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
        // when
        ProcessInstance throwMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(THROW_MESSAGE2)
                                                     .start();

        ProcessInstance catchMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(CATCH_MESSAGE)
                                                     .start();
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        assertProcessEnded(throwMessage.getId());
        assertProcessEnded(catchMessage.getId());

        assertThat(receivedEvents).hasSize(3)
                                  .extracting("type",
                                              "messageName",
                                              "correlationKey")
                                  .contains(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, TEST_MESSAGE, null),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, TEST_MESSAGE, null),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, TEST_MESSAGE, null));
    }

    @Deployment(resources = "org/activiti/engine/test/bpmn/event/message/MessageThrowCatchEventTest.testIntermediateThrowCatchMessageEventGatewayCorrelationKey.bpmn20.xml")
    public void testIntermediateThrowCatchMessageEventGatewayNonMatchingCorrelationKey() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new CountDownMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
        // when
        // when
        ProcessInstance throwMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(THROW_MESSAGE2)
                                                     .variable("correlationId", 1)
                                                     .start();

        ProcessInstance catchMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(CATCH_MESSAGE)
                                                     .variable("correlationId", 2)
                                                     .start();
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isFalse();

        assertProcessEnded(throwMessage.getId());
        assertThat(processEngine.getRuntimeService()
                                .createProcessInstanceQuery()
                                .processInstanceId(catchMessage.getId())
                                .singleResult())
                                .isNotNull();

        assertThat(receivedEvents).hasSize(2)
                                  .extracting("type",
                                              "messageName",
                                              "correlationKey")
                                  .contains(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, TEST_MESSAGE, "1"),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, TEST_MESSAGE, "2"));
    }

    @Deployment
    public void testIntermediateThrowCatchMessageEventGatewayCorrelationKey() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new CountDownMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
        // when
        ProcessInstance throwMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(THROW_MESSAGE2)
                                                     .variable("correlationId", 1)
                                                     .start();

        ProcessInstance catchMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(CATCH_MESSAGE)
                                                     .variable("correlationId", 1)
                                                     .start();
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        assertProcessEnded(throwMessage.getId());
        assertProcessEnded(catchMessage.getId());

        assertThat(receivedEvents).hasSize(3)
                                  .extracting("type",
                                              "messageName",
                                              "correlationKey")
                                  .contains(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, TEST_MESSAGE, "1"),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, TEST_MESSAGE, "1"),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, TEST_MESSAGE, "1"));
    }


    @Deployment
    public void testIntermediateThrowCatchMessageBoundary() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new CountDownMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
        // when
        ProcessInstance throwMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(THROW_MESSAGE2)
                                                     .start();

        ProcessInstance catchMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(CATCH_MESSAGE)
                                                     .start();
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        assertProcessEnded(throwMessage.getId());
        assertProcessEnded(catchMessage.getId());

        assertThat(receivedEvents).hasSize(3)
                                  .extracting("type",
                                              "messageName",
                                              "correlationKey")
                                  .contains(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, TEST_MESSAGE, null),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, TEST_MESSAGE, null),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, TEST_MESSAGE, null));
    }

    @Deployment
    public void testIntermediateThrowCatchMessageBoundaryCorrelationKey() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new CountDownMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
        // when
        ProcessInstance throwMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(THROW_MESSAGE2)
                                                     .variable("correlationId", 1)
                                                     .start();

        ProcessInstance catchMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(CATCH_MESSAGE)
                                                     .variable("correlationId", 1)
                                                     .start();
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        assertProcessEnded(throwMessage.getId());
        assertProcessEnded(catchMessage.getId());

        assertThat(receivedEvents).hasSize(3)
                                  .extracting("type",
                                              "messageName",
                                              "correlationKey")
                                  .contains(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, TEST_MESSAGE, "1"),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, TEST_MESSAGE, "1"),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, TEST_MESSAGE, "1"));
    }

    @Deployment
    public void testIntermediateThrowCatchMessageBoundarySubprocess() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new CountDownMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
        // when
        ProcessInstance throwMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(THROW_MESSAGE2)
                                                     .start();

        ProcessInstance catchMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(CATCH_MESSAGE)
                                                     .start();
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        assertProcessEnded(throwMessage.getId());
        assertProcessEnded(catchMessage.getId());

        assertThat(receivedEvents).hasSize(3)
                                  .extracting("type",
                                              "messageName",
                                              "correlationKey")
                                  .contains(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, TEST_MESSAGE, null),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, TEST_MESSAGE, null),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, TEST_MESSAGE, null));
    }

    @Deployment
    public void testIntermediateThrowCatchMessageBoundarySubprocessCorrelationKey() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new CountDownMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
        // when
        ProcessInstance throwMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(THROW_MESSAGE2)
                                                     .variable("correlationId", 1)
                                                     .start();

        ProcessInstance catchMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(CATCH_MESSAGE)
                                                     .variable("correlationId", 1)
                                                     .start();
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        assertProcessEnded(throwMessage.getId());
        assertProcessEnded(catchMessage.getId());

        assertThat(receivedEvents).hasSize(3)
                                  .extracting("type",
                                              "messageName",
                                              "correlationKey")
                                  .contains(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, TEST_MESSAGE, "1"),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, TEST_MESSAGE, "1"),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, TEST_MESSAGE, "1"));
    }

    @Deployment
    public void testIntermediateThrowCatchMessageEventSubprocessCorrelationKey() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new CountDownMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
        // when
        ProcessInstance throwMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(THROW_MESSAGE2)
                                                     .businessKey("bk1")
                                                     .variable("invoiceId", 1)
                                                     .variable("correlationId", 1)
                                                     .start();

        ProcessInstance catchMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(CATCH_MESSAGE)
                                                     .variable("invoiceId", 1)
                                                     .businessKey("bk2")
                                                     .variable("correlationId", 1)
                                                     .start();
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        assertProcessEnded(throwMessage.getId());
        assertProcessEnded(catchMessage.getId());

        assertThat(receivedEvents).hasSize(3)
                                  .extracting("type",
                                              "messageName",
                                              "businessKey",
                                              "correlationKey")
                                  .contains(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, "testMessage-1", "bk1", "1"),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, "testMessage-1", "bk2", "1"),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, "testMessage-1", "bk2", "1"));
    }

    @Deployment(resources = "org/activiti/engine/test/bpmn/event/message/MessageThrowCatchEventTest.testIntermediateThrowCatchMessageBoundarySubprocessCorrelationKey.bpmn20.xml")
    public void testIntermediateThrowCatchMessageBoundarySubprocessNonMatchingCorrelationKey() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new CountDownMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
        // when
        ProcessInstance throwMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(THROW_MESSAGE2)
                                                     .variable("correlationId", 1)
                                                     .start();

        ProcessInstance catchMessage = runtimeService.createProcessInstanceBuilder()
                                                     .processDefinitionKey(CATCH_MESSAGE)
                                                     .variable("correlationId", 2)
                                                     .start();
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isFalse();

        assertProcessEnded(throwMessage.getId());
        assertThat(processEngine.getRuntimeService()
                                .createProcessInstanceQuery()
                                .processInstanceId(catchMessage.getId())
                                .singleResult()).isNotNull();

        assertThat(receivedEvents).hasSize(2)
                                  .extracting("type",
                                              "messageName",
                                              "correlationKey")
                                  .contains(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, TEST_MESSAGE, "1"),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, TEST_MESSAGE, "2"));
    }


    @Deployment(resources = {
        "org/activiti/engine/test/bpmn/event/message/MessageThrowCatchEventTest.throwMessageCorrelationKey.bpmn20.xml",
        "org/activiti/engine/test/bpmn/event/message/MessageThrowCatchEventTest.catchMessageCorrelationKey.bpmn20.xml"
    })
    public void testThrowCatchIntermediateMessageEventCorrelationKey() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new CountDownMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
        // when
        ProcessInstance throwMsg = runtimeService.createProcessInstanceBuilder()
                                                 .processDefinitionKey(THROW_MESSAGE)
                                                 .businessKey("businessKey1")
                                                 .variable("customerId", "2")
                                                 .variable("invoiceId", "1")
                                                 .start();

        ProcessInstance catchMsg = runtimeService.createProcessInstanceBuilder()
                                                 .processDefinitionKey(CATCH_MESSAGE)
                                                 .businessKey("businessKey2")
                                                 .variable("customerId", "2")
                                                 .variable("invoiceId", "1")
                                                 .start();

        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        assertProcessEnded(throwMsg.getProcessInstanceId());
        assertProcessEnded(catchMsg.getProcessInstanceId());

        HistoricProcessInstance startMsg = historyService.createHistoricProcessInstanceQuery()
                                                         .processDefinitionKey(CATCH_MESSAGE)
                                                         .includeProcessVariables()
                                                         .singleResult();

        assertThat(startMsg.getProcessVariables()).containsEntry("foo", "bar");

        assertThat(receivedEvents).hasSize(3)
                                  .extracting("type",
                                              "messageName",
                                              "correlationKey",
                                              "businessKey")
                                  .contains(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, "newInvoice-1", "2", "businessKey1"),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, "newInvoice-1", "2", "businessKey2"),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, "newInvoice-1", "2", "businessKey2"));

    }

    @Deployment(resources = {
            "org/activiti/engine/test/bpmn/event/message/MessageThrowCatchEventTest.catchMessageCorrelationKey.bpmn20.xml"
        })
    public void testMessageEventSubscriptionQueryWithCorrelationKey() {

        // given
        runtimeService.createProcessInstanceBuilder()
                      .processDefinitionKey(CATCH_MESSAGE)
                      .businessKey("businessKey2")
                      .variable("customerId", "2")
                      .variable("invoiceId", "1")
                      .start();
        // when
        EventSubscriptionEntity subscription = newEventSubscriptionQuery().eventType("message")
                                                                          .eventName("newInvoice-1")
                                                                          .configuration("2")
                                                                          .singleResult();
        // then
        assertThat(subscription).isNotNull();
        assertThat(subscription).extracting("eventName",
                                            "configuration")
                                .containsExactly("newInvoice-1",
                                                 "2");
    }

    @Deployment(resources = {
            "org/activiti/engine/test/bpmn/event/message/MessageThrowCatchEventTest.catchMessageCorrelationKey.bpmn20.xml"
        })
    public void testMessageEventSubscriptionWithSameCorrelationKeyFails() {

        // given
        runtimeService.createProcessInstanceBuilder()
                      .processDefinitionKey(CATCH_MESSAGE)
                      .businessKey("businessKey2")
                      .variable("customerId", "2")
                      .variable("invoiceId", "1")
                      .start();

        // when
        Throwable exception = catchThrowable(() -> runtimeService.createProcessInstanceBuilder()
                                                                 .processDefinitionKey(CATCH_MESSAGE)
                                                                 .businessKey("businessKey2")
                                                                 .variable("customerId", "2")
                                                                 .variable("invoiceId", "1")
                                                                 .start());

        // then
        assertThat(exception).isInstanceOf(ActivitiIllegalArgumentException.class);
    }

    protected EventSubscriptionQueryImpl newEventSubscriptionQuery() {
        return new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutor());
    }

    protected static BlockingQueue<ThrowMessage> getThrowMessageQueue(SubscriptionKey key) {
        return messageQueueRegistry.computeIfAbsent(key,
                                                    MessageThrowCatchEventTest::createMessageQueue);
    }

    protected static BlockingQueue<ThrowMessage> registerSubscription(SubscriptionKey key, Optional<String> executionId) {
        messageExecutionRegistry.compute(key, (k, v) -> {

            if(messageExecutionRegistry.containsKey(k)) {
                throw new ActivitiException("Duplicate key " + k + " for executionId " + executionId);
            }

            return executionId;
        });

        return getThrowMessageQueue(key);
    }

    protected static void removeSubscription(SubscriptionKey key) {
        messageExecutionRegistry.remove(key);
    }
    protected static BlockingQueue<ThrowMessage> createMessageQueue(SubscriptionKey key) {
        return new LinkedBlockingQueue<>();
    }

    private void initExistingMessageSubscriptions() {
        // Initialize existing message event subscriptions, i.e. start and catch messages
        newEventSubscriptionQuery().eventType("message")
                                   .list()
                                   .stream()
                                   .forEach(subscription -> {
                                       Optional<String> correlationKey = Optional.of(subscription)
                                                                                 .filter(it -> it.getProcessInstanceId() != null)
                                                                                 .map(it -> it.getConfiguration()); // <- correlationKey

                                       SubscriptionKey key = new SubscriptionKey(subscription.getEventName(),
                                                                                 correlationKey);

                                       BlockingQueue<ThrowMessage> messageQueue = registerSubscription(key, correlationKey);

                                       // TODO: Use reactive
                                       new Thread(() -> {
                                           try {
                                               ThrowMessage throwMessage = messageQueue.take();

                                               String messageName = throwMessage.getName();

                                               Map<String, Object> payload = throwMessage.getPayload()
                                                                                         .orElse(null);

                                               String businessKey = throwMessage.getBusinessKey()
                                                                                .orElse(null);

                                               runtimeService.startProcessInstanceByMessage(messageName,
                                                                                            businessKey,
                                                                                            payload);
                                               startCountDownLatch.countDown();
                                           } catch (InterruptedException e) {
                                               log.error(e.getMessage(), e);
                                           }
                                       }).start();
                                   });
    }

    static class SubscriptionKey {
        private final String messageName;
        private final Optional<String> correlationKey;

        public SubscriptionKey(String messageName,
                               Optional<String> correlationKey) {
            this.messageName = messageName;
            this.correlationKey = correlationKey;
        }

        public String getMessageName() {
            return messageName;
        }

        public Optional<String> getCorrelationKey() {
            return correlationKey;
        }

        @Override
        public int hashCode() {
            return Objects.hash(correlationKey,
                                messageName);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SubscriptionKey other = (SubscriptionKey) obj;
            return Objects.equals(correlationKey, other.correlationKey) && Objects.equals(messageName,
                                                                                          other.messageName);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("SubscriptionKey [messageName=");
            builder.append(messageName);
            builder.append(", correlationKey=");
            builder.append(correlationKey);
            builder.append("]");
            return builder.toString();
        }

    }

    static class CountDownMessageListener implements ActivitiEventListener {

        private final CountDownLatch countDownLatch;

        public CountDownMessageListener(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void onEvent(ActivitiEvent event) {
            Context.getTransactionContext()
                   .addTransactionListener(TransactionState.COMMITTED,
                       new TransactionListener() {

                        @Override
                        public void execute(CommandContext commandContext) {
                            countDownLatch.countDown();
                        }
                    });

        }

        @Override
        public boolean isFailOnException() {
            // TODO Auto-generated method stub
            return false;
        }
    }
}
