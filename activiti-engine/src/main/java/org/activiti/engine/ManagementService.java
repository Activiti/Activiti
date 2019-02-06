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
package org.activiti.engine;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.event.EventLogEntry;
import org.activiti.engine.impl.cmd.CustomSqlExecution;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandConfig;
import org.activiti.engine.impl.persistence.entity.DeadLetterJobEntity;
import org.activiti.engine.impl.persistence.entity.SuspendedJobEntity;
import org.activiti.engine.impl.persistence.entity.TimerJobEntity;
import org.activiti.engine.management.TableMetaData;
import org.activiti.engine.management.TablePage;
import org.activiti.engine.management.TablePageQuery;
import org.activiti.engine.runtime.DeadLetterJobQuery;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.JobQuery;
import org.activiti.engine.runtime.SuspendedJobQuery;
import org.activiti.engine.runtime.TimerJobQuery;

/**
 * Service for admin and maintenance operations on the process engine.
 * 
 * These operations will typically not be used in a workflow driven application, but are used in for example the operational console.
 *
 */
@Internal
public interface ManagementService {

  /**
   * Get the mapping containing {table name, row count} entries of the Activiti database schema.
   */
  Map<String, Long> getTableCount();

  /**
   * Gets the table name (including any configured prefix) for an Activiti entity like Task, Execution or the like.
   */
  String getTableName(Class<?> activitiEntityClass);

  /**
   * Gets the metadata (column names, column types, etc.) of a certain table. Returns null when no table exists with the given name.
   */
  TableMetaData getTableMetaData(String tableName);

  /**
   * Creates a {@link TablePageQuery} that can be used to fetch {@link TablePage} containing specific sections of table row data.
   */
  TablePageQuery createTablePageQuery();

  /**
   * Returns a new JobQuery implementation, that can be used to dynamically query the jobs.
   */
  JobQuery createJobQuery();
  
  /**
   * Returns a new TimerJobQuery implementation, that can be used to dynamically query the timer jobs.
   */
  TimerJobQuery createTimerJobQuery();
  
  /**
   * Returns a new SuspendedJobQuery implementation, that can be used to dynamically query the suspended jobs.
   */
  SuspendedJobQuery createSuspendedJobQuery();
  
  /**
   * Returns a new DeadLetterJobQuery implementation, that can be used to dynamically query the dead letter jobs.
   */
  DeadLetterJobQuery createDeadLetterJobQuery();

  /**
   * Forced synchronous execution of a job (eg. for administration or testing) The job will be executed, even if the process definition and/or the process instance is in suspended state.
   * 
   * @param jobId
   *          id of the job to execute, cannot be null.
   * @throws ActivitiObjectNotFoundException
   *           when there is no job with the given id.
   */
  void executeJob(String jobId);
  
  /**
   * Moves a timer job to the executable job table (eg. for administration or testing). The timer job will be moved, even if the process definition and/or the process instance is in suspended state.
   * 
   * @param jobId
   *          id of the timer job to move, cannot be null.
   * @throws ActivitiObjectNotFoundException
   *           when there is no job with the given id.
   */
  Job moveTimerToExecutableJob(String jobId);
  
  /**
   * Moves a job to the dead letter job table (eg. for administration or testing). The job will be moved, even if the process definition and/or the process instance has retries left.
   * 
   * @param jobId
   *          id of the job to move, cannot be null.
   * @throws ActivitiObjectNotFoundException
   *           when there is no job with the given id.
   */
  Job moveJobToDeadLetterJob(String jobId);
  
  /**
   * Moves a job that is in the dead letter job table back to be an executable job, 
   * and resetting the retries (as the retries was 0 when it was put into the dead letter job table).
   * 
   * @param jobId
   *          id of the job to move, cannot be null.
   * @param retries
   *          the number of retries (value greater than 0) which will be set on the job.
   * @throws ActivitiObjectNotFoundException
   *           when there is no job with the given id.
   */
  Job moveDeadLetterJobToExecutableJob(String jobId, int retries);

  /**
   * Delete the job with the provided id.
   * 
   * @param jobId
   *          id of the job to delete, cannot be null.
   * @throws ActivitiObjectNotFoundException
   *           when there is no job with the given id.
   */
  void deleteJob(String jobId);
  
  /**
   * Delete the timer job with the provided id.
   * 
   * @param jobId
   *          id of the timer job to delete, cannot be null.
   * @throws ActivitiObjectNotFoundException
   *           when there is no job with the given id.
   */
  void deleteTimerJob(String jobId);
  
