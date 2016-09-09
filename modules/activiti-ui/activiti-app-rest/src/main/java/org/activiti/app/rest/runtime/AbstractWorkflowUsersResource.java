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
package org.activiti.app.rest.runtime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.activiti.app.model.common.ResultListDataRepresentation;
import org.activiti.app.model.idm.UserRepresentation;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.task.IdentityLink;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class AbstractWorkflowUsersResource {

  private static final int MAX_PEOPLE_SIZE = 50;

  @Autowired
  private IdentityService identityService;

  @Autowired
  private RuntimeService runtimeService;

  @Autowired
  private TaskService taskService;

  public ResultListDataRepresentation getUsers(String filter, String email, String excludeTaskId, String excludeProcessId, Long groupId) {

    // Actual query
    int page = 0;
    int pageSize = MAX_PEOPLE_SIZE;

    UserQuery userQuery = identityService.createUserQuery();
    if (StringUtils.isNotEmpty(filter)) {
      userQuery.userFullNameLike("%" + filter + "%");
    }
    List<User> matchingUsers = userQuery.listPage(page, pageSize);

    // Filter out users already part of the task/process of which the ID has been passed
    if (excludeTaskId != null) {
      filterUsersInvolvedInTask(excludeTaskId, matchingUsers);
    } else if (excludeProcessId != null) {
      filterUsersInvolvedInProcess(excludeProcessId, matchingUsers);
    }

    List<UserRepresentation> userRepresentations = new ArrayList<UserRepresentation>(matchingUsers.size());
    for (User user : matchingUsers) {
      userRepresentations.add(new UserRepresentation(user));
    }
    ResultListDataRepresentation result = new ResultListDataRepresentation(userRepresentations);

    if (page != 0 || (page == 0 && matchingUsers.size() == pageSize)) {
      // Total differs from actual result size, need to fetch it
      result.setTotal(userQuery.count());
    }
    return result;
  }

  protected void filterUsersInvolvedInProcess(String excludeProcessId, List<User> matchingUsers) {
    Set<String> involvedUsers = getInvolvedUsersAsSet(runtimeService.getIdentityLinksForProcessInstance(excludeProcessId));
    removeinvolvedUsers(matchingUsers, involvedUsers);
  }

  protected void filterUsersInvolvedInTask(String excludeTaskId, List<User> matchingUsers) {
    Set<String> involvedUsers = getInvolvedUsersAsSet(taskService.getIdentityLinksForTask(excludeTaskId));
    removeinvolvedUsers(matchingUsers, involvedUsers);
  }

  protected Set<String> getInvolvedUsersAsSet(List<IdentityLink> involvedPeople) {
    Set<String> involved = null;
    if (involvedPeople.size() > 0) {
      involved = new HashSet<String>();
      for (IdentityLink link : involvedPeople) {
        if (link.getUserId() != null) {
          involved.add(link.getUserId());
        }
      }
    }
    return involved;
  }

  protected void removeinvolvedUsers(List<User> matchingUsers, Set<String> involvedUsers) {
    if (involvedUsers != null) {
      // Using iterator to be able to remove without ConcurrentModExceptions
      Iterator<User> userIt = matchingUsers.iterator();
      while (userIt.hasNext()) {
        if (involvedUsers.contains(userIt.next().getId().toString())) {
          userIt.remove();
        }
      }
    }
  }

}
