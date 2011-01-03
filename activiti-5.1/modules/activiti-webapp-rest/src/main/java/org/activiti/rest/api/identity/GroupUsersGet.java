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

import java.util.Map;

import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.UserQueryProperty;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiPagingWebScript;
import org.springframework.extensions.webscripts.*;

/**
 * Returns info about a groups's users.
 *
 * @author Erik Winlof
 */
public class GroupUsersGet extends ActivitiPagingWebScript
{

  public GroupUsersGet() {
    properties.put("id", UserQueryProperty.USER_ID);
    properties.put("firstName", UserQueryProperty.FIRST_NAME);
    properties.put("lastName", UserQueryProperty.LAST_NAME);
    properties.put("email", UserQueryProperty.EMAIL);
  }

  /**
   * Collects info about a groups's users for the webscript template.
   *
   * @param req The webscripts request
   * @param status The webscripts status
   * @param cache The webscript cache
   * @param model The webscripts template model
   */
  @Override
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    String groupId = req.getMandatoryPathParameter("groupId");
    UserQuery userQuery = getIdentityService().createUserQuery().memberOfGroup(groupId);
    paginateList(req, userQuery, "users", model, "id");
  }

}
