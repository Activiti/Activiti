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

import org.activiti.engine.Task;
import org.activiti.engine.TaskQuery;
import org.activiti.impl.identity.GroupImpl;
import org.activiti.impl.interceptor.Command;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.persistence.PersistenceSession;

/**
 * @author Joram Barrez
 * @deprecated Use the {@link TaskQuery} functionality instead.
 */
public class FindCandidateTasksCmd implements Command<List<Task>> {
  
  protected String userId;
  
  public FindCandidateTasksCmd(String userId) {
    this.userId = userId;
  }

  public List<Task> execute(CommandContext commandContext) {
    PersistenceSession persistenceSession = commandContext.getPersistenceSession();
    
    List<String> groupIds = new ArrayList<String>();
    List<GroupImpl> groups = persistenceSession.findGroupsByUser(userId);
    for (GroupImpl group : groups) {
      groupIds.add(group.getId());
    }
    
    return persistenceSession.findCandidateTasks(userId, groupIds);
  }

}
