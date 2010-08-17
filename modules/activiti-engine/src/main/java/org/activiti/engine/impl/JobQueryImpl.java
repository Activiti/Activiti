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

import java.util.List;

import org.activiti.engine.Job;
import org.activiti.engine.JobQuery;
import org.activiti.engine.Page;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;


/**
 * @author Joram Barrez
 * @author Tom Baeyens
 */
public class JobQueryImpl extends AbstractQuery<Job> implements JobQuery {
  
  protected String processInstanceId;
  protected String executionId;
  
  public JobQueryImpl() {
  }

  public JobQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public JobQueryImpl processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }
  
  public JobQueryImpl executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }
  
  public JobQueryImpl orderAsc(String column) {
    super.addOrder(column, SORTORDER_ASC);
    return this;
  }
  
  public JobQueryImpl orderDesc(String column) {
    super.addOrder(column, SORTORDER_DESC);
    return this;
  }

  public long executeCount(CommandContext commandContext) {
    return commandContext
      .getRuntimeSession()
      .findJobCountByQueryCriteria(this);
  }

  public List<Job> executeList(CommandContext commandContext, Page page) {
    return commandContext
      .getRuntimeSession()
      .findJobsByQueryCriteria(this, page);
  }
  
  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getExecutionId() {
    return executionId;
  }
}
