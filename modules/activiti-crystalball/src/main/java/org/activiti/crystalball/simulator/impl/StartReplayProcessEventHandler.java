package org.activiti.crystalball.simulator.impl;

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


import org.activiti.crystalball.simulator.CrystalballException;
import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.crystalball.simulator.SimulationEventHandler;
import org.activiti.crystalball.simulator.SimulationRunContext;
import org.activiti.crystalball.simulator.delegate.event.impl.ProcessInstanceCreateTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

/**
 * This class schedules replay start simulation event and takes care about process start and
 * next event schedule
 *
 * @author martin.grofcik
 */
public class StartReplayProcessEventHandler implements SimulationEventHandler {

  private static Logger log = LoggerFactory.getLogger(StartReplayProcessEventHandler.class.getName());

  /** variable name where original process instance ID is stored - only for internal replay purposes */
  public static final String PROCESS_INSTANCE_ID = "_replay.processInstanceId";

  private final String eventTypeToReplay;
  private final String eventTypeToSchedule;
  private final Collection<SimulationEvent> events;
  private final String processInstanceId;
  private final String processToStartIdKey;
  private final String businessKey;
  private final String variablesKey;

  public StartReplayProcessEventHandler(String processInstanceId, String eventTypeToReplay, String eventTypeToSchedule, Collection<SimulationEvent> events, String processToStartIdKey, String businessKey, String variablesKey) {
    this.eventTypeToReplay = eventTypeToReplay;
    this.eventTypeToSchedule = eventTypeToSchedule;
    this.events = events;
    this.processInstanceId = processInstanceId;
    this.processToStartIdKey = processToStartIdKey;
    this.businessKey = businessKey;
    this.variablesKey = variablesKey;
  }

  @Override
  public void init() {
    SimulationEvent toReplayStartEvent = findProcessInstanceStartEvent();
    SimulationEvent startEvent = new SimulationEvent.Builder(eventTypeToSchedule).
                properties(toReplayStartEvent.getProperties()).
                build();
    // add start process event
    SimulationRunContext.getEventCalendar().addEvent(startEvent);
  }

  private SimulationEvent findProcessInstanceStartEvent() {
    for (SimulationEvent event : events) {
      if (eventTypeToReplay.equals(event.getType()) && processInstanceId.equals(event.getProperty(ProcessInstanceCreateTransformer.PROCESS_INSTANCE_ID))) {
        return event;
      }
    }
    throw new CrystalballException("ProcessInstance to replay start not found");
  }

  @Override
  public void handle(SimulationEvent event) {
    // start process now
    String processDefinitionId = (String) event.getProperty(processToStartIdKey);
    String businessKey = (String) event.getProperty(this.businessKey);
    @SuppressWarnings("unchecked")
    Map<String, Object> variables = (Map<String, Object>) event.getProperty(variablesKey);
    variables.put(PROCESS_INSTANCE_ID, processInstanceId);

    log.debug("Starting new processDefId[{}] businessKey[{}] with variables[{}]", processDefinitionId, businessKey, variables);
    SimulationRunContext.getRuntimeService().startProcessInstanceById(processDefinitionId, businessKey, variables);
  }
}
