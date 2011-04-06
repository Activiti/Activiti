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

import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Task;
import org.activiti.rest.builder.JSONConverter;
import org.activiti.rest.model.RestTask;
import org.activiti.rest.util.JSONUtil;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author Frederik Heremans
 */
public class TaskJSONConverter implements JSONConverter<Task> {

  public JSONObject getJSONObject(Task object) throws JSONException {
    JSONObject json = new JSONObject();
    TaskEntity task = (TaskEntity) object;
    
    if(task != null) {
      JSONUtil.putRetainNull(json, "id", task.getId());
      JSONUtil.putRetainNull(json, "name", task.getName());
      JSONUtil.putRetainNull(json, "description", task.getDescription());
      JSONUtil.putRetainNull(json, "priority", task.getPriority());
      JSONUtil.putRetainNull(json, "assignee", task.getAssignee());
      JSONUtil.putRetainNull(json, "executionId", task.getExecutionId());
      JSONUtil.putRetainNull(json, "processInstanceId", task.getProcessInstanceId());
      
      // TODO: custom handling, review when ACT-160 is fixed
      if(task instanceof RestTask) {
        // Custom handling, extra field is present
        JSONUtil.putRetainNull(json, "formResourceKey", ((RestTask)task).getFormResourceKey());
      }
    }
    return json;
  }

  public Task getObject(JSONObject jsonObject) throws JSONException {
    throw new UnsupportedOperationException();
  }

}
