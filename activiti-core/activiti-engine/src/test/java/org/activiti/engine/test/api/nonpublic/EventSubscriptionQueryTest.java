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


package org.activiti.engine.test.api.nonpublic;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;


public class EventSubscriptionQueryTest extends PluggableActivitiTestCase {

  public void testQueryByEventName() {

    processEngineConfiguration.getCommandExecutor().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        MessageEventSubscriptionEntity messageEventSubscriptionEntity1 = commandContext.getEventSubscriptionEntityManager().createMessageEventSubscription();
        messageEventSubscriptionEntity1.setEventName("messageName");
        commandContext.getEventSubscriptionEntityManager().insert(messageEventSubscriptionEntity1);

        MessageEventSubscriptionEntity messageEventSubscriptionEntity2 = commandContext.getEventSubscriptionEntityManager().createMessageEventSubscription();
        messageEventSubscriptionEntity2.setEventName("messageName");
        commandContext.getEventSubscriptionEntityManager().insert(messageEventSubscriptionEntity2);

        MessageEventSubscriptionEntity messageEventSubscriptionEntity3 = commandContext.getEventSubscriptionEntityManager().createMessageEventSubscription();
        messageEventSubscriptionEntity3.setEventName("messageName2");
        commandContext.getEventSubscriptionEntityManager().insert(messageEventSubscriptionEntity3);

        return null;
      }
    });

    List<EventSubscriptionEntity> list = newEventSubscriptionQuery().eventName("messageName").list();
    assertThat(list).hasSize(2);

    list = newEventSubscriptionQuery().eventName("messageName2").list();
    assertThat(list).hasSize(1);

    cleanDb();

  }

  public void testQueryByEventType() {

    processEngineConfiguration.getCommandExecutor().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        MessageEventSubscriptionEntity messageEventSubscriptionEntity1 = commandContext.getEventSubscriptionEntityManager().createMessageEventSubscription();
        messageEventSubscriptionEntity1.setEventName("messageName");
        commandContext.getEventSubscriptionEntityManager().insert(messageEventSubscriptionEntity1);

        MessageEventSubscriptionEntity messageEventSubscriptionEntity2 = commandContext.getEventSubscriptionEntityManager().createMessageEventSubscription();
        messageEventSubscriptionEntity2.setEventName("messageName");
        commandContext.getEventSubscriptionEntityManager().insert(messageEventSubscriptionEntity2);

        SignalEventSubscriptionEntity signalEventSubscriptionEntity3 = commandContext.getEventSubscriptionEntityManager().createSignalEventSubscription();
        signalEventSubscriptionEntity3.setEventName("messageName2");
        commandContext.getEventSubscriptionEntityManager().insert(signalEventSubscriptionEntity3);

        return null;
      }
    });

    List<EventSubscriptionEntity> list = newEventSubscriptionQuery().eventType("signal").list();
    assertThat(list).hasSize(1);

    list = newEventSubscriptionQuery().eventType("message").list();
    assertThat(list).hasSize(2);

    cleanDb();

  }

  public void testQueryByActivityId() {

    processEngineConfiguration.getCommandExecutor().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        MessageEventSubscriptionEntity messageEventSubscriptionEntity1 = commandContext.getEventSubscriptionEntityManager().createMessageEventSubscription();
        messageEventSubscriptionEntity1.setEventName("messageName");
        messageEventSubscriptionEntity1.setActivityId("someActivity");
        commandContext.getEventSubscriptionEntityManager().insert(messageEventSubscriptionEntity1);

        MessageEventSubscriptionEntity messageEventSubscriptionEntity2 = commandContext.getEventSubscriptionEntityManager().createMessageEventSubscription();
        messageEventSubscriptionEntity2.setEventName("messageName");
        messageEventSubscriptionEntity2.setActivityId("someActivity");
        commandContext.getEventSubscriptionEntityManager().insert(messageEventSubscriptionEntity2);

        SignalEventSubscriptionEntity signalEventSubscriptionEntity3 = commandContext.getEventSubscriptionEntityManager().createSignalEventSubscription();
        signalEventSubscriptionEntity3.setEventName("messageName2");
        signalEventSubscriptionEntity3.setActivityId("someOtherActivity");
        commandContext.getEventSubscriptionEntityManager().insert(signalEventSubscriptionEntity3);

        return null;
      }
    });

    List<EventSubscriptionEntity> list = newEventSubscriptionQuery().activityId("someOtherActivity").list();
    assertThat(list).hasSize(1);

    list = newEventSubscriptionQuery().activityId("someActivity").eventType("message").list();
    assertThat(list).hasSize(2);

    cleanDb();

  }

  public void testQueryByEventSubscriptionId() {

    processEngineConfiguration.getCommandExecutor().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        MessageEventSubscriptionEntity messageEventSubscriptionEntity1 = commandContext.getEventSubscriptionEntityManager().createMessageEventSubscription();
        messageEventSubscriptionEntity1.setEventName("messageName");
        messageEventSubscriptionEntity1.setActivityId("someActivity");
        commandContext.getEventSubscriptionEntityManager().insert(messageEventSubscriptionEntity1);

        MessageEventSubscriptionEntity messageEventSubscriptionEntity2 = commandContext.getEventSubscriptionEntityManager().createMessageEventSubscription();
        messageEventSubscriptionEntity2.setEventName("messageName");
        messageEventSubscriptionEntity2.setActivityId("someOtherActivity");
        commandContext.getEventSubscriptionEntityManager().insert(messageEventSubscriptionEntity2);

        return null;
      }
    });

    List<EventSubscriptionEntity> list = newEventSubscriptionQuery().activityId("someOtherActivity").list();
    assertThat(list).hasSize(1);

    final EventSubscriptionEntity entity = list.get(0);

    list = newEventSubscriptionQuery().eventSubscriptionId(entity.getId()).list();

    assertThat(list).hasSize(1);

    cleanDb();

  }

  @Deployment
  public void testQueryByExecutionId() {

    // starting two instances:
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("catchSignal");
    runtimeService.startProcessInstanceByKey("catchSignal");

    // test query by process instance id
    EventSubscriptionEntity subscription = newEventSubscriptionQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(subscription).isNotNull();

    Execution executionWaitingForSignal = runtimeService.createExecutionQuery().activityId("signalEvent").processInstanceId(processInstance.getId()).singleResult();

    // test query by execution id
    EventSubscriptionEntity signalSubscription = newEventSubscriptionQuery().executionId(executionWaitingForSignal.getId()).singleResult();
    assertThat(signalSubscription).isNotNull();

    assertThat(subscription).isEqualTo(signalSubscription);

    cleanDb();

  }

  protected EventSubscriptionQueryImpl newEventSubscriptionQuery() {
    return new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutor());
  }

  protected void cleanDb() {
    processEngineConfiguration.getCommandExecutor().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        final List<EventSubscriptionEntity> subscriptions = new EventSubscriptionQueryImpl(commandContext).list();
        for (EventSubscriptionEntity eventSubscriptionEntity : subscriptions) {
          EventSubscriptionEntityManager eventSubscriptionEntityManager = Context.getCommandContext().getEventSubscriptionEntityManager();
          eventSubscriptionEntityManager.delete(eventSubscriptionEntity);
        }
        return null;
      }
    });

  }

}
