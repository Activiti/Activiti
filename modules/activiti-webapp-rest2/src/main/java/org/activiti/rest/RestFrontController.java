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

package org.activiti.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.rest.handlers.TaskSummaryHandler;


/**
 * @author Tom Baeyens
 */
public class RestFrontController extends HttpServlet {

  private static final long serialVersionUID = 1L;
  
  protected List<RestHandler> restHandlers = new ArrayList<RestHandler>();
  
  public RestFrontController() {
    restHandlers.add(new TaskSummaryHandler());
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    for (RestHandler restHandler: restHandlers) {
      Map<String, Object> arguments = restHandler.parseArguments(request);
      if (arguments!=null) {
        restHandler.handle(request, response, arguments);
        return;
      }
    }
    throw new RuntimeException("no handler factory for "+request.getContextPath()+" | "+request.getRequestURI()+" | "+request.getRequestURL());
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
  }

  
}
