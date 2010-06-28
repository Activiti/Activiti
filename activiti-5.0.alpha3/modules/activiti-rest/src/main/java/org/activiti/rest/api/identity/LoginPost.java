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

import org.activiti.ProcessEngine;
import org.activiti.ProcessEngineInfo;
import org.activiti.ProcessEngines;
import org.activiti.rest.util.ActivitiWebScript;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Authenticates username and password..
 *
 * @author Erik Winlšf
 */
public class LoginPost extends ActivitiWebScript
{

  /**
   * Authenticates username and password and prepares the response for the webscript template.
   *
   * @param req The webscripts request
   * @param status The webscripts status
   * @param cache The webscript cache
   * @param model The webscripts template model
   */
  @Override
  protected void executeWebScript(WebScriptRequest req, Status status, Cache cache, Map<String, Object> model)
  {
    // Extract user and password from JSON POST
    Content c = req.getContent();
    if (c == null)
    {
      throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Missing POST body.");
    }

    try
    {
      JSONObject json = new JSONObject(c.getContent());
      String userId = json.getString("userId");
      String password = json.getString("password");
      if (userId == null || userId.length() == 0)
      {
        throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Username not specified");
      }
      if (password == null) {
        throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Password not specified");
      }
      String engineName = config.getEngine();
      ProcessEngine pe = ProcessEngines.getProcessEngine(engineName);
      if (pe != null) {
        if (!pe.getIdentityService().checkPassword(userId, password)) {
          throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, "Username and password does not match.");
        }
        // Login successful ...
      }
      else {
        String message;
        ProcessEngineInfo pei = ProcessEngines.getProcessEngineInfo(engineName);
        if (pei != null) {
          message = pei.getException();
        }
        else {
          message = "Can't find process engine named '" + engineName + "' which is needed to authenticate username and password.";
          List<ProcessEngineInfo> processEngineInfos = ProcessEngines.getProcessEngineInfos();
          if (processEngineInfos.size() > 0) {
            message += "\nHowever " + processEngineInfos.size() + " other process engine(s) was found: ";
          }
          for (ProcessEngineInfo processEngineInfo : processEngineInfos)
          {
            message += "Process engine '" + processEngineInfo.getName() + "' (" + processEngineInfo.getResourceUrl() + "):";
            if (processEngineInfo.getException() != null) {
              message += processEngineInfo.getException();
            }
            else {
              message += "OK";
            }
          }
        }
        throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
      }
    }
    catch (JSONException e)
    {
      throw new WebScriptException(Status.STATUS_BAD_REQUEST,
        "Unable to parse JSON POST body: " + e.getMessage());
    }
    catch (IOException e)
    {
      throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
        "Unable to retrieve POST body: " + e.getMessage());
    }
  }

}