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

package org.activiti.engine.impl;

import org.activiti.engine.HistoricDataService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;

/**
 * @author Tom Baeyens
 * @author Christian Stettler
 */
// TODO: define/implement semantics of historic signalData: only completed processes vs. also ongoing ones
public class HistoricDataServiceImpl extends ServiceImpl implements HistoricDataService {

  public HistoricActivityInstance findHistoricActivityInstance(String activityId, String processInstanceId) {
    return null;
  }

  public HistoricProcessInstance findHistoricProcessInstance(String processInstanceId) {
    return null;
  }

  
//  @Override
//  public void configurationCompleted(ProcessEngineConfiguration processEngineConfiguration) {
//    super.configurationCompleted(processEngineConfiguration);
//    registerEventConsumers(processEngineConfiguration.getProcessEventBus());
//  }
//
//  public void registerEventConsumers(ProcessEventBus processEventBus) {
//    // TODO: where/how to register historic signalData service with event bus?
//    
//    processEventBus.subscribe(new ProcessInstanceStartedEventConsumer(), ProcessInstanceStartedEvent.class);
//    processEventBus.subscribe(new ProcessInstanceEndedEventConsumer(), ProcessInstanceEndedEvent.class);
//    processEventBus.subscribe(new ActivityStartedEventConsumer(), ActivityStartedEvent.class);
//    processEventBus.subscribe(new ActivityEndedEventConsumer(), ActivityEndedEvent.class);
//  }
//
//  public HistoricProcessInstance findHistoricProcessInstance(final String processInstanceId) {
//    return commandExecutor.execute(new Command<HistoricProcessInstance>() {
//      public HistoricProcessInstance execute(CommandContext commandContext) {
//        return commandContext.getPersistenceSession().findHistoricProcessInstance(processInstanceId);
//      }
//    });
//  }
//
//  public HistoricActivityInstance findHistoricActivityInstance(final String activityId, final String processInstanceId) {
//    return commandExecutor.execute(new Command<HistoricActivityInstance>() {
//      public HistoricActivityInstance execute(CommandContext commandContext) {
//        return commandContext.getPersistenceSession().findHistoricActivityInstance(activityId, processInstanceId);
//      }
//    });
//  }
//
//  private static void ensureCommandContextAvailable() {
//    if (CommandContext.getCurrent() == null) {
//      throw new IllegalStateException("History events can only be processed in the context of a command execution");
//    }
//  }
//
//  private static class ProcessInstanceStartedEventConsumer implements ProcessEventConsumer<ProcessInstanceStartedEvent> {
//    public void consumeEvent(ProcessInstanceStartedEvent event) {
//      ensureCommandContextAvailable();
//
//      String processInstanceId = event.getProcessInstanceId();
//      String processInstanceId = event.getProcessDefinitionId();
//      Date startTime = ClockUtil.getCurrentTime();
//
//      HistoricProcessInstanceEntity historicProcessInstance = new HistoricProcessInstanceEntity(processInstanceId, processInstanceId, startTime);
//
//      CommandContext.getCurrent().getPersistenceSession().insertHistoricProcessInstance(historicProcessInstance);
//    }
//  }
//
//  private static class ProcessInstanceEndedEventConsumer implements ProcessEventConsumer<ProcessInstanceEndedEvent> {
//    public void consumeEvent(ProcessInstanceEndedEvent event) {
//      ensureCommandContextAvailable();
//
//      HistoricProcessInstanceEntity historicProcessInstance = CommandContext.getCurrent().getPersistenceSession().findHistoricProcessInstance(event.getProcessInstanceId());
//
//      if (historicProcessInstance == null) {
//        String processInstanceId = event.getProcessInstanceId();
//        String processInstanceId = event.getProcessDefinitionId();
//        Date startTime = ClockUtil.getCurrentTime();
//        historicProcessInstance = new HistoricProcessInstanceEntity(processInstanceId, processInstanceId, startTime);
//
//        // throw new IllegalArgumentException("No historic process instance found for process instance id '" + event.getProcessInstanceId() + "'");
//      }
//
//      Date endTime = ClockUtil.getCurrentTime();
//      // TODO: does end state name makes sense at all (might be multiple)
//      historicProcessInstance.markEnded(endTime, "endStateName");
//
//      CommandContext.getCurrent().getPersistenceSession().insertHistoricProcessInstance(historicProcessInstance);
//    }
//  }
//
//  private static class ActivityStartedEventConsumer implements ProcessEventConsumer<ActivityStartedEvent> {
//    public void consumeEvent(ActivityStartedEvent event) {
//      ensureCommandContextAvailable();
//
//      String activityId = event.getActivityId();
//      String activityName = event.getPayload().getName();
//      String activityType = event.getPayload().getType();
//      String processInstanceId = event.getProcessInstanceId();
//      String processInstanceId = event.getProcessDefinitionId();
//      Date startTime = ClockUtil.getCurrentTime();
//
//      HistoricActivityInstanceEntity historicActivityInstance = new HistoricActivityInstanceEntity(activityId, activityName, activityType, processInstanceId, processInstanceId, startTime);
//
//      CommandContext.getCurrent().getPersistenceSession().insertHistoricActivityInstance(historicActivityInstance);
//    }
//  }
//
//  private static class ActivityEndedEventConsumer implements ProcessEventConsumer<ActivityEndedEvent> {
//    public void consumeEvent(ActivityEndedEvent event) {
//      ensureCommandContextAvailable();
//
//      HistoricActivityInstanceEntity historicActivityInstance = CommandContext.getCurrent().getPersistenceSession().findHistoricActivityInstance(event.getActivityId(), event.getProcessInstanceId());
//
//      if (historicActivityInstance == null) {
//        String activityId = event.getActivityId();
//        String activityName = event.getPayload().getName();
//        String activityType = event.getPayload().getType();
//        String processInstanceId = event.getProcessInstanceId();
//        String processInstanceId = event.getProcessDefinitionId();
//        Date startTime = ClockUtil.getCurrentTime();
//
//        historicActivityInstance = new HistoricActivityInstanceEntity(activityId, activityName, activityType, processInstanceId, processInstanceId, startTime);
//
//        // throw new IllegalArgumentException("No historic activity instance found for activity id '" + event.getActivityId() + "' and process instance id '" + event.getProcessInstanceId() + "'");
//      }
//
//      historicActivityInstance.markEnded(ClockUtil.getCurrentTime());
//
//      CommandContext.getCurrent().getPersistenceSession().insertHistoricActivityInstance(historicActivityInstance);
//    }
//  }
}
