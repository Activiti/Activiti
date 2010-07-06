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
package org.activiti.surf;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.surf.FrameworkUtil;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.exception.UserFactoryException;
import org.springframework.extensions.surf.support.AbstractUserFactory;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.*;

/**
 * This factory loads users from Activiti, fetching their properties and groups.
 *
 * The user is stored in the request context and can be fetched using context.getUser().
 * The user is also available in the root of the a script component context as 'user'.
 *
 * @author Erik Winlöf
 */
public class ActivitiUserFactory extends AbstractUserFactory
{

  /**
   * The endpoint to retrieve the user info from.
   */
  private String endpointId;

  /**
   * The database admin group id (used to resolve if user has admin capability).
   */
  private String adminGroupId;

  /**
   * The database security role group type id (used to separate security groups).
   */
  private String securityRoleGroupTypeId;

  /**
   * The database assignment group type id (used to separate assigment groups).
   */
  private String assignmentGroupTypeId;

  /**
   * Setter for the endpoint id.
   *
   * @param endpointId The endpoint id
   */
  public void setEndpointId(String endpointId) {
    this.endpointId = endpointId;
  }

  /**
   * Setter for the admin group id.
   *
   * @param adminGroupId The admin group id
   */
  public void setAdminGroupId(String adminGroupId) {
    this.adminGroupId = adminGroupId;
  }

  /**
   * Setter for security role group type id.
   *
   * @param securityRoleGroupTypeId The security role group type id
   */
  public void setSecurityRoleGroupTypeId(String securityRoleGroupTypeId)
  {
    this.securityRoleGroupTypeId = securityRoleGroupTypeId;
  }

  /**
   * Setter for assignment group type id.
   *
   * @param assignmentGroupTypeId The assignment group type id
   */
  public void setAssignmentGroupTypeId(String assignmentGroupTypeId)
  {
    this.assignmentGroupTypeId = assignmentGroupTypeId;
  }

  /**
   * Authenticates the username and password against the endpoint.
   *
   * @param request The webscript request
   * @param username The username
   * @param password The password
   *
   * @return true if username and password matched
   */
  public boolean authenticate(HttpServletRequest request, String username, String password) {
    boolean authenticated = false;
    try
    {
      // Make sure our credentials are in the vault
      CredentialVault vault = FrameworkUtil.getCredentialVault(request.getSession(), username);
      Credentials credentials = vault.newCredentials(endpointId);
      credentials.setProperty(Credentials.CREDENTIAL_USERNAME, username);
      credentials.setProperty(Credentials.CREDENTIAL_PASSWORD, password);

      // Build a connector whose connector session is bound to the current session
      AuthenticatingConnector connector = (AuthenticatingConnector)
        FrameworkUtil.getConnector(request.getSession(), username, endpointId);
      authenticated = connector.handshake();
    }
    catch (Throwable ex)
    {
      // Log the authentication failure
      ex.printStackTrace();
    }

    return authenticated;
  }

  /**
   * Loads the user info for the user with the userId from the configure endpoint.
   *
   * @param context The request context
   * @param userId The activiti username
   * @return an activiti user object
   * @throws UserFactoryException
   */
  public User loadUser(RequestContext context, String userId)
    throws UserFactoryException
  {
    return loadUser(context, userId, null);
  }

  /**
   * Loads the user info for the user with the userId form a specific endpoint.
   *
   * @param context The request context
   * @param userId The activiti username
   * @param userEndpointId The endpoint to load the user from
   * @return an activiti user object
   * @throws UserFactoryException
   */
  @SuppressWarnings("unchecked")
  public User loadUser(RequestContext context, String userId, String userEndpointId)
    throws UserFactoryException
  {
    if (userEndpointId == null)
    {
      // Use configured enpoint
      userEndpointId = this.endpointId;
    }

    ActivitiUser user = null;
    try
    {
      /**
       * Ensure we bind the connector to the current user name - if this is the first load
       * of a user we will use the userId as passed into the method
       */
      String currentUserId = context.getUserId();
      if (currentUserId == null)
      {
        currentUserId = userId;
      }

      // Get a connector whose connector session is bound to the current session
      HttpSession session = ServletUtil.getSession();
      Connector connector = FrameworkUtil.getConnector(session, currentUserId, userEndpointId);

      // Invoke and check for OK response
      Response response = connector.call("/user/" + URLEncoder.encode(userId));
      if (Status.STATUS_OK != response.getStatus().getCode()) {
        throw new UserFactoryException("Unable to create user - failed to retrieve user info: " +
          response.getStatus().getMessage(), (Exception) response.getStatus().getException());
      }

      if (response.getStatus().getCode() == ResponseStatus.STATUS_OK) {
        // Save the user json object
        JSONObject userJson = new JSONObject(response.getResponse());

        // Get the user's groups
        response = connector.call("/user/" + URLEncoder.encode(userId) + "/groups");
        if (Status.STATUS_OK != response.getStatus().getCode()) {
          throw new UserFactoryException("Unable to create user - failed to retrieve group info: " +
            response.getStatus().getMessage(), (Exception) response.getStatus().getException());
        }
        JSONObject result = new JSONObject(response.getResponse());
        JSONArray groupsArray = result.getJSONArray("data");
        JSONObject groupObject;
        HashMap<String, String> securityGroups = new HashMap<String, String>();
        HashMap<String, String> assignmentGroups = new HashMap<String, String>();
        for (int i = 0, il = groupsArray.length(); i < il; i++) {
          groupObject = (JSONObject) groupsArray.get(i);
          if (groupObject.getString("type").equals(securityRoleGroupTypeId)) {
            securityGroups.put(groupObject.getString("id"), groupObject.getString("name"));
          }
          else if (groupObject.getString("type").equals(assignmentGroupTypeId)) {
            assignmentGroups.put(groupObject.getString("id"), groupObject.getString("name"));
          }
        }

        // Resolve the users capabilities
        Map<String, Boolean> capabilities = new HashMap<String, Boolean>();
        capabilities.put(User.CAPABILITY_ADMIN, securityGroups.containsKey(adminGroupId));
        capabilities.put(User.CAPABILITY_GUEST, false);
        capabilities.put(User.CAPABILITY_MUTABLE, false);

        // Create the activiti user
        user = new ActivitiUser(userJson.getString("id"), capabilities);
        user.setFirstName(userJson.getString("firstName"));
        user.setLastName(userJson.getString("lastName"));
        user.setEmail(userJson.getString("email"));
        user.setSecurityRoleGroups(securityGroups);
        user.setAssignmentGroups(assignmentGroups);
      }
      else {
        throw new UserFactoryException("Code '" + response.getStatus().getCode() + "' received while loading user object.");
      }
    }
    catch (Exception ex) {
      // Unable to read back the user json object
      throw new UserFactoryException("Unable to retrieve user from repository", ex);
    }

    return user;
  }

}
