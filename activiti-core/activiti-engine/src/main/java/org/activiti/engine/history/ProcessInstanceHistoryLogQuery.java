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
package org.activiti.engine.history;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.task.Comment;

/**
 * Allows to fetch the {@link ProcessInstanceHistoryLog} for a process instance.
 *
 * Note that every includeXXX() method below will lead to an additional query.
 *
 * This class is actually a convenience on top of the other specific queries such as {@link HistoricTaskInstanceQuery}, {@link HistoricActivityInstanceQuery}, ... It will execute separate queries for
 * each included type, order the data according to the date (ascending) and wrap the results in the {@link ProcessInstanceHistoryLog}.
 *
 */
@Internal
public interface ProcessInstanceHistoryLogQuery {

  /**
   * The {@link ProcessInstanceHistoryLog} will contain the {@link HistoricTaskInstance} instances.
   */
  ProcessInstanceHistoryLogQuery includeTasks();

  /**
   * The {@link ProcessInstanceHistoryLog} will contain the {@link HistoricActivityInstance} instances.
   */
  ProcessInstanceHistoryLogQuery includeActivities();

  /**
   * The {@link ProcessInstanceHistoryLog} will contain the {@link HistoricVariableInstance} instances.
   */
  ProcessInstanceHistoryLogQuery includeVariables();

  /**
   * The {@link ProcessInstanceHistoryLog} will contain the {@link Comment} instances.
   */
  ProcessInstanceHistoryLogQuery includeComments();

  /**
   * The {@link ProcessInstanceHistoryLog} will contain the {@link HistoricVariableUpdate} instances.
   */
  ProcessInstanceHistoryLogQuery includeVariableUpdates();

  /**
   * The {@link ProcessInstanceHistoryLog} will contain the {@link HistoricFormProperty} instances.
   */
  ProcessInstanceHistoryLogQuery includeFormProperties();

  /** Executes the query. */
  ProcessInstanceHistoryLog singleResult();

}
