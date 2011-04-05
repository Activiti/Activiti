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
package org.activiti.explorer.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.activiti.explorer.ExplorerApp;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.AbstractApplicationServlet;

/**
 * Servlet providing Activiti and Spring integration with Vaadin
 * 
 * @author Patrick Oberg
 * @author Joram Barrez
 */
public class SpringApplicationServlet extends AbstractApplicationServlet {

  private static final long serialVersionUID = 1L;
  
  protected WebApplicationContext applicationContext;

  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    super.init(servletConfig);
    applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletConfig.getServletContext());
  }

  @Override
  protected Class< ? extends Application> getApplicationClass() throws ClassNotFoundException {
    return ExplorerApp.class;
  }

  @Override
  protected Application getNewApplication(HttpServletRequest request) {
    return (Application) applicationContext.getBean(ExplorerApp.class);
  }
}
