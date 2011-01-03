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

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.ISO8601DateFormat;


/**
 * Util class containing methods for handling common JSON operations.
 * 
 * @author Frederik Heremans
 */
public abstract class JSONUtil {
    
  /**
   * Puts the given value in the {@link JSONObject}. When the value is null,
   * {@link JSONObject#NULL} is put as value, resulting in an explicit NULL
   * value in the rendered JSON output.
   */
  public static void putRetainNull(JSONObject object, String key, Object value) throws JSONException {
    putDefault(object, key, value, JSONObject.NULL);
  }
  
  /**
   * Puts the given value in the {@link JSONObject}. When the value is null,
   * an empty string is used as value.
   */
  public static void putEmptyStringIfNull(JSONObject object, String key, Object value) throws JSONException {
    putDefault(object, key, value, "");
  }

  public static void putDefault(JSONObject object, String key, Object value, Object defaultValue) throws JSONException {
    if (value == null) {
      value = defaultValue;
    }
    object.put(key, value);
  }

  public static void putPagingInfo(JSONObject object, Map<String, Object> model) throws JSONException {
    putDefault(object, "total", model.get("total"), 0L);
    putDefault(object, "start", model.get("start"), 0L);
    putDefault(object, "size", model.get("size"), 0L);
    object.put("sort", model.get("sort"));
    object.put("order", model.get("order"));
  }

  public static JSONObject putNewObject(JSONObject base, String key) throws JSONException {
    JSONObject newObject = new JSONObject();
    base.put(key, newObject);
    return newObject;
  }

  public static JSONArray putNewArray(JSONObject base, String key) throws JSONException {
    JSONArray newArray = new JSONArray();
    base.put(key, newArray);
    return newArray;
  }
  
  public static String formatISO8601Date(Calendar calendar) {
    if(calendar != null) {
      return ISO8601DateFormat.format(calendar.getTime());      
    }
    return null;
  }
  
  public static String formatISO8601Date(Date date) {
    if(date != null) {
      return ISO8601DateFormat.format(date);     
    }
    return null;
  }

}
