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

package org.activiti.rest.common.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tijs Rademakers
 */
public class ActivitiServletContextListener implements ServletContextListener {
  
  protected static final Logger LOGGER = LoggerFactory.getLogger(ActivitiServletContextListener.class);

  public void contextInitialized(ServletContextEvent event) {
    LOGGER.info("Booting Activiti Process Engine");
    ProcessEngine processEngine = null;
    try {
      processEngine = ProcessEngines.getDefaultProcessEngine();
    } catch (Exception e) {
      LOGGER.error("Error starting the Activiti REST API", e);
    }
    if (processEngine == null) {
      LOGGER.error("Could not start the Activiti REST API");
    }
  }

  public void contextDestroyed(ServletContextEvent event) {
    LOGGER.info("Destroying Activiti Process Engine");
    ProcessEngines.destroy();
  }
}
