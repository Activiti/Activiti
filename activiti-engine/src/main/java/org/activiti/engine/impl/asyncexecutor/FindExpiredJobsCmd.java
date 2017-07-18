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
package org.activiti.engine.impl.asyncexecutor;

import java.util.List;

import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.JobEntity;

/**

 */
public class FindExpiredJobsCmd implements Command<List<JobEntity>> {
  
  protected int pageSize;
  
  public FindExpiredJobsCmd(int pageSize) {
    this.pageSize = pageSize;
  }
  
  @Override
  public List<JobEntity> execute(CommandContext commandContext) {
    return commandContext.getJobEntityManager().findExpiredJobs(new Page(0, pageSize));
  }

}
