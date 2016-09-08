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
package org.activiti.app.rest.editor;

import java.util.List;

import org.activiti.app.constant.GroupTypes;
import org.activiti.app.model.common.ResultListDataRepresentation;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest resource for managing groups, used in the editor app.
 */
@RestController
public class EditorGroupsResource {

  @Autowired
  protected IdentityService identityService;

  @RequestMapping(value = "/rest/editor-groups", method = RequestMethod.GET)
  public ResultListDataRepresentation getGroups(@RequestParam(required = false, value = "filter") String filter) {
    String groupNameFilter = filter;
    if (StringUtils.isEmpty(groupNameFilter)) {
      groupNameFilter = "%";
    } else {
      groupNameFilter = "%" + groupNameFilter + "%";
    }
    List<Group> matchingGroups = identityService.createGroupQuery()
        .groupNameLike(groupNameFilter)
        .groupType(GroupTypes.TYPE_ASSIGNMENT)
        .list();

    ResultListDataRepresentation result = new ResultListDataRepresentation(matchingGroups);
    // TODO: get total result count instead of page-count, in case the matching
    // list's size is equal to the page size
    return result;
  }
}
