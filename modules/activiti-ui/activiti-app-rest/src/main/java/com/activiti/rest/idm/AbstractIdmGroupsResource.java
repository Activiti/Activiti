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
package com.activiti.rest.idm;

import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.activiti.model.common.ResultListDataRepresentation;
import com.activiti.service.exception.BadRequestException;
import com.activiti.service.exception.NotFoundException;

/**
 * @author Joram Barrez
 */
public class AbstractIdmGroupsResource {

  @Autowired
  private IdentityService identityService;

  public List<Group> getGroups() {
    List<Group> groups = identityService.createGroupQuery().list();
    return groups;
  }

  public Group getGroup(String groupId) {
    Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
    return group;
  }

  public ResultListDataRepresentation getGroupUsers(String groupId, String filter, Integer page, Integer pageSize) {
    int pageValue = page != null ? page.intValue() : 0;
    int pageSizeValue = pageSize != null ? pageSize.intValue() : 50;
    List<User> users = identityService.createUserQuery().memberOfGroup(groupId).userFullNameLike(filter).listPage(pageValue, pageSizeValue);

    ResultListDataRepresentation resultListDataRepresentation = new ResultListDataRepresentation(users);
    resultListDataRepresentation.setStart(pageValue * pageSizeValue);
    resultListDataRepresentation.setSize(users.size());
    resultListDataRepresentation.setTotal(identityService.createUserQuery().memberOfGroup(groupId).userFullNameLike(filter).count());
    return resultListDataRepresentation;
  }

  public Group createNewGroup(Group groupRepresentation) {
    if (StringUtils.isBlank(groupRepresentation.getName())) {
      throw new BadRequestException("Group name required");
    }

    Group newGroup = identityService.newGroup(groupRepresentation.getName());
    newGroup.setName(groupRepresentation.getName());
    identityService.saveGroup(newGroup);
    return newGroup;
  }

  public Group updateGroup(String groupId, Group groupRepresentation) {
    if (StringUtils.isBlank(groupRepresentation.getName())) {
      throw new BadRequestException("Group name required");
    }

    Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
    if (group == null) {
      throw new NotFoundException();
    }
    
    group.setName(groupRepresentation.getName());
    identityService.saveGroup(group);
    
    return group;
  }

  public void deleteGroup(String groupId) {
    Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
    if (group == null) {
      throw new NotFoundException();
    }

    identityService.deleteGroup(groupId);
  }

  public void addGroupMember(String groupId, String userId) {
    verifySecurityForGroupMember(groupId, userId);
    Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
    if (group == null) {
      throw new NotFoundException();
    }
    
    User user = identityService.createUserQuery().userId(userId).singleResult();
    if (user == null) {
      throw new NotFoundException();
    }
    
    identityService.createMembership(userId, groupId);
  }

  public void deleteGroupMember(String groupId, String userId) {
    verifySecurityForGroupMember(groupId, userId);
    Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
    if (group == null) {
      throw new NotFoundException();
    }
    
    User user = identityService.createUserQuery().userId(userId).singleResult();
    if (user == null) {
      throw new NotFoundException();
    }
    
    identityService.deleteMembership(userId, groupId);
  }

  protected void verifySecurityForGroupMember(String groupId, String userId) {
    // Check existence
    Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
    User user = identityService.createUserQuery().userId(userId).singleResult();
    for (User groupMember : identityService.createUserQuery().memberOfGroup(groupId).list()) {
      if (groupMember.getId().equals(userId)) {
        user = groupMember;
      }
    }

    if (group == null || user == null) {
      throw new NotFoundException();
    }
  }

}
