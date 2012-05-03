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

package org.activiti.rest.api.identity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.activiti.engine.identity.Group;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
public class GroupSearchResource extends SecuredResource {

  @Get
  public DataResponse searchGroups() {
    if (authenticate() == false)
      return null;

    String searchText = (String) getRequest().getAttributes().get("searchText");
    if (searchText == null) {
      searchText = "";
    }
    searchText = searchText.toLowerCase();

    List<Group> groups = ActivitiUtil.getIdentityService().createGroupQuery().list();
    List<GroupInfo> groupList = new ArrayList<GroupInfo>();
    for (Group group : groups) {
      if (group.getName().toLowerCase().contains(searchText)
          || group.getId().toLowerCase().contains(searchText)) {
        groupList.add(new GroupInfo(group));
      }
    }

    Collections.sort(groupList, new GroupResponseComparable());

    DataResponse response = new DataResponse();
    response.setStart(0);
    response.setSize(groupList.size());
    response.setSort("name");
    response.setOrder("asc");
    response.setTotal(groupList.size());
    response.setData(groupList);

    return response;
  }

  protected class GroupResponseComparable implements Comparator<GroupInfo> {

    public int compare(GroupInfo group1, GroupInfo group2) {
      return group1.getName().compareTo(group2.getName());
    }
  }
}
