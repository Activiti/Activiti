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
package com.activiti.rest.editor;

import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.springframework.beans.factory.annotation.Autowired;

import com.activiti.model.common.ResultListDataRepresentation;

public class AbstractEditorGroupsResource {

  @Autowired
  private IdentityService identityService;

  public ResultListDataRepresentation getGroups(String filter) {
    List<Group> matchingGroups = identityService.createGroupQuery().groupNameLike(filter).list();

    ResultListDataRepresentation result = new ResultListDataRepresentation(matchingGroups);
    // TODO: get total result count instead of page-count, in case the matching list's size is equal to the page size
    return result;
  }

  public ResultListDataRepresentation getUsersForGroup(String groupId) {
    List<User> groupUsers = identityService.createUserQuery().memberOfGroup(groupId).list();
    return new ResultListDataRepresentation(groupUsers);
  }

}
