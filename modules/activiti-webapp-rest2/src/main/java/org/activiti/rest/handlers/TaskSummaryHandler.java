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

package org.activiti.rest.handlers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.rest.RestHandler;


/**
 * @author Tom Baeyens
 */
public class TaskSummaryHandler implements RestHandler {
  
  public Map<String, Object> parseArguments(HttpServletRequest request) {
    
    if (request.getRequestURI().equals(request.getContextPath()+"/service/task-summary")) {
      Map<String, Object> args = new HashMap<String, Object>();
      args.put("userId", request.getParameter("userId"));
      return args;
    }
    return null;
  }

  public void handle(HttpServletRequest request, HttpServletResponse response, Map<String, Object> arguments) throws ServletException, IOException  {
    JSONObject jsonObject = new JSONObject();
    
    JSONArray dataArray = new JSONArray();
    jsonObject.put("data", dataArray);
    jsonObject.put("total", 0);
    jsonObject.put("start", 0);
    jsonObject.put("sort", "id");
    jsonObject.put("order", "asc");
    jsonObject.put("size", 0);
    
    String jsonString = jsonObject.toString(2);
    response.getWriter().write(jsonString);
  }
}
