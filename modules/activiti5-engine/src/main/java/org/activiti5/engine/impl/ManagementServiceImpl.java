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
package org.activiti5.engine.impl;

import java.util.List;
import java.util.Map;

import org.activiti5.engine.ActivitiIllegalArgumentException;
import org.activiti5.engine.ManagementService;
import org.activiti5.engine.event.EventLogEntry;
import org.activiti5.engine.impl.cmd.CancelJobCmd;
import org.activiti5.engine.impl.cmd.CustomSqlExecution;
import org.activiti5.engine.impl.cmd.DeleteEventLogEntry;
import org.activiti5.engine.impl.cmd.ExecuteCustomSqlCmd;
import org.activiti5.engine.impl.cmd.ExecuteJobsCmd;
import org.activiti5.engine.impl.cmd.GetEventLogEntriesCmd;
import org.activiti5.engine.impl.cmd.GetJobExceptionStacktraceCmd;
import org.activiti5.engine.impl.cmd.GetPropertiesCmd;
import org.activiti5.engine.impl.cmd.GetTableCountCmd;
import org.activiti5.engine.impl.cmd.GetTableMetaDataCmd;
import org.activiti5.engine.impl.cmd.GetTableNameCmd;
import org.activiti5.engine.impl.cmd.SetJobRetriesCmd;
import org.activiti5.engine.impl.interceptor.Command;
import org.activiti5.engine.impl.interceptor.CommandConfig;
import org.activiti5.engine.management.TableMetaData;
import org.activiti5.engine.management.TablePageQuery;
import org.activiti5.engine.runtime.JobQuery;
import org.activiti5.engine.runtime.TimerJobQuery;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Falko Menge
 * @author Saeid Mizaei
 */
public class ManagementServiceImpl extends ServiceImpl implements ManagementService {

  public Map<String, Long> getTableCount() {
    return commandExecutor.execute(new GetTableCountCmd());
  }
  
  public String getTableName(Class<?> activitiEntityClass) {
    return commandExecutor.execute(new GetTableNameCmd(activitiEntityClass));    
  }
  
  public TableMetaData getTableMetaData(String tableName) {
    return commandExecutor.execute(new GetTableMetaDataCmd(tableName));
  }

  public void executeJob(String jobId) {
    commandExecutor.execute(new ExecuteJobsCmd(jobId));
  }
  
  public void deleteJob(String jobId) {
    commandExecutor.execute(new CancelJobCmd(jobId));
  }

  public void setJobRetries(String jobId, int retries) {
    commandExecutor.execute(new SetJobRetriesCmd(jobId, retries));
  }

  public TablePageQuery createTablePageQuery() {
    return new TablePageQueryImpl(commandExecutor);
  }
  
  public JobQuery createJobQuery() {
    return new JobQueryImpl(commandExecutor);
  }
  
  public TimerJobQuery createTimerJobQuery() {
    return new TimerJobQueryImpl(commandExecutor);
  }

  public String getJobExceptionStacktrace(String jobId) {
    return commandExecutor.execute(new GetJobExceptionStacktraceCmd(jobId));
  }

  public Map<String, String> getProperties() {
    return commandExecutor.execute(new GetPropertiesCmd());
  }

  public <T> T executeCommand(Command<T> command) {
    if (command == null) {
      throw new ActivitiIllegalArgumentException("The command is null");
    }
    return commandExecutor.execute(command);
  }
  
  public <T> T executeCommand(CommandConfig config, Command<T> command) {
    if (config == null) {
      throw new ActivitiIllegalArgumentException("The config is null");
    }
    if (command == null) {
      throw new ActivitiIllegalArgumentException("The command is null");
    }
    return commandExecutor.execute(config, command);
  }
  
  @Override
	public <MapperType, ResultType> ResultType executeCustomSql(CustomSqlExecution<MapperType, ResultType> customSqlExecution) {
  	Class<MapperType> mapperClass = customSqlExecution.getMapperClass();
  	return commandExecutor.execute(new ExecuteCustomSqlCmd<MapperType, ResultType>(mapperClass, customSqlExecution));
	}
  
  @Override
  public List<EventLogEntry> getEventLogEntries(Long startLogNr, Long pageSize) {
  	return commandExecutor.execute(new GetEventLogEntriesCmd(startLogNr, pageSize));
  }
  
  @Override
  public List<EventLogEntry> getEventLogEntriesByProcessInstanceId(String processInstanceId) {
    return commandExecutor.execute(new GetEventLogEntriesCmd(processInstanceId));
  }
  
  @Override
  public void deleteEventLogEntry(long logNr) {
  	commandExecutor.execute(new DeleteEventLogEntry(logNr));
  }

}