  /**
   * Delete the dead letter job with the provided id.
   * 
   * @param jobId
   *          id of the dead letter job to delete, cannot be null.
   * @throws ActivitiObjectNotFoundException
   *           when there is no job with the given id.
   */
  void deleteDeadLetterJob(String jobId);

  /**
   * Sets the number of retries that a job has left.
   * 
   * Whenever the JobExecutor fails to execute a job, this value is decremented. When it hits zero, the job is supposed to be dead and not retried again. In that case, this method can be used to
   * increase the number of retries.
   * 
   * @param jobId
   *          id of the job to modify, cannot be null.
   * @param retries
   *          number of retries.
   */
  void setJobRetries(String jobId, int retries);
  
  /**
   * Sets the number of retries that a timer job has left.
   * 
   * Whenever the JobExecutor fails to execute a timer job, this value is decremented. When it hits zero, the job is supposed to be dead and not retried again. In that case, this method can be used to
   * increase the number of retries.
   * 
   * @param jobId
   *          id of the timer job to modify, cannot be null.
   * @param retries
   *          number of retries.
   */
  void setTimerJobRetries(String jobId, int retries);

  /**
   * Returns the full stacktrace of the exception that occurs when the job with the given id was last executed. 
   * Returns null when the job has no exception stacktrace.
   * 
   * @param jobId
   *          id of the job, cannot be null.
   * @throws ActivitiObjectNotFoundException
   *           when no job exists with the given id.
   */
  String getJobExceptionStacktrace(String jobId);
  
  /**
   * Returns the full stacktrace of the exception that occurs when the {@link TimerJobEntity} with the given id was last executed. 
   * Returns null when the job has no exception stacktrace.
   * 
   * @param jobId
   *          id of the job, cannot be null.
   * @throws ActivitiObjectNotFoundException
   *           when no job exists with the given id.
   */
  String getTimerJobExceptionStacktrace(String jobId);
  
  /**
   * Returns the full stacktrace of the exception that occurs when the {@link SuspendedJobEntity} with the given id was last executed. 
   * Returns null when the job has no exception stacktrace.
   * 
   * @param jobId
   *          id of the job, cannot be null.
   * @throws ActivitiObjectNotFoundException
   *           when no job exists with the given id.
   */
  String getSuspendedJobExceptionStacktrace(String jobId);
  
  /**
   * Returns the full stacktrace of the exception that occurs when the {@link DeadLetterJobEntity} with the given id was last executed. 
   * Returns null when the job has no exception stacktrace.
   * 
   * @param jobId
   *          id of the job, cannot be null.
   * @throws ActivitiObjectNotFoundException
   *           when no job exists with the given id.
   */
  String getDeadLetterJobExceptionStacktrace(String jobId);
  

  /** get the list of properties. */
  Map<String, String> getProperties();

  /**
   * programmatic schema update on a given connection returning feedback about what happened
   */
  String databaseSchemaUpgrade(Connection connection, String catalog, String schema);

  /**
   * Executes a given command with the default {@link CommandConfig}.
   * 
   * @param command
   *          the command, cannot be null.
   * @return the result of command execution
   */
  <T> T executeCommand(Command<T> command);

  /**
   * Executes a given command with the specified {@link CommandConfig}.
   * 
   * @param config
   *          the command execution configuration, cannot be null.
   * @param command
   *          the command, cannot be null.
   * @return the result of command execution
   */
  <T> T executeCommand(CommandConfig config, Command<T> command);

  /**
   * Executes the sql contained in the {@link CustomSqlExecution} parameter.
   */
  <MapperType, ResultType> ResultType executeCustomSql(CustomSqlExecution<MapperType, ResultType> customSqlExecution);

  /**
   * Returns a list of event log entries, describing everything the engine has processed. Note that the event logging must specifically must be enabled in the process engine configuration.
   * 
   * Passing null as arguments will effectively fetch ALL event log entries. Be careful, as this list might be huge!
   */
  List<EventLogEntry> getEventLogEntries(Long startLogNr, Long pageSize);

  /**
   * Returns a list of event log entries for a specific process instance id. Note that the event logging must specifically must be enabled in the process engine configuration.
   * 
   * Passing null as arguments will effectively fetch ALL event log entries. Be careful, as this list might be huge!
   */
  List<EventLogEntry> getEventLogEntriesByProcessInstanceId(String processInstanceId);

  /**
   * Delete a EventLogEntry. Typically only used in testing, as deleting log entries defeats the whole purpose of keeping a log.
   */
  void deleteEventLogEntry(long logNr);

}
