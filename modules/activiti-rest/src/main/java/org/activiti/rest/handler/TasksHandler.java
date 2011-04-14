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

package org.activiti.rest.handler;

import java.util.List;
import java.util.logging.Logger;

import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.task.Task;
import org.activiti.rest.impl.HttpServletMethod;
import org.activiti.rest.impl.IntegerParameter;
import org.activiti.rest.impl.Parameter;
import org.activiti.rest.impl.RestCall;
import org.activiti.rest.impl.RestHandler;


/**
 * @author Tom Baeyens
 */
public class TasksHandler extends RestHandler {
  
  private static Logger log = Logger.getLogger(TasksHandler.class.getName());
  
  public HttpServletMethod getMethod() {
    return HttpServletMethod.GET;
  }
  
  public String getUrlPattern() {
    return "/tasks";
  }

  protected Parameter<Integer> firstResult = new IntegerParameter("first", "first result to be shown starting from 0 (zero)", 0, Integer.MAX_VALUE);
  protected Parameter<Integer> maxResults = new IntegerParameter("max", "max number of tasks to be retrieved", 1, Integer.MAX_VALUE);

  public void handle(RestCall call) {
    // call the activiti api
    List<Task> tasks = getTaskService()
      .createTaskQuery()
      .taskAssignee(call.getAuthenticatedUserId())
      .listPage(firstResult.get(call), maxResults.get(call));
    
    // convert to json
    JSONArray tasksJson = new JSONArray();
    for (Task task: tasks) {
      JSONObject taskJson = convertTaskToJson(task);
      tasksJson.put(taskJson);
    }
    
    // send response
    call.sendResponse(tasksJson);
  }

  public static JSONObject convertTaskToJson(Task task) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.putOpt("name", task.getName());
    jsonObject.putOpt("description", task.getDescription());
    jsonObject.putOpt("owner", task.getOwner());
    jsonObject.putOpt("assignee", task.getAssignee());
    return jsonObject;
  }
}
