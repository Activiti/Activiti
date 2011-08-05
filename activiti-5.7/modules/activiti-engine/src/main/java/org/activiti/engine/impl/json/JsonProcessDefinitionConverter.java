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
package org.activiti.engine.impl.json;

import java.io.Reader;

import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.repository.ProcessDefinition;


/**
 * @author Tom Baeyens
 */
public class JsonProcessDefinitionConverter extends JsonObjectConverter<ProcessDefinition> {

  public JSONObject toJsonObject(ProcessDefinition processDefinition) {
    ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) processDefinition;
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("id", processDefinitionEntity.getId());
    if (processDefinitionEntity.getKey()!=null) {
      jsonObject.put("key", processDefinitionEntity.getKey());
    }
    if (processDefinitionEntity.getDeploymentId()!=null) {
      jsonObject.put("deploymentId", processDefinitionEntity.getDeploymentId());
    }
    return jsonObject;
  }

  public ProcessDefinition toObject(Reader reader) {
    return null;
  }
}
