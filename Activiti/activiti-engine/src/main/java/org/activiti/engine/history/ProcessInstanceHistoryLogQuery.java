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
