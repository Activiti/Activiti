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
package org.activiti.kickstart.engine;

import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;

/**
 * @author Joram Barrez
 */
public class ProcessEnginesServletContextListener implements ServletContextListener {
  
  protected static final Logger LOGGER = Logger.getLogger(ProcessEnginesServletContextListener.class.getName());

  public void contextInitialized(ServletContextEvent servletContextEvent) {
    ProcessEngines.init();
    
    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
    if (processEngine == null) {
      LOGGER.warning("Could not construct a process engine for KickStart. " 
              + "Please verify if your activiti.cfg.xml configuration is correct.");
      
      if ("true".equals(System.getProperty("KickStartDebugInMem"))) {
        LOGGER.info("KickStartDebugInMem system property found. Switching to in memory Activiti configuratuon");
        processEngine = StandaloneInMemProcessEngineConfiguration
          .createStandaloneInMemProcessEngineConfiguration().buildProcessEngine();
        ProcessEngines.registerProcessEngine(processEngine);
      }
    }
  }

  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    ProcessEngines.destroy();
  }

}