package org.activiti.engine.test.bpmn.event.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

    private static final String START_MESSAGE = "startMessage";
    private static final String CATCH_MESSAGE = "catchMessage";
    private static final String THROW_MESSAGE = "throwMessage";
    private static final String TEST_MESSAGE = "testMessage";
    private static List<ActivitiEvent> receivedEvents = new LinkedList<>();
    private static Map<String, BlockingQueue<ThrowMessage>> messageQueueRegistry = new ConcurrentHashMap<>();
    private static CountDownLatch startCountDownLatch = new CountDownLatch(1);

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
                Queue<ThrowMessage> queue = messageQueueRegistry.computeIfAbsent(message.getName(),
                                                                                 MessageThrowCatchEventTest::createMessageQueue);
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
            String messageName = ActivitiMessageEvent.class.cast(event)
                                                           .getMessageName();

            BlockingQueue<ThrowMessage> messageQueue = messageQueueRegistry.computeIfAbsent(messageName,
                                                                                            MessageThrowCatchEventTest::createMessageQueue);
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
                 new Thread(new Runnable() {
                     
                     @Override
                     public void run() {
                         try {
                             ThrowMessage message = messageQueue.take();
                             
                             runtimeService.createExecutionQuery()
                                           .messageEventSubscriptionName(message.getName())
                                           .list()
                                           .forEach(s -> {
                                               Map<String, Object> payload = message.getPayload()
                                                                                    .orElse(null);
                                               
                                               runtimeService.messageEventReceived(message.getName(), s.getId(), payload);

                                               countDownLatch.countDown();
                                           });
                             
                         } catch (InterruptedException e) {
                             // TODO Auto-generated catch block
                             e.printStackTrace();
                         }
                     }
                 }).start();
             }

         }
        

    }

    protected static BlockingQueue<ThrowMessage> createMessageQueue(String messageName) {
        return new LinkedBlockingQueue<>();
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

        runtimeService.addEventListener(myListener,
                                        ActivitiEventType.ACTIVITY_MESSAGE_SENT,
                                        ActivitiEventType.ACTIVITY_MESSAGE_WAITING,
                                        ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
        
        newEventSubscriptionQuery().eventType("message")
                                   .list()
                                   .forEach(s -> {
                                       BlockingQueue<ThrowMessage> messageQueue = messageQueueRegistry.computeIfAbsent(s.getEventName(),
                                                                                                                       MessageThrowCatchEventTest::createMessageQueue);
                                       new Thread(new Runnable() {

                                           @Override
                                           public void run() {
                                               try {
                                                   ThrowMessage throwMessage = messageQueue.take();
                                                   Map<String, Object> payload = throwMessage.getPayload()
                                                                                             .orElse(null);
                                                   
                                                   runtimeService.startProcessInstanceByMessage(throwMessage.getName(), payload);
                                                   
                                                   startCountDownLatch.countDown();
                                                   
                                               } catch (InterruptedException e) {
                                                   // TODO Auto-generated catch block
                                                   e.printStackTrace();
                                               }
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

    @Deployment(
            resources = {"org/activiti/engine/test/bpmn/event/message/MessageThrowCatchEventTest.throwMessage.bpmn20.xml", "org/activiti/engine/test/bpmn/event/message/MessageThrowCatchEventTest.catchMessage.bpmn20.xml"
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
                                              "messageName")
                                  .contains(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, TEST_MESSAGE),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, TEST_MESSAGE),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, TEST_MESSAGE));

    }
    
    @Deployment(resources = {
            "org/activiti/engine/test/bpmn/event/message/MessageThrowCatchEventTest.throwMessage.bpmn20.xml", 
            "org/activiti/engine/test/bpmn/event/message/MessageThrowCatchEventTest.startMessage.bpmn20.xml"
    })
    public void testThrowCatchStartMessageEvent() throws Exception {
        // when
        ProcessInstance throwMsg = runtimeService.startProcessInstanceByKey(THROW_MESSAGE);
        
        // then
        assertThat(startCountDownLatch.await(1, TimeUnit.SECONDS)).isTrue();

        HistoricProcessInstance startMsg = historyService.createHistoricProcessInstanceQuery()
                                                         .processDefinitionKey(START_MESSAGE)
                                                         .includeProcessVariables()
                                                         .singleResult();
        
        assertThat(startMsg.getProcessVariables()).containsEntry("foo", "bar");

        assertProcessEnded(throwMsg.getId());
        assertProcessEnded(startMsg.getId());
        
        assertThat(receivedEvents).hasSize(2)
                                  .extracting("type",
                                              "messageName")
                                  .contains(tuple(ActivitiEventType.ACTIVITY_MESSAGE_SENT, TEST_MESSAGE),
                                            tuple(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, TEST_MESSAGE));
    }
    
    protected EventSubscriptionQueryImpl newEventSubscriptionQuery() {
        return new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutor());
      }
    

}
