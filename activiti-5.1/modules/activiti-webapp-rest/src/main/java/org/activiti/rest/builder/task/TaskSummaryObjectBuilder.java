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

package org.activiti.rest.builder.task;

import java.util.Map;
import java.util.Map.Entry;

import org.activiti.rest.builder.BaseJSONObjectBuilder;
import org.activiti.rest.util.JSONUtil;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author Frederik Heremans
 */
public class TaskSummaryObjectBuilder extends BaseJSONObjectBuilder {

  @Override
  @SuppressWarnings("unchecked")
  public JSONObject createJsonObject(Object modelObject) throws JSONException {
    JSONObject result = new JSONObject();
    Map<String, Object> model = getModelAsMap(modelObject);
    
    JSONObject assigned = JSONUtil.putNewObject(result, "assigned");
    assigned.put("total", model.get("assigned"));
    
    JSONObject unassigned = JSONUtil.putNewObject(result, "unassigned");
    unassigned.put("total", model.get("unassigned"));
    JSONObject groups = JSONUtil.putNewObject(unassigned, "groups");
    
    Map<String, Long> unassignedByGroup = (Map<String, Long>) model.get("unassignedByGroup");
    for(Entry<String, Long> groupEntry : unassignedByGroup.entrySet()) {
      groups.put(groupEntry.getKey(), groupEntry.getValue());
    }
    
    return result;
  }

}
