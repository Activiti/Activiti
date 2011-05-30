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

package org.activiti.engine.impl.cmd;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.JobEntity;


/**
 * @author Tom Baeyens
 */
public class DeleteJobsCmd implements Command<Void> {

  List<String> jobIds;
  
  public DeleteJobsCmd(List<String> jobIds) {
    this.jobIds = jobIds;
  }

  public DeleteJobsCmd(String jobId) {
    this.jobIds = new ArrayList<String>();
    jobIds.add(jobId);
  }

  public Void execute(CommandContext commandContext) {
    JobEntity jobToDelete = null;
    for (String jobId: jobIds) {
      jobToDelete = Context
        .getCommandContext()
        .getJobManager()
        .findJobById(jobId);
      
      if(jobToDelete != null) {
        // When given job doesn't exist, ignore
        jobToDelete.delete();
      }
    }
    return null;
  }
}
