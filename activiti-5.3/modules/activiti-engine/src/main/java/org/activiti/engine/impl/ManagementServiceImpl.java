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
package org.activiti.engine.impl;

import java.util.Map;

import org.activiti.engine.ManagementService;
import org.activiti.engine.impl.cmd.ExecuteJobsCmd;
import org.activiti.engine.impl.cmd.GetJobExceptionStacktraceCmd;
import org.activiti.engine.impl.cmd.GetPropertiesCmd;
import org.activiti.engine.impl.cmd.GetTableCountCmd;
import org.activiti.engine.impl.cmd.GetTableMetaDataCmd;
import org.activiti.engine.management.TableMetaData;
import org.activiti.engine.management.TablePageQuery;
import org.activiti.engine.runtime.JobQuery;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class ManagementServiceImpl extends ServiceImpl implements ManagementService {

  public Map<String, Long> getTableCount() {
    return commandExecutor.execute(new GetTableCountCmd());
  }
  
  public TableMetaData getTableMetaData(String tableName) {
    return commandExecutor.execute(new GetTableMetaDataCmd(tableName));
  }

  public void executeJob(String jobId) {
    commandExecutor.execute(new ExecuteJobsCmd(jobId));
  }

  public TablePageQuery createTablePageQuery() {
    return new TablePageQueryImpl(commandExecutor);
  }
  
  public JobQuery createJobQuery() {
    return new JobQueryImpl(commandExecutor);
  }

  public String getJobExceptionStacktrace(String jobId) {
    return commandExecutor.execute(new GetJobExceptionStacktraceCmd(jobId));
  }

  public Map<String, String> getProperties() {
    return commandExecutor.execute(new GetPropertiesCmd());
  }
}
