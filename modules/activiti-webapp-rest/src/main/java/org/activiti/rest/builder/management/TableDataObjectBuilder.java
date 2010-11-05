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

package org.activiti.rest.builder.management;

import java.lang.reflect.Array;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.management.TablePage;
import org.activiti.rest.builder.BaseJSONObjectBuilder;
import org.activiti.rest.util.JSONUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Frederik Heremans
 */
public class TableDataObjectBuilder extends BaseJSONObjectBuilder {

  @Override
  public JSONObject createJsonObject(Object modelObject) throws JSONException {
    Map<String, Object> model = getModelAsMap(modelObject);
    JSONObject result = new JSONObject();
    JSONUtil.putPagingInfo(result, model);

    JSONArray rows = JSONUtil.putNewArray(result, "data");
    TablePage tablePage = (TablePage) model.get("tablePage");
    if (tablePage != null) {
      for (Map<String, Object> row : tablePage.getRows()) {
        rows.put(createJSONObjectForRow(row));
      }
    }
    return result;
  }

  private JSONObject createJSONObjectForRow(Map<String, Object> row) throws JSONException {
    JSONObject json = new JSONObject();
    for (Entry<String, Object> column : row.entrySet()) {
      putColumnValue(json, column.getKey(), column.getValue());
    }
    return json;
  }

  protected void putColumnValue(JSONObject json, String key, Object value) throws JSONException {
    if (value == null) {
      JSONUtil.putRetainNull(json, key, null);
    } else if (value instanceof List< ? >) {
      json.put(key, ((List< ? >) value).size());
    } else if (value.getClass().isArray()) {
      json.put(key, Array.getLength(value));
    } else if (value instanceof Date) {
      String date = JSONUtil.formatISO8601Date((Date) value);
      JSONUtil.putRetainNull(json, key, date);
    } else {
      // All other types can be handled by JSONObject
      JSONUtil.putRetainNull(json, key, value);
    }
  }

}
