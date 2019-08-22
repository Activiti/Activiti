/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.test.bpmn.event.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MessageThrowCatchEventTest extends ResourceActivitiTestCase {

    private static final String END_MESSAGE = "endMessage";
    private static final String START_MESSAGE = "startMessage";
    private static final String CATCH_MESSAGE = "catchMessage";
    private static final String THROW_MESSAGE = "throwMessage";
    private static final String TEST_MESSAGE = "testMessage";
    private static List<ActivitiEvent> receivedEvents = new LinkedList<>();
    private static Map<SubscriptionKey, BlockingQueue<ThrowMessage>> messageQueueRegistry = new ConcurrentHashMap<>();
    private static CountDownLatch startCountDownLatch;

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
                   .addTransactionListener(TransactionState.COMMITTED, new ThrowMessageTransactionListener(message));

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
                                                          message.getCorrelationKey().orElse(null));
                
                Queue<ThrowMessage> queue = getMessageQueue(key);
                queue.offer(message);
            }
        }
    }

    static class ThrowMessageListener implements ActivitiEventListener {

        private final CountDownLatch countDownLatch;

        public ThrowMessageListener(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void onEvent(ActivitiEvent event) {
            ActivitiMessageEvent message = ActivitiMessageEvent.class.cast(event);

            SubscriptionKey key = new SubscriptionKey(message.getMessageName(), 
                                                      message.getMessageCorrelationKey());
            
            BlockingQueue<ThrowMessage> messageQueue = getMessageQueue(key);
            Context.getTransactionContext()
                   .addTransactionListener(TransactionState.COMMITTED, 
                                           new HandleMessageTransactionListener(messageQueue));
        }

        @Override
        public boolean isFailOnException() {
            // TODO Auto-generated method stub
            return false;
        }
        
        class HandleMessageTransactionListener implements TransactionListener {
            
            private final BlockingQueue<ThrowMessage> messageQueue;
            
            
            public HandleMessageTransactionListener(BlockingQueue<ThrowMessage> messageQueue) {
                this.messageQueue = messageQueue;
            }


            @Override
            public void execute(CommandContext commandContext) {
                RuntimeService runtimeService = commandContext.getProcessEngineConfiguration()
                                                              .getRuntimeService();
                new Thread(() -> {
                    try {
                        ThrowMessage message = messageQueue.take();

                        runtimeService.createExecutionQuery()
                                      .messageEventSubscriptionName(message.getName())
                                      .list()
                                      .forEach(s -> {
                                          Map<String, Object> payload = message.getPayload()
                                                                               .orElse(null);

                                          runtimeService.messageEventReceived(message.getName(),
                                                                              s.getId(),
                                                                              payload);
                                          countDownLatch.countDown();
                                      });

                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }).start();
             }

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

    public MessageThrowCatchEventTest() {
        super("/org/activiti/engine/test/bpmn/event/message/MessageThrowCatchEventTest.activiti.cfg.xml");
    }

    @Before
    public void setUp() {
        receivedEvents.clear();
        
        startCountDownLatch = new CountDownLatch(1);
        
        runtimeService.addEventListener(myListener,
                                        ActivitiEventType.ACTIVITY_MESSAGE_SENT,
                                        ActivitiEventType.ACTIVITY_MESSAGE_WAITING,
                                        ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
        
        newEventSubscriptionQuery().eventType("message")
                                   .list()
                                   .forEach(subscription -> {
                                       SubscriptionKey key = new SubscriptionKey(subscription.getEventName(), null);
                                       
                                       BlockingQueue<ThrowMessage> messageQueue = getMessageQueue(key);
                                       new Thread(() -> {
                                           try {
                                               ThrowMessage throwMessage = messageQueue.take();
                                               Map<String, Object> payload = throwMessage.getPayload()
                                                                                         .orElse(null);

                                               String businessKey = throwMessage.getBusinessKey()
                                                                                .orElse(null);
                                               
                                               runtimeService.startProcessInstanceByMessage(throwMessage.getName(),
                                                                                            businessKey,
                                                                                            payload);
                                               startCountDownLatch.countDown();
                                           } catch (InterruptedException e) {
                                               // TODO Auto-generated catch block
                                               e.printStackTrace();
                                           }
                                       }).start();
                                   });
                                                                                            
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
                                                             .allSatisfy(result -> {
                                                                 assertThat(result).isInstanceOf(TestThrowMessageDelegateFactory.class);
                                                             });
    }

    @Deployment(resources = {
        "org/activiti/engine/test/bpmn/event/message/MessageThrowCatchEventTest.throwMessage.bpmn20.xml", 
        "org/activiti/engine/test/bpmn/event/message/MessageThrowCatchEventTest.catchMessage.bpmn20.xml"
    })
    public void testThrowCatchIntermediateMessageEvent() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new ThrowMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_WAITING);
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
        // when
        ProcessInstance throwMsg = runtimeService.createProcessInstanceBuilder()
                                                 .businessKey("foobar")
                                                 .processDefinitionKey(THROW_MESSAGE)
                                                 .start();
        
        // then
        assertThat(startCountDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

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
        // when
        ProcessInstance throwMsg = runtimeService.createProcessInstanceBuilder()
                                                 .businessKey("foobar")
                                                 .processDefinitionKey(END_MESSAGE)
                                                 .start();
        
        // then
        assertThat(startCountDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

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

        runtimeService.addEventListener(new ThrowMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_WAITING);
        // when
        ProcessInstance throwCatchMsg = runtimeService.createProcessInstanceBuilder()
                                                 .processDefinitionKey("throwCatch")
                                                 .start();
        
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        assertProcessEnded(throwCatchMsg.getId());
        
        assertThat(receivedEvents).hasSize(3)
                                  .extracting("type",
                                              "messageName")
                                  .contains(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, TEST_MESSAGE),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, TEST_MESSAGE),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, TEST_MESSAGE));
    }

    @Deployment
    public void testIntermediateThrowCatchMessageCorrelationKey() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new ThrowMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_WAITING);
        // when
        ProcessInstance throwCatchMsg = runtimeService.createProcessInstanceBuilder()
                                                 .variable("foo", 1)
                                                 .variable("bar", 1)
                                                 .processDefinitionKey("throwCatch")
                                                 .start();
        
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        assertProcessEnded(throwCatchMsg.getId());
        
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

        runtimeService.addEventListener(new ThrowMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_WAITING);
        // when
        ProcessInstance throwCatchMsg = runtimeService.createProcessInstanceBuilder()
                                                      .variable("foo", 1)
                                                      .variable("bar", 2)
                                                      .processDefinitionKey("throwCatch")
                                                      .start();
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isFalse();

        assertThat(processEngine.getRuntimeService()
                                .createProcessInstanceQuery()
                                .processInstanceId(throwCatchMsg.getId())
                                .singleResult()).isNotNull();
        
        assertThat(receivedEvents).hasSize(2)
                                  .extracting("type",
                                              "messageName",
                                              "correlationKey")
                                  .containsExactly(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, TEST_MESSAGE, "1"),
                                                   tuple(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, TEST_MESSAGE, "2"));
    }
    
    @Deployment
    public void testIntermediateThrowCatchMessageParallel() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new ThrowMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_WAITING);
        // when
        ProcessInstance throwCatchMsg = runtimeService.createProcessInstanceBuilder()
                                                 .businessKey("foobar")
                                                 .processDefinitionKey("throwCatch")
                                                 .start();
        
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        assertProcessEnded(throwCatchMsg.getId());
        
        assertThat(receivedEvents).hasSize(3)
                                  .extracting("type",
                                              "messageName",
                                              "correlationKey")
                                  .contains(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, TEST_MESSAGE, null),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, TEST_MESSAGE, null),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, TEST_MESSAGE, null));
    }    
    
    @Deployment(resources = "org/activiti/engine/test/bpmn/event/message/MessageThrowCatchEventTest.testIntermediateThrowCatchMessageParallelCorrelationKey.bpmn20.xml")
    public void testIntermediateThrowCatchMessageParallelNonMatchingCorrelationKey() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new ThrowMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_WAITING);
        // when
        ProcessInstance throwCatchMsg = runtimeService.createProcessInstanceBuilder()
                                                 .businessKey("foobar")
                                                 .variable("foo", 1)
                                                 .variable("bar", 2)
                                                 .processDefinitionKey("throwCatch")
                                                 .start();
        
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isFalse();

        assertThat(processEngine.getRuntimeService()
                                .createProcessInstanceQuery()
                                .processInstanceId(throwCatchMsg.getId())
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
    public void testIntermediateThrowCatchMessageParallelCorrelationKey() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new ThrowMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_WAITING);
        // when
        ProcessInstance throwCatchMsg = runtimeService.createProcessInstanceBuilder()
                                                 .businessKey("foobar")
                                                 .variable("foo", 1)
                                                 .variable("bar", 1)
                                                 .processDefinitionKey("throwCatch")
                                                 .start();
        
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        assertProcessEnded(throwCatchMsg.getId());
        
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

        runtimeService.addEventListener(new ThrowMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_WAITING);
        // when
        ProcessInstance throwCatchMsg = runtimeService.createProcessInstanceBuilder()
                                                 .businessKey("foobar")
                                                 .processDefinitionKey("throwCatch")
                                                 .start();
        
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        assertProcessEnded(throwCatchMsg.getId());
        
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

        runtimeService.addEventListener(new ThrowMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_WAITING);
        // when
        ProcessInstance throwCatchMsg = runtimeService.createProcessInstanceBuilder()
                                                 .businessKey("foobar")
                                                 .variable("foo", 1)
                                                 .variable("bar", 1)
                                                 .processDefinitionKey("throwCatch")
                                                 .start();
        
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        assertProcessEnded(throwCatchMsg.getId());
        
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

        runtimeService.addEventListener(new ThrowMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_WAITING);
        // when
        ProcessInstance throwCatchMsg = runtimeService.createProcessInstanceBuilder()
                                                 .businessKey("foobar")
                                                 .processDefinitionKey("throwCatch")
                                                 .start();
        
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        assertProcessEnded(throwCatchMsg.getId());
        
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

        runtimeService.addEventListener(new ThrowMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_WAITING);
        // when
        ProcessInstance throwCatchMsg = runtimeService.createProcessInstanceBuilder()
                                                 .businessKey("foobar")
                                                 .variable("foo", 1)
                                                 .variable("bar", 1)
                                                 .processDefinitionKey("throwCatch")
                                                 .start();
        
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        assertProcessEnded(throwCatchMsg.getId());
        
        assertThat(receivedEvents).hasSize(3)
                                  .extracting("type",
                                              "messageName",
                                              "correlationKey")
                                  .contains(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, TEST_MESSAGE, "1"),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, TEST_MESSAGE, "1"),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, TEST_MESSAGE, "1"));
    }        
    
    @Deployment(resources = "org/activiti/engine/test/bpmn/event/message/MessageThrowCatchEventTest.testIntermediateThrowCatchMessageBoundarySubprocessCorrelationKey.bpmn20.xml")
    public void testIntermediateThrowCatchMessageBoundarySubprocessNonMatchingCorrelationKey() throws Exception {
        // given
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runtimeService.addEventListener(new ThrowMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_WAITING);
        // when
        ProcessInstance throwCatchMsg = runtimeService.createProcessInstanceBuilder()
                                                 .businessKey("foobar")
                                                 .variable("foo", 1)
                                                 .variable("bar", 2)
                                                 .processDefinitionKey("throwCatch")
                                                 .start();
        
        // then
        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isFalse();

        assertThat(processEngine.getRuntimeService()
                                .createProcessInstanceQuery()
                                .processInstanceId(throwCatchMsg.getId())
                                .singleResult())
                                .isNotNull();
        
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

        runtimeService.addEventListener(new ThrowMessageListener(countDownLatch),
                                        ActivitiEventType.ACTIVITY_MESSAGE_WAITING);
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
    
    
    protected EventSubscriptionQueryImpl newEventSubscriptionQuery() {
        return new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutor());
      }
    
    protected static BlockingQueue<ThrowMessage> getMessageQueue(SubscriptionKey key) {
        return messageQueueRegistry.computeIfAbsent(key,
                                                    MessageThrowCatchEventTest::createMessageQueue);        
    }

    protected static BlockingQueue<ThrowMessage> createMessageQueue(SubscriptionKey key) {
        return new LinkedBlockingQueue<>();
    }
    
    static class SubscriptionKey {
        private final String messageName;
        private final String correlationKey;

        public SubscriptionKey(String messageName, String correlationKey) {
            this.messageName = messageName;
            this.correlationKey = correlationKey;
        }
        
        public String getMessageName() {
            return messageName;
        }
        
        public String getCorrelationKey() {
            return correlationKey;
        }

        @Override
        public int hashCode() {
            return Objects.hash(correlationKey, messageName);
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
        
    }
    
}
