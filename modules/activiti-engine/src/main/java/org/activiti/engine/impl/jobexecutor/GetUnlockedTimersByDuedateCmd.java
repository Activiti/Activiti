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
package org.activiti.engine.impl.jobexecutor;

import java.util.Date;
import java.util.List;

import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TimerEntity;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class GetUnlockedTimersByDuedateCmd implements Command<List<TimerEntity>> {

  protected Date duedate;
  protected Page page;
  
  public GetUnlockedTimersByDuedateCmd(Date duedate, Page page) {
	  this.duedate = duedate;
	  this.page = page;
  }

  public List<TimerEntity> execute(CommandContext commandContext) {
    return Context
      .getCommandContext()
      .getJobEntityManager()
      .findUnlockedTimersByDuedate(duedate, page);
  }
}
