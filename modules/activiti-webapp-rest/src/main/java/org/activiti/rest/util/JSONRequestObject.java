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

import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.impl.util.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.IOException;
import java.util.*;

public class JSONRequestObject implements ActivitiRequestObject {

  /**
   * The json json
   */
  private JSONObject json = null;

  /**
   * Constructor
   *
   * @param req The webscript request
   * @throws java.io.IOException if json of correct format cannot be created
   */
  JSONRequestObject(WebScriptRequest req) throws IOException {
    try {
      json = new JSONObject(req.getContent().getContent());
    }
    catch(Throwable t) {
      json = new JSONObject();
    }
  }

  /**
   * Constructor
   *
   * @param jsonObject The webscript request
   */
  JSONRequestObject(JSONObject jsonObject){
    json = jsonObject;
  }

  /**
   * Gets a json parameter string value.
   *
   * @param param The name of the parameter
   * @return The string value of the parameter
   */
  public String getString(String param) {
    Object value = json.optString(param, null);
    if (value == null) {
      return null;
    }
    if(value instanceof String) {
      return json.getString(param);
    }
    throw ActivitiRequest.getInvalidTypeException(param, value.toString(), STRING);
  }

  /**
   * Gets a json parameter string value.
   *
   * @param param The name of the parameter
   * @return The string value of the parameter
   */
  public Integer getInt(String param) {
    Object value = json.optString(param, null);
    if (value == null) {
      return null;
    }
    if(value instanceof String) {
      return json.getInt(param);
    }
    throw ActivitiRequest.getInvalidTypeException(param, value.toString(), INTEGER);
  }

  /**
   * Gets a json parameter boolean value.
   *
   * @param param The name of the parameter
   * @return The boolean value of the parameter
   */
  public Boolean getBoolean(String param) {
    Object value = json.optString(param, null);
    if (value == null) {
      return null;
    }
    if(value instanceof Boolean) {
      return json.getBoolean(param);
    }
    throw ActivitiRequest.getInvalidTypeException(param, value.toString(), BOOLEAN);
  }

  /**
   * Gets a json parameter boolean value.
   *
   * @param param The name of the parameter
   * @return The boolean value of the parameter
   */
  public Date getDate(String param) {
    Object value = json.optString(param, null);
    if (value == null) {
      return null;
    }
    if(value instanceof String) {
      return ActivitiRequest.parseDate(json.getString(param), param);
    }
    throw ActivitiRequest.getInvalidTypeException(param, value.toString(), DATE);
  }

  /**
   * Gets a json parameter string value.
   *
   * @param param The name of the parameter
   * @return The string value of the parameter
   */
  public JSONRequestObject getBodyObject(String param) {
    Object value = json.optJSONObject(param);
    if (value == null) {
      return null;
    }
    if(value instanceof JSONObject) {
      return new JSONRequestObject(json.getJSONObject(param));
    }
    throw ActivitiRequest.getInvalidTypeException(param, value.toString(), OBJECT);
  }

  /**
   * Gets a json parameter string value.
   *
   * @param param The name of the parameter
   * @return The string value of the parameter
   */
  public List getList(String param) {
    Object value = json.get(param);
    if (value == null) {
      return null;
    }
    if(value instanceof JSONArray) {
      return toList(json.getJSONArray(param));
    }
    throw ActivitiRequest.getInvalidTypeException(param, value.toString(), ARRAY);
  }

  /**
   * Converts a JSONArray to a List
   *
   * @param jsonArray
   * @return
   */
  private List toList(JSONArray jsonArray) {
    List list = new ArrayList();
    Object obj;
    for (int i = 0; i < jsonArray.length(); i++) {
      obj = jsonArray.get(i);
      if (obj instanceof JSONArray) {
        list.add(toList((JSONArray) obj));
      }
      if (obj instanceof JSONObject) {
        list.add(new JSONRequestObject((JSONObject) obj));
      }
      else {
        list.add(obj);
      }
    }
    return list;
  }

  /**
   * Gets the json as a map.
   *
   * @return The json as a map
   */
  public Map<String, Object> getFormVariables() {
    Map<String, Object> map = new HashMap<String, Object>();
    Iterator keys = json.keys();
    String key, typeKey, type;
    String[] keyPair;
    Object value;
    while (keys.hasNext()) {
      key = (String) keys.next();
      keyPair = key.split("_");
      if (keyPair.length == 1) {
        typeKey = keyPair[0] + "_type";
        if (json.has(typeKey)) {
          type = json.getString(typeKey);
          if (type.equals("Integer")) {
            value = json.getInt(key);
          } else if (type.equals("Boolean")) {
            value = json.getBoolean(key);
          } else if (type.equals("Date")) {
            value = json.getString(key);
          } else if (type.equals("User")) {
            value = json.getString(key);
          } else if (type.equals("String")) {
            value = json.getString(key);
          } else {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Parameter '" + keyPair[0] + "' is of unknown type '" + type + "'");
          }
        } else {
          value = json.get(key);
        }
        map.put(key, value);
      } else if (keyPair.length == 2) {
        if (keyPair[1].equals("required")) {
          if (!json.has(keyPair[0]) || json.get(keyPair[0]) == null) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Parameter '" + keyPair[0] + "' has no value");
          }
        }
      }
    }
    return map;
  }
}
