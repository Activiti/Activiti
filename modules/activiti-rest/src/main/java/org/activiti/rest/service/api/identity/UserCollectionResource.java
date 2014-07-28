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

package org.activiti.rest.service.api.identity;

import java.util.HashMap;
import java.util.Set;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.UserQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.common.api.SecuredResource;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

/**
 * @author Frederik Heremans
 */
public class UserCollectionResource extends SecuredResource {

  protected static HashMap<String, QueryProperty> properties = new HashMap<String, QueryProperty>();
  
  static {
    properties.put("id", UserQueryProperty.USER_ID);
    properties.put("firstName", UserQueryProperty.FIRST_NAME);
    properties.put("lastName", UserQueryProperty.LAST_NAME);
    properties.put("email", UserQueryProperty.EMAIL);
  }
  
  @Get("json")
  public DataResponse getUsers() {
    if(!authenticate())
      return null;

    UserQuery query = ActivitiUtil.getIdentityService().createUserQuery();
    Form form = getQuery();
    Set<String> names = form.getNames();
    
    if(names.contains("id")) {
      query.userId(getQueryParameter("id", form));
    }
    if(names.contains("firstName")) {
      query.userFirstName(getQueryParameter("firstName", form));
    }
    if(names.contains("lastName")) {
      query.userLastName(getQueryParameter("lastName", form));
    }
    if(names.contains("email")) {
      query.userEmail(getQueryParameter("email", form));
    }
    if(names.contains("firstNameLike")) {
      query.userFirstNameLike(getQueryParameter("firstNameLike", form));
    }
    if(names.contains("lastNameLike")) {
      query.userLastNameLike(getQueryParameter("lastNameLike", form));
    }
    if(names.contains("emailLike")) {
      query.userEmailLike(getQueryParameter("emailLike", form));
    }
    if(names.contains("memberOfGroup")) {
      query.memberOfGroup(getQueryParameter("memberOfGroup", form));
    }
    if(names.contains("potentialStarter")) {
      query.potentialStarter(getQueryParameter("potentialStarter", form));
    }

    return new UserPaginateList(this).paginateList(form, query, "id", properties);
  }
  
  @Post
  public UserResponse createUser(UserRequest request) {
  	if(authenticate() == false) return null;
  	
    if(request.getId() == null) {
      throw new ActivitiIllegalArgumentException("Id cannot be null.");
    }

    // Check if a user with the given ID already exists so we return a CONFLICT
    if(ActivitiUtil.getIdentityService().createUserQuery().userId(request.getId()).count() > 0) {
      throw new ResourceException(Status.CLIENT_ERROR_CONFLICT.getCode(), "A user with id '" + request.getId() + "' already exists.", null, null);
    }
    
    User created = ActivitiUtil.getIdentityService().newUser(request.getId());
    created.setEmail(request.getEmail());
    created.setFirstName(request.getFirstName());
    created.setLastName(request.getLastName());
    created.setPassword(request.getPassword());
    ActivitiUtil.getIdentityService().saveUser(created);
    
    setStatus(Status.SUCCESS_CREATED);
    
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createUserResponse(this, created, true);
  }
  
}
