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
package org.activiti.impl.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.activiti.engine.ProcessEngines;


/**
 * @author Tom Baeyens
 */
public class ProcessEnginesServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  public void init() throws ServletException {
    ProcessEngines.init();
  }

  @Override
  public void destroy() {
    ProcessEngines.destroy();
  }

 // the next is commented as that should become part of the probe app
//
//  @Override
//  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//    response.setContentType("text/html");
//    ServletOutputStream out = response.getOutputStream();
//    out.println("<html>");
//    out.println("<body>");
//    out.println("<h1>Activiti Version</h1>");
//    out.println("<p>"+ProcessEngine.VERSION+"</p>");
//    out.println("<h1>Process Engines</h1>");
//    Map<String, ProcessEngine> processEngines = ProcessEngines.getProcessEngines();
//    if (processEngines.isEmpty()) {
//      out.println("<p>No successful initialized process engine available</p>");
//    } else {
//      for (String processEngineName : processEngines.keySet()) {
//        out.println("<p>");
//        out.println(processEngineName == null ? "The default process engine" : processEngineName);
//        out.println("</p>");
//      }
//    }
//    out.println("<h1>Initialization errors</h1>");
//    Map<String, String> errors = ProcessEngines.getErrors();
//    if (errors.isEmpty()) {
//      out.println("<p>All configured process engines are successfully initialized</p>");
//    } else {
//      for(String resourceUrl : errors.keySet()) {
//        out.println("<b>"+resourceUrl+"</b>");
//        out.println("<pre>"+errors.get(resourceUrl)+"</pre>");
//      }
//    }
//    out.println("</body>");
//    out.println("</html>");
//    out.flush();
//  }
  
  
}
