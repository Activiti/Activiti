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
package org.activiti.engine.impl.event.logger;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.event.logger.handler.EventLoggerEventHandler;
import org.activiti.engine.impl.interceptor.CommandContext;


public abstract class AbstractEventFlusher implements EventFlusher {

  protected List<EventLoggerEventHandler> eventHandlers = new ArrayList<EventLoggerEventHandler>();

  @Override
  public void closed(CommandContext commandContext) {
    // Not interested in closed
  }

  public List<EventLoggerEventHandler> getEventHandlers() {
    return eventHandlers;
  }

  public void setEventHandlers(List<EventLoggerEventHandler> eventHandlers) {
    this.eventHandlers = eventHandlers;
  }

  public void addEventHandler(EventLoggerEventHandler databaseEventLoggerEventHandler) {
    eventHandlers.add(databaseEventLoggerEventHandler);
  }

}
