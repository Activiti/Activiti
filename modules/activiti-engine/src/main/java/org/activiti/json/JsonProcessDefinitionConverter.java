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
package org.activiti.json;

import java.io.Reader;

import org.activiti.engine.ProcessDefinition;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.json.JSONObject;


/**
 * @author Tom Baeyens
 */
public class JsonProcessDefinitionConverter extends JsonObjectConverter<ProcessDefinition> {

  public JSONObject toJsonObject(ProcessDefinition processDefinition) {
    ProcessDefinitionImpl processDefinitionImpl = (ProcessDefinitionImpl) processDefinition;
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("id", processDefinitionImpl.getId());
    if (processDefinitionImpl.getKey()!=null) {
      jsonObject.put("key", processDefinitionImpl.getKey());
    }
    if (processDefinitionImpl.getDeployment()!=null) {
      jsonObject.put("deploymentId", processDefinitionImpl.getDeployment().getId());
    }
    return jsonObject;
  }

  public ProcessDefinition toObject(Reader reader) {
    return null;
  }
}
