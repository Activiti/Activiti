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
package org.activiti.rest.util;

import org.activiti.*;
import org.activiti.identity.Group;
import org.activiti.impl.json.JSONObject;
import org.activiti.rest.Config;
import org.springframework.extensions.surf.util.Base64;
import org.springframework.extensions.webscripts.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for all activiti webscripts.
 *
 * @author Erik Winlšf
 */
public class ActivitiWebScript extends DeclarativeWebScript {

  /**
   * The activiti config bean
   */
  protected Config config;

  /**
   * Setter for the activiti config bean
   *
   * @param config The activiti config bean
   */
  public void setConfig(Config config) {
    this.config = config;
  }

  /**
   * The entry point for the webscript.
   *
   * Will create a model a call the executeWebScript() so extending activiti webscript may implement custom logic.
   *
   * @param  req The webscript request
   * @param  status The webscripts status
   * @param  cache The webscript cache
   * @return The webscript template model
   */
  @Override
  protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
  {
    // Prepare model with process engine info
    Map<String, Object> model = new HashMap<String, Object>();

    // todo: set the current user context when the core api implements security checks  

    // Let implementing webscript do something useful
    executeWebScript(req, status, cache, model);

    // Return model
    return model;
  }

  /**
   * Override this class to implement custom logic.
   *
   * @param req The webscript request
   * @param status The webscript
   * @param cache
   * @param model
   */
  protected void executeWebScript(WebScriptRequest req, Status status, Cache cache, Map<String, Object> model){
    // Override to make something useful
  }

  /**
   * Returns the process engine info.
   *
   * @return The process engine info
   */
  protected ProcessEngineInfo getProcessEngineInfo() {
    return ProcessEngines.getProcessEngineInfo(config.getEngine());
  }

  /**
   * Returns the process engine.
   *
   * @return The process engine
   */
  protected ProcessEngine getProcessEngine() {
    return ProcessEngines.getProcessEngine(config.getEngine());
  }

  /**
   * Returns the identity service.
   *
   * @return The identity service
   */
  protected IdentityService getIdentityService() {
    return getProcessEngine().getIdentityService();
  }

  /**
   * Returns the management service.
   *
   * @return The management service.
   */
  protected ManagementService getManagementService() {
    return getProcessEngine().getManagementService();
  }

  /**
   * Returns The process service.
   *
   * @return The process service
   */
  protected ProcessService getProcessService() {
    return getProcessEngine().getProcessService();
  }

  /**
   * Returns the task service.
   *
   * @return The task service
   */
  protected TaskService getTaskService() {
    return getProcessEngine().getTaskService();
  }

  /**
   * Returns the webscript request body in an abstracted form so multiple formats may be
   * implemented seamlessly in the future.
   *
   * @param req The webscript request
   * @return The webscript requests body
   */
  protected ActivitiWebScriptBody getBody(WebScriptRequest req) {
    try
    {
      return new ActivitiWebScriptBody(req);
    } catch (IOException e)
    {
      throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Can't read body");
    }
  }

  /**
   * Gets a path parameter value and throws an exception if its not present.
   *
   * @param req The webscript request
   * @param param The name of the path parameter
   * @return The value of the path parameter
   * @throws WebScriptException if parameter isn't present
   */
  protected String getMandatoryPathParameter(WebScriptRequest req, String param)
  {
    return checkMandatory(req.getServiceMatch().getTemplateVars().get(param), param, true);
  }

  /**
   * Gets a path parameter value.
   *
   * @param req The webscript request
   * @param param The name of the path parameter
   * @return The path parameter value or null if not present
   */
  protected String getPathParameter(WebScriptRequest req, String param)
  {
    return checkMandatory(req.getServiceMatch().getTemplateVars().get(param), param, false);
  }

  /**
   * Gets an int parameter value.
   *
   * @param req The webscript request
   * @param param The name of the int parameter
   * @return The int parameter value or Integer.MIN_VALUE if not present
   */
  protected int getInt(WebScriptRequest req, String param) {
    String value = getString(req, param);
    return value != null ? Integer.parseInt(value) : Integer.MIN_VALUE;
  }

  /**
   * Gets a mandatory int parameter and throws an exception if its not present.
   *
   * @param req The webscript request
   * @param param The name of the path parameter
   * @return The int parameter value
   * @throws WebScriptException if parameter isn't present
   */
  protected int getMandatoryInt(WebScriptRequest req, String param) {
    String value = getMandatoryString(req, param);
    return value != null ? Integer.parseInt(value) : Integer.MIN_VALUE;
  }

