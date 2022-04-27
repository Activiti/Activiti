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
package org.activiti.engine.impl.cmd;

import java.util.List;

import org.activiti.engine.event.EventLogEntry;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;


public class GetEventLogEntriesCmd implements Command<List<EventLogEntry>> {

  protected String processInstanceId;
  protected Long startLogNr;
  protected Long pageSize;

  public GetEventLogEntriesCmd() {

  }

  public GetEventLogEntriesCmd(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public GetEventLogEntriesCmd(Long startLogNr, Long pageSize) {
    this.startLogNr = startLogNr;
    this.pageSize = pageSize;
  }

  @Override
  public List<EventLogEntry> execute(CommandContext commandContext) {
    if (processInstanceId != null) {
      return commandContext.getEventLogEntryEntityManager().findEventLogEntriesByProcessInstanceId(processInstanceId);

    } else if (startLogNr != null) {
      return commandContext.getEventLogEntryEntityManager().findEventLogEntries(startLogNr, pageSize != null ? pageSize : -1);

    } else {
      return commandContext.getEventLogEntryEntityManager().findAllEventLogEntries();
    }
  }

}
