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

package org.activiti.rest.builder.process;

import java.util.List;

import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.builder.JSONConverter;
import org.activiti.rest.util.JSONUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author Frederik Heremans
 */
public class ProcessInstanceJSONConverter implements JSONConverter<ProcessInstance> {

  public JSONObject getJSONObject(ProcessInstance processInstance) throws JSONException {
    JSONObject json = new JSONObject();
    
    JSONUtil.putRetainNull(json, "id", processInstance.getId());
    JSONUtil.putRetainNull(json, "processDefinitionId", processInstance.getProcessDefinitionId());
    JSONUtil.putRetainNull(json, "businessKey", processInstance.getBusinessKey());
    JSONUtil.putRetainNull(json, "ended", processInstance.isEnded());
    addActiveActivityNames(processInstance, json);
    return json;
  }

  
  private void addActiveActivityNames(ProcessInstance processInstance, JSONObject object) throws JSONException {
    if(processInstance instanceof PvmProcessInstance) {      
      List<String> activeActivities = ((PvmProcessInstance) processInstance).findActiveActivityIds();
      JSONArray activityNames =JSONUtil.putNewArray(object, "activityNames");
      for (String activity : activeActivities) {
        activityNames.put(activity);
      }
    }
  }

  public ProcessInstance getObject(JSONObject jsonObject) throws JSONException {
    throw new UnsupportedOperationException();
  }

}
