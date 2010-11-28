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

package org.activiti.rest.api.cycle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.JSONRequestObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

/**
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class UserConfigPost extends ActivitiCycleWebScript {

  @Override
  void execute(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {

    JSONRequestObject json = (JSONRequestObject) req.getBody();

    // public void updateConfiguration(Map<String, List<Map<String, String>>>
    // connectorConfigMap, String currentUserId)
    Map<String, List<Map<String, String>>> connectorConfigMap = new HashMap<String, List<Map<String, String>>>();

    for (String key : json.getFormVariables().keySet()) {
      List<Map<String, String>> configs;
      Object value = json.getFormVariables().get(key);
      if (value instanceof JSONObject) {
        configs = new ArrayList<Map<String, String>>();
        configs.add(getConfigParams((JSONObject) value));
        connectorConfigMap.put(key, configs);
      } else if (value instanceof JSONArray) {
        configs = new ArrayList<Map<String, String>>();
        for (int i = 0; i < ((JSONArray) value).length(); i++) {
          configs.add(getConfigParams(((JSONArray) value).getJSONObject(i)));
          connectorConfigMap.put(key, configs);
        }
      }
    }
    this.cycleService.updateConfiguration(connectorConfigMap, req.getCurrentUserId());
  }

  private Map<String, String> getConfigParams(JSONObject config) {
    Map<String, String> params = new HashMap<String, String>();
    for (int i = 0; i < config.names().length(); i++) {
      String name = String.valueOf(config.names().get(i));
      String param = String.valueOf(config.get(name));
      params.put(name, param);
    }
    return params;
  }

}
