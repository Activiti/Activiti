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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.Job;
import org.activiti.engine.JobQuery;
import org.activiti.engine.Page;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;


/**
 * @author jbarrez
 */
public class JobQueryImpl extends AbstractQuery<Job> implements JobQuery {
  
  protected String processInstanceId;
  
  public JobQuery processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }
  
  public JobQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public long executeCount(CommandContext commandContext) {
    return commandContext
      .getRuntimeSession()
      .dynamicJobCount(createParamMap());
  }

  public List<Job> executeList(CommandContext commandContext, Page page) {
    return commandContext
      .getRuntimeSession()
      .dynamicFindJobs(createParamMap(), page);
  }
  
  protected Map<String, Object> createParamMap() {
    Map<String, Object> params = new HashMap<String, Object>();
    if (processInstanceId != null) {
      params.put("processInstanceId", processInstanceId);
    }
    return params;
  }

}
