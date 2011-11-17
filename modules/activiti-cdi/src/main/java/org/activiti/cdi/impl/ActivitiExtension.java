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
package org.activiti.cdi.impl;

import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;

import org.activiti.cdi.annotation.BusinessProcessScoped;
import org.activiti.cdi.impl.context.BusinessProcessContext;
import org.activiti.cdi.impl.context.ThreadContext;
import org.activiti.cdi.impl.context.ThreadScoped;
import org.activiti.cdi.impl.util.ActivitiServices;
import org.activiti.cdi.impl.util.BeanManagerLookup;
import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.activiti.engine.ProcessEngine;

/**
 * CDI-Extension registering a custom context for {@link BusinessProcessScoped}
 * beans.
 * 
 * Also starts / stops the activiti {@link ProcessEngine} and deploys all
 * processes listed in the 'processes.xml'-file.
 * 
 * @author Daniel Meyer
 */
public class ActivitiExtension implements Extension {

  private static Logger logger = Logger.getLogger(ActivitiExtension.class.getName());
  private ProcessEngine processEngine;

  public void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery event) {
    event.addScope(BusinessProcessScoped.class, true, true);
    event.addScope(ThreadScoped.class, true, false);
  }

  public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager) {       
    BeanManagerLookup.localInstance = manager;
    event.addContext(new BusinessProcessContext(manager));
    event.addContext(new ThreadContext());
  }

  public void afterDeploymentValidation(@Observes AfterDeploymentValidation event, BeanManager beanManager) {
    try {   
      logger.info("Initializing activiti-cdi.");      
      // initialize the process engine
      initializeProcessEngine(beanManager);      
      // deploy the processes if engine was set up correctly
      deployProcesses();      
    } catch (Exception e) {
      // interpret engine initialization problems as definition errors
      event.addDeploymentProblem(e);
    }
  }

  protected void initializeProcessEngine(BeanManager beanManager) {
    ProcessEngineLookup processEngineProvisionStrategy = ProgrammaticBeanLookup.lookup(ProcessEngineLookup.class, beanManager);
    processEngine = processEngineProvisionStrategy.getProcessEngine();
    ActivitiServices activitiServices = ProgrammaticBeanLookup.lookup(ActivitiServices.class, beanManager);
    activitiServices.setProcessEngine(processEngine);
  }

  private void deployProcesses() {
    new ProcessDeployer(processEngine).deployProcesses();
  }

  public void beforeShutdown(@Observes BeforeShutdown event) {
    ProcessEngineLookup processEngineProvisionStrategy = ProgrammaticBeanLookup.lookup(ProcessEngineLookup.class);
    processEngineProvisionStrategy.ungetProcessEngine();
    processEngine = null;
    logger.info("Activiti-cdi extension shutdown.");
  }

}
