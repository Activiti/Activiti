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
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;

import org.activiti.cdi.annotation.BusinessProcessScoped;
import org.activiti.cdi.impl.context.BusinessProcessContext;
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

  private static ProcessEngine processEngine;

  public static ProcessEngine getProcessEngine() {
    return processEngine;
  }

  public void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery event) {
    event.addScope(BusinessProcessScoped.class, true, true);
  }

  public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager) {
    logger.info("Initializing activiti-cdi extension.");
    try {
      BeanManagerLookup.localInstance = manager;

      // initialize the process engine
      initializeProcessEngine();

      // register custom CDI context implementation business process scoped
      // beans
      event.addContext(new BusinessProcessContext());

      // deploy the processes
      deployProcesses();

    } catch (Exception e) {
      // interpret engine initialization problems as definition errors
      // TODO: lookup process engine earlier?
      event.addDefinitionError(e);
      return;
    }
  }

  protected void initializeProcessEngine() {
    ProcessEngineLookup processEngineProvisionStrategy = ProgrammaticBeanLookup.lookup(ProcessEngineLookup.class);
    processEngine = processEngineProvisionStrategy.getProcessEngine();
  }

  private void deployProcesses() {
    new ProcessDeployer().deployProcesses();
  }

  public void beforeShutdown(@Observes BeforeShutdown event) {
    ProcessEngineLookup processEngineProvisionStrategy = ProgrammaticBeanLookup.lookup(ProcessEngineLookup.class);
    processEngineProvisionStrategy.ungetProcessEngine();   
    processEngine = null;
    logger.info("Activiti-cdi extension shutdown.");
  }

}
