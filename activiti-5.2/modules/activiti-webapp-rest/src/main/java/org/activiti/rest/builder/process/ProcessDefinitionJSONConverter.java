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

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.rest.builder.JSONConverter;
import org.activiti.rest.model.RestProcessDefinition;
import org.activiti.rest.util.JSONUtil;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author Frederik Heremans
 */
public class ProcessDefinitionJSONConverter implements JSONConverter<ProcessDefinition> {

  public JSONObject getJSONObject(ProcessDefinition processDefinition) throws JSONException {
    JSONObject json = new JSONObject();

    JSONUtil.putRetainNull(json, "id", processDefinition.getId());
    JSONUtil.putRetainNull(json, "key", processDefinition.getKey());
    JSONUtil.putRetainNull(json, "name", processDefinition.getName());
    JSONUtil.putRetainNull(json, "version", processDefinition.getVersion());
    JSONUtil.putRetainNull(json, "deploymentId", processDefinition.getDeploymentId());
    JSONUtil.putRetainNull(json, "resourceName", processDefinition.getResourceName());
    JSONUtil.putRetainNull(json, "diagramResourceName", processDefinition.getDiagramResourceName());
    // TODO: custom handling, review when ACT-160 is fixed
    if(processDefinition instanceof RestProcessDefinition) {      
      JSONUtil.putRetainNull(json, "startFormResourceKey", ((RestProcessDefinition) processDefinition).getStartFormResourceKey());
    }
    return json;
  }

  public ProcessDefinition getObject(JSONObject jsonObject) throws JSONException {
    throw new UnsupportedOperationException();
  }

}