  /**
   * Gets an int parameter value
   *
   * @param req The webscript request
   * @param param The name of the int parameter
   * @param defaultValue THe value to return if the parameter isn't present
   * @return The int parameter value of defaultValue if the parameter isn't present
   */
  protected int getInt(WebScriptRequest req, String param, int defaultValue) {
    String value = getString(req, param);
    return value != null ? Integer.parseInt(value) : defaultValue;
  }

  /**
   * Gets the string parameter value.
   *
   * @param req The webscript request
   * @param param The name of the string parameter
   * @return The string parameter value or null if the parameter isn't present
   */
  protected String getString(WebScriptRequest req, String param) {
    return checkMandatory(req.getParameter(param), param, false);
  }

  /**
   * Gets a mandatory string parameter value of throws an exception if the parameter isn't present.
   *
   * @param req The webscript request
   * @param param The name of the string parameter value
   * @return The string parameter value
   * @throws WebScriptException if the parameter isn't present
   */
  protected String getMandatoryString(WebScriptRequest req, String param) {
    return checkMandatory(req.getParameter(param), param, true);
  }

  /**
   * Gets the string parameter value.
   *
   * @param req The webscript request.
   * @param param The name of the string parameter value
   * @param defaultValue The value to return if the parameter isn't present
   * @return The value of the string parameter or the default value if parameter isn't present
   */
  protected String getString(WebScriptRequest req, String param, String defaultValue) {
    String value = checkMandatory(req.getParameter(param), param, false);
    return value != null ? value : defaultValue;
  }

  /**
   * Gets a string parameter from the body
   * @param body The activiti webscript request body
   * @param param The name of the string parameter
   * @return The value of the string body parameter
   * @throws WebScriptException if string body parameter isn't present  
   */
  protected String getMandatoryString(ActivitiWebScriptBody body, String param) {
    return checkMandatory(body.getParameter(param), param, true);
  }

  /**
   * Throws and exception if the parameter value is null or empty and mandatory is true
   *
   * @param value The parameter value to test
   * @param param The name of the parameter
   * @param mandatory If true the value wil be tested
   * @return The parameter value
   * @throws WebScriptException if mandatory is true and value is null or empty
   */
  protected String checkMandatory(String value, String param, boolean mandatory)
  {
    if (value == null || value.isEmpty())
    {
      if (mandatory)
      {
        throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Parameter '" + param + "' is missing");
      }
      else
      {
        return null;
      }
    }
    return value;
  }

  /**
   * Returns the username for the current user.
   *
   * @param req The webscript request
   * @return THe username of the current user
   */
  protected String getCurrentUserId(WebScriptRequest req)
  {
    String authorization = req.getHeader("Authorization");
    if (authorization != null) {
      String [] parts = authorization.split(" ");
      if (parts.length == 2) {
        return new String(Base64.decode(parts[1])).split(":")[0];        
      }
    }
    return null;
  }

  /**
   * Tests if user is in group.
   *
   * @param req The webscript request
   * @param userId The id of the user to test
   * @param groupId The if of the group to test the user against
   * @return true of user is in group
   */
  protected boolean isUserInGroup(WebScriptRequest req, String userId, String groupId)
  {
    if (userId != null) {
      List<Group> groups = getIdentityService().findGroupsByUser(userId);
      for (Group group : groups)
      {
        if (config.getAdminGroupId().equals(group.getId())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Tests if user has manager role.
   *
   * @param req The webscript request.
   * @return true if the user has manager role 
   */
  protected boolean isManager(WebScriptRequest req) {
    return isUserInGroup(req, getCurrentUserId(req), config.getManagerGroupId());
  }

  /**
   * Tests if user has admin role.
   * 
   * @param req The webscript request
   * @return true if the user has admin role
   */
  protected boolean isAdmin(WebScriptRequest req) {
    return isUserInGroup(req, getCurrentUserId(req), config.getAdminGroupId());
  }

  /**
   * A class that wraps the webscripts request body so multiple formats 
   * such as XML may be supported in the future.
   */
  public class ActivitiWebScriptBody
  {

    /**
     * The json body
     */
    private JSONObject json = null;

    /**
     * Constructor
     * 
     * @param req THe webscript request
     * @throws IOException if body of correct format cannot be created
     */
    ActivitiWebScriptBody(WebScriptRequest req) throws IOException
    {
      json = new JSONObject(req.getContent().getContent());
    }

    /**
     * Gets a body parameter value.
     * 
     * @param param The name of the parameter
     * @return The value fo the parameter
     */
    String getParameter(String param) {
      return json.getString(param);
    }
  }

}
