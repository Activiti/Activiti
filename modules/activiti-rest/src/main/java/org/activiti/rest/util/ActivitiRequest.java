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

import org.activiti.engine.impl.util.json.JSONObject;
import org.springframework.extensions.surf.util.Base64;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Helper class that wrapps the webscript request to perform methods upon it.
 *
 * @author Erik Winlof
 */
public class ActivitiRequest {

  /**
   * The wrapped webscript request
   */
  private WebScriptRequest req;

  /**
   * Constructor
   *
   * @param req
   *    The webscript request
   */
  public ActivitiRequest(WebScriptRequest req) {
    this.req = req;
  }

  /**
   * Getter for obtaining the webscript request.
   *
   * @return The webscript request
   */
  public WebScriptRequest getWebScriptRequest() {
    return req;
  }

  /**
   * Getter for obtaining the underlying webscript servlet request.
   *
   * @return The webscript servlet request
   */
  public WebScriptServletRequest getWebScriptServletRequest() {
    return (WebScriptServletRequest) req;
  }

  /**
   * Getter for obtaining the underlying http servlet request.
   *
   * @return The http servlet request
   */
  public HttpServletRequest getHttpServletRequest() {
    return getWebScriptServletRequest().getHttpServletRequest();
  }

  /**
   * Getter for obtaining a http session.
   *
   * @return A http session
   */
  public HttpSession getHttpSession() {
    return getHttpServletRequest().getSession(true);
  }

  /**
   * Returns the username for the current user.
   *
   * @return The username of the current user
   */
  public String getCurrentUserId() {
    String authorization = req.getHeader("Authorization");
    if (authorization != null) {
      String[] parts = authorization.split(" ");
      if (parts.length == 2) {
        return new String(Base64.decode(parts[1])).split(":")[0];
      }
    }
    return null;
  }

  /**
   * Returns the webscript request body in an abstracted form so multiple
   * formats may be implemented seamlessly in the future.
   *
   * @return The webscript requests body
   */
  public ActivitiWebScriptBody getBody() {
    try {
      return new ActivitiWebScriptBody(req);
    } catch (IOException e) {
      throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Can't read body");
    }
  }

  /**
   * Gets a path parameter value and throws an exception if its not present.
   *
   * @param param The name of the path parameter
   * @return The value of the path parameter
   * @throws WebScriptException if parameter isn't present
   */
  public String getMandatoryPathParameter(String param) {
    return checkString(req.getServiceMatch().getTemplateVars().get(param), param, true);
  }

  /**
   * Gets a path parameter value.
   *
   * @param param The name of the path parameter
   * @return The path parameter value or null if not present
   */
  public String getPathParameter(String param) {
    return checkString(req.getServiceMatch().getTemplateVars().get(param), param, false);
  }

  /**
   * Gets an int parameter value.
   *
   * @param param The name of the int parameter
   * @return The int parameter value or Integer.MIN_VALUE if not present
   */
  public int getInt(String param) {
    String value = getString(param);
    return value != null ? Integer.parseInt(value) : Integer.MIN_VALUE;
  }

  /**
   * Gets a mandatory int parameter and throws an exception if its not present.
   *
   * @param param The name of the path parameter
   * @return The int parameter value
   * @throws WebScriptException if parameter isn't present
   */
  public int getMandatoryInt(String param) {
    String value = getMandatoryString(param);
    return value != null ? Integer.parseInt(value) : Integer.MIN_VALUE;
  }

  /**
   * Gets an int parameter value
   *
   * @param param The name of the int parameter
   * @param defaultValue The value to return if the parameter isn't present
   * @return The int parameter value of defaultValue if the parameter isn't present
   */
  public int getInt(String param, int defaultValue) {
    String value = getString(param);
    return value != null ? Integer.parseInt(value) : defaultValue;
  }

  /**
   * Gets the string parameter value.
   *
   * @param param The name of the string parameter
   * @return The string parameter value or null if the parameter isn't present
   */
  public String getString(String param) {
    return checkString(req.getParameter(param), param, false);
  }

  /**
   * Gets a mandatory string parameter value of throws an exception if the
   * parameter isn't present.
   *
   * @param param The name of the string parameter value
   * @return The string parameter value
   * @throws WebScriptException if the parameter isn't present
   */
  public String getMandatoryString(String param) {
    return checkString(req.getParameter(param), param, true);
  }

  /**
   * Gets the string parameter value.
   *
   * @param param The name of the string parameter value
   * @param defaultValue The value to return if the parameter isn't present
   * @return The value of the string parameter or the default value if parameter isn't present
   */
  public String getString(String param, String defaultValue) {
    String value = checkString(req.getParameter(param), param, false);
    return value != null ? value : defaultValue;
  }

