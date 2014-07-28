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

package org.activiti.rest.service.api.legacy.identity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.activiti.engine.identity.Group;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.common.api.SecuredResource;
import org.apache.commons.lang3.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
public class LegacyGroupSearchResource extends SecuredResource {

  @Get("json")
  public DataResponse searchGroups() {
    if (authenticate() == false)
      return null;

    String searchText = (String) getQuery().getValues("searchText");
    if (searchText != null) {
      searchText = searchText.toLowerCase();
    }

    List<Group> groups = ActivitiUtil.getIdentityService().createGroupQuery().list();
    List<LegacyGroupInfo> groupList = new ArrayList<LegacyGroupInfo>();
    for (Group group : groups) {
      
      if (StringUtils.isNotEmpty(searchText)) {
        if (group.getName().toLowerCase().contains(searchText)
            || group.getId().toLowerCase().contains(searchText)) {
          groupList.add(new LegacyGroupInfo(group));
        }
      } else {
        groupList.add(new LegacyGroupInfo(group));
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
  
  protected Status getAuthenticationFailureStatus() {
    return Status.CLIENT_ERROR_FORBIDDEN;
  }

  protected class GroupResponseComparable implements Comparator<LegacyGroupInfo> {

    public int compare(LegacyGroupInfo group1, LegacyGroupInfo group2) {
      return group1.getName().compareTo(group2.getName());
    }
  }
}
