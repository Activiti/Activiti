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

import org.springframework.extensions.surf.util.Base64;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

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
   * Returns the webscript request obj in an abstracted form so multiple
   * formats may be implemented seamlessly in the future.
   *
   * @return The webscript requests obj
   */
  public ActivitiRequestObject getBody() {
    try {
      if (this.req.getContentType().contains("multipart")) {
        return new MultipartRequestObject(req);
      } else {
        return new JSONRequestObject(req);
      }
    } catch (IOException e) {
      throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Can't read obj");
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
   * Gets an date parameter value.
   *
   * @param param The name of the date parameter
   * @return The date parameter value or null if not present
   */
  public Date getDate(String param) {
    String value = getString(param);
    return value != null ? parseDate(value, param) : null;
  }

  /**
   * Gets a mandatory date parameter and throws an exception if its not present.
   *
   * @param param The name of the date parameter
   * @return The date parameter value
   * @throws WebScriptException if parameter isn't present
   */
  public Date getMandatoryDate(String param, Date defaultValue) {
    String value = getMandatoryString(param);
    return value != null ? parseDate(value, param) : defaultValue;
  }

  /**
   * Gets an date parameter value
   *
   * @param param The name of the date parameter
   * @param defaultValue The value to return if the parameter isn't present
   * @return The date parameter value of defaultValue if the parameter isn't present
   */
  public Date getDate(String param, Date defaultValue) {
    String value = getString(param);
    return value != null ? parseDate(value, param) : defaultValue;
  }


  /**
   * Gets an integer parameter value.
   *
   * @param param The name of the int parameter
   * @return The integer parameter value or null if not present
   */
  public Integer getInteger(String param) {
    String value = getString(param);
    return value != null ? parseInt(value, param) : null;
  }

  /**
   * Gets a mandatory integer parameter and throws an exception if its not present.
   *
   * @param param The name of the integer parameter
   * @return The integer parameter value
   * @throws WebScriptException if parameter isn't present
   */
  public Integer getMandatoryInteger(String param, Integer defaultValue) {
    String value = getMandatoryString(param);
    return value != null ? parseInt(value, param) : defaultValue;
  }

  /**
   * Gets an integer parameter value
   *
   * @param param The name of the integer parameter
   * @param defaultValue The value to return if the parameter isn't present
   * @return The integer parameter value of defaultValue if the parameter isn't present
   */
  public Integer getInteger(String param, Integer defaultValue) {
    String value = getString(param);
    return value != null ? parseInt(value, param) : defaultValue;
  }


  /**
   * Gets a boolean parameter value.
   *
   * @param param The name of the boolean parameter
   * @return The boolean parameter value or null if not present
   */
  public Boolean getBoolean(String param) {
    String value = getString(param);
    return value != null ? parseBoolean(value, param) : null;
  }

  /**
   * Gets a mandatory boolean parameter and throws an exception if its not present.
   *
   * @param param The name of the boolean parameter
   * @return The boolean parameter value
   * @throws WebScriptException if parameter isn't present
   */
  public Boolean getMandatoryBoolean(String param, Boolean defaultValue) {
    String value = getMandatoryString(param);
    return value != null ? parseBoolean(value, param) : defaultValue;
  }

  /**
   * Gets a boolean parameter value
   *
   * @param param The name of the boolean parameter
   * @param defaultValue The value to return if the parameter isn't present
   * @return The boolean parameter value of defaultValue if the parameter isn't present
   */
  public Boolean getBoolean(String param, Boolean defaultValue) {
    String value = getString(param);
    return value != null ? parseBoolean(value, param) : defaultValue;
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
   * Gets a string parameter from the obj
   *
   * @param obj The activiti webscript request obj
   * @param param The name of the string parameter
   * @return The value of the string obj parameter
   * @throws WebScriptException if string obj parameter isn't present
   */
  public String getMandatoryString(ActivitiRequestObject obj, String param) {
    return checkString(obj.getString(param), param, true);
  }

  /**
   * Gets a string parameter from the obj
   *
   * @param obj The activiti webscript request obj
   * @param param The name of the string parameter
   * @return The value of the string obj parameter
   * @throws WebScriptException if string obj parameter isn't present
   */
  public List getMandatoryList(ActivitiRequestObject obj, String param, String type) {
    List list = (List) checkObject(obj.getList(param), param, true);
    for (Object item : list) {
      if (item == null) {
        throw getInvalidTypeException(param, null, ActivitiRequestObject.ARRAY);
      }
      if (type != null) {
        if (item instanceof String && !type.equals(ActivitiRequestObject.STRING)) {
          throw getInvalidTypeException(param, item.toString(), ActivitiRequestObject.STRING + " array");
        }
        if (item instanceof Date && !type.equals(ActivitiRequestObject.DATE)) {
          throw getInvalidTypeException(param, item.toString(), ActivitiRequestObject.DATE + " array");
        }
        if (item instanceof Integer && !type.equals(ActivitiRequestObject.INTEGER)) {
          throw getInvalidTypeException(param, item.toString(), ActivitiRequestObject.INTEGER + " array");
        }
        if (item instanceof Boolean && !type.equals(ActivitiRequestObject.BOOLEAN)) {
          throw getInvalidTypeException(param, item.toString(), ActivitiRequestObject.BOOLEAN + " array");
        }
        if (item instanceof ActivitiRequestObject && !type.equals(ActivitiRequestObject.OBJECT)) {
          throw getInvalidTypeException(param, item.toString(), ActivitiRequestObject.OBJECT + " array");
        }
        if (item instanceof List && !type.equals(ActivitiRequestObject.ARRAY)) {
          throw getInvalidTypeException(param, item.toString(), ActivitiRequestObject.ARRAY + " array");
        }
      }
    }
    return list;
  }

  /**
   * Gets a parameter as Map
   *
   * @return The value of the string obj parameter
   * @throws WebScriptException if string obj parameter isn't present
   */
  public Map<String, Object> getFormVariables() {
    return getBody().getFormVariables();
  }

  /**
   * Throws an exception if the parameter value is null or empty and mandatory
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
   * Throws an exception if the parameter value is null or empty and mandatory
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
   * Returns a date and throws an exception if the value isn't an date value
   *
   * @param value The value to convert to a date
   * @param param The name of the parameter
   * @return A date based on the value parameter
   */
  public static Date parseDate(String value, String param)
  {
    try {
      return ISO8601DateFormat.parse(value.replaceAll(" ", "+"));
    }
    catch (NumberFormatException nfe) {
      throw getInvalidTypeException(param, value, "iso8601 date");
    }
  }

  /**
   * Returns an integer and throws an exception if the value isn't an int value
   *
   * @param value The value to convert to an integer
   * @param param The name of the parameter
   * @return An integer based on the value parameter
   */
  public static Integer parseInt(String value, String param)
  {
    try {
      return Integer.parseInt(value);
    }
    catch (NumberFormatException nfe) {
      throw getInvalidTypeException(param, value, "integer");
    }
  }

  /**
   * Returns aboolean and throws an exception if the value isn't a boolean value
   *
   * @param value The value to convert to an boolean
   * @param param The name of the parameter
   * @return A boolean based on the value parameter
   */
  public static Boolean parseBoolean(String value, String param)
  {
    if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
      return Boolean.parseBoolean(value);      
    }
    throw getInvalidTypeException(param, value, "bool");
  }

  public static WebScriptException getInvalidTypeException(String param, String value, String type) {
    return new WebScriptException(Status.STATUS_BAD_REQUEST, "Value for param '" + param + "' is not a valid " + type + " value: '" + value + "'");
  }

}
