package org.activiti.crystalball.simulator;

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

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.springframework.beans.factory.FactoryBean;

/**
 * 
 * @author martin.grofcik
 */
public class SimpleSimulationRunFactory implements FactoryBean<SimulationRun> {

  protected Map<String, SimulationEventHandler> customEventHandlerMap;
  protected HashMap<String, SimulationEventHandler> eventHandlerMap;
  protected FactoryBean<ProcessEngineImpl> processEngine;
  protected FactoryBean<EventCalendar> eventCalendar;
  protected JobExecutor jobExecutor;

  public SimpleSimulationRunFactory() {
  }

  @Override
  public SimulationRun getObject() throws Exception {
    return new SimpleSimulationRun.Builder()
        .eventHandlers(customEventHandlerMap).processEngine(processEngine.getObject())
        .eventCalendar(eventCalendar.getObject()).jobExecutor(jobExecutor).build();
  }

  @Override
  public Class<? extends SimulationRun> getObjectType() {
    return SimpleSimulationRun.class;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }

  public void setCustomEventHandlerMap(Map<String, SimulationEventHandler> customEventHandlerMap) {
    this.customEventHandlerMap = customEventHandlerMap;
  }

  public void setEventHandlerMap(HashMap<String, SimulationEventHandler> eventHandlerMap) {
    this.eventHandlerMap = eventHandlerMap;
  }

  public void setProcessEngine(FactoryBean<ProcessEngineImpl> processEngine) {
    this.processEngine = processEngine;
  }

  public void setEventCalendar(FactoryBean<EventCalendar> eventCalendar) {
    this.eventCalendar = eventCalendar;
  }

  public void setJobExecutor(JobExecutor jobExecutor) {
    this.jobExecutor = jobExecutor;
  }
}
