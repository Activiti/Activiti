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

package org.activiti.rest.builder.engine;

import java.util.Map;

import org.activiti.engine.ProcessEngineInfo;
import org.activiti.rest.builder.BaseJSONObjectBuilder;
import org.activiti.rest.util.JSONUtil;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Frederik Heremans
 */
public class ProcessEngineObjectBuilder extends BaseJSONObjectBuilder {

  @Override
  public JSONObject createJsonObject(Object modelObject) throws JSONException {
    JSONObject result = new JSONObject();
    Map<String, Object> model = getModelAsMap(modelObject);

    JSONUtil.putRetainNull(result, "version", model.get("version"));
    ProcessEngineInfo processEngineInfo = (ProcessEngineInfo) model.get("processEngineInfo");
    if(processEngineInfo != null) {
      JSONUtil.putRetainNull(result, "name", processEngineInfo.getName());
      JSONUtil.putRetainNull(result, "resourceUrl", processEngineInfo.getResourceUrl());
      JSONUtil.putRetainNull(result, "exception", processEngineInfo.getException());
    }
    return result;
  }

}
