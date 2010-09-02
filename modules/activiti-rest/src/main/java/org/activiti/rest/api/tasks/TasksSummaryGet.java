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
package org.activiti.rest.api.tasks;

import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates a summary of a users tasks.
 *
 * @author Erik Winlof
 */
public class TasksSummaryGet extends ActivitiWebScript {

  /**
   * Creates a summary of a users tasks for the webscript template.
   *
   * @param req The webscripts request
   * @param status The webscripts status
   * @param cache The webscript cache
   * @param model The webscripts template model
   */
  @Override
  protected void executeWebScript(WebScriptRequest req, Status status, Cache cache, Map<String, Object> model)
  {
    String user = getMandatoryString(req, "user");
    TaskService ts = getTaskService();
    List<Group> groups = getIdentityService().findGroupsByUserIdAndGroupType(user, config.getAssignmentGroupTypeId());
    Map<String, Long> unassignedByGroup = new HashMap<String, Long>();
    long tasksInGroup;
    for (Group group : groups)
    {
      tasksInGroup = ts.createTaskQuery().candidateGroup(group.getId()).count();
      unassignedByGroup.put(group.getId(), tasksInGroup);
    }
    model.put("unassignedByGroup", unassignedByGroup);
    model.put("unassigned", ts.createTaskQuery().candidateUser(user).count());
    model.put("assigned", ts.createTaskQuery().assignee(user).count());
  }

}
