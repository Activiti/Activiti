/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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


package org.activiti.engine.impl.persistence.entity;

import java.util.List;

import org.activiti.engine.event.EventLogEntry;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.data.DataManager;
import org.activiti.engine.impl.persistence.entity.data.EventLogEntryDataManager;


public class EventLogEntryEntityManagerImpl extends AbstractEntityManager<EventLogEntryEntity> implements EventLogEntryEntityManager {

  protected EventLogEntryDataManager eventLogEntryDataManager;

  public EventLogEntryEntityManagerImpl(ProcessEngineConfigurationImpl processEngineConfiguration, EventLogEntryDataManager eventLogEntryDataManager) {
    super(processEngineConfiguration);
    this.eventLogEntryDataManager = eventLogEntryDataManager;
  }

  @Override
  protected DataManager<EventLogEntryEntity> getDataManager() {
    return eventLogEntryDataManager;
  }

  @Override
  public List<EventLogEntry> findAllEventLogEntries() {
    return eventLogEntryDataManager.findAllEventLogEntries();
  }

  @Override
  public List<EventLogEntry> findEventLogEntries(long startLogNr, long pageSize) {
   return eventLogEntryDataManager.findEventLogEntries(startLogNr, pageSize);
  }

  @Override
  public List<EventLogEntry> findEventLogEntriesByProcessInstanceId(String processInstanceId) {
    return eventLogEntryDataManager.findEventLogEntriesByProcessInstanceId(processInstanceId);
  }

  @Override
  public void deleteEventLogEntry(long logNr) {
    eventLogEntryDataManager.deleteEventLogEntry(logNr);
  }

  public EventLogEntryDataManager getEventLogEntryDataManager() {
    return eventLogEntryDataManager;
  }

  public void setEventLogEntryDataManager(EventLogEntryDataManager eventLogEntryDataManager) {
    this.eventLogEntryDataManager = eventLogEntryDataManager;
  }

}
