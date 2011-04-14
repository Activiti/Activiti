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

package org.activiti.rest.impl;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.rest.RestServlet;



/**
 * @author Tom Baeyens
 */
public abstract class RestHandler {
  
  private RestServlet restServlet;

  public abstract String getUrlPattern();

  public abstract void handle(RestCall call);

  public abstract HttpServletMethod getMethod();
  
  private String processEngineName = ProcessEngines.NAME_DEFAULT;
  
  public ProcessEngine getProcessEngine() {
    return restServlet.getProcessEngine();
  }
  
  public TaskService getTaskService() {
    return getProcessEngine().getTaskService();
  }
  
  public String getProcessEngineName() {
    return processEngineName;
  }

  public void setProcessEngineName(String processEngineName) {
    this.processEngineName = processEngineName;
  }

  public void setRestServlet(RestServlet restServlet) {
    this.restServlet = restServlet;
  }
}