  /**
   * Gets a string parameter from the body
   *
   * @param body The activiti webscript request body
   * @param param The name of the string parameter
   * @return The value of the string body parameter
   * @throws WebScriptException if string body parameter isn't present
   */
  public String getMandatoryString(ActivitiWebScriptBody body, String param) {
    return checkString(body.getString(param), param, true);
  }

  /**
   * Gets a parameter as Map
   *
   * @param body The activiti webscript request body
   * @return The value of the string body parameter
   * @throws WebScriptException if string body parameter isn't present
   */
  public Map<String, Object> getFormVariables(ActivitiWebScriptBody body) {
    return body.getFormVariables();
  }

  /**
   * Throws and exception if the parameter value is null or empty and mandatory
   * is true
   *
   * @param value The parameter value to test
   * @param param The name of the parameter
   * @param mandatory If true the value wil be tested
   * @return The parameter value
   * @throws WebScriptException if mandatory is true and value is null or empty
   */
  protected String checkString(String value, String param, boolean mandatory) {
    if (value != null && value.length() == 0) {
      value = null;
    }
    return (String) checkObject(value, param, mandatory);
  }

  /**
   * Throws and exception if the parameter value is null or empty and mandatory
   * is true
   *
   * @param value The parameter value to test
   * @param param The name of the parameter
   * @param mandatory If true the value wil be tested
   * @return The parameter value
   * @throws WebScriptException if mandatory is true and value is null or empty
   */
  protected Object checkObject(Object value, String param, boolean mandatory) {
    if (value == null) {
      if (mandatory) {
        throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Parameter '" + param + "' is missing");
      } else {
        return null;
      }
    }
    return value;
  }

  /**
   * A class that wraps the webscripts request body so multiple formats such as
   * XML may be supported in the future.
   */
  public class ActivitiWebScriptBody {

    /**
     * The json body
     */
    private JSONObject jsonBody = null;

    /**
     * Constructor
     *
     * @param req The webscript request
     * @throws IOException if body of correct format cannot be created
     */
    ActivitiWebScriptBody(WebScriptRequest req) throws IOException {
      jsonBody = new JSONObject(req.getContent().getContent());
    }

    /**
     * Gets a body parameter string value.
     *
     * @param param The name of the parameter
     * @return The string value of the parameter
     */
    String getString(String param) {
      return jsonBody.getString(param);
    }

    /**
     * Gets a body parameter string value.
     *
     * @param param The name of the parameter
     * @return The string value of the parameter
     */
    int getInt(String param) {
      return jsonBody.getInt(param);
    }

    /**
     * Gets the body as a map.
     *
     * @return The body as a map
     */
    Map<String, Object> getFormVariables() {
      Map<String, Object> map = new HashMap<String, Object>();
      Iterator keys = jsonBody.keys();
      String key, typeKey, type;
      String[] keyPair;
      Object value;
      while (keys.hasNext()) {
        key = (String) keys.next();
        keyPair = key.split("_");
        if (keyPair.length == 1) {
          typeKey = keyPair[0] + "_type";
          if (jsonBody.has(typeKey)) {
            type = jsonBody.getString(typeKey);
            if (type.equals("Integer")) {
              value = jsonBody.getInt(key);
            } else if (type.equals("Boolean")) {
              value = jsonBody.getBoolean(key);
            } else if (type.equals("Date")) {
              value = jsonBody.getString(key);
            } else if (type.equals("User")) {
              value = jsonBody.getString(key);
            } else if (type.equals("String")) {
              value = jsonBody.getString(key);
            } else if (type.equals("RepositoryFolder")) {
              // TODO: Check implementation in CustomizedViewConnector, but
              // should be moved there
              value = jsonBody.getString(key);
              // if (conn != null) {
              // value = conn.getRepositoryFolder(jsonBody.getString(key));
              // } else {
              // throw new WebScriptException(Status.STATUS_BAD_REQUEST,
              // "Parameter '" + keyPair[0] + "' of type '" + type
              // + "' requested without providing a repository connector");
              // }
            } else {
              throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Parameter '" + keyPair[0] + "' is of unknown type '" + type + "'");
            }
          } else {
            value = jsonBody.get(key);
          }
          map.put(key, value);
        } else if (keyPair.length == 2) {
          if (keyPair[1].equals("required")) {
            if (!jsonBody.has(keyPair[0]) || jsonBody.get(keyPair[0]) == null) {
              throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Parameter '" + keyPair[0] + "' has no value");
            }
          }
        }
      }
      return map;
    }

  }
}
