/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.impl.history;

import java.util.Date;

import org.activiti.engine.HistoricDataService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.ServiceImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.impl.event.ActivityEndedEvent;
import org.activiti.impl.event.ActivityStartedEvent;
import org.activiti.impl.event.ProcessInstanceEndedEvent;
import org.activiti.impl.event.ProcessInstanceStartedEvent;
import org.activiti.impl.interceptor.Command;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.time.Clock;
import org.activiti.pvm.event.ProcessEventBus;
import org.activiti.pvm.event.ProcessEventConsumer;

/**
 * @author Tom Baeyens
 * @author Christian Stettler
 */
// TODO: define/implement semantics of historic data: only completed processes vs. also ongoing ones
public class HistoricDataServiceImpl extends ServiceImpl implements HistoricDataService {

  @Override
  public void configurationCompleted(ProcessEngineConfiguration processEngineConfiguration) {
    super.configurationCompleted(processEngineConfiguration);
    registerEventConsumers(processEngineConfiguration.getProcessEventBus());
  }

  public void registerEventConsumers(ProcessEventBus processEventBus) {
    // TODO: where/how to register historic data service with event bus?
    
// unconfiguring history event generation till persistence is refactored.
//
//    processEventBus.subscribe(new ProcessInstanceStartedEventConsumer(), ProcessInstanceStartedEvent.class);
//    processEventBus.subscribe(new ProcessInstanceEndedEventConsumer(), ProcessInstanceEndedEvent.class);
//    processEventBus.subscribe(new ActivityStartedEventConsumer(), ActivityStartedEvent.class);
//    processEventBus.subscribe(new ActivityEndedEventConsumer(), ActivityEndedEvent.class);
  }

  public HistoricProcessInstance findHistoricProcessInstance(final String processInstanceId) {
    return commandExecutor.execute(new Command<HistoricProcessInstance>() {
      public HistoricProcessInstance execute(CommandContext commandContext) {
        return commandContext.getPersistenceSession().findHistoricProcessInstance(processInstanceId);
      }
    });
  }

  public HistoricActivityInstance findHistoricActivityInstance(final String activityId, final String processInstanceId) {
    return commandExecutor.execute(new Command<HistoricActivityInstance>() {
      public HistoricActivityInstance execute(CommandContext commandContext) {
        return commandContext.getPersistenceSession().findHistoricActivityInstance(activityId, processInstanceId);
      }
    });
  }

  private static void ensureCommandContextAvailable() {
    if (CommandContext.getCurrent() == null) {
      throw new IllegalStateException("History events can only be processed in the context of a command execution");
    }
  }

  private static class ProcessInstanceStartedEventConsumer implements ProcessEventConsumer<ProcessInstanceStartedEvent> {
    public void consumeEvent(ProcessInstanceStartedEvent event) {
      ensureCommandContextAvailable();

      String processInstanceId = event.getProcessInstanceId();
      String processDefinitionId = event.getProcessDefinitionId();
      Date startTime = Clock.getCurrentTime();

      HistoricProcessInstanceImpl historicProcessInstance = new HistoricProcessInstanceImpl(processInstanceId, processDefinitionId, startTime);

      CommandContext.getCurrent().getPersistenceSession().saveHistoricProcessInstance(historicProcessInstance);
    }
  }

  private static class ProcessInstanceEndedEventConsumer implements ProcessEventConsumer<ProcessInstanceEndedEvent> {
    public void consumeEvent(ProcessInstanceEndedEvent event) {
      ensureCommandContextAvailable();

      HistoricProcessInstanceImpl historicProcessInstance = CommandContext.getCurrent().getPersistenceSession().findHistoricProcessInstance(event.getProcessInstanceId());

      if (historicProcessInstance == null) {
        String processInstanceId = event.getProcessInstanceId();
        String processDefinitionId = event.getProcessDefinitionId();
        Date startTime = Clock.getCurrentTime();
        historicProcessInstance = new HistoricProcessInstanceImpl(processInstanceId, processDefinitionId, startTime);

        // throw new IllegalArgumentException("No historic process instance found for process instance id '" + event.getProcessInstanceId() + "'");
      }

      Date endTime = Clock.getCurrentTime();
      // TODO: does end state name makes sense at all (might be multiple)
      historicProcessInstance.markEnded(endTime, "endStateName");

      CommandContext.getCurrent().getPersistenceSession().saveHistoricProcessInstance(historicProcessInstance);
    }
  }

  private static class ActivityStartedEventConsumer implements ProcessEventConsumer<ActivityStartedEvent> {
    public void consumeEvent(ActivityStartedEvent event) {
      ensureCommandContextAvailable();

      String activityId = event.getActivityId();
      String activityName = event.getPayload().getName();
      String activityType = event.getPayload().getType();
      String processInstanceId = event.getProcessInstanceId();
      String processDefinitionId = event.getProcessDefinitionId();
      Date startTime = Clock.getCurrentTime();

      HistoricActivityInstanceImpl historicActivityInstance = new HistoricActivityInstanceImpl(activityId, activityName, activityType, processInstanceId, processDefinitionId, startTime);

      CommandContext.getCurrent().getPersistenceSession().saveHistoricActivityInstance(historicActivityInstance);
    }
  }

  private static class ActivityEndedEventConsumer implements ProcessEventConsumer<ActivityEndedEvent> {
    public void consumeEvent(ActivityEndedEvent event) {
      ensureCommandContextAvailable();

      HistoricActivityInstanceImpl historicActivityInstance = CommandContext.getCurrent().getPersistenceSession().findHistoricActivityInstance(event.getActivityId(), event.getProcessInstanceId());

      if (historicActivityInstance == null) {
        String activityId = event.getActivityId();
        String activityName = event.getPayload().getName();
        String activityType = event.getPayload().getType();
        String processInstanceId = event.getProcessInstanceId();
        String processDefinitionId = event.getProcessDefinitionId();
        Date startTime = Clock.getCurrentTime();

        historicActivityInstance = new HistoricActivityInstanceImpl(activityId, activityName, activityType, processInstanceId, processDefinitionId, startTime);

        // throw new IllegalArgumentException("No historic activity instance found for activity id '" + event.getActivityId() + "' and process instance id '" + event.getProcessInstanceId() + "'");
      }

      historicActivityInstance.markEnded(Clock.getCurrentTime());

      CommandContext.getCurrent().getPersistenceSession().saveHistoricActivityInstance(historicActivityInstance);
    }
  }
}
