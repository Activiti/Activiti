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


import org.activiti.engine.management.TableMetaData;
import org.activiti.rest.builder.JSONConverter;
import org.activiti.rest.util.JSONUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Frederik Heremans
 */
public class TableMetaDataJSONConverter implements JSONConverter<TableMetaData> {

  public JSONObject getJSONObject(TableMetaData tableMetaData) throws JSONException {
    JSONObject json = new JSONObject();
    JSONUtil.putRetainNull(json, "tableName", tableMetaData.getTableName());

    JSONArray namesArray = JSONUtil.putNewArray(json, "columnNames");
    if(tableMetaData.getColumnNames() != null) {
      for(String columnName : tableMetaData.getColumnNames()) {
        namesArray.put(columnName);
      }
    }
    
    JSONArray typesArray = JSONUtil.putNewArray(json, "columnTypes");
    if(tableMetaData.getColumnTypes() != null) {
      for(String columnType : tableMetaData.getColumnTypes()) {
        typesArray.put(columnType);
      }
    }
    
    return json;
  }

  public TableMetaData getObject(JSONObject jsonObject) throws JSONException {
    throw new UnsupportedOperationException();
  }

}
